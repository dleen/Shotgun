package edu.uw.cs.biglearn.shotgun;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import edu.uw.cs.biglearn.util.MatUtil;

public class FMRIWordPrediction {
	/* Dimension of the input fmri feature */
	public static int Xp = 21764;
	/* Dimension of the output semantic feature */
	public static int Yp = 218;
	/* Number of training examples */
	public static int ntrain = 300;
	/* Number of test examples */
	public static int ntest = 60;


	/**
	 * Run shotgun on a single feature varidx, using given lambdas
	 * @param varidx
	 * @param lambdas
	 * @throws FileNotFoundException
	 */
	public void shotgunSingleFeature(int varidx, double[] lambdas) throws FileNotFoundException {
				// Read the word feature matrix
				// wordfeature[i] corresponds to the ith semantic feature.
				float[][] wordfeature_trans = MatUtil.readMatrix("data/fmri/word_feature_std.mtx");
				// Read the training data
				float[][] Xtrain_trans = MatUtil.readMatrix("data/fmri/subject1_fmri_std.train.mtx");
				float[] widtrain = MatUtil.readVector("data/fmri/subject1_wordid.train.mtx");
				float[] Ytrain_trans = new float[ntrain];
				for (int j = 0; j < ntrain; j++) {
					Ytrain_trans[j] = wordfeature_trans[varidx][(int)widtrain[j]-1];
				}

				// Read the test data
				float[][] Xtest_trans = MatUtil.readMatrix("data/fmri/subject1_fmri_std.test.mtx");
				// widtest is a 60 * 2 matrix. The first column contains the true word id that generates the fmri signal. The second
				// column contains random selected word ids.
				float[][] widtest_trans = MatUtil.readMatrix("data/fmri/subject1_wordid.test.mtx");
				float[] Ytest_trans = new float[ntest];
				for (int j = 0; j < ntest; j++) {
					Ytest_trans[j] = wordfeature_trans[varidx][(int)widtest_trans[0][j]-1];
				}

				// Compute the lasso fit for each lambda.
				System.out.println("Training for semantic feature: " + varidx);
				ArrayList<float[]> results = new ArrayList<float[]>();
				for (double lambda: lambdas) {
					Shooting S = new Shooting(Xtrain_trans, Ytrain_trans, lambda);
					results.add(S.shotgun(10*Xp));
				}

				// Compute the errors on training data, test data.
				int i = 0;
				for (float[] what : results) {
					System.out.println("Lambda: " + lambdas[i]);
					float testerror = MatUtil.l2(MatUtil.minus(MatUtil.multiply(Xtest_trans, what), Ytest_trans));
					testerror = (float) (Math.pow(testerror, 2)/ntest);

					float trainerror = MatUtil.l2(MatUtil.minus(MatUtil.multiply(Xtrain_trans, what), Ytrain_trans));
					trainerror = (float) (Math.pow(trainerror, 2)/ntrain);
					System.out.println("Training error: " + trainerror);
					System.out.println("Testing error: " + testerror);
					System.out.println("NNZ: " + MatUtil.l0(what));
					i++;
				}
	}

	/**
	 * Run shotgun on the entire data set and do word prediction.
	 * @throws FileNotFoundException
	 */
	public void shotgunAll() throws FileNotFoundException {
		// Read the word feature matrix
		float[][] wordfeature_trans = MatUtil.readMatrix("data/fmri/word_feature_std.mtx");// wordfeature[i] corresponds to the ith semantic feature.
		// Read the training data
		float[][] Xtrain_trans = MatUtil.readMatrix("data/fmri/subject1_fmri_std.train.mtx");
		float[] widtrain = MatUtil.readVector("data/fmri/subject1_wordid.train.mtx");
		float[][] Ytrain_trans = new float[Yp][ntrain];  // Ytrain[i] cooresponds to the ith semantic feature.
		for (int i = 0; i < Yp; i++) {
			for (int j = 0; j < ntrain; j++) {
				Ytrain_trans[i][j] = wordfeature_trans[i][(int)widtrain[j]-1];
			}
		}
		// Read the test data
		float[][] Xtest_trans = MatUtil.readMatrix("data/fmri/subject1_fmri_std.test.mtx");
		float[][] widtest_trans = MatUtil.readMatrix("data/fmri/subject1_wordid.test.mtx");
		float[][] Ytest_trans = new float[Yp][ntest];
		float[][] Ytest2_trans = new float[Yp][ntest];

		for (int i = 0; i < Yp; i++) {
			for (int j = 0; j < ntest; j++) {
				Ytest_trans[i][j] = wordfeature_trans[i][(int)widtest_trans[0][j]-1];
				Ytest2_trans[i][j] = wordfeature_trans[i][(int)widtest_trans[1][j]-1];
			}
		}

		double[] lambdas = new double[] {0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4};
		float[][] betas = new float[Yp][Xp];
		float[][] Yhat_trans = new float[Yp][ntest];

		double start = System.currentTimeMillis();
		for (int i = 0; i < Yp; i++) {
			System.out.println("Training for semantic feature: " + i);
			ArrayList<float[]> results = new ArrayList<float[]>();
			for (double lambda: lambdas) {
				// System.out.println("Lambda: " + lambda);
				Shooting S = new Shooting(Xtrain_trans, Ytrain_trans[i], lambda);
				results.add(S.shotgun(10*Xp));
			}

			// Pick the best lambda using the held out test set.
			float min = Float.MAX_VALUE;
			for (float[] what : results) {
				float testerror = MatUtil.l2(MatUtil.minus(MatUtil.multiply(Xtest_trans, what), Ytest_trans[i]));
				testerror = (float) (Math.pow(testerror, 2)/ntest);

				float trainerror = MatUtil.l2(MatUtil.minus(MatUtil.multiply(Xtrain_trans, what), Ytrain_trans[i]));
				trainerror = (float) (Math.pow(trainerror, 2)/ntrain);
				// System.out.println("Training error: " + trainerror);
				// System.out.println("Testing error: " + testerror);
				if (testerror < min) {
					min = testerror;
					betas[i] = what;
				}
			}
		    Yhat_trans[i] = MatUtil.multiply(Xtest_trans, betas[i]);

		    // Count how many mistakes we made under the current model.
			double mistake = 0;
			for (int n = 0; n < ntest; n++) {
				// compute distance between yhat and ytest1, ytest2;
				double dist1 = 0;
				double dist2 = 0;
				for (int j = 0; j <= i; j++) {
					// l2 distance
					double d1 = Math.pow((Yhat_trans[j][n] - Ytest_trans[j][n]), 2);
					double d2 = Math.pow((Yhat_trans[j][n] - Ytest2_trans[j][n]), 2);
					// l1 distance
					//double d1 = Math.abs((double)Yhat_trans[j][n] - Ytest_trans[j][n]);
					//double d2 = Math.abs((double)Yhat_trans[j][n] - Ytest2_trans[j][n]);
					dist1 += d1;
					dist2 += d2;
				}

				// If dist(yhat, y1) > dist(yhat, y2), we made a mistake in prediction.
				if (dist1 > dist2) {
					mistake += 1;
				}  else if (dist1 == dist2) { // dist(yhat, y1) == dist(yhat, y2) is counted as 0.5.
					mistake += 0.5;
				}
			}
			System.out.println("Number of mistakes: " + mistake);
			System.out.println("Classification error: " + (float)mistake / ntest);
		}
		System.out.println("Total time elapsed: " + (System.currentTimeMillis() - start)/1000 + " secs");
	}

	public static void main(String[] args) throws FileNotFoundException {
		FMRIWordPrediction experiment = new FMRIWordPrediction();

		// double[] lambdas = new double[]{0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
		// experiment.shotgunSingleFeature(1, lambdas);

		experiment.shotgunAll();
	}
}
