/**
 * StarRecognizer.java
 * 
 * Revision History:<br>
 * Mar 23, 2009 bpaulson - File created
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
package srl.recognition.paleo.multistroke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.core.sketch.comparators.TimePeriodComparator;
import srl.core.util.IsAConstants;
import srl.recognition.constraint.confidence.AcuteAngleConstraint;
import srl.recognition.constraint.constrainable.ConstrainableLine;
import srl.recognition.paleo.Fit;
import srl.recognition.paleo.StrokeFeatures;


/**
 * 10-line star recognizer
 * 
 * @author bpaulson
 */
public class StarRecognizer {

	/**
	 * Input shapes
	 */
	private List<Shape> m_input;

	/**
	 * Output shapes
	 */
	private List<Shape> m_output;

	/**
	 * Boolean denoting if last check was acute (true) or obtuse (false)
	 */
	private boolean m_lastWasAcute = false;

	/**
	 * Constant to denote a star
	 */
	public static final String STAR = "Star";

	/**
	 * Cutoff angle to distinguish obtuse from acute
	 */
	private final double CUTOFF = 0.35;

	/**
	 * Ratio threshold for distance:stroke length ratio
	 */
	private final double DIST_RATIO = 0.11;

	/**
	 * Minimum number of rotations required by star
	 */
	private final double ROTATION_THRESHOLD = 0.55;

	/**
	 * Constructor
	 * 
	 * @param input
	 *            input shapes
	 */
	public StarRecognizer(List<Shape> input) {
		m_input = input;
		Collections.sort(m_input, new TimePeriodComparator());
	}

	/**
	 * Recognize stars within a list of shapes (given in constructor)
	 * 
	 * @return new set of shapes
	 */
	public List<Shape> recognize() {
		m_output = new ArrayList<Shape>();
		if (m_input.size() <= 0)
			return m_output;

		// contains strokes + substrokes (if stroke is poly)
		List<Shape> possibleStar = new ArrayList<Shape>();

		// contains original strokes
		List<Shape> possibleStarOrig = new ArrayList<Shape>();

		// populate m_output
		for (Shape shape : m_input) {

			// non-lines go into output
			if (!shape.getInterpretation().label.equals(Fit.LINE)
					&& !shape.getInterpretation().label.startsWith(Fit.POLYGON)
					&& !shape.getInterpretation().label
							.startsWith(Fit.POLYLINE)) {
				m_output.add(shape);
				generateStar(possibleStarOrig, possibleStar);
				possibleStar.clear();
				possibleStarOrig.clear();
				continue;
			}

			// handle polyline/polygon
			if (shape.getInterpretation().label.startsWith(Fit.POLYGON)
					|| shape.getInterpretation().label.startsWith(Fit.POLYLINE)) {
				boolean continuationFailed = false;

				// handle individual lines
				for (Shape ss : shape.getShapes()) {
					if (continuationFailed)
						break;
					if (isStarContinuation(possibleStar, ss)) {
						possibleStar.add(ss);
					} else {
						continuationFailed = true;
					}
				}

				boolean added = false;

				// generate star
				if (continuationFailed) {
					if (possibleStarOrig.size() <= 0) {
						possibleStarOrig.add(shape);
						added = true;
					}
					// create star shape
					generateStar(possibleStarOrig, possibleStar);
					possibleStar.clear();
					possibleStarOrig.clear();
				}

				if (!added)
					possibleStarOrig.add(shape);
			}

			// handle the line
			else {

				// star continues
				if (isStarContinuation(possibleStar, shape)) {
					possibleStar.add(shape);
					possibleStarOrig.add(shape);
				}

				// we have a line but its not part of the current star
				else {
					// create star shape
					generateStar(possibleStarOrig, possibleStar);
					possibleStar.clear();
					possibleStarOrig.clear();
					possibleStar.add(shape);
					possibleStarOrig.add(shape);
				}
			}
		}
		generateStar(possibleStarOrig, possibleStar);
		Collections.sort(m_output, new TimePeriodComparator());
		return m_output;
	}

	/**
	 * Generates a star shape and adds it to output
	 */
	private void generateStar(List<Shape> possibleStarOrig,
			List<Shape> possibleStar) {
		// star must have at 10 lines
		if (checkFinalStar(possibleStar)) {
			Shape newShape = new Shape();
			newShape.setLabel(STAR);
			newShape.setAttribute(IsAConstants.PRIMITIVE,
					IsAConstants.PRIMITIVE);
			// newShape.setRecognitionTime(System.currentTimeMillis());
			List<Stroke> subStrokes = new ArrayList<Stroke>();
			List<Shape> subShapes = new ArrayList<Shape>();
			double conf = 0.0;
			for (Shape s : possibleStarOrig) {
				subShapes.add(s);
				subStrokes.addAll(s.getStrokes());
				if (s.getInterpretation() != null)
					conf += s.getInterpretation().confidence;
			}
			conf /= (double) possibleStarOrig.size();
			newShape.setStrokes(subStrokes);
			newShape.setShapes(subShapes);
			newShape.getInterpretation().confidence = (conf);
			m_output.add(newShape);
		} else {
			m_output.addAll(possibleStarOrig);
		}
	}

	/**
	 * Determines if new shape is a continuation of the star in current
	 * 
	 * @param current
	 *            list of current dashed line
	 * @param newShape
	 *            candidate for dash continuation
	 * @return true if possible candidate else false
	 */
	private boolean isStarContinuation(List<Shape> current, Shape newShape) {
		// stop if we have 10 lines already
		if (current.size() >= 10)
			return false;

		// continue if first shape
		if (current.size() <= 0) {
			return true;
		}

		Shape lastShape = current.get(current.size() - 1);
		AcuteAngleConstraint aac = new AcuteAngleConstraint();
		double conf = aac.solve(new ConstrainableLine(lastShape),
				new ConstrainableLine(newShape));
		double dis = lastShape.getLastStroke().getLastPoint()
				.distance(newShape.getFirstStroke().getFirstPoint());
		double length = lastShape.getLastStroke().getPathLength()
				+ newShape.getFirstStroke().getPathLength();
		double ratio = dis / length;

		// check 1: are endpoints close?
		if (ratio > DIST_RATIO) {
			// System.out.println("bad ratio = " + ratio);
			return false;
		}

		// check 2: are we alternating slopes?
		if (current.size() == 1) {
			if (conf > CUTOFF)
				m_lastWasAcute = false;
			else
				m_lastWasAcute = true;
		} else {
			// obtuse / obtuse
			if (conf > CUTOFF && !m_lastWasAcute) {
				// System.out.println("acute conf too high = " + conf);
				return false;
			}
			// acute /acute
			else if (conf <= CUTOFF && m_lastWasAcute) {
				// System.out.println("obtuse conf too low = " + conf);
				return false;
			}
			// update flag
			else if (conf > CUTOFF)
				m_lastWasAcute = false;
			else
				m_lastWasAcute = true;
		}

		return true;
	}

	/**
	 * Determines if list of shapes meet the final star requirements
	 * 
	 * @param current
	 *            list of current shapes
	 * @return true if star possible else false
	 */
	private boolean checkFinalStar(List<Shape> current) {
		if (current.size() != 10)
			return false;

		// check 1: endpoints close
		double dis = current
				.get(0)
				.getFirstStroke()
				.getFirstPoint()
				.distance(
						current.get(current.size() - 1).getLastStroke()
								.getLastPoint());
		double length = current.get(0).getFirstStroke().getPathLength()
				+ current.get(current.size() - 1).getLastStroke()
						.getPathLength();
		double ratio = dis / length;
		if (ratio > DIST_RATIO)
			return false;

		// check 2: total rotation
		Stroke tmpStroke = new Stroke();
		tmpStroke.addPoint(current.get(0).getFirstStroke().getFirstPoint());
		for (Shape s : current)
			tmpStroke.addPoint(s.getLastStroke().getLastPoint());
		StrokeFeatures sf = new StrokeFeatures(tmpStroke, false);
		double revs = sf.getNumRevolutions();
		if (revs < ROTATION_THRESHOLD)
			return false;
		return true;
	}
}
