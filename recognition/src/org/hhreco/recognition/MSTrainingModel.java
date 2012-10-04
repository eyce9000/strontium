/*
 * $Id: MSTrainingModel.java,v 1.7 2003/10/03 22:02:56 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.hhreco.util.NullIterator;

/**
 * MSTrainingModel (Multi-Stroke Training Model) is a data structure for storing
 * training examples and their types. Each training example is a set of
 * TimedStroke objects.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class MSTrainingModel {
	/**
	 * Internal constant for the array slot of negative examples.
	 */
	public static final boolean NEGATIVE = false;

	/**
	 * Internal constant for the array slot of positive examples.
	 */
	public static final boolean POSITIVE = true;

	/**
	 * Store a mapping from types to their positive/negative examples.
	 */
	private HashMap _map;

	/**
	 * Construct an empty training model.
	 */
	public MSTrainingModel() {
		_map = new HashMap();
	}

	/**
	 * Add a negative example to this training model for the given type.
	 */
	public final void addNegativeExample(String t, TimedStroke s[]) {
		addExample(t, s, NEGATIVE);
	}

	/**
	 * Add a positive example to this training model for the given type.
	 */
	public final void addPositiveExample(String t, TimedStroke s[]) {
		addExample(t, s, POSITIVE);
	}

	/**
	 * Add an example to this training model for the given type (either positive
	 * or negative, denoted by the "which" argument).
	 */
	public final void addExample(String t, TimedStroke s[], boolean which) {
		ArrayList[] l = (ArrayList[]) _map.get(t);
		if (l == null) {
			l = new ArrayList[2];
			l[0] = new ArrayList();
			l[1] = new ArrayList();
			_map.put(t, l);
		}
		int index = (which) ? 1 : 0;
		l[index].add(s);
	}

	/**
	 * Remvoe all data from the model.
	 */
	public void clear() {
		_map.clear();
	}

	/**
	 * Add the data in the specified model into "this" model.
	 */
	public final void combine(MSTrainingModel model) {
		for (Iterator iter = model.types(); iter.hasNext();) {
			String type = (String) iter.next();
			for (Iterator examples = model.positiveExamples(type); examples
					.hasNext();) {
				TimedStroke s[] = (TimedStroke[]) examples.next();
				this.addPositiveExample(type, s);
			}
			for (Iterator examples = model.negativeExamples(type); examples
					.hasNext();) {
				TimedStroke s[] = (TimedStroke[]) examples.next();
				this.addNegativeExample(type, s);
			}
		}
	}

	/**
	 * Return true if the training type with the specified name is in the model,
	 * or false otherwise.
	 */
	public final boolean containsType(String t) {
		return _map.get(t) != null;
	}

	/**
	 * An internal method to get the example count for a particular type (either
	 * positive or negative, denoted by the "which" argument).
	 */
	private final int getExampleCount(String t, boolean which) {
		ArrayList[] l = (ArrayList[]) _map.get(t);
		if (l == null) {
			return 0;
		} else {
			int index = (which) ? 1 : 0;
			return l[index].size();
		}
	}

	/**
	 * An internal method to get the examples for a particular type (either
	 * positive or negative, denoted by the "which" argument). Each example
	 * returned in the iterator is an array of TimedStrokes (TimedStroke[]).
	 */
	private final Iterator getExamples(String t, boolean which) {
		ArrayList[] l = (ArrayList[]) _map.get(t);
		if (l == null) {
			return new NullIterator();
		}
		int index = (which) ? 1 : 0;
		return l[index].iterator();
	}

	/**
	 * Return the positive example at the specified index for the given type.
	 */
	public final TimedStroke[] getPositiveExample(String t, int i) {
		ArrayList[] l = (ArrayList[]) _map.get(t);
		return (TimedStroke[]) l[1].get(i);// POSITIVE=1
	}

	/**
	 * Return the negative example at the specified index for the given type.
	 */
	public final TimedStroke[] getNegativeExample(String t, int i) {
		ArrayList[] l = (ArrayList[]) _map.get(t);
		return (TimedStroke[]) l[0].get(i);// NEGATIVE=0
	}

	/**
	 * Return how many types are contained in this training model.
	 */
	public final int getTypeCount() {
		return _map.size();
	}

	/**
	 * Return the number of negative examples for the given type.
	 */
	public final int negativeExampleCount(String t) {
		return getExampleCount(t, NEGATIVE);
	}

	/**
	 * An iterator over the negative examples for the given type. Each example
	 * returned in the iterator is an array of TimedStrokes (TimedStroke[]).
	 */
	public final Iterator negativeExamples(String t) {
		return getExamples(t, NEGATIVE);
	}

	/**
	 * Returns the number of positive examples for the given type.
	 */
	public final int positiveExampleCount(String t) {
		return getExampleCount(t, POSITIVE);
	}

	/**
	 * An iterator over the positive examples for the given type. Each example
	 * returned in the iterator is an array of TimedStrokes (TimedStroke[]).
	 */
	public final Iterator positiveExamples(String t) {
		return getExamples(t, POSITIVE);
	}

	/**
	 * Remove the specified example of the given type from this training model.
	 * (either positive or negative, denoted by the "which" argument). If the
	 * type contains no examples (both positive and negative), remove the type
	 * from the model.
	 */
	public final void removeExample(String t, TimedStroke s[], boolean which) {
		ArrayList[] l = (ArrayList[]) _map.get(t);
		if (l == null) {
			return;
		}
		int index = (which) ? 1 : 0;
		l[index].remove(s);
		if (l[1].isEmpty() && l[0].isEmpty()) {
			_map.remove(t);
		}
	}

	/**
	 * Remove the specified negative example of the specified type from this
	 * training set. This method cannot be called while iterating over the
	 * negative examples, otherwise a ConcurrentModificationException will be
	 * thrown.
	 */
	public final void removeNegativeExample(String t, TimedStroke[] s) {
		removeExample(t, s, NEGATIVE);
	}

	/**
	 * Remove the specified positive example of the specified type from this
	 * training set. This method cannot be called while iterating over the
	 * positive examples, otherwise a ConcurrentModificationException will be
	 * thrown.
	 */
	public final void removePositiveExample(String t, TimedStroke[] s) {
		removeExample(t, s, POSITIVE);
	}

	/**
	 * Remove the specified type from this training set. This method cannot be
	 * called while iterating over the types, otherwise a
	 * ConcurrentModificationException will be thrown.
	 */
	public final void removeType(String t) {
		_map.remove(t);
	}

	/**
	 * An iterator over the types contained in this training model.
	 */
	public Iterator types() {
		return _map.keySet().iterator();
	}

	/**
	 * Text representation of this class containing information on the types and
	 * the number of positive and negative examples in the model.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (Iterator iter = types(); iter.hasNext();) {
			String type = (String) iter.next();
			int numPos = positiveExampleCount(type);
			int numNeg = negativeExampleCount(type);
			buf.append(type + ": " + numPos + " positives, " + numNeg
					+ " negatives\n");
			buf.append(numPos + "\t" + type + "\n");
		}
		return buf.toString();
	}
}
