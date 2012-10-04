/**
 * PaleoNNRecognizer.java
 * 
 * Revision History:<br>
 * Feb 18, 2009 bpaulson - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package srl.recognition.paleo.paleoNN;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.patternrec.classifiers.core.CResult;
import srl.patternrec.classifiers.core.Classifiable;
import srl.patternrec.classifiers.core.Classifier;
import srl.recognition.IRecognitionResult;
import srl.recognition.RecognitionResult;
import srl.recognition.paleo.ComplexFitNN;
import srl.recognition.paleo.Fit;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoFeatureExtractor;
import srl.recognition.paleo.StrokeFeatures;
import srl.recognition.recognizer.IRecognizer;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Neural network version of Paleo
 * 
 * @author bpaulson
 */
public class PaleoNNRecognizer extends Classifier implements
IRecognizer<Stroke, IRecognitionResult> {

	/**
	 * Neural net
	 */
	private MultilayerPerceptron m_nn;

	/**
	 * Stroke features
	 */
	private StrokeFeatures m_features;

	/**
	 * Paleo feature extractor
	 */
	private PaleoFeatureExtractor m_pfe = null;

	/**
	 * Paleo config file
	 */
	private PaleoConfig m_config;

	/**
	 * Stroke to recognize
	 */
	private Stroke m_stroke;

	/**
	 * Keeps a history of previously recognized strokes
	 */
	private Map<Stroke, IRecognitionResult> m_history = new HashMap<Stroke, IRecognitionResult>();

	/**
	 * Flag denoting if complex test should be performed
	 */
	private boolean m_doComplex = true;

	/**
	 * Flag denoting if history should be turned on
	 */
	private boolean m_historyOn = false;
	
	private Logger log = LoggerFactory.getLogger(PaleoNNRecognizer.class);

	/**
	 * Default constructor
	 * 
	 * @param config
	 *            paleo config file
	 */
	public PaleoNNRecognizer(PaleoConfig config) {

		resetNN();
		m_config = config;
	}

	/**
	 * Set the stroke features
	 * 
	 * @param features
	 *            stroke features
	 */
	public void setFeatures(StrokeFeatures features) {
		m_features = features;
		m_pfe = new PaleoFeatureExtractor(m_features, m_config);
	}

	public void setHistoryOn(boolean flag) {
		m_historyOn = flag;
	}

	/**
	 * Resets the neural network
	 * @throws Exception 
	 */
	private void resetNN() {
		try{
		InputStream is = PaleoNNRecognizer.class
				.getResourceAsStream("models/paleo.limited.model");
		if (is != null)
			m_nn = (MultilayerPerceptron) weka.core.SerializationHelper
			.read(is);
		}
		catch(Exception ex){
			log.error("Error loading PaleoNN Model", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	@Override
	public IRecognitionResult recognize() {
		if (m_nn == null || m_stroke == null)
			return null;

		// see if this stroke has been recognized before
		if (m_historyOn) {
			IRecognitionResult r = m_history.get(m_stroke);
			if (r != null)
				return r;
		}

		if (m_features == null) {
			m_features = new StrokeFeatures(m_stroke, false);
			setFeatures(m_features);
		}
		IRecognitionResult r = new RecognitionResult();
		try {
			Instance testInstance = m_pfe.getInstance(null);
			double[] results = m_nn.distributionForInstance(testInstance);
			for (int i = 0; i < results.length; i++) {
				String name = (String) m_pfe.getClassLabels().elementAt(i);
				Fit f = null;
				if(name!=null)
					f = m_pfe.getFit(name);
				Shape fitShape = new Shape();
				if (f != null)
					fitShape = (Shape) f.getShape().clone();
				else
					fitShape.setLabel(name);
				fitShape.getInterpretation().confidence = (results[i]);
				r.addShapeToNBestList(fitShape);
			}

			// handle complex
			if (m_config.isComplexTestOn() && m_doComplex) {
				m_doComplex = false;
				try {
					ComplexFitNN complex = new ComplexFitNN(m_features, this);
					if (complex.getSubShapes().size() >= 2) {

						Shape best = r.getBestShape();
						boolean complexBest = false;
						if (best.getInterpretation().label
								.startsWith(Fit.COMPLEX))
							complexBest = true;

						// remove old complex interpretation
						for (Shape s : r.getNBestList()) {
							if (s.getInterpretation().label
									.startsWith(Fit.COMPLEX)) {
								r.getNBestList().remove(s);
								break;
							}
						}

						// see if complex contains only lines
						// if (allLines(complex.getSubShapes())) {
						// for (Shape s : r.getNBestList())
						// if
						// (s.getInterpretation().label.startsWith(Fit.POLYLINE)
						// && s.getInterpretation().confidence <
						// complex.getShape()
						// .getInterpretation().confidence
						// && s.getSubShapes().size() > 1)
						// s.getInterpretation().confidence =
						// (complex.getShape()
						// .getInterpretation().confidence);
						// }
						// else {

						if (complex.getShape().getInterpretation().confidence > r
								.getBestShape().getInterpretation().confidence) {
							// augment complex confidence by x% of the
							// difference

							// double complexScore = calcComplexScore(complex
							// .getSubShapes());
							// double topScore =
							// calcShapeScore(r.getBestShape());

							double complexScore = complex.getSubShapes().size();
							double topScore = 1;
							if (r.getBestShape().getInterpretation().label
									.startsWith(Fit.POLYGON)
									|| r.getBestShape().getInterpretation().label
									.startsWith(Fit.POLYLINE))
								topScore = r.getBestShape().getShapes().size();

							// if (allLines(complex.getSubShapes()))
							// complexScore *= 2;
							if (complexScore > topScore) {
								double newConf = complex.getShape()
										.getInterpretation().confidence
										- (0.25 * (complexScore - topScore));
								if (newConf < 0)
									newConf = 0;
								complex.getShape().getInterpretation().confidence = (newConf);
							}
							if (allLines(complex.getSubShapes()))
								complex.getShape().getInterpretation().confidence = (Math
										.max(complex.getShape()
												.getInterpretation().confidence - 0.1,
												0.0));
						}

						// if complex was originally chosen by the NN as the
						// best shape then use its confidence
						if (complexBest
								&& best.getInterpretation().confidence > complex
								.getShape().getInterpretation().confidence)
							complex.getShape().getInterpretation().confidence = (best
									.getInterpretation().confidence);

						r.addShapeToNBestList(complex.getShape());
						r.sortNBestList();

						// }
					}
				} catch (NullPointerException e) {
					// NN bug
				}
				m_doComplex = true;
			} else {
				// remove complex from list because complex fit is not on
				for (Shape s : r.getNBestList()) {
					if (s.getInterpretation().label.startsWith(Fit.COMPLEX)) {
						r.getNBestList().remove(s);
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		r.sortNBestList();

		// check for bad NN state (typically happens on small dots?)
		if (Double.isNaN(r.getBestShape().getInterpretation().confidence)) {
			resetNN();
			r = new RecognitionResult();
			Fit f = m_pfe.getFit(Fit.DOT);
			Shape fitShape = new Shape();
			if (f != null)
				fitShape = (Shape) f.getShape().clone();
			else
				fitShape.setLabel(Fit.DOT);
			fitShape.getInterpretation().confidence = (0.0);
			r.addShapeToNBestList(fitShape);
			return r;
		}

		// add to history
		if (m_historyOn)
			m_history.put(m_stroke, r);

		return r;
	}

	/**
	 * Determines if all sub shapes are lines or polylines
	 * 
	 * @param subShapes
	 *            sub shape list
	 * @return true if all lines, else false
	 */
	private boolean allLines(List<Shape> subShapes) {
		for (Shape s : subShapes) {
			if (!s.getInterpretation().label.startsWith(Fit.POLYLINE)
					&& !s.getInterpretation().label.equalsIgnoreCase(Fit.LINE))
				return false;
		}
		return true;
	}

	/**
	 * Calculates the complex interpretation score
	 */
	protected double calcComplexScore(List<Shape> subShapes) {
		double m_complexScore = 0;
		for (Shape s : subShapes)
			m_complexScore += calcShapeScore(s);
		return m_complexScore;
	}

	/**
	 * Calculates the complex interpretation score
	 */
	protected double calcShapeScore(Shape shape) {
		if (shape.getInterpretation().label.equalsIgnoreCase(Fit.LINE))
			return 1;
		if (shape.getInterpretation().label.equalsIgnoreCase(Fit.ARC))
			return 3;
		if (shape.getInterpretation().label.equalsIgnoreCase(Fit.CURVE))
			return 5;
		if (shape.getInterpretation().label.equalsIgnoreCase(Fit.ELLIPSE))
			return 4;
		if (shape.getInterpretation().label.equalsIgnoreCase(Fit.CIRCLE))
			return 4;
		if (shape.getInterpretation().label.startsWith(Fit.POLYGON)
				|| shape.getInterpretation().label.startsWith(Fit.POLYLINE))
			return shape.getShapes().size();
		if (shape.getInterpretation().label.equalsIgnoreCase(Fit.RECTANGLE)
				|| shape.getInterpretation().label.equalsIgnoreCase(Fit.SQUARE)
				|| shape.getInterpretation().label
				.equalsIgnoreCase(Fit.DIAMOND))
			return 4;
		return 5;
	}

	/**
	 * Clears history of recognized strokes
	 */
	public void clear() {
		if (m_historyOn)
			m_history.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.IRecognizer#submitForRecognition(java
	 * .lang.Object)
	 */
	@Override
	public void submitForRecognition(Stroke submission) {
		m_stroke = submission;
		m_features = null;
	}

	@Override
	public List<CResult> classify(Classifiable query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void train() {
		// TODO Auto-generated method stub

	}
}
