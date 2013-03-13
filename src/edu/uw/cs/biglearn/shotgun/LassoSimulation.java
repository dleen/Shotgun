package edu.uw.cs.biglearn.shotgun;

import java.util.Arrays;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import edu.uw.cs.biglearn.util.MatUtil;

import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;

import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.DoubleBlas;
import cern.colt.matrix.tdouble.algo.SmpDoubleBlas;

public class LassoSimulation {

	public static void run() throws FileNotFoundException {

		/*
		 *  Load the simulation data.
		 */
		// float[][] Xtrain_trans = MatUtil.readMatrix("data/lasso_synthetic/Xtrain.mtx");
		// float[][] Xtest_trans = MatUtil.readMatrix("data/lasso_synthetic/Xtest.mtx");
		// float[] Ytrain = MatUtil.readVector("data/lasso_synthetic/Ytrain.mtx");
		// float[] Ytest = MatUtil.readVector("data/lasso_synthetic/Ytest.mtx");
		// int p = Xtrain_trans.length;
		// int n = Xtrain_trans[0].length;

		// ArrayList<float[]> results = new ArrayList<float[]>();

		double[] lambdas = new double[] {0.02, 0.04, 0.06, 0.08, 0.1, 0.12, 0.14, 0.16, 0.18, 0.2};

		ArrayList<DenseDoubleMatrix1D> results = new ArrayList<DenseDoubleMatrix1D>();

		DenseDoubleMatrix2D Xtrain_trans = MatUtil.readMatrixMarket("data/lasso_synthetic/Xtrain.mtx");
		DenseDoubleMatrix2D Xtest_trans = MatUtil.readMatrixMarket("data/lasso_synthetic/Xtest.mtx");
		DenseDoubleMatrix1D Ytrain = MatUtil.readVectorMarket("data/lasso_synthetic/Ytrain.mtx");
		DenseDoubleMatrix1D Ytest = MatUtil.readVectorMarket("data/lasso_synthetic/Ytest.mtx");


		int p = Xtrain_trans.rows();
		int n = Xtrain_trans.columns();

		DoubleBlas db = new SmpDoubleBlas();
		DenseDoubleAlgebra da = new DenseDoubleAlgebra();
		System.out.println(db);

		/**
		 * Compute LASSO fit for each lambda.
		 */
		double start = System.currentTimeMillis();
		for (double lambda: lambdas) {
			Shooting_market S = new Shooting_market(Xtrain_trans, Ytrain, lambda);
			DenseDoubleMatrix1D what = S.scd(p * 10);
			// Shooting S = new Shooting(Xtrain_trans, Ytrain, lambda);
			// float[] what = S.scd(p * 10);
			results.add(what);
		}
		double elapsed = System.currentTimeMillis() - start;
		System.out.println("Elapsed time: " + elapsed/1000 + "secs");

		System.out.println(results.get(0));
		// System.out.println(Arrays.toString(results.get(0)));

		/**
		 * Evaluate training error and test error.
		 */
		int i = 0;


		for (DenseDoubleMatrix1D what : results) {
		// for (float[] what : results) {
			System.out.println("Lambda: " + lambdas[i]);

			Xtrain_trans.zMult(what, Ytrain, 1.0d, -1.0d, true);
			// db.daxpy(-1.0, temp, Ytrain);
			double trainerror = da.norm2(Ytrain);

			trainerror = (double) (Math.pow(trainerror, 2) / Ytrain.size());

			Xtest_trans.zMult(what, Ytest, 1.0d, -1.0d, true);
			// db.daxpy(-1.0, temp, Ytest);
			double testerror = da.norm2(Ytest);

			testerror = (double) (Math.pow(testerror, 2) / Ytest.size());

			// float trainerror = MatUtil.l2(MatUtil.minus(MatUtil.multiply(Xtrain_trans, what), Ytrain));
			// trainerror = (float) (Math.pow(trainerror, 2) / Ytrain.length);

			// float testerror = MatUtil.l2(MatUtil.minus(MatUtil.multiply(Xtest_trans, what), Ytest));
			// testerror = (float) (Math.pow(trainerror, 2) / Ytrain.length);

			System.out.println("Training error : " + trainerror);
			System.out.println("Test error: " + testerror);
			System.out.println("NNZ: " + MatUtil.l0(what));
			++i;
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		run();
	}
}
