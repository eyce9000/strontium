/*
 * $Id: TrainableClassifier.java,v 1.2 2003/08/12 17:48:35 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.classification;

/**
 * This classifier trains on a data set and performs classification based on
 * what it has seen. It assumes that the feature set it is given is consistent
 * with the feature sets that it was trained on, i.e. the same features are at
 * the same indices.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public interface TrainableClassifier extends Classifier {
	/**
	 * Train the classifier with a given training set. This method will throw a
	 * ClassifierException if the training set is not self consisistent, i.e.
	 * the feature sets that it contains do not have the same number of features
	 * in them.
	 */
	public void train(TrainingSet s, int numFeatures)
			throws ClassifierException;

	/**
	 * Return whether this classifier is incremental, i.e. whether this
	 * classifier can support multiple calls to "train".
	 */
	public boolean isIncremental();

	/**
	 * Clear all results of previous trainings (presumably so that this
	 * classifier can be trained again from scratch).
	 */
	public void clear();
}
