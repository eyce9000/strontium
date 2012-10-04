/*
 * $Id: SimpleData.java,v 1.4 2003/09/30 16:30:05 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;

/**
 * An instance of typed data that represents dynamic, user-defined types. If you
 * are writing a low-level recognizer and that recognizes strokes based on a
 * feature vector and knows nothing about the semantics of the recognition,
 * other than a string representation of the type, then this is the class for
 * you (e.g. new SimpleData("scribble")). However, if you have semantic
 * knowledge of the data that is being represented, e.g. the number of sides on
 * the polygon that you just recognized, then you should probably use a
 * statically-typed form of TypedData.
 * 
 * @see Type
 * @see TypedData
 * @author Heloise Hse (hse@eecs.berkeley.edu)
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 */
public final class SimpleData implements TypedData {
	public static Type TYPE = new Type(SimpleData.class, "");
	private String _id;

	/**
	 * Create a SimpleData object without an ID.
	 */
	public SimpleData() {
	}

	/**
	 * Create a SimpleData object and initilize the ID.
	 */
	public SimpleData(String typeId) {
		init(typeId);
	}

	/**
	 * Initilize the ID.
	 */
	public void init(String typeId) {
		_id = typeId;
	}

	/**
	 * Return the Type of this data.
	 */
	public Type getType() {
		return new Type(getClass(), _id);
	}

	/**
	 * Return the string ID.
	 */
	public String getTypeID() {
		return _id;
	}

	/**
	 * Return the string ID.
	 */
	public String toString() {
		return _id;
	}
}
