/*
 * $Id: FeatureExtractor.java,v 1.1 2003/08/08 00:04:53 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;

/**
 * A feature extractor performs computation on a set of strokes and outputs the
 * feature values. Example of features are aspect ratios, moments, etc.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public interface FeatureExtractor {
	/**
	 * Return the feature values extracted from the set of strokes.
	 */
	public double[] apply(TimedStroke[] s);

	/**
	 * Return the name of the feature extractor.
	 */
	public String getName();
}
