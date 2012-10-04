/*
 * $Id: Type.java,v 1.6 2003/10/16 00:14:13 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;

import java.util.HashMap;

/**
 * A unique identifier for the type of a piece of data that results from a
 * recognition. For example, different drawings of squares have different
 * parameters (size, etc.) but they all have the same type.
 * 
 * @see TypedData
 * @see SimpleData
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class Type {
	private Class _class;
	private String _id;
	private static HashMap _typeMap = new HashMap();

	/**
	 * Record the specified type so that objects of this type can be
	 * automatically generated on the fly.
	 */
	public static void addNativeType(String nativeType) {
		_typeMap.put(strip(nativeType), nativeType);
	}

	/**
	 * Remove the package name of the given string, e.g.
	 * hhreco.toolbox.TriangleData would become TriangleData.
	 */
	private static String strip(String classWithPackage) {
		int i = classWithPackage.lastIndexOf(".");
		return classWithPackage.substring(i + 1);
	}

	/**
	 * Check if the specified type is in the record.
	 */
	public static boolean isNativeType(String type) {
		return _typeMap.containsKey(type);
	}

	/**
	 * A Type object that has no ID.
	 */
	public static final Type NO_TYPE = new Type(null, null) {
		public String toString() {
			return "NO_TYPE";
		}
	};

	/**
	 * Create a Type of the specified class and string ID.
	 */
	public Type(Class c, String id) {
		_class = c;
		_id = id;
	}

	/**
	 * Create a TypedData object by instantiating an object based on the Class
	 * specified in this Type object. Return null if the instantiated object is
	 * not an instance of TypedData.
	 */
	public TypedData createTypedData() {
		try {
			Object o = _class.newInstance();
			if (o instanceof TypedData) {
				return (TypedData) o;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Return the string ID.
	 */
	public String getID() {
		return _id;
	}

	/**
	 * Instantiate a Type object based on the specified type name. If no native
	 * types match this type name, return a Type of SimpleData.
	 */
	public static Type makeType(String typeName) {
		String className = (String) _typeMap.get(typeName);
		if (className != null) {
			try {
				return new Type(Class.forName(className), typeName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return NO_TYPE;
		} else {
			return new Type(SimpleData.class, typeName);
		}
	}

	/**
	 * Check the equivalence of this object and the specified object.
	 */
	public boolean equals(Object o) {
		if (o instanceof Type) {
			Type t = (Type) o;
			boolean sameClass = _class.equals(t._class);
			if (sameClass && _class.equals(SimpleData.class)) {
				return _id.equals(t.getID());
			}
			return sameClass;
		}
		return false;
	}

	/**
	 * Return the hashCode of this object. If the class is a SimpleData, use the
	 * string ID hash code, otherwise use the hash code of the class.
	 */
	public int hashCode() {
		if (_class.equals(SimpleData.class)) {
			return _id.hashCode();
		} else {
			return _class.hashCode();
		}
	}

	/**
	 * Return the parent of this type. If this is a NO_TYPE or SimpleData Type,
	 * return NO_TYPE. Otherwise, get the super class of this class.
	 */
	public Type getParent() {
		if (this == NO_TYPE || _class.equals(SimpleData.class)) {
			return NO_TYPE;
		}

		Class c = _class.getSuperclass();
		return new Type(c, c.getName());
	}

	/**
	 * Text representation of this Type.
	 */
	public String toString() {
		if (_class == SimpleData.class) {
			return _id;
		}
		return "<" + strip(_class.getName()) + ">";
	}
}
