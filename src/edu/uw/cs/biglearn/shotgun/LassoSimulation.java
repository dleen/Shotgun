package edu.uw.cs.biglearn.shotgun;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import edu.uw.cs.biglearn.util.MatUtil;

public class LassoSimulation {

	public static void run() throws FileNotFoundException {

		/**
		 *  Load the simulation data.
		 */
		// float[][] Xtrain_trans = MatUtil.readMatrix("data/lasso_synthetic/Xtrain.mtx");
		// float[][] Xtest_trans = MatUtil.readMatrix("data/lasso_synthetic/Xtest.mtx");
		// float[] Ytrain = MatUtil.readVector("data/lasso_synthetic/Ytrain.mtx");
		// float[] Ytest = MatUtil.readVector("data/lasso_synthetic/Ytest.mtx");
		// int p = Xtrain_trans.length;
		// int n = Xtrain_trans[0].length;

		double[] lambdas = new double[] {0.02, 0.04, 0.06, 0.08, 0.1, 0.12, 0.14, 0.16, 0.18, 0.2};
		ArrayList<float[]> results = new ArrayList<float[]>();

		MatUtil.readMatrixMarket("data/lasso_synthetic/Xtrain.mtx");
		MatUtil.readMatrixMarket("data/lasso_synthetic/Xtest.mtx");
		MatUtil.readVectorMarket("data/lasso_synthetic/Ytrain.mtx");
		MatUtil.readVectorMarket("data/lasso_synthetic/Ytest.mtx");

		/**
		 * Compute LASSO fit for each lambda.
		 */
		// double start = System.currentTimeMillis();
		// for (double lambda: lambdas) {
		// 	Shooting S = new Shooting(Xtrain_trans, Ytrain, lambda);
		// 	float[] what = S.scd(p*10);
		// 	results.add(what);
		// }
		// double elapsed = System.currentTimeMillis() - start;
		// System.out.println("Elapsed time: " + elapsed/1000 + "secs");

		/**
		 * Evaluate training error and test error.
		 */
		// int i = 0;
		// for (float[] what : results) {
		// 	System.out.println("Lambda: " + lambdas[i]);
		// 	float trainerror = MatUtil.l2(MatUtil.minus(MatUtil.multiply(Xtrain_trans, what), Ytrain));
		// 	trainerror = (float) (Math.pow(trainerror, 2)/Ytrain.length);

		// 	float testerror = MatUtil.l2(MatUtil.minus(MatUtil.multiply(Xtest_trans, what), Ytest));
		// 	testerror = (float)(Math.pow(testerror, 2)/Ytest.length);

		// 	System.out.println("Training error : " + trainerror);
		// 	System.out.println("Test error: " + testerror);
		// 	System.out.println("NNZ: " + MatUtil.l0(what));
		// 	++i;
		// }
	}

	public static void main(String[] args) throws FileNotFoundException {
		run();
	}
}
