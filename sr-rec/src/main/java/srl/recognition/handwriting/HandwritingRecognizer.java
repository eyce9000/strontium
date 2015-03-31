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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Shape;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;
import srl.core.util.Pair;
import srl.core.util.lists.DisjointSet;
import srl.recognition.grouping.CivilGrouper;
import srl.recognition.grouping.HandwritingGrouper;
import srl.recognition.recognizer.OverTime;
import srl.recognition.recognizer.OverTimeException;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Handwriting recognizer originally used for COA symbols. It has been modified
 * to work with CivilSketch handwriting.<br>
 * <br>
 * This entire class should eventually be cleaned (NOTE: see
 * CivilHandwritingRecognizer.java) to be either fully generic (i.e., working
 * with both COA and CivilSketch data), or become fully CivilSketch.<br>
 * <br>
 * I've modified it to work with CivilSketch, but haven't gone back to COA data
 * and tested it. Since this HandwritingRecognizer is in a different branch of
 * the repository, the COA data does not matter. But, if the SLOTH & CivilSketch
 * branches were ever to be merged, this class would need a great deal of
 * reworking.
 * 
 * @author beoff, awolin
 */
public class HandwritingRecognizer {

	/**
	 * WEKA neural network model
	 */
	private MultilayerPerceptron m_mlp;

	private MultilayerPerceptron m_mlpEchelon;

	private MultilayerPerceptron m_mlpInner;

	private HWRType m_dictionaryType = HWRType.INNER;

	private Attribute m_targetAttribute;

	private Attribute m_targetInnerAttribute;

	private Attribute m_targetEchelonAttribute;

	private int PIXELCOUNT = 11;

	/**
	 * WEKA model dataset
	 */
	private Instances m_dataSet;

	private Instances m_innerDataSet;

	private Instances m_echelonDataSet;

	private List<Stroke> m_strokesSubmitted;

	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory.getLogger(HandwritingRecognizer.class);

	/**
	 * Default constructor that initializes the models.
	 */
	public HandwritingRecognizer() {

		try {
			m_mlpEchelon = (MultilayerPerceptron) weka.core.SerializationHelper
					.read("models/2009-03-10-11.02-PixelCount11ECHELON500-1.model");
		} catch (Exception e) {
			log.error(e.toString());
		}

		try {
			m_mlpInner = (MultilayerPerceptron) weka.core.SerializationHelper
					.read("models/2009-03-14-11.19-PixelCount11INNER1000-1.model");
		} catch (Exception e) {
			log.error(e.toString());
		}

		m_targetInnerAttribute = BuildTargetAttribute
				.buildUppercaseLetterAttribute();

		m_targetEchelonAttribute = BuildTargetAttribute.buildEchelonAttribute();

		m_innerDataSet = BuildTargetAttribute.createInstancesDataSet();

		m_echelonDataSet = BuildTargetAttribute.createEchelonDataSet();

		m_strokesSubmitted = new ArrayList<Stroke>();
	}

	/**
	 * HWR for CivilSketch data ONLY.
	 * 
	 * NOTE: Much of the functionality here is unused, such as InnerAttribute
	 * vs. EchelonAttribute. This was left in for now in case people wanted to
	 * try and create a more general HWR, but as the project progresses this
	 * function should be cleaned if the HWR is further tuned specifically
	 * toward CivilSketch data.
	 * 
	 * @param hwrType
	 *            type of handwriting recognizer.
	 */
	public HandwritingRecognizer(HWRType hwrType) {

		if (hwrType == HWRType.CIVIL) {

			try {
				m_mlp = (MultilayerPerceptron) weka.core.SerializationHelper
						.read(getClass().getClassLoader().getResourceAsStream("models/civilHWR-MLP_8-2010-04-15.model"));

				m_mlpInner = m_mlpEchelon = m_mlp;
			} catch (Exception e) {
				log.error(e.toString());
			}
		}

		m_targetInnerAttribute = BuildTargetAttribute.buildCivilAttribute();

		m_targetEchelonAttribute = BuildTargetAttribute.buildCivilAttribute();

		m_innerDataSet = BuildTargetAttribute.createCivilInstancesDataSet();

		m_echelonDataSet = BuildTargetAttribute.createCivilInstancesDataSet();

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

		// For NON-CIVIL models. This is probably broken now for COA data.
		if (m_dictionaryType != HWRType.CIVIL) {

			characterInstance = StrokePixelator.getInstance(holderSketch,
					PIXELCOUNT, m_dataSet);

			Attribute strokeCountAtt = m_dataSet.attribute("StrokeCount");

			Attribute bbRatio = m_dataSet.attribute("BoundingBoxRatio");

			characterInstance.setValue(strokeCountAtt, strokes.size());

			characterInstance.setValue(bbRatio,
					holderSketch.getBoundingBox().height
							/ holderSketch.getBoundingBox().width);

			try {
				distribution = m_mlp.distributionForInstance(characterInstance);
				OverTime.overTimeCheck(startTime, maxTime, log);
			} catch (OverTimeException ote) {
				// Don't log it
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}

		} else {

			// Initialize the feature set outside of the recognizer
			characterInstance = CivilAttributes.generateCivilFeatures(
					holderSketch, m_dataSet);

			try {
				distribution = m_mlp.distributionForInstance(characterInstance);
				OverTime.overTimeCheck(startTime, maxTime, log);
			} catch (OverTimeException ote) {
				// Don't log it
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
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

		m_mlp = m_mlpInner;
		m_targetAttribute = m_targetInnerAttribute;
		m_dataSet = m_innerDataSet;

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

		// s.setAttribute(c.getBestResult(), Double.toString(c.getConfidence(c
		// .getBestResult().charAt(0))));

		// s.setConfidence(c.getConfidence(c.getBestResult().charAt(0)));

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

		m_mlp = m_mlpInner;
		m_targetAttribute = m_targetInnerAttribute;
		m_dataSet = m_innerDataSet;

		Character c = characterRecognizer(list, OverTime.timeRemaining(
				startTime, maxTime));
		Shape s = new Shape();

		s.setStrokes(list);

		s.setAttribute("TEXT_BEST", character);
		s.setAttribute(character, Double.toString(c.getConfidence(character)));

		s.setInterpretation("Text", c.getConfidence(character));

		return s;
	}

	public Shape recognizeOneText(List<Stroke> list, long maxTime)
			throws OverTimeException {

		// Store the start time
		long startTime = System.currentTimeMillis();

		sortListLeftMost(list);

		// first pregroup
		List<List<Stroke>> groupings = this.pregroupStrokes(list);

		ArrayList<ArrayList<Integer>> what = AllPossibleGroupings
				.computepossibiliites(groupings.size());
		OverTime.overTimeCheck(startTime, maxTime, log);

		log.debug("Number Of Possible Groupings : " + what.size());

		HandwritingInterpretations hi = new HandwritingInterpretations(
				m_dictionaryType);

		boolean submitted = false;
		for (ArrayList<Integer> aLI : what) {
			CharacterGroup characters = new CharacterGroup();
			int start = 0;
			int end = 0;
			boolean jumpOut = false;

			for (Integer i : aLI) {
				end += i;
				List<Stroke> chargroup = new ArrayList<Stroke>();
				for (int groupCount = start; groupCount < end; groupCount++) {
					chargroup.addAll(groupings.get(groupCount));
				}

				Character c = characterRecognizer(chargroup, OverTime
						.timeRemaining(startTime, maxTime));
				log.debug("Integer " + i + " Character " + c.getBestResult()
						+ " Confidence " + c.getHighestConfidence());
				int size = characters.m_characters.size();
				Character prevChar = null;
				if (size > 0) {
					prevChar = characters.m_characters.get(size - 1);
				}
				characters.add(c);
				if (prevChar != null) {
					double maxXPrev = prevChar.getBoundingBox().getMaxX();
					double centerCur = c.getBoundingBox().getCenterX();
					if (centerCur < maxXPrev) {
						jumpOut = true;
						break;
					}
				}
				start += i;
			}

			if (!jumpOut) {
				hi.submitCharacters(characters);
				submitted = true;
			} else {
				log.debug("Cancelling");
			}

		}
		if (!submitted) {
			hi.submitCharacters(new CharacterGroup());
		}

		Shape builtshape = new Shape();
		builtshape.setLabel("Text");
		builtshape.setStrokes(list);
		hi.setAttributes(builtshape);

		return builtshape;
	}

	public Shape recognizeOneText(List<Stroke> list, HWRType type,
			long maxTime) throws OverTimeException {

		m_dictionaryType = type;
		return recognizeOneText(list, maxTime);
	}

	/**
	 * Recognize CivilSketch text.
	 * 
	 * @param maxTime
	 *            maximum allotted recognition time.
	 * @return the text shapes found.
	 * @throws OverTimeException
	 *             thrown if the recognizer exceeds the maximum allotted run
	 *             time.
	 */
	public List<Shape> recognizeCivil(long maxTime) throws OverTimeException {

		System.out.println("----------");

		// Store the start time
		long startTime = System.currentTimeMillis();

		List<Shape> shapeGroups;

		// HandwritingGrouper hg = new HandwritingGrouper();
		// shapegroups = hg.group(m_strokesSubmitted);

		shapeGroups = CivilGrouper.groupIntoCharacters(m_strokesSubmitted);

		// Collections.sort(m_strokesSubmitted, new StrokeTimeComparator());
		// shapegroups = new ArrayList<Shape>();
		// Shape group = new Shape();
		// group.setStrokes(m_strokesSubmitted);
		// shapegroups.add(group);

		OverTime.overTimeCheck(startTime, maxTime, log);

		log.debug("Number Of Strokes " + m_strokesSubmitted.size());
		log.debug("Number Of Words Found " + shapeGroups.size());

		// Create a Map of the character with the (min strokes, max strokes)
		// allowed for the character
		Map<String, Pair<Integer, Integer>> numStrokesPerChar = new HashMap<String, Pair<Integer, Integer>>();

		// Roman
		numStrokesPerChar.put("F", new Pair<Integer, Integer>(2, 3));
		numStrokesPerChar.put("M", new Pair<Integer, Integer>(1, 5));
		numStrokesPerChar.put("X", new Pair<Integer, Integer>(2, 2));
		numStrokesPerChar.put("Y", new Pair<Integer, Integer>(2, 3));

		// Greek letters
		numStrokesPerChar.put("alpha", new Pair<Integer, Integer>(1, 1)); // α
		numStrokesPerChar.put("beta", new Pair<Integer, Integer>(1, 2)); // β
		numStrokesPerChar.put("gamma", new Pair<Integer, Integer>(1, 1)); // γ
		numStrokesPerChar.put("theta", new Pair<Integer, Integer>(1, 1)); // θ

		// Numbers
		numStrokesPerChar.put("0", new Pair<Integer, Integer>(1, 1));
		numStrokesPerChar.put("1", new Pair<Integer, Integer>(1, 3));
		numStrokesPerChar.put("2", new Pair<Integer, Integer>(1, 2));
		numStrokesPerChar.put("3", new Pair<Integer, Integer>(1, 1));
		numStrokesPerChar.put("4", new Pair<Integer, Integer>(1, 2));
		numStrokesPerChar.put("5", new Pair<Integer, Integer>(1, 2));
		numStrokesPerChar.put("6", new Pair<Integer, Integer>(1, 1));
		numStrokesPerChar.put("7", new Pair<Integer, Integer>(1, 3));
		numStrokesPerChar.put("8", new Pair<Integer, Integer>(1, 1));
		numStrokesPerChar.put("9", new Pair<Integer, Integer>(1, 2));

		// Symbols
		numStrokesPerChar.put("period", new Pair<Integer, Integer>(1, 1)); // .
		numStrokesPerChar.put("equals", new Pair<Integer, Integer>(2, 2)); // =

		// Recognize the text shapes
		List<Shape> textShapes = new ArrayList<Shape>();

		for (int g = 0; g < shapeGroups.size(); g++) {

			// UNUSED BRUTE FORCE METHOD. GROUPING IS NOW PREFERRED.
			// List<Shape> shapes =
			// recognizeCivilBruteForce(shapeGroups.get(g),
			// numStrokesPerChar, startTime, maxTime);

			// for (int s = 0; s < shapes.size(); s++) {
			// textShapes.add(shapes.get(s));
			// }

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
		}

		return textShapes;
	}

	/**
	 * Brute force recognition without grouping. Takes a window of strokes,
	 * tries to find the best character for those strokes, and then uses the
	 * remaining strokes. This produces relatively poor recognition results with
	 * the neural network approach, since we scale and transform stroke bounding
	 * boxes, which can produce odd results here.<br>
	 * <br>
	 * CURRENTLY UNUSED IN FAVOR OF GROUPING.
	 * 
	 * @param shape
	 *            shape to recognize.
	 * @param numStrokesPerChar
	 *            mapping from a character to the maximum number of strokes that
	 *            character could be drawn with. This helps prune and reduce the
	 *            complexity.
	 * @param startTime
	 *            recognition start time.
	 * @param maxTime
	 *            maximum allotted recognition time.
	 * @return
	 * @throws OverTimeException
	 *             if the recognition exceeds the {@code maxTime}.
	 */
	private List<Shape> recognizeCivilBruteForce(Shape shape,
			Map<String, Pair<Integer, Integer>> numStrokesPerChar,
			long startTime, long maxTime) throws OverTimeException {

		List<List<Shape>> textShapes = new ArrayList<List<Shape>>();
		int startIndex = 0;

		recognizeCivilBruteForce(textShapes, new ArrayList<Shape>(), shape
				.getStrokes(), startIndex, numStrokesPerChar, startTime,
				Long.MAX_VALUE);

		double bestConfidence = 0.0;
		List<Shape> bestWord = null;

		// Find the best set of characters
		for (List<Shape> word : textShapes) {

			double confidence = textConfidence(word);

			if (confidence > bestConfidence) {
				bestConfidence = confidence;
				bestWord = word;
			}
		}

		return bestWord;
	}

	/**
	 * Tail-recursive brute force function for finding CivilSketch text.
	 * 
	 * @param textRecognized
	 *            recognized words.
	 * @param charsRecognized
	 *            current character string recognized.
	 * @param strokes
	 *            strokes to form into characters.
	 * @param startIndex
	 *            the stroke index that we should start recognizing characters
	 *            at. Any strokes below this {@code startIndex} should have
	 *            already been formed into characters, stored in {@code
	 *            charsRecognized}.
	 * @param numStrokesPerChar
	 * @param startTime
	 * @param maxTime
	 *            maximum allotted recognition time.
	 * @throws OverTimeException
	 *             if the recognition exceeds the {@code maxTime}.
	 */
	private void recognizeCivilBruteForce(List<List<Shape>> textRecognized,
			List<Shape> charsRecognized, List<Stroke> strokes,
			int startIndex,
			Map<String, Pair<Integer, Integer>> numStrokesPerChar,
			long startTime, long maxTime) throws OverTimeException {

		if (startIndex < strokes.size()) {

			for (int i = 1; i < Math.min(strokes.size() - startIndex + 1, 5); i++) {

				for (String character : numStrokesPerChar.keySet()) {

					if (numStrokesPerChar.get(character) != null
							&& i >= numStrokesPerChar.get(character).getFirst()
							&& i <= numStrokesPerChar.get(character).getLast()) {

						List<Stroke> strokeSubset = new ArrayList<Stroke>(
								strokes.subList(startIndex, startIndex + i));

						Shape textShape = recognizeOneChar(strokeSubset,
								OverTime.timeRemaining(startTime, maxTime),
								character);

						// Only append characters above a certain confidence.
						// Helps with pruning.
						if (textShape.getInterpretation().confidence >= 0.25) {

							List<Shape> newCharsRecognized = new ArrayList<Shape>(
									charsRecognized);
							newCharsRecognized.add(textShape);

							// Tail recursion
							recognizeCivilBruteForce(textRecognized,
									newCharsRecognized, strokes,
									startIndex + i, numStrokesPerChar,
									startTime, maxTime);
						}
					}
				}
			}
		} else {

			// Store the recognized series of characters as a word
			textRecognized.add(charsRecognized);
		}
	}

	/**
	 * Confidence function that averages the confidence from a set of shapes.
	 * 
	 * @param textShapes
	 *            text confidences.
	 * @return the averaged confidence value across all {@code textShapes}.
	 */
	private double textConfidence(List<Shape> textShapes) {

		double confidence = 0.0;

		String textString = "";

		for (int i = 0; i < textShapes.size(); i++) {

			String character = textShapes.get(i).getAttribute("TEXT_BEST");
			double characterConfidence = textShapes.get(i).getInterpretation().confidence;

			textString += character + "("
					+ textShapes.get(i).getStrokes().size() + ")";
			confidence += characterConfidence;
		}

		confidence = confidence / textShapes.size();

		System.out.println(textString + " --> " + confidence);

		return confidence;
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

		switch (m_dictionaryType) {

		case ECHELON:
			m_mlp = m_mlpEchelon;
			m_targetAttribute = m_targetEchelonAttribute;
			m_dataSet = m_echelonDataSet;
			break;
		case INNER:
			m_mlp = m_mlpInner;
			m_targetAttribute = m_targetInnerAttribute;
			m_dataSet = m_innerDataSet;
			break;
		case DECISIONGRAPHIC:
			m_mlp = m_mlpInner;
			m_targetAttribute = m_targetInnerAttribute;
			m_dataSet = m_innerDataSet;
			break;
		case UNIQUEDESIGNATOR:
			m_mlp = m_mlpInner;
			m_targetAttribute = m_targetInnerAttribute;
			m_dataSet = m_innerDataSet;
			break;
		case CIVIL:
			return recognizeCivil(maxTime);
		default:
			m_mlp = m_mlpInner;
			m_targetAttribute = m_targetInnerAttribute;
			m_dataSet = m_innerDataSet;
			break;
		}

		HandwritingGrouper hg = new HandwritingGrouper();

		List<Shape> shapegroups;

		if (m_dictionaryType.equals(HWRType.DECISIONGRAPHIC)) {
			shapegroups = hg.groupIntersection(m_strokesSubmitted);
		} else {
			shapegroups = hg.group(m_strokesSubmitted);
		}

		OverTime.overTimeCheck(startTime, maxTime, log);

		log.debug("Number Of Strokes " + m_strokesSubmitted.size());
		log.debug("Number Of Words Found " + shapegroups.size());

		// COMMENT
		/*
		 * Shape testOneShape = recognizeOneText(m_strokesSubmitted,
		 * OverTime.timeRemaining(startTime, maxTime)); String bestText =
		 * testOneShape.getAttribute("TEXT_BEST");
		 * //log.error("Single group recognized as: " + bestText +
		 * " at accuracy: " + testOneShape.getAttribute(bestText));
		 * System.out.println("Single group recognized as: " + bestText +
		 * " at accuracy: " + testOneShape.getAttribute(bestText));
		 */
		// POSSIBLY RETURN THIS IF HIGH ACCURACY

		ArrayList<Shape> textshapes = new ArrayList<Shape>();
		for (Shape shape1 : shapegroups) {
			log.debug("Number of Strokes in Shape "
					+ shape1.getStrokes().size());

			Shape builtshape = recognizeOneText(shape1.getStrokes(), OverTime
					.timeRemaining(startTime, maxTime));

			if (builtshape.getAttribute("TEXT_BEST").equals("filled_square")
					|| builtshape.getAttribute("TEXT_BEST").equals(
							"unfilled_square")
					|| builtshape.getAttribute("TEXT_BEST").equals(
							"filled_triangle")
					|| builtshape.getAttribute("TEXT_BEST").equals(
							"unfilled_triangle")
					|| builtshape.getAttribute("TEXT_BEST").equals("period")
					|| builtshape.getStrokes().size() > HandwritingInterpretations
							.approximateStrokes(builtshape
									.getAttribute("TEXT_BEST"))
					|| Float.valueOf(builtshape.getAttribute(builtshape
							.getAttribute("TEXT_BEST"))) < 0.50) {

				log.debug("----- Need to regroup, possibly");

				// regroup here into several groups
				List<List<Stroke>> groups = regroupStrokesIntersection(builtshape);
				log.debug("This many new groups " + groups.size());
				List<Shape> newGroups = new ArrayList<Shape>();
				boolean letters = false;

				for (List<Stroke> group : groups) {
					Shape onegroup = recognizeOneText(group, OverTime
							.timeRemaining(startTime, maxTime));

					log.debug("New Group Recognize as "
							+ onegroup.getAttribute("TEXT_BEST"));

					if (!(onegroup.getAttribute("TEXT_BEST").startsWith(
							"filled") || onegroup.getAttribute("TEXT_BEST")
							.startsWith("unfilled"))) {
						letters = true;
					}

					newGroups.add(onegroup);
				}

				if (!letters) {
					textshapes.addAll(newGroups);
				} else {
					CharacterGroup characters = new CharacterGroup();
					List<Stroke> list = new ArrayList<Stroke>();

					if (m_dictionaryType.equals(HWRType.CIVIL)) {

						for (Shape shape : newGroups) {

							Shape textShape = recognizeOneChar(shape
									.getStrokes(), OverTime.timeRemaining(
									startTime, maxTime));

							textshapes.add(textShape);
						}
					} else {

						for (Shape shape : newGroups) {
							list.addAll(shape.getStrokes());
							Character c = characterRecognizer(shape
									.getStrokes(), OverTime.timeRemaining(
									startTime, maxTime));
							characters.add(c);
						}

						HandwritingInterpretations hi = new HandwritingInterpretations(
								m_dictionaryType);
						hi.submitCharacters(characters);
						Shape textShape = new Shape();
						textShape.setLabel("Text");
						textShape.setStrokes(list);
						hi.setAttributes(textShape);
						textshapes.add(textShape);
					}
				}
			} else {
				// If is is a multiple, split it and add them to textShapes
				textshapes.add(builtshape);
			}

			OverTime.overTimeCheck(startTime, maxTime, log);
		}

		if (m_dictionaryType.equals(HWRType.ECHELON)) {

			if (textshapes.size() > 2) {
				log.info("Returning too many echelon shapes");
			}

			if (listContainsCountOfShape(textshapes, "*", 2)) {
				log.info("---> Improperly Grouped Platoon, Fixing");

				List<Stroke> platoonList = new ArrayList<Stroke>();

				List<Shape> nonPlatoonShapes = new ArrayList<Shape>();

				for (Shape sh : textshapes) {
					if (sh.getAttribute("TEXT_BEST").equals("*")) {
						platoonList.addAll(sh.getStrokes());
					}
					nonPlatoonShapes.add(sh);
				}

				Shape platoonShape = this.recognizeOneText(platoonList,
						OverTime.timeRemaining(startTime, maxTime));
				// call recognize oneText
				textshapes.clear();

				textshapes.addAll(nonPlatoonShapes);
				textshapes.add(platoonShape);

				for (Shape sh : textshapes) {
					log.info("Shape " + sh.getInterpretation().label + " "
							+ sh.getAttribute("TEXT_BEST") + " "
							+ sh.getAttribute(sh.getAttribute("TEXT_BEST")));
				}
			} else if (listContainsCountOfShape(textshapes, "1", 2)) {
				log.info("---> Improperly Grouped Batallion Fixing");

				List<Stroke> batallionList = new ArrayList<Stroke>();

				List<Shape> nonBatallionShapes = new ArrayList<Shape>();

				for (Shape sh : textshapes) {
					if (sh.getAttribute("TEXT_BEST").equals("1")) {
						batallionList.addAll(sh.getStrokes());
					}
					nonBatallionShapes.add(sh);
				}

				Shape batallionShape = this.recognizeOneText(batallionList,
						OverTime.timeRemaining(startTime, maxTime));
				// call recognize oneText
				textshapes.clear();

				textshapes.addAll(nonBatallionShapes);
				textshapes.add(batallionShape);

				for (Shape sh : textshapes) {
					log.info("Shape " + sh.getInterpretation().label + " "
							+ sh.getAttribute("TEXT_BEST") + " "
							+ sh.getAttribute(sh.getAttribute("TEXT_BEST")));
				}

			}

			log.info("(Should be <= 2) Number of Shapes After Combining "
					+ textshapes.size());

		}

		log.debug("Returning From Recognizer");

		for (Shape sh : textshapes) {
			log.debug("Shape " + sh.getInterpretation().label + " "
					+ sh.getAttribute("TEXT_BEST") + " "
					+ sh.getAttribute(sh.getAttribute("TEXT_BEST")));
		}

		return textshapes;

	}

	private List<List<Stroke>> pregroupStrokes(List<Stroke> strokes) {

		DisjointSet ds = new DisjointSet(strokes.size());

		for (int i = 0; i < strokes.size() - 1; i++) {
			Stroke s1 = strokes.get(i);
			Stroke s2 = strokes.get(i + 1);
			double maxXPrev = s1.getBoundingBox().getMaxX();
			double centerCur = s2.getBoundingBox().getCenterX();
			if (centerCur < maxXPrev) {
				ds.union(ds.find(i), ds.find(i + 1));
			}
		}

		List<List<Stroke>> groupings = new ArrayList<List<Stroke>>();

		for (int i = 0; i < strokes.size(); i++) {
			if (ds.find(i) == i) {
				List<Stroke> group = new ArrayList<Stroke>();
				for (int j = 0; j < strokes.size(); j++) {
					if (ds.find(j) == i) {
						Stroke addstroke = strokes.get(j);
						group.add(addstroke);
					}
				}
				groupings.add(group);
			}
		}

		return groupings;
	}

	private List<List<Stroke>> regroupStrokesIntersection(Shape builtshape) {

		log.debug("Regrouping ");
		List<Stroke> strokes = builtshape.getStrokes();

		DisjointSet ds = new DisjointSet(strokes.size());

		for (int i = 0; i < strokes.size() - 1; i++) {
			Stroke iStroke = strokes.get(i);
			for (int j = i + 1; j < strokes.size(); j++) {
				Stroke jStroke = strokes.get(j);
				if (iStroke.getBoundingBox().growWidth(-.1).growHeight(.15)
						.intersects(
								jStroke.getBoundingBox().growWidth(-.1)
										.growHeight(.15))) {
					ds.union(i, j);
				}
			}
		}

		List<List<Stroke>> groupings = new ArrayList<List<Stroke>>();

		for (int i = 0; i < strokes.size(); i++) {
			if (ds.find(i) == i) {
				List<Stroke> group = new ArrayList<Stroke>();
				for (int j = 0; j < strokes.size(); j++) {
					if (ds.find(j) == i) {
						Stroke addstroke = strokes.get(j);
						group.add(addstroke);
					}
				}
				groupings.add(group);
			}
		}

		return groupings;
	}

	public void clear() {

		m_strokesSubmitted = new ArrayList<Stroke>();
	}

	@SuppressWarnings("unused")
	private boolean listContainsThreeFilledCircles(List<Shape> txtShapes) {

		int filledCircleCount = 0;

		for (Shape s : txtShapes) {
			if (s.getAttribute("TEXT_BEST").equals("*"))
				filledCircleCount++;
		}

		if (filledCircleCount >= 3)
			return true;
		else
			return false;
	}

	private boolean listContainsCountOfShape(List<Shape> txtShapes,
			String echelonType, int expectedCount) {

		int count = 0;

		for (Shape s : txtShapes) {
			if (s.getAttribute("TEXT_BEST").equals(echelonType))
				count++;
		}

		if (count >= expectedCount)
			return true;
		else
			return false;
	}

	public void sortListLeftMost(List<Stroke> list) {

		Collections.sort(list, new Comparator<Stroke>() {

			public int compare(Stroke st1, Stroke st2) {
				return (int) (st1.getBoundingBox().getCenterPoint().getX() - st2
						.getBoundingBox().getCenterPoint().getX());
			}
		});

	}

	public void setHWRType(HWRType type) {
		m_dictionaryType = type;
	}

	public HWRType getHWRType() {
		return m_dictionaryType;
	}

	public List<Stroke> getSubmittedStrokes() {
		return m_strokesSubmitted;
	}
}
