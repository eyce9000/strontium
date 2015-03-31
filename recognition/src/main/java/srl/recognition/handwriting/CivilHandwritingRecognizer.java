/**
 * HandwritingRecognizer.java
 * 
 * Revision History:<br>
 * Jan 13, 2009 bde - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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

package srl.recognition.handwriting;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Shape;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;
import srl.recognition.grouping.CivilGrouper;
import srl.recognition.recognizer.OverTime;
import srl.recognition.recognizer.OverTimeException;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Handwriting recognizer that works solely with CivilSketch handwriting.<br>
 * <br>
 * This entire class has been cleaned from the HandwritingRecognizer.java
 * version, which is more generic.<br>
 * <br>
 * This class is currently unused since a more generic method is recommended,
 * but, if specific HWRs are preferred, use this instead.
 * 
 * @author awolin, beoff
 */
public class CivilHandwritingRecognizer {

	/**
	 * WEKA neural network model
	 */
	private MultilayerPerceptron m_mlp;

	/**
	 * Target WEKA attribute set.
	 */
	private Attribute m_targetAttribute;

	/**
	 * WEKA model dataset.
	 */
	private Instances m_dataSet;

	/**
	 * Strokes submitted to the recognizer.
	 */
	private List<Stroke> m_strokesSubmitted;

	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory
			.getLogger(CivilHandwritingRecognizer.class);

	/**
	 * Default constructor that initializes the models.
	 */
	public CivilHandwritingRecognizer() {

		try {
			InputStream stream = CivilHandwritingRecognizer.class.getResourceAsStream("models/civilHWR-MLP_8-2010-04-15.model");
			m_mlp = (MultilayerPerceptron) weka.core.SerializationHelper
					.read(stream);
		} catch (Exception e) {
			log.error(e.toString(),e);
		}

		m_targetAttribute = BuildTargetAttribute.buildCivilAttribute();
		m_dataSet = BuildTargetAttribute.createCivilInstancesDataSet();

		m_strokesSubmitted = new ArrayList<Stroke>();
	}

	/**
	 * Recognize a series of strokes as a character.
	 * 
	 * @param list
	 *            strokes to recognize.
	 * @param maxTime
	 *            maximum allotted recognition time.
	 * @return the recognized character with confidences.
	 * @throws OverTimeException
	 *             if the recognition exceeds the {@code maxTime}.
	 */
	public Character characterRecognizer(List<Stroke> strokes, long maxTime)
			throws OverTimeException {

		// Store the start time
		long startTime = System.currentTimeMillis();

		Sketch holderSketch = new Sketch();
		holderSketch.setStrokes(strokes);

		Instance characterInstance = null;
		double[] distribution = null;

		// Initialize the feature set outside of the recognizer
		characterInstance = CivilAttributes.generateCivilFeatures(holderSketch,
				m_dataSet);

		try {
			distribution = m_mlp.distributionForInstance(characterInstance);
			OverTime.overTimeCheck(startTime, maxTime, log);
		} catch (OverTimeException ote) {
			// Don't log it
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}

		List<ResultConfidencePairing> rcp = new ArrayList<ResultConfidencePairing>();

		for (int i = 0; i < distribution.length; i++) {

			rcp.add(new ResultConfidencePairing(m_targetAttribute.value(i),
					distribution[i]));
			OverTime.overTimeCheck(startTime, maxTime, log);
		}

		return new Character(rcp, strokes);
	}

	/**
	 * Submit for recognition, given a set of strokes
	 */
	public void submitForRecognition(List<Stroke> submission) {
		for (Stroke st : submission) {
			submitForRecognition(st);
		}

	}

	/**
	 * Takes in an Stroke, and adds it the the group manager
	 * 
	 * @param submission
	 */
	public void submitForRecognition(Stroke submission) {
		m_strokesSubmitted.add(submission);
	}

	/**
	 * Recognize a series of strokes as a character.
	 * 
	 * @param list
	 *            strokes to recognize.
	 * @param maxTime
	 *            maximum allotted recognition time.
	 * @return the recognized character with confidences.
	 * @throws OverTimeException
	 *             if the recognition exceeds the {@code maxTime}.
	 */
	public Shape recognizeOneChar(List<Stroke> list, long maxTime)
			throws OverTimeException {

		// Store the start time
		long startTime = System.currentTimeMillis();

		Character c = characterRecognizer(list, OverTime.timeRemaining(
				startTime, maxTime));
		Shape s = new Shape();
		s.setStrokes(list);

		// The number of character results we have for each shape
		int numResults = 3;

		// We have to handle periods separately, since recognition on them is
		// quite poor.
		if (c.getBestResult().equals("period")) {

			s.setAttribute("TEXT_BEST", c.getResults().get(1).getResult());
			s.setInterpretation("Text", c.getConfidence(c.getResults().get(1).getResult()));

			for (int i = 0; i < Math.min(numResults, c.getResults().size() - 1); i++) {
				s.setAttribute(c.getResults().get(1 + i).getResult(), Double
						.toString(c.getConfidence(c.getResults().get(1 + i)
								.getResult())));
			}
		} else {

			s.setAttribute("TEXT_BEST", c.getBestResult());
			s.setInterpretation("Text", c.getConfidence(c.getBestResult()));

			for (int i = 0; i < Math.min(numResults, c.getResults().size()); i++) {
				s.setAttribute(c.getResults().get(i).getResult(), Double
						.toString(c.getConfidence(c.getResults().get(i)
								.getResult())));
			}
		}

		return s;
	}

	/**
	 * Recognize a series of strokes as a given character.
	 * 
	 * @param list
	 *            strokes to recognize.
	 * @param maxTime
	 *            maximum allotted recognition time.
	 * @param character
	 *            character to recognize.
	 * @return the recognized character with confidences.
	 * @throws OverTimeException
	 *             if the recognition exceeds the {@code maxTime}.
	 */
	public Shape recognizeOneChar(List<Stroke> list, long maxTime,
			String character) throws OverTimeException {

		// Store the start time
		long startTime = System.currentTimeMillis();

		Character c = characterRecognizer(list, OverTime.timeRemaining(
				startTime, maxTime));
		Shape s = new Shape();

		s.setStrokes(list);

		s.setAttribute("TEXT_BEST", character);
		s.setAttribute(character, Double.toString(c.getConfidence(character)));

		s.setInterpretation("Text", c.getConfidence(character));

		return s;
	}

	/**
	 * Recognize the submitted strokes as handwriting.
	 * 
	 * @param maxTime
	 *            maximum allotted recognition time.
	 * @return recognized text.
	 * @throws OverTimeException
	 *             if the recognition exceeds the {@code maxTime}.
	 */
	public List<Shape> recognize(long maxTime) throws OverTimeException {

		// Store the start time
		long startTime = System.currentTimeMillis();

		List<Shape> shapeGroups;

		// Group strokes
		shapeGroups = CivilGrouper.groupIntoCharacters(m_strokesSubmitted);

		OverTime.overTimeCheck(startTime, maxTime, log);

		log.debug("----------");
		log.debug("  Number Of Strokes " + m_strokesSubmitted.size());
		log.debug("  Number Of Words Found " + shapeGroups.size());

		// Recognize the text shapes
		List<Shape> textShapes = new ArrayList<Shape>();

		// Go through each character grouping and recognize each character
		for (int g = 0; g < shapeGroups.size(); g++) {

			Shape charShape = recognizeOneChar(
					shapeGroups.get(g).getStrokes(), OverTime.timeRemaining(
							startTime, maxTime));

			// Periods currently have poor recognition, so we're recognizing
			// them here
			if (charShape.getBoundingBox().getDiagonalLength() < 10.0) {
				charShape.setAttribute("TEXT_BEST", "period");
				charShape.setAttribute("period", "0.95");
				charShape.getInterpretation().confidence = (0.95);
			}

			textShapes.add(charShape);

			log.debug("  Found a " + charShape.getAttribute("TEXT_BEST")
					+ " with confidence " + charShape.getInterpretation().confidence);
		}

		return textShapes;
	}

	/**
	 * Clear the submitted strokes holder.
	 */
	public void clear() {
		m_strokesSubmitted = new ArrayList<Stroke>();
	}

	/**
	 * Get the stroke submitted to the recognizer.
	 * 
	 * @return the submitted strokes.
	 */
	public List<Stroke> getSubmittedStrokes() {
		return m_strokesSubmitted;
	}
}
