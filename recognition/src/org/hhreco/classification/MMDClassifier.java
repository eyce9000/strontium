/*
 * $Id: MMDClassifier.java,v 1.4 2003/10/13 23:56:58 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.classification;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Minimum distance classifier measures the normalized Euclidean distance
 * between the test example and each of the training classes.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class MMDClassifier implements TrainableClassifier {
	private ArrayList _categories = new ArrayList();
	private int _numFeatures = 0;

	/**
	 * Classify the given example by computing the normalized Euclidean distance
	 * to each training class.
	 */
	public Classification classify(FeatureSet s) throws ClassifierException {
		if (s.getFeatureCount() != _numFeatures) {
			throw new ClassifierException("Incorrect number of features: "
					+ s.getFeatureCount() + ", should be " + _numFeatures);
		}
		String[] types = new String[_categories.size()];
		double[] values = new double[_categories.size()];
		int i = 0;
		for (Iterator iter = _categories.iterator(); iter.hasNext();) {
			Category cat = (Category) iter.next();
			double val = cat.distance(s.getFeatures());
			types[i] = cat.getType();
			values[i] = val;
			i++;
		}
		return new Classification(types, values);
	}

	/**
	 * Train the classifier with the given training set. This method will throw
	 * a ClassifierException if the training set is not self consisistent, i.e.
	 * the feature sets that it contains do not have the same number of features
	 * in them.
	 * 
	 * Any previously training data, if any, will be removed upon the call of
	 * this method.
	 */
	public void train(TrainingSet s, int numFeatures)
			throws ClassifierException {
		clear();
		_numFeatures = numFeatures;
		for (Iterator iter = s.types(); iter.hasNext();) {
			String type = (String) iter.next();
			int num = s.positiveExampleCount(type);
			FeatureSet[] examples = new FeatureSet[num];
			int k = 0;
			for (Iterator e = s.positiveExamples(type); e.hasNext();) {
				examples[k++] = (FeatureSet) e.next();
			}
			DataRep dr = new DataRep(examples, numFeatures);
			Category cg = new Category(type, dr);
			_categories.add(cg);
		}
	}

	/**
	 * Return whether this classifier is incremental, i.e. whether new data can
	 * be added to the existing training set (A) without reprocessing the
	 * examples in A.
	 */
	public boolean isIncremental() {
		return false;
	}

	/**
	 * Clear all previously trained data.
	 */
	public void clear() {
		_categories.clear();
		_numFeatures = 0;
	}

	/**
	 * An internal data structure storing the information of a class.
	 */
	private static class Category {
		/** The name of the category. */
		private String _type;
		/** The data representation of this category. */
		private DataRep _dataRep;

		/**
		 * Create a catetory with the given 'type' name and data representation.
		 */
		public Category(String type, DataRep dataRep) {
			_type = type;
			_dataRep = dataRep;
		}

		/** Return the name of this category. */
		public String getType() {
			return _type;
		}

		/** Return the data representation. */
		public DataRep getDataRep() {
			return _dataRep;
		}

		/**
		 * Return the normalized Euclidean distance between the given example
		 * and this class.
		 */
		private double distance(double[] vals) {
			int sum = 0;
			double[] means = _dataRep.getMeans();
			double[] variances = _dataRep.getVariances();
			for (int i = 0; i < vals.length; i++) {
				double diff = vals[i] - means[i];
				sum += (diff * diff) / variances[i];
			}
			return sum;
		}
	}
}
