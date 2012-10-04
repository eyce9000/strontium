/*
 * $Id: SVMClassifier.java,v 1.16 2003/08/22 16:18:10 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.classification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

/**
 * This class uses libsvm, a SVM software library written in Java, to do SVM
 * classification. [1]
 * <p>
 * [1]Chih-Chung Chang and Chih-Jen Lin http://www.csie.ntu.edu.tw/~cjlin/libsvm
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class SVMClassifier implements TrainableClassifier {
	private boolean _normalizeScale = true;
	private int _numFeatures;
	private HashMap _labelToType = new HashMap();
	private svm_model _svmModel;
	private svm_parameter _svmParam = null;
	private double[] _minFeatureVals;
	private double[] _maxFeatureVals;

	/**
	 * Create a SVM Classifier with default parameters and specify whether the
	 * data needs to be scaled.
	 */
	public SVMClassifier(boolean normalizeScale) {
		_normalizeScale = normalizeScale;
		_svmParam = defaultSVMParam();
	}

	/**
	 * Create a SVM Classifier, specify whether the data needs to be scaled, and
	 * use the given set of parameters for the SVM classifier.
	 */
	public SVMClassifier(boolean normalizeScale, svm_parameter p) {
		_normalizeScale = normalizeScale;
		_svmParam = p;
	}

	/**
	 * Classify the given example using a single-class classifier.
	 */
	private Classification classifySingle(FeatureSet fs) {
		if (_normalizeScale) {
			fs = scale(fs);
		}
		svm_node[] ex = new svm_node[fs.getFeatureCount()];
		for (int j = 0; j < fs.getFeatureCount(); j++) {
			ex[j] = new svm_node();
			ex[j].index = j;
			ex[j].value = fs.getFeature(j);
		}
		double val = svm.svm_predict(_svmModel, ex);
		String[] types = new String[1];
		double[] confidences = new double[1];
		types[0] = (String) _labelToType.values().iterator().next();
		confidences[0] = val;
		return new Classification(types, confidences);
	}

	/**
	 * Classify the given example using SVM.
	 */
	public Classification classify(FeatureSet fs) throws ClassifierException {
		if (_svmParam.svm_type == svm_parameter.ONE_CLASS) {
			System.out.println("ONE_CLASS");
			return classifySingle(fs);
		} else {
			if (_normalizeScale) {
				fs = scale(fs);
			}
			svm_node[] ex = new svm_node[fs.getFeatureCount()];
			for (int j = 0; j < fs.getFeatureCount(); j++) {
				ex[j] = new svm_node();
				ex[j].index = j;
				ex[j].value = fs.getFeature(j);
			}
			int label = (int) svm.svm_predict(_svmModel, ex);
			String type = (String) _labelToType.get(new Integer(label));
			String[] types = { type };
			double[] values = { 1.0 };
			return new Classification(types, values);
			/*
			 * svm.SVMResult result = svm.svm_predict_hh(_svmModel, ex); int[]
			 * labels = result.classPrediction(); ArrayList pairs = new
			 * ArrayList(); for(int i=0; i<labels.length; i++){ String type
			 * =(String)_labelToType.get(new Integer(labels[i])); double[]
			 * decFuncValues = result.values(labels[i]); for(int j=0;
			 * j<decFuncValues.length; j++){ pairs.add(new
			 * Pair(type,decFuncValues[j])); } } String[] types = new
			 * String[pairs.size()]; double[] values = new double[pairs.size()];
			 * int i=0; for(Iterator iter=pairs.iterator(); iter.hasNext();){
			 * Pair p = (Pair)iter.next(); types[i]=p.type; values[i]=p.value;
			 * i++; } return new Classification(types,values);
			 */
		}
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
	 * Convert TraininSet examples into libsvm data structure, build a
	 * svm_problem object for the training set, and call svm_train to compute
	 * the svm model. If scaling is indicated, this method will scale the data
	 * values to be in the range of 0 and 1.
	 */
	public void train(TrainingSet tset, int numFeatures)
			throws ClassifierException {
		_numFeatures = numFeatures;
		if (_normalizeScale) {
			tset = scale(tset, 0, 1);// scale values to be in the range of 0 and
										// 1
		}
		ArrayList list = new ArrayList();
		int label = 1;
		for (Iterator types = tset.types(); types.hasNext();) {
			String type = (String) types.next();
			_labelToType.put(new Integer(label), type);
			for (Iterator examples = tset.positiveExamples(type); examples
					.hasNext();) {
				FeatureSet fs = (FeatureSet) examples.next();
				svm_node[] ex = new svm_node[fs.getFeatureCount()];
				for (int j = 0; j < fs.getFeatureCount(); j++) {
					ex[j] = new svm_node();
					ex[j].index = j;
					ex[j].value = fs.getFeature(j);
				}
				list.add(new Example(label, ex));
			}
			label++;
		}
		svm_problem prob = new svm_problem();
		int num = list.size();
		prob.l = num;
		double[] y = new double[num];
		svm_node[][] x = new svm_node[num][];
		for (int i = 0; i < num; i++) {
			Example ex = (Example) list.get(i);
			y[i] = ex.label;
			x[i] = ex.nodes;
		}
		prob.y = y;
		prob.x = x;
		if (_svmParam == null) {
			_svmParam = defaultSVMParam();
		}
		_svmModel = svm.svm_train(prob, _svmParam);

	}

	/**
	 * Set up the default set of parameters for the SVM classifier.
	 */
	public static svm_parameter defaultSVMParam() {
		svm_parameter p = new svm_parameter();
		// default values for now
		p = new svm_parameter();
		p.svm_type = svm_parameter.C_SVC;
		p.kernel_type = svm_parameter.RBF;
		p.degree = 3;
		p.gamma = 0.5;
		p.coef0 = 0;
		p.nu = 0.5;
		p.cache_size = 40;
		p.C = 100;
		p.eps = 1e-3;
		p.p = 0.1;
		p.shrinking = 1;
		p.nr_weight = 0;
		p.weight_label = null;
		p.weight = null;
		return p;
	}

	/**
	 * Scale the given feature vector based on the scale obtained from the
	 * training set. This method is called by classify to scale the test
	 * example.
	 */
	public FeatureSet scale(FeatureSet fvals) {
		double[] normVals = new double[fvals.getFeatureCount()];
		for (int i = 0; i < fvals.getFeatureCount(); i++) {
			normVals[i] = (fvals.getFeature(i) - _minFeatureVals[i])
					/ (_maxFeatureVals[i] - _minFeatureVals[i]);
		}
		return new FeatureSet(normVals);
	}

	/**
	 * Scale the feature values in the training set to be in the range of
	 * [min,max].
	 */
	public TrainingSet scale(TrainingSet tset, int min, int max) {
		_minFeatureVals = new double[_numFeatures];
		_maxFeatureVals = new double[_numFeatures];
		for (int i = 0; i < _numFeatures; i++) {
			_minFeatureVals[i] = Double.POSITIVE_INFINITY;
			_maxFeatureVals[i] = Double.NEGATIVE_INFINITY;
		}
		// figure out the minimum and maximum values of each feature
		for (Iterator types = tset.types(); types.hasNext();) {
			String type = (String) types.next();
			for (Iterator examples = tset.positiveExamples(type); examples
					.hasNext();) {
				FeatureSet fs = (FeatureSet) examples.next();
				for (int i = 0; i < fs.getFeatureCount(); i++) {
					_minFeatureVals[i] = Math.min(_minFeatureVals[i],
							fs.getFeature(i));
					_maxFeatureVals[i] = Math.max(_maxFeatureVals[i],
							fs.getFeature(i));
				}
			}
			for (Iterator examples = tset.negativeExamples(type); examples
					.hasNext();) {
				FeatureSet fs = (FeatureSet) examples.next();
				for (int i = 0; i < fs.getFeatureCount(); i++) {
					_minFeatureVals[i] = Math.min(_minFeatureVals[i],
							fs.getFeature(i));
					_maxFeatureVals[i] = Math.max(_maxFeatureVals[i],
							fs.getFeature(i));
				}
			}
		}
		// scale feature values to be in the range of [0,1]
		TrainingSet normalizedSet = new TrainingSet();
		for (Iterator types = tset.types(); types.hasNext();) {
			String type = (String) types.next();
			for (Iterator examples = tset.positiveExamples(type); examples
					.hasNext();) {
				FeatureSet fs = (FeatureSet) examples.next();
				double[] vals = new double[_numFeatures];
				for (int i = 0; i < fs.getFeatureCount(); i++) {
					vals[i] = (fs.getFeature(i) - _minFeatureVals[i])
							/ (_maxFeatureVals[i] - _minFeatureVals[i]);
				}
				normalizedSet.addPositiveExample(type, new FeatureSet(vals));
			}
			for (Iterator examples = tset.negativeExamples(type); examples
					.hasNext();) {
				FeatureSet fs = (FeatureSet) examples.next();
				double[] vals = new double[_numFeatures];
				for (int i = 0; i < fs.getFeatureCount(); i++) {
					vals[i] = (fs.getFeature(i) - _minFeatureVals[i])
							/ (_maxFeatureVals[i] - _minFeatureVals[i]);
				}
				normalizedSet.addNegativeExample(type, new FeatureSet(vals));
			}
		}
		return normalizedSet;
	}

	/**
	 * Not an incremental classifier. Calls to train will remove the original
	 * training data and retrain the classifier with the new set.
	 */
	public boolean isIncremental() {
		return false;
	}

	public void clear() {
	}

	/**
	 * An internal data structure representing an example.
	 */
	private static class Example {
		public int label;
		public svm_node[] nodes;

		public Example(int l, svm_node[] n) {
			label = l;
			nodes = n;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(label + " ");
			for (int i = 0; i < nodes.length; i++) {
				sb.append(nodes[i].index + ":" + nodes[i].value + " ");
			}
			return sb.toString();
		}
	}
}
