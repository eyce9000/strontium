/*
 * $Id: Classifier.java,v 1.1 2003/06/01 17:18:59 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.classification;

/**
 * A Classifier performs generic classification on feature sets, the semantics
 * of which it knows nothing about. It assumes that the feature set it is given
 * is consistent with the feature sets that it was trained on, i.e. the same
 * features are at the same indices.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public interface Classifier {
	/**
	 * Return a classification for the specified feature set, or throw a runtime
	 * exception if the given feature set does not have the same number of
	 * features as the training examples.
	 */
	public Classification classify(FeatureSet s) throws ClassifierException;
}
