/*
 * $Id: DataRep.java,v 1.2 2003/08/22 16:18:09 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.classification;

/**
 * Representing a collection of data with the means, standard deviations, and
 * variances of the feature components.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class DataRep {
	/** The mean vector, one for each feature */
	private double[] _means;
	/** The standard deviation vector */
	private double[] _stds;
	/** The variance vector */
	private double[] _vars;
	/** The number of examples */
	int _n;

	/**
	 * Compute the means, standard deviations, variances from the given set of
	 * feature vectors.
	 */
	public DataRep(FeatureSet[] examples, int numFeatures) {
		_n = examples.length;
		double[][] fvectors = new double[examples.length][];
		for (int i = 0; i < examples.length; i++) {
			fvectors[i] = examples[i].getFeatures();
		}
		_means = computeMeans(fvectors, numFeatures);
		_vars = computeVariances(_means, fvectors, numFeatures);
		_stds = new double[numFeatures];
		for (int i = 0; i < numFeatures; i++) {
			_stds[i] = Math.sqrt(_vars[i]);
		}
	}

	/**
	 * Return the mean vector.
	 */
	public double[] getMeans() {
		return _means;
	}

	/**
	 * Return the standard deviations of the feature components.
	 */
	public double[] getStds() {
		return _stds;
	}

	/**
	 * Return the variances of the feature components.
	 */
	public double[] getVariances() {
		return _vars;
	}

	/**
	 * Compute the means for the feature elements from the given set of
	 * examples, fvals.
	 */
	public static double[] computeMeans(double[][] fvals, int numFeatures) {
		int numEx = fvals.length;
		double[] means = new double[numFeatures];
		for (int i = 0; i < numFeatures; i++) {// initialization
			means[i] = 0;
		}
		for (int i = 0; i < numEx; i++) {
			double[] vals = fvals[i];
			for (int j = 0; j < numFeatures; j++) {
				means[j] += vals[j];
			}
		}
		for (int i = 0; i < numFeatures; i++) {
			means[i] /= numEx;
		}
		return means;
	}

	/**
	 * Compute the standard deviations for the feature elements from the given
	 * set of examples, fvals.
	 */
	public static double[] computeStd(double[] means, double[][] fvals,
			int numFeatures) {
		double[] vars = computeVariances(means, fvals, numFeatures);
		for (int i = 0; i < vars.length; i++) {
			vars[i] = Math.sqrt(vars[i]);
		}
		return vars;
	}

	/**
	 * Compute the variances for the feature elements from the given set of
	 * examples, fvals.
	 */
	public static double[] computeVariances(double[] means, double[][] fvals,
			int numFeatures) {
		int numEx = fvals.length;
		double[] var = new double[numFeatures];
		for (int i = 0; i < numFeatures; i++) {// initialization
			var[i] = 0;
		}
		for (int i = 0; i < numEx; i++) {
			double[] vals = fvals[i];
			for (int j = 0; j < numFeatures; j++) {
				double diff = vals[j] - means[j];
				var[j] += diff * diff;
			}
		}
		for (int i = 0; i < numFeatures; i++) {
			var[i] /= numEx;
		}
		return var;
	}

	/**
	 * The text representation of this class.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("means:");
		for (int i = 0; i < _means.length; i++) {
			buf.append(_means[i] + " ");
		}
		buf.append("\n");
		buf.append("stds:");
		for (int i = 0; i < _stds.length; i++) {
			buf.append(_stds[i] + " ");
		}
		buf.append("\n");
		buf.append("variances:");
		for (int i = 0; i < _vars.length; i++) {
			buf.append(_vars[i] + " ");
		}
		return buf.toString();
	}
}
