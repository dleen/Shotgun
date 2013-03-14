package edu.uw.cs.biglearn.shotgun;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.uw.cs.biglearn.util.MatUtil;

import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;

import cern.colt.matrix.tdouble.algo.DoubleBlas;
import cern.colt.matrix.tdouble.algo.SmpDoubleBlas;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;


public class Shooting_market_sparse {

	final DoubleBlas db;
	final DenseDoubleAlgebra da;

	/* Dimension of X */
	int n, p;

	/* Regularization parameter */
	double lambda;

	/* Stores the positive part of w */
	DenseDoubleMatrix1D wplus;

	/* Stores the negative part of w */
	DenseDoubleMatrix1D wminus;

	/* Stores the product of Xw */
	DenseDoubleMatrix1D xw;

	/* The transpose of the design matrix X */
	final SparseDoubleMatrix2D XTrans;

	/* The response vector Y. */
	final DenseDoubleMatrix1D Y;

	/* Stores the current parameter. */
	DenseDoubleMatrix1D w;

	/* Stores the stale parameter. */
	DenseDoubleMatrix1D oldw;

	static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

	/**
	 * Constructor
	 * @param XTrans
	 * @param Y
	 * @param lambda
	 */
	public Shooting_market_sparse(SparseDoubleMatrix2D XTrans, DenseDoubleMatrix1D Y, double lambda) {
		this.XTrans = XTrans;
		this.Y = Y;
		p = XTrans.rows();
		n = XTrans.columns();
		this.lambda = lambda;

		/**
		 * Initialize the parameter
		 */
		// wplus  = new DenseDoubleMatrix1D(p);
		// wminus = new DenseDoubleMatrix1D(p);
		// xw     = new DenseDoubleMatrix1D(n);
		// w      = new DenseDoubleMatrix1D(p);
		// oldw   = new DenseDoubleMatrix1D(p);

		wplus  = new DenseDoubleMatrix1D(p);
		wminus = new DenseDoubleMatrix1D(p);
		xw     = new DenseDoubleMatrix1D(n);
		w      = new DenseDoubleMatrix1D(p);
		oldw   = new DenseDoubleMatrix1D(p);

		this.db = new SmpDoubleBlas();
		this.da = new DenseDoubleAlgebra();
	}

	/**
	 * Perform coordinate descent at K random chosen coordinates;
	 * @param j
	 */
	public void shoot(int K) {
		Random r = new Random();
		double upd = 0;

		for (int i = 0; i < K; i++) {
			int j = r.nextInt(2 * p);

			double grad_no_lambda =
			// why is Y dense. Should be sparse no?
			(1.0d / n) * (- Y.zDotProduct(XTrans.viewRow(j % p)) +
				xw.zDotProduct(XTrans.viewRow(j % p)));

			if (j < p) {
				upd = Math.max(-wplus.get(j), -grad_no_lambda - this.lambda);
				wplus.set(j, wplus.get(j) + upd);
				db.daxpy(upd, XTrans.viewRow(j), xw);
			}
			else {
				upd = Math.max(-wminus.get(j % p), grad_no_lambda - this.lambda);
				wminus.set(j % p, wminus.get(j % p) + upd);
				db.daxpy(-upd, XTrans.viewRow(j % p), xw);
			}
		}
	}

	/**
	 * Run Sequential SCD until convergence or exceeding maxiter.
	 */
	public DenseDoubleMatrix1D scd (int maxiter) {
		int iter = 0;

		DenseDoubleMatrix1D wdelta;

		while (iter  < maxiter) {
			shoot(p); // only do non zero coords here!!!
			// hang on, why only do the non zero ones?
			w = (DenseDoubleMatrix1D)wplus.copy();
			db.daxpy(-1.0, wminus, w);
			wdelta = (DenseDoubleMatrix1D)w.copy();
			db.daxpy(-1.0, oldw, wdelta);
			double delta = da.norm2(wdelta);
			System.out.println(delta);
			if (delta < 1e-5) {
				System.out.println(MatUtil.l0(w));
				// System.out.println(iter);
				break;
			}
			oldw = (DenseDoubleMatrix1D)w.copy();
			iter++;
				// System.out.println(MatUtil.l2(w));
				// System.out.println(iter);
		}
		return w;
	}

	/**
	 * Run Shotgun parallel SCD until convergence or exceeding maxiter.
	 */
	public DenseDoubleMatrix1D shotgun (int maxiter) {
		final int batchsize = p;
		int iter = 0;

		DenseDoubleMatrix1D wdelta;

		while(iter < maxiter) {
			ExecutorService threadpool = Executors.newFixedThreadPool(NUM_CORES);
			// submit batchsize coordinate descent jobs in parallel.
			for (int i = 0; i < NUM_CORES; i++) {
				threadpool.submit(new Runnable() {
					public void run() {
						shoot(batchsize / NUM_CORES);
					}
				});
			}
			iter += batchsize;

			// Wait for jobs to terminate and check the result
			threadpool.shutdown();
			while (!threadpool.isTerminated()) {
				try {
					threadpool.awaitTermination(20, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// check whether the result has converged.
			w = (DenseDoubleMatrix1D)wplus.copy();
			db.daxpy(-1.0, wminus, w);
			wdelta = (DenseDoubleMatrix1D)w.copy();
			db.daxpy(-1.0, oldw, wdelta);
			double delta = da.norm2(wdelta);
			if (delta < 1e-5) {
				break;
			}
			oldw = (DenseDoubleMatrix1D)w.copy();
		}
		return w;
	}
}
