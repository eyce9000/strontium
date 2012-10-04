/**
 * DashBoundaryRecognizer.java
 * 
 * Revision History:<br>
 * Feb 19, 2009 bpaulson - File created
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
 * Dashed boundary recognizer (rectangle and diamond)
 * 
 * @author bpaulson
 */
public class DashBoundaryRecognizer {

	/**
	 * Input shapes
	 */
	private List<Shape> m_input;

	/**
	 * Output shapes
	 */
	private List<Shape> m_output;

	/**
	 * Keeps track of the last endpoint used
	 */
	private int m_lastEndpointUsed = -1;

	/**
	 * Threshold used to determine if dashed lines are "close"
	 */
	public static final double CLOSENESS_THRESHOLD = 0.5;

	/**
	 * Alternative threshold
	 */
	public static final double CLOSENESS_THRESHOLD2 = 2.1;

	/**
	 * Constructor
	 * 
	 * @param input
	 *            input shapes
	 */
	public DashBoundaryRecognizer(List<Shape> input) {
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
		List<Shape> possibleBoundary = new ArrayList<Shape>();

		// find all dashed lines
		for (Shape shape : m_input) {
			if (shape.getInterpretation().label.equals(Fit.LINE)
					&& shape.hasAttribute(IsAConstants.DASHED))
				possibleBoundary.add(shape);
			else
				m_output.add(shape);
		}

		// do we have enough lines?
		if (possibleBoundary.size() < 4 && possibleBoundary.size() > 0) {
			for (Shape s : m_input)
				if (!m_output.contains(s))
					m_output.add(s);
		}

		// we have more than 4 lines - search for boundaries
		else {
			findBoundaries(possibleBoundary);
		}
		Collections.sort(m_output, new TimePeriodComparator());
		return m_output;
	}

	/**
	 * Searches for possible boundaries
	 * 
	 * @param possibleBoundary
	 *            dashed line shapes found thus far
	 * @return all boundaries found
	 */
	private void findBoundaries(List<Shape> possibleBoundary) {
		List<Shape> binHoriz = new ArrayList<Shape>();
		List<Shape> binVert = new ArrayList<Shape>();
		List<Shape> binPos = new ArrayList<Shape>();
		List<Shape> binNeg = new ArrayList<Shape>();

		// step 1: place each dashed line into a bin corresponding to its
		// orientation
		for (Shape s : possibleBoundary) {
			double slope = getSlope(s.getFirstStroke().getFirstPoint(), s
					.getLastStroke().getLastPoint());
			double angle = angleBetween(slope, 0.0);
			if (angle <= 15.0)
				binHoriz.add(s);
			else if (angle >= 75.0)
				binVert.add(s);
			else {
				if (slope < 0)
					binNeg.add(s);
				else
					binPos.add(s);
			}
		}

		// step 2: search for rectangles
		boolean found = true;
		while (binHoriz.size() > 1 && binVert.size() > 1 && found) {
			found = doSearch(binHoriz, binVert, true);
		}

		// step 3: search for diamonds
		found = true;
		while (binNeg.size() > 1 && binPos.size() > 1 && found) {
			found = doSearch(binNeg, binPos, false);
		}

		// step 4: add whatever is left in bins back to output
		m_output.addAll(binHoriz);
		m_output.addAll(binVert);
		m_output.addAll(binNeg);
		m_output.addAll(binPos);
	}

	/**
	 * Searches for an alternative combination of 4 shapes (2 from each bin)
	 * 
	 * @param bin1
	 *            bin1
	 * @param bin2
	 *            bin2
	 * @param isRect
	 *            true if searching for rectangle, else false (for diamond)
	 * @return true if shape found, else false
	 */
	private boolean doSearch(List<Shape> bin1, List<Shape> bin2, boolean isRect) {
		// NOTE: this function is commented for searching for a rectangle where
		// bin1=horizontal bin and bin2=vertical bin; the same rules apply for
		// diamonds though
		boolean found = false;
		List<Shape> currRect = new ArrayList<Shape>();
		for (int i = 0; i < bin1.size() && !found; i++) {

			// add current horizontal line to list
			currRect.clear();
			currRect.add(bin1.get(i));

			// find another vertical line that is coincident
			for (int j = 0; j < bin2.size() && !found; j++) {
				double dist = getDistanceFromLast(bin1.get(i), bin2.get(j));
				double ratio = dist
						/ (getLength(bin1.get(i)) + getLength(bin2.get(j)));
				double ratio2 = dist
						/ (avgDashLength(bin1.get(i)) + avgDashLength(bin2
								.get(j)));
				if (ratio < CLOSENESS_THRESHOLD
						&& ratio2 <= CLOSENESS_THRESHOLD2) {
					currRect.add(bin2.get(j));

					// find next horizontal line that is coincident
					for (int k = 0; k < bin1.size() && !found; k++) {
						if (k == i)
							continue;
						if (m_lastEndpointUsed == 0)
							dist = getDistanceFromLast(bin2.get(j), bin1.get(k));
						else
							dist = getDistanceFromFirst(bin2.get(j),
									bin1.get(k));
						ratio = dist
								/ (getLength(bin2.get(j)) + getLength(bin1
										.get(k)));
						ratio2 = dist
								/ (avgDashLength(bin1.get(k)) + avgDashLength(bin2
										.get(j)));
						if (ratio < CLOSENESS_THRESHOLD
								&& ratio2 <= CLOSENESS_THRESHOLD2) {
							currRect.add(bin1.get(k));

							// find final vertical line that is coincident
							for (int l = 0; l < bin2.size() && !found; l++) {
								if (l == j)
									continue;
								if (m_lastEndpointUsed == 0)
									dist = getDistanceFromLast(bin1.get(k),
											bin2.get(l));
								else
									dist = getDistanceFromFirst(bin1.get(k),
											bin2.get(l));
								ratio = dist
										/ (getLength(bin2.get(l)) + getLength(bin1
												.get(k)));
								ratio2 = dist
										/ (avgDashLength(bin1.get(k)) + avgDashLength(bin2
												.get(l)));
								if (ratio < CLOSENESS_THRESHOLD
										&& ratio2 <= CLOSENESS_THRESHOLD2) {
									currRect.add(bin2.get(l));

									// make sure final vertical line is
									// coincident with first horizontal line
									if (m_lastEndpointUsed == 0)
										dist = getDistanceFromLast(bin2.get(l),
												bin1.get(i));
									else
										dist = getDistanceFromFirst(
												bin2.get(l), bin1.get(i));
									ratio = dist
											/ (getLength(bin2.get(l)) + getLength(bin1
													.get(i)));
									ratio2 = dist
											/ (avgDashLength(bin1.get(i)) + avgDashLength(bin2
													.get(l)));
									if (ratio < CLOSENESS_THRESHOLD
											&& ratio2 <= CLOSENESS_THRESHOLD2) {
										// FOUND!
										found = true;
										if (isRect)
											generateRectangle(currRect);
										else
											generateDiamond(currRect);
										bin1.removeAll(currRect);
										bin2.removeAll(currRect);
									}
								}
							}
						}
					}
				}
			}
		}
		return found;
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
	 * Get the length of a dashed line (from endpoints)
	 * 
	 * @param s
	 *            dashed line shape
	 * @return distance between endpoints
	 */
	private double getLength(Shape s) {
		return s.getFirstStroke().getFirstPoint()
				.distance(s.getLastStroke().getLastPoint());
	}

	/**
	 * Get the length of the average dash size in a shape
	 * 
	 * @param s
	 *            shape to get the average dash size for
	 * @return average dash size
	 */
	private double avgDashLength(Shape s) {
		double sum = 0.0;
		for (Shape ss : s.getShapes()) {
			sum += ss.getFirstStroke().getPathLength();
		}
		return sum / s.getShapes().size();
	}

	/**
	 * Get the distance between the last point of the first shape and either of
	 * the endpoints from the second shape
	 * 
	 * @param s1
	 *            first shape
	 * @param s2
	 *            second shape
	 * @return distance
	 */
	private double getDistanceFromLast(Shape s1, Shape s2) {
		double dist1 = s1.getLastStroke().getLastPoint()
				.distance(s2.getFirstStroke().getFirstPoint());
		double dist2 = s1.getLastStroke().getLastPoint()
				.distance(s2.getLastStroke().getLastPoint());
		if (dist1 < dist2)
			m_lastEndpointUsed = 0;
		else
			m_lastEndpointUsed = 1;
		return Math.min(dist1, dist2);
	}

	/**
	 * Get the distance between the first point of the first shape and either of
	 * the endpoints from the second shape
	 * 
	 * @param s1
	 *            first shape
	 * @param s2
	 *            second shape
	 * @return distance
	 */
	private double getDistanceFromFirst(Shape s1, Shape s2) {
		double dist1 = s1.getFirstStroke().getFirstPoint()
				.distance(s2.getFirstStroke().getFirstPoint());
		double dist2 = s1.getFirstStroke().getFirstPoint()
				.distance(s2.getLastStroke().getLastPoint());
		if (dist1 < dist2)
			m_lastEndpointUsed = 0;
		else
			m_lastEndpointUsed = 1;
		return Math.min(dist1, dist2);
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
		double value = Math.atan((s1 - s2) / (1 + s1 * s2)) * (180 / Math.PI);
		if (Double.isNaN(value))
			value = 90.0;
		value = Math.abs(value);
		return value;
	}

	/**
	 * Generates a dash rectangle from the given shapes (should be four) and
	 * adds it to output
	 */
	private void generateRectangle(List<Shape> rectDash) {
		Shape newShape = new Shape();
		newShape.setLabel(Fit.RECTANGLE);
		newShape.setAttribute(IsAConstants.PRIMITIVE, IsAConstants.PRIMITIVE);
		newShape.setAttribute(IsAConstants.DASHED, IsAConstants.DASHED);
		newShape.setAttribute(IsAConstants.CLOSED, IsAConstants.CLOSED);
		// newShape.setRecognitionTime(System.currentTimeMillis());
		List<Stroke> subStrokes = new ArrayList<Stroke>();
		List<Shape> subShapes = new ArrayList<Shape>();

		// TODO: make confidence be dependent on combination of dashed lines to
		// form a rectangle?
		double conf = 0.0;
		for (Shape s : rectDash) {
			subShapes.add(s);
			subStrokes.addAll(s.getStrokes());
			conf += s.getInterpretation().confidence;
		}
		conf /= (double) rectDash.size();
		newShape.setStrokes(subStrokes);
		newShape.setShapes(subShapes);
		newShape.getInterpretation().confidence = (conf);
		m_output.add(newShape);
	}

	/**
	 * Generates a dash diamond from the given shapes (should be four) and adds
	 * it to output
	 */
	private void generateDiamond(List<Shape> rectDash) {
		Shape newShape = new Shape();
		newShape.setLabel(Fit.DIAMOND);
		newShape.setAttribute(IsAConstants.PRIMITIVE, IsAConstants.PRIMITIVE);
		newShape.setAttribute(IsAConstants.DASHED, IsAConstants.DASHED);
		newShape.setAttribute(IsAConstants.CLOSED, IsAConstants.CLOSED);
		// newShape.setRecognitionTime(System.currentTimeMillis());
		List<Stroke> subStrokes = new ArrayList<Stroke>();
		List<Shape> subShapes = new ArrayList<Shape>();

		// TODO: make confidence be dependent on combination of dashed lines to
		// form a rectangle?
		double conf = 0.0;
		for (Shape s : rectDash) {
			subShapes.add(s);
			subStrokes.addAll(s.getStrokes());
			conf += s.getInterpretation().confidence;
		}
		conf /= (double) rectDash.size();
		newShape.setStrokes(subStrokes);
		newShape.setShapes(subShapes);
		newShape.getInterpretation().confidence = (conf);
		m_output.add(newShape);
	}
}
