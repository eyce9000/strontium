/**
 * ComplexFitNN.java
 * 
 * Revision History:<br>
 * Oct 21, 2009 bpaulson - File created
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
package srl.recognition.paleo;

import java.util.ArrayList;
import java.util.List;


import org.openawt.svg.SVGGroup;

import srl.core.sketch.Interpretation;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.core.util.IsAConstants;
import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.paleoNN.PaleoNNRecognizer;
import srl.segmentation.paleo.ComplexShapeSegmenterNN;

/**
 * Complex fit that uses the NN version of Paleo
 * 
 * @author bpaulson
 */
public class ComplexFitNN extends Fit {

	/**
	 * Determines if the complex fit is the best fit
	 */
	protected boolean m_bestFit;

	/**
	 * Paleo config file
	 */
	protected PaleoConfig m_config;

	/**
	 * Sub strokes of the complex fit
	 */
	protected List<Stroke> m_subStrokes;

	/**
	 * Sub shapes of the complex fit
	 */
	protected List<Shape> m_subShapes;

	/**
	 * NN Recognizer
	 */
	protected PaleoNNRecognizer m_recognizer;

	/**
	 * Fit stroke to a complex fit
	 * 
	 * @param features
	 *            features of the stroke
	 */
	public ComplexFitNN(StrokeFeatures features, PaleoNNRecognizer recognizer) {
		super(features);
		m_recognizer = recognizer;
		m_bestFit = true;
		m_err = 0.0;
		m_subShapes = new ArrayList<Shape>();

		// do complex segmentation
		ComplexShapeSegmenterNN segmenter = new ComplexShapeSegmenterNN(
				m_features, m_recognizer);
		m_subStrokes = segmenter.getSegmentations().get(0)
				.getSegmentedStrokes();

		// compute complex fit
		for (Stroke s : m_subStrokes) {
			m_recognizer.submitForRecognition(s);
			IRecognitionResult result = m_recognizer.recognize();
			m_subShapes.add(result.getBestShape());
			m_err += result.getBestShape().getInterpretation().confidence;
		}
		m_err /= m_subStrokes.size();

		// generate beautified polyline
		generateComplex();
		try {
			computeBeautified();
			m_beautified.getAttributes().remove(IsAConstants.PRIMITIVE);
			m_beautified.setShapes(m_subShapes);
			String label = Fit.COMPLEX + " (";
			for (Shape s : m_subShapes)
				label += s.getInterpretation().label + ", ";
			label = label.substring(0, label.lastIndexOf(','));
			label += ")";
			m_beautified.setInterpretation(new Interpretation(label, m_err));
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("ComplexFit: passed = " + m_passed + " error = " + m_err
//				+ " num subs: " + m_subShapes.size() + " subFits: ");
//		for (int i = 0; i < m_subShapes.size(); i++) {
//			log.debug(m_subShapes.get(i).getInterpretation().label + " + ");
//		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return COMPLEX;
	}

	/**
	 * Get the list of sub strokes used in the complex interpretation
	 * 
	 * @return sub strokes
	 */
	public List<Stroke> getSubStrokes() {
		return m_subStrokes;
	}

	/**
	 * Generates the beautified complex shape
	 */
	protected void generateComplex() {
		m_shape = new SVGGroup();
		for(Shape shape:m_subShapes){
			((SVGGroup)m_shape).addShape(shape.getBeautifiedShape());
		}
	}
	

	/**
	 * Return list of subshapes
	 * 
	 * @return list of subshapes
	 */
	public List<Shape> getSubShapes() {
		return m_subShapes;
	}

	/**
	 * Determines if the complex fit is the best fit
	 * 
	 * @return true or false
	 */
	public boolean isBestFit() {
		return m_bestFit;
	}
}
