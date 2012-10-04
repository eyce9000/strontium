/*
 * $Id: KNNClassifier.java,v 1.3 2003/08/22 16:18:09 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.classification;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This K-nearest neighbor classifier compares a test example with every example
 * in the training set by computing the normalized Euclidean distance.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class KNNClassifier implements TrainableClassifier {
	private ArrayList _categories = new ArrayList();
	private int _numFeatures = 0;

	/**
	 * Classify the given example by testing it against each example in the
	 * training set.
	 */
	public Classification classify(FeatureSet s) throws ClassifierException {
		if (s.getFeatureCount() != _numFeatures) {
			throw new ClassifierException("Incorrect number of features: "
					+ s.getFeatureCount() + ", should be " + _numFeatures);
		}
		ArrayList pairs = new ArrayList();
		for (Iterator iter = _categories.iterator(); iter.hasNext();) {
			Category cat = (Category) iter.next();
			double[] normVals = cat.normalize(s.getFeatures());
			for (int i = 0; i < cat.getExampleCount(); i++) {
				double[] refVals = cat.getExample(i);
				double dist = distance(normVals, refVals);
				pairs.add(new Pair(cat.getType(), dist));
			}
		}
		String[] types = new String[pairs.size()];
		double[] values = new double[pairs.size()];
		int i = 0;
		for (Iterator iter = pairs.iterator(); iter.hasNext();) {
			Pair p = (Pair) iter.next();
			types[i] = p.type;
			values[i] = p.value;
			i++;
		}
		return new Classification(types, values);
	}

	/**
	 * A simple data structure to temporarily store classification results.
	 */
	private static class Pair {
		String type;
		double value;

		public Pair(String t, double v) {
			type = t;
			value = v;
		}
	}

	/**
	 * Train the classifier with a given training set. This method will throw a
	 * ClassifierException if the training set is not self consisistent, i.e.
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
			Category cg = new Category(type, dr, examples);
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
	 * Remove previously trained data.
	 */
	public void clear() {
		_categories.clear();
		_numFeatures = 0;
	}

	/**
	 * Compute the Euclidean distance of the two vectors.
	 */
	private static double distance(double[] vals, double[] ref) {
		int sum = 0;
		for (int i = 0; i < vals.length; i++) {
			double diff = vals[i] - ref[i];
			sum += diff * diff;
		}
		return Math.sqrt(sum);
	}

	/**
	 * An internal data structure storing the information of a class. Class is
	 * the term used in statistics. I'm calling it Category here to avoid the
	 * confusion with the "class" term used in the Object Orientated language.
	 */
	private static class Category {
		/** The name of the category. */
		private String _type;
		/** The data representation of this category. */
		private DataRep _dataRep;
		/** Normalized feature values for each example. */
		private double[][] _examples;
		/** The number of examples for this category. */
		private int _numExamples;

		/**
		 * Create a catetory with the given 'type' name, data representation,
		 * and examples. Normalize the example features.
		 */
		public Category(String type, DataRep dataRep, FeatureSet[] examples) {
			_type = type;
			_dataRep = dataRep;
			_examples = new double[examples.length][];
			_numExamples = examples.length;

			for (int i = 0; i < examples.length; i++) {
				double[] vals = examples[i].getFeatures();
				_examples[i] = normalize(vals);
			}
		}

		/**
		 * Normalize the feature values by subtracting the means and dividing by
		 * the standard deviations.
		 */
		public double[] normalize(double[] ex) {
			double[] means = _dataRep.getMeans();
			double[] stds = _dataRep.getStds();
			double[] normVals = new double[ex.length];
			for (int i = 0; i < ex.length; i++) {
				normVals[i] = (ex[i] - means[i]) / stds[i];
			}
			return normVals;
		}

		/** Return the name of this category. */
		public String getType() {
			return _type;
		}

		/** Return the data representation. */
		public DataRep getDataRep() {
			return _dataRep;
		}

		/** Return the i'th example. */
		public double[] getExample(int i) {
			return _examples[i];
		}

		/** Return the number of examples in this category. */
		public int getExampleCount() {
			return _numExamples;
		}
	}
}
