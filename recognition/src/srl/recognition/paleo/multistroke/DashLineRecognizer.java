/**
 * DashLineRecognizer.java
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
package srl.recognition.paleo.multistroke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import srl.core.sketch.Point;
import srl.core.sketch.SComponent;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.core.sketch.comparators.TimePeriodComparator;
import srl.core.util.IsAConstants;
import srl.recognition.paleo.Fit;


/**
 * Dashed Line Recognizer
 * 
 * @author bpaulson
 */
public class DashLineRecognizer {

	/**
	 * Input shapes
	 */
	private List<Shape> m_input;

	/**
	 * Output shapes
	 */
	private List<Shape> m_output;

	/**
	 * Running stroke length total
	 */
	private double m_strokeLength;

	/**
	 * Constructor
	 * 
	 * @param input
	 *            input shapes
	 */
	public DashLineRecognizer(List<Shape> input) {
		m_input = input;
		Collections.sort(m_input, new TimePeriodComparator());
	}

	/**
	 * Recognize dashed lines within a list of shapes (given in constructor)
	 * 
	 * @return new set of shapes
	 */
	public List<Shape> recognize() {
		m_output = new ArrayList<Shape>();
		if (m_input.size() <= 0)
			return m_output;
		List<Shape> possibleDash = new ArrayList<Shape>();

		// populate m_output
		for (Shape shape : m_input) {

			// non-lines and large dots go into output
			if (!shape.getInterpretation().label.equals(Fit.LINE)
					&& (!shape.getInterpretation().label.equals(Fit.DOT) || (shape
							.getInterpretation().label.equals(Fit.DOT) && shape
							.getBoundingBox().getDiagonalLength() > 4.0))) {

				m_output.add(shape);
			}

			// handle the line
			else {

				// first line in list
				if (possibleDash.size() == 0) {
					possibleDash.add(shape);
					m_strokeLength = shape.getFirstStroke().getPathLength();
				}

				// dash continues
				else if (isDashContinuation(possibleDash, shape)) {
					possibleDash.add(shape);
				}

				// we have a line but its not part of the current dash
				else {
					// create dash shape
					generateDash(possibleDash);
					possibleDash.clear();
					m_strokeLength = 0.0;
					possibleDash.add(shape);
					m_strokeLength = shape.getFirstStroke().getPathLength();
				}
			}
		}
		generateDash(possibleDash);
		Collections.sort(m_output, new TimePeriodComparator());
		return m_output;
	}

	/**
	 * Generates a dash shape and adds it to output
	 */
	private void generateDash(List<Shape> possibleDash) {
		// dash must have at least 2 lines
		if (possibleDash.size() > 1) {
			Shape newShape = new Shape();
			newShape.setLabel(Fit.LINE);
			newShape.setAttribute(IsAConstants.PRIMITIVE,
					IsAConstants.PRIMITIVE);
			newShape.setAttribute(IsAConstants.DASHED, IsAConstants.DASHED);
			// newShape.setRecognitionTime(System.currentTimeMillis());
			List<Stroke> subStrokes = new ArrayList<Stroke>();
			List<Shape> subShapes = new ArrayList<Shape>();
			double conf = 0.0;
			for (Shape s : possibleDash) {
				subShapes.add(s);
				subStrokes.addAll(s.getStrokes());
				if (s.getInterpretation() != null)
					conf += s.getInterpretation().confidence;
			}
			conf /= (double) possibleDash.size();
			newShape.setStrokes(subStrokes);
			newShape.setShapes(subShapes);
			newShape.getInterpretation().confidence = (conf);
			m_output.add(newShape);
		} else {
			for (Shape s : possibleDash)
				m_output.add(s);
		}
	}

	/**
	 * Determines if new shape is a continuation of the dashed lines in current
	 * 
	 * @param current
	 *            list of current dashed line
	 * @param newShape
	 *            candidate for dash continuation
	 * @return true if possible candidate else false
	 */
	private boolean isDashContinuation(List<Shape> current, Shape newShape) {

		Shape lastShape = current.get(current.size() - 1);

		double slope = getSlope(newShape.getFirstStroke());
		double slopePrev = getSlope(lastShape.getFirstStroke());

		double angle = angleBetween(slopePrev, slope);
		double slope1 = getSlope(lastShape.getLastStroke().getLastPoint(),
				current.get(0).getFirstStroke().getFirstPoint());
		double angle2 = angleBetween(slope1, slope);
		double distanceToLast = lastShape.getLastStroke().getLastPoint()
				.distance(newShape.getFirstStroke().getFirstPoint());
		if (angle >= 90.0)
			angle = 180.0 - angle;
		if (angle2 >= 90.0)
			angle2 = 180.0 - angle2;
		m_strokeLength += newShape.getFirstStroke().getPathLength();
		m_strokeLength += distanceToLast;
		double dis = current.get(0).getFirstStroke().getFirstPoint()
				.distance(newShape.getLastStroke().getLastPoint());
		double ratio = dis / m_strokeLength;
		double dis2 = lastShape.getLastStroke().getLastPoint()
				.distance(newShape.getFirstStroke().getFirstPoint());
		double dis3 = lastShape.getLastStroke().getLastPoint()
				.distance(newShape.getLastStroke().getLastPoint());

		// TODO: this may need to be tweaked
		double mult = (1.0 - ratio) * angle;
		if ((mult < 4.0 && ratio > 0.7 && angle2 < 40.0 && dis2 < dis3)
				|| (mult < 1.21 && ratio > 0.95 && dis2 < dis3)) {
			// System.out.println("TRUE: ratio = " + ratio + " angle = " + angle
			// + " mult = " + mult);
			return true;
		}
		// System.out.println("FALSE: ratio = " + ratio + " angle = " + angle
		// + " mult = " + mult);

		// Slope can be unreliable for extremely small lines
		if (newShape.getLastStroke().getPathLength() < 5.0) {
			double slopePrevSegment = getSlope(current.get(0).getFirstStroke()
					.getFirstPoint(), lastShape.getLastStroke().getLastPoint());
			double slopeNewSegment = getSlope(current.get(0).getFirstStroke()
					.getFirstPoint(), newShape.getLastStroke().getLastPoint());

			double angleSeg = angleBetween(slopePrevSegment, slopeNewSegment);

			if (angleSeg >= 90.0)
				angleSeg = 180.0 - angleSeg;

			if (angleSeg <= 10) {
				// return true;
			}
		}

		return false;
	}

	/**
	 * Get the slope of a given Stroke
	 * 
	 * @param s
	 *            stroke to find slope of
	 * @return slope of stroke (assumed to be a line)
	 */
	private double getSlope(Stroke s) {
		Point p1 = s.getFirstPoint();
		Point p2 = s.getLastPoint();
		return getSlope(p1, p2);
	}

	/**
	 * Get the slope of a line between the two endpoints
	 * 
	 * @param p1
	 *            first point
	 * @param p2
	 *            second point
	 * @return slope
	 */
	private double getSlope(Point p1, Point p2) {
		return (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
	}

	/**
	 * Determines the angle between two slopes
	 * 
	 * @param s1
	 *            slope 1
	 * @param s2
	 *            slope 2
	 * @return angle between slope1 and slope2
	 */
	private double angleBetween(double s1, double s2) {
		if (s1 == s2)
			return 0.0;
		if (Double.isInfinite(s1) && Double.isInfinite(s2))
			return 0.0;
		double value = Math.atan2((s1 - s2), (1 + s1 * s2)) * (180 / Math.PI);
		if (Double.isNaN(value))
			value = 90.0;
		value = Math.abs(value);
		if (Double.isInfinite(s1) && !Double.isInfinite(s2))
			value = 90.0 - angleBetween(s2, 0.0);
		else if (Double.isInfinite(s2) && !Double.isInfinite(s1))
			value = 90.0 - angleBetween(s1, 0.0);
		return value;
	}
}
