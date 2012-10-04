/*
 * $Id: Recognition.java,v 1.1 2003/06/01 17:15:53 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;

/**
 * An interpretation of a stroke or a set of strokes as a data/confidence pair.
 * 
 * @author Heloise Hse (heloise@eecs.berkeley.edu)
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 */
public class Recognition {
	private double _confidence;
	private TypedData _data;

	/**
	 * Construct a new recognition using the given data and confidence.
	 */
	public Recognition(TypedData data, double confidence) {
		_data = data;
		_confidence = confidence;
	}

	/**
	 * Return the confidence of this recognition.
	 */
	public double getConfidence() {
		return _confidence;
	}

	/**
	 * Return the type of this recognition.
	 */
	public Type getType() {
		return _data.getType();
	}

	/**
	 * Return the data of this recognition.
	 */
	public TypedData getData() {
		return _data;
	}

	/**
	 * Print the contents of this recognition.
	 */
	public String toString() {
		return "Recognition[" + getType().getID() + ", " + _confidence + "]";
	}
}
