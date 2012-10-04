/*
 * $Id: HHRecognizer.java,v 1.13 2003/10/23 17:46:16 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;


import org.hhreco.classification.Classification;
import org.hhreco.classification.ClassifierException;
import org.hhreco.classification.FeatureSet;
import org.hhreco.classification.SVMClassifier;
import org.hhreco.classification.TrainableClassifier;
import org.hhreco.classification.TrainingSet;
import org.hhreco.toolbox.ApproximateStrokeFilter;
import org.hhreco.toolbox.InterpolateStrokeFilter;
import org.hhreco.toolbox.Util;
import org.openawt.geom.AffineTransform;
import org.openawt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * This is a general purpose recognizer in which feature extractors and the
 * classifier can be user-specified. By default, the magnitudes of Zernike's
 * moments to the 8th order are used as features.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class HHRecognizer implements MultiStrokeRecognizer {
	public static double SIZE = 100;
	public static double CX = 0;
	public static double CY = 0;
	private FeatureExtractor[] _extractors = null;
	private TrainableClassifier _classifier = null;
	private TrainingSet _set = null;

	/**
	 * Create a recognizer that uses the default Zernike Moments feature
	 * extractor and the default SVM classifier.
	 */
	public HHRecognizer() {
		_extractors = defaultFeatureExtractors();
		_classifier = new SVMClassifier(true);
	}

	/**
	 * Create a recognizer that uses the specified set of feature extractors and
	 * the default SVM classifier.
	 */
	public HHRecognizer(FeatureExtractor[] extractors) {
		_extractors = extractors;
		_classifier = new SVMClassifier(true);
	}

	/**
	 * Create a recognizer that uses the default Zernike Moments feature
	 * extractor and the specified classifier.
	 */
	public HHRecognizer(TrainableClassifier c) {
		_extractors = defaultFeatureExtractors();
		_classifier = c;
	}

	/**
	 * Create a recognizer that uses the specified set of feature extractors and
	 * the classifier.
	 */
	public HHRecognizer(FeatureExtractor[] extractors, TrainableClassifier c) {
		_extractors = extractors;
		_classifier = c;
	}

	public static FeatureExtractor[] defaultFeatureExtractors() {
		FeatureExtractor[] extractors = new FeatureExtractor[1];
		extractors[0] = new ZernikeFE(8);
		return extractors;
	}

	/**
	 * Add the given shape to the existing training set and retrain the
	 * recognizer.
	 */
	public int addAndRetrain(String type, TimedStroke[] strokes) {
		FeatureSet fs = extractFeatures(_extractors, strokes);
		_set.addPositiveExample(type, fs);
		try {
			if (!_classifier.isIncremental()) {
				_classifier.clear();
			}
			_classifier.train(_set, fs.getFeatureCount());
		} catch (ClassifierException ex) {
			ex.printStackTrace();
		}
		return fs.getFeatureCount();
	}

	/**
	 * Add the given set of data to the existing training set and retrain the
	 * recognizer.
	 */
	public int addAndRetrain(MSTrainingModel model) {
		TrainingSet set = new TrainingSet();
		int numFeatures = createTrainingSet(model, set);
		for (Iterator iter = set.types(); iter.hasNext();) {
			String type = (String) iter.next();
			for (Iterator iter2 = set.positiveExamples(type); iter2.hasNext();) {
				FeatureSet fs = (FeatureSet) iter2.next();
				_set.addPositiveExample(type, fs);
			}
		}
		try {
			if (!_classifier.isIncremental()) {
				_classifier.clear();
			}
			_classifier.train(_set, numFeatures);
		} catch (ClassifierException ex) {
			ex.printStackTrace();
		}
		return numFeatures;
	}

	/**
	 * Extract features from the training examples and train the classifier with
	 * them. The recognizer is trained from scratch, any previously existing
	 * training data will be thrown away.
	 */
	public int train(MSTrainingModel model) {
		_set = new TrainingSet();
		int numFeatures = createTrainingSet(model, _set);
		try {
			if (!_classifier.isIncremental()) {
				_classifier.clear();
			}
			_classifier.train(_set, numFeatures);
		} catch (ClassifierException ex) {
			ex.printStackTrace();
		}
		return numFeatures;
	}

	/**
	 * Extract features from the examples in the 'model' and add them to the
	 * training set, 'set'. Return the number of features extracted for an
	 * example.
	 */
	private int createTrainingSet(MSTrainingModel model, TrainingSet set) {
		int numFeatures = -1;
		boolean isFirst = true;
		for (Iterator iter = model.types(); iter.hasNext();) {
			String type = (String) iter.next();
			for (Iterator iter2 = model.positiveExamples(type); iter2.hasNext();) {
				TimedStroke[] strokes = (TimedStroke[]) iter2.next();
				FeatureSet f = extractFeatures(_extractors, strokes);
				if (isFirst) {
					isFirst = false;
					numFeatures = f.getFeatureCount();
				} else if (numFeatures != f.getFeatureCount()) {
					throw new RuntimeException("Unequal feature numbers: "
							+ numFeatures + " vs. " + f.getFeatureCount());
				}
				set.addPositiveExample(type, f);
			}
		}
		return numFeatures;
	}

	/**
	 * Extract features from the given set of strokes.
	 */
	public static FeatureSet extractFeatures(FeatureExtractor[] extractors,
			TimedStroke[] strokes) {
		double[][] vals = new double[extractors.length][];
		int numFeatures = 0;
		for (int i = 0; i < extractors.length; i++) {
			vals[i] = extractors[i].apply(strokes);
			numFeatures += vals[i].length;
		}

		// concatenate the features
		double[] res = new double[numFeatures];
		int j = 0;
		for (int i = 0; i < vals.length; i++) {
			System.arraycopy(vals[i], 0, res, j, vals[i].length);
			j += vals[i].length;
		}
		return new FeatureSet(res);
	}

	public RecognitionSet strokeStarted(TimedStroke s) {
		return null;
	}

	public RecognitionSet strokeModified(TimedStroke s) {
		return null;
	}

	public RecognitionSet strokeCompleted(TimedStroke s) {
		return null;
	}

	/**
	 * Perform recognition on the given shape. The shape is assumed to have been
	 * scale and translation normalized.
	 */
	public RecognitionSet sessionCompleted(TimedStroke strokes[]) {
		try {
			FeatureSet f = extractFeatures(_extractors, strokes);
			Classification cl = _classifier.classify(f);
			RecognitionSet rset = new RecognitionSet();
			for (int j = 0; j < cl.getTypeCount(); j++) {
				if (Double.isNaN(cl.getConfidence(j))) {
					continue;
				} else {
					String type = cl.getType(j);
					double conf = cl.getConfidence(j);
					Recognition r = new Recognition(new SimpleData(type), conf);
					rset.addRecognition(r);
				}
			}
			if (rset.getRecognitionCount() == 0) {
				return RecognitionSet.NO_RECOGNITION;
			}
			return rset;
		} catch (ClassifierException ex) {
			ex.printStackTrace();
			return RecognitionSet.NO_RECOGNITION;
		}
	}

	/**
	 * This preprocessing routine normalizes the scaling and the translation of
	 * the given shape. The size becomes SIZE x SIZE, and the center of the
	 * shape locates at (0,0). The transform parameters are recorded in the 'at'
	 * passed in. It also approximates and interpolates the set of strokes using
	 * the specified filter.
	 */
	public static TimedStroke[] preprocess(TimedStroke[] s,
			ApproximateStrokeFilter approxFilter,
			InterpolateStrokeFilter interpFilter, AffineTransform at) {
		// translate such that the center is at (0,0)
		Rectangle2D bbox = Util.getBounds(s);
		double dx = -bbox.getMinX() - bbox.getWidth() / 2;
		double dy = -bbox.getMinY() - bbox.getHeight() / 2;
		for (int i = 0; i < s.length; i++) {
			s[i].translate(dx, dy);
		}

		// then scale
		if (at == null) {
			at = new AffineTransform();
		}
		double rw = SIZE / bbox.getWidth();
		double rh = SIZE / bbox.getHeight();
		at.scale(rw, rh);
		for (int i = 0; i < s.length; i++) {
			s[i].transform(at);
			if (approxFilter != null) {
				s[i] = approxFilter.approximate(s[i]);
			}
			if (interpFilter != null) {
				s[i] = interpFilter.interpolate(s[i]);
			}
		}

		at.translate(dx, dy);
		return s;
	}
}
