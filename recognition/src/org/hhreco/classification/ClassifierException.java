/*
 * $Id: ClassifierException.java,v 1.1 2003/06/01 17:18:59 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.classification;

/**
 * Thrown when there is some internal error in the training or classification
 * process.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class ClassifierException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3228679858222219596L;

	/**
	 * Construct a ClassifierException with no error message.
	 */
	public ClassifierException() {
		super();
	}

	/**
	 * Construct a ClassifierException with the specified error message.
	 */
	public ClassifierException(String s) {
		super(s);
	}
}
