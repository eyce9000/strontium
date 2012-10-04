/*
 * $Id: FeatureSet.java,v 1.2 2003/06/18 23:45:03 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.classification;

/**
 * A data structure for storing features of an example; it is basically a
 * typesafe array of doubles with appropriate accessor methods. It is up to the
 * client to maintain consistency between different instances of feature sets,
 * i.e. that they have the same number of elements and that the features at
 * specific indices are referencing the same feature component.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class FeatureSet {
	/** The feature vector. */
	private double _features[];

	/** Create a feature vector of size 'num'. */
	public FeatureSet(int num) {
		_features = new double[num];
	}

	/** Create a feature vector with the given vector. */
	public FeatureSet(double features[]) {
		_features = features;
	}

	/** Return the i'th feature. */
	public final double getFeature(int i) {
		return _features[i];
	}

	/** Return the number of features in the vector. */
	public final int getFeatureCount() {
		return _features.length;
	}

	/** Return the feature vector. */
	public double[] getFeatures() {
		return _features;
	}

	/** Set the i'th feature to the specified value. */
	public final void setFeature(int i, double val) {
		_features[i] = val;
	}

	/** Text representation of the feature vector. */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		double[] vals = getFeatures();
		for (int i = 0; i < vals.length; i++) {
			buf.append(i + ":" + vals[i] + " ");
		}
		return buf.toString();
	}
}
