/**
 * ArrowFit.java
 * 
 * Revision History:<br>
 * Jun 25, 2008 bpaulson - File created
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
package srl.recognition.paleo;

import org.openawt.geom.GeneralPath;
import org.openawt.geom.Line2D;
import org.openawt.geom.Path2D;
import org.openawt.geom.Point2D;
import org.openawt.svg.SVGGroup;
import org.openawt.svg.SVGPath;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;


/**
 * Fit stroke to an arrow
 * 
 * @author bpaulson
 */
public class ArrowFit extends Fit {

	/**
	 * Arrow types
	 * 
	 * @author bpaulson
	 */
	public static enum Type {
		STANDARD, TRIANGLE, DIAMOND
	};

	/**
	 * Arrow type used for fit
	 */
	protected Type m_arrowType;

	/**
	 * List of substrokes from segmentation
	 */
	protected List<Stroke> m_subStrokes;

	/**
	 * Denotes whether this arrow is a linear arrow (true) or a curved arrow
	 * (false)
	 */
	protected boolean m_linear;

	/**
	 * Sum of the values used to discriminate a standard arrow (used to compare
	 * with other types of arrows)
	 */
	protected double m_standardSum;

	/**
	 * Boolean denoting whether a standard arrow check passed
	 */
	protected boolean m_standardPassed = false;

	/**
	 * Boolean denoting whether a triangle arrow check passed
	 */
	protected boolean m_trianglePassed = false;

	/**
	 * Boolean denoting whether a diamond arrow check passed
	 */
	protected boolean m_diamondPassed = false;

	/**
	 * Size difference between last two strokes (the head)
	 */
	protected double m_lastTwoDiff = -1.0;

	/**
	 * Number of intersections
	 */
	protected double m_numIntersect = -1.0;

	/**
	 * Distance between two points that meet at the head
	 */
	protected double m_headDistance = -1.0;

	/**
	 * Constructor for arrow fit
	 * 
	 * @param features
	 *            features of the stroke
	 * @param segmentation
	 *            corner finding segmentation to use for arrow fit
	 */
	public ArrowFit(StrokeFeatures features, Segmentation segmentation) {
		super(features);
		m_subStrokes = segmentation.getSegmentedStrokes();

		// test 1: must have enough sub-strokes to be an arrow (min 4)
		if (m_subStrokes.size() < 4) {
			m_passed = false;
			m_fail = 0;
		}

		// test 2: dcr must be high
		// if (m_features.getDCR() < M_DCR_TO_BE_POLYLINE)
		// m_passed = false;

		// only continue if we have enough strokes
		if (m_passed) {
			// test 2: see if stroke passes standard arrow test
			m_standardPassed = checkStandardArrow();

			// test 3: see if stroke passes standard arrow test
			// m_trianglePassed = checkTriangleArrow();

			// test 4: see if stroke passes diamond arrow test
			// m_diamondPassed = checkDiamondArrow();

			// if no tests passed then we don't have an arrow
			if (!m_standardPassed && !m_trianglePassed && !m_diamondPassed) {
				m_passed = false;
				m_fail = 1;
			}
		}

		// create shape/beautified object
		try {
			computeBeautified();
		} catch (Exception e) {

			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("ArrowFit: passed = " + m_passed + "(" + m_fail + ") type = "
//				+ m_arrowType + " standard = " + m_standardPassed
//				+ " triangle = " + m_trianglePassed + " diamond = "
//				+ m_diamondPassed + " substroke = " + m_subStrokes.size()
//				+ " dcr = " + m_features.getDCR());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return ARROW;
	}

	/**
	 * Check to see if stroke is a standard arrow
	 * 
	 * @return true if test passes; else false
	 */
	protected boolean checkStandardArrow() {
		boolean passed = true;
		Stroke last = m_subStrokes.get(m_subStrokes.size() - 1);
		Stroke secondLast = m_subStrokes.get(m_subStrokes.size() - 2);
		Stroke thirdLast = m_subStrokes.get(m_subStrokes.size() - 3);
		Stroke fourthLast = m_subStrokes.get(m_subStrokes.size() - 4);
		double lastLength = StrokeFeatures.getStrokeLength(last);
		double secondLastLength = StrokeFeatures.getStrokeLength(secondLast);
		double thirdLastLength = StrokeFeatures.getStrokeLength(thirdLast);

		// test 1: last two sub-strokes must be close in size
		m_lastTwoDiff = Math.abs(lastLength - secondLastLength)
				/ (lastLength + secondLastLength);
		if (m_lastTwoDiff > 0.5)
			passed = false;

		// test 2: two points at the "head" of the arrow should be close
		m_headDistance = last.getFirstPoint().distance(
				thirdLast.getFirstPoint())
				/ m_features.getStrokeLength();
		if (m_headDistance > 0.11)
			passed = false;
		m_standardSum = m_headDistance;

		// test 3: line connecting tips of arrow head should intersect shaft of
		// arrow
		Line2D.Double line1 = new Line2D.Double(
				thirdLast.getLastPoint().getX(), thirdLast.getLastPoint()
						.getY(), last.getLastPoint().getX(), last
						.getLastPoint().getY());
		ArrayList<Point2D> intersect = StrokeFeatures.getIntersection(
				fourthLast, line1);
		m_numIntersect = intersect.size();
		if (m_numIntersect <= 0)
			passed = false;
		// Line2D.Double line2 = new Line2D.Double(fourthLast.getPoints().get(
		// fourthLast.getNumPoints() / 2).getX(), fourthLast.getPoints()
		// .get(fourthLast.getNumPoints() / 2).getY(), fourthLast
		// .getLastPoint().getX(), fourthLast.getLastPoint().getY());
		// double perpDiff = Math.abs(getSlope(line1) - (1.0 /
		// getSlope(line2)));
		// if (perpDiff > 5)
		// passed = false;

		// if passed make a beautified standard arrow
		if (passed) {

			m_arrowType = Type.STANDARD;
			
			GeneralPath arrowPath = new GeneralPath();

			// generate beginning path of stroke;
			if (m_subStrokes.size() == 4) {

				m_linear = true;

				// we have a linear arrow so beautify accordingly
				arrowPath.moveTo(fourthLast.getFirstPoint()
						.getX(), fourthLast.getFirstPoint().getY());
				arrowPath.lineTo(
						fourthLast.getLastPoint().getX(), fourthLast
								.getLastPoint().getY());
			} else {

				m_linear = false;

				// we have some other general arrow path
				for (int i = 0; i < m_subStrokes.size() - 3; i++) {
					if (i == 0)
						arrowPath.moveTo(m_subStrokes.get(i)
								.getFirstPoint().getX(), m_subStrokes.get(i)
								.getFirstPoint().getY());
					for (int j = 0; j < m_subStrokes.get(i).getNumPoints(); j++)
						arrowPath.lineTo(m_subStrokes.get(i)
								.getPoint(j).getX(), m_subStrokes.get(i)
								.getPoint(j).getY());
				}
			}

			// compute and generate ideal head of arrow
			double size = (thirdLastLength + secondLastLength + lastLength) / 3;
			double angle = 0;

			// if linear, the angle of the shaft is the angle of the first line
			if (m_linear) {
				angle = Math.atan2(fourthLast.getFirstPoint().getY()
						- fourthLast.getLastPoint().getY(), fourthLast
						.getFirstPoint().getX()
						- fourthLast.getLastPoint().getX());
			}

			// compute angle of "curved" shaft as the line between the midpoint
			// and the last point of the segment before the arrow head
			else {
				angle = Math.atan2(
						fourthLast.getPoints()
								.get(fourthLast.getNumPoints() / 2).getY()
								- fourthLast.getLastPoint().getY(), fourthLast
								.getPoints().get(fourthLast.getNumPoints() / 2)
								.getX()
								- fourthLast.getLastPoint().getX());
			}

			// make arrow heads be 45 degree angles when beautified
			double deltaAngle = Math.PI / 4.0;

			// make beautified arrow head
			Point2D.Double p1 = new Point2D.Double(fourthLast.getLastPoint()
					.getX() - Math.sin(angle - deltaAngle) * size, fourthLast
					.getLastPoint().getY()
					+ Math.cos(angle - deltaAngle)
					* size);
			Point2D.Double p2 = new Point2D.Double(fourthLast.getLastPoint()
					.getX() + Math.sin(angle + deltaAngle) * size, fourthLast
					.getLastPoint().getY()
					- Math.cos(angle + deltaAngle)
					* size);
			arrowPath.lineTo(p1.getX(), p1.getY());
			arrowPath.lineTo(fourthLast.getLastPoint().getX(),
					fourthLast.getLastPoint().getY());
			arrowPath.lineTo(p2.getX(), p2.getY());
			
			m_shape = new SVGPath(arrowPath);
		}

		/*
		 * System.out.println("StandardArrow: passed = " + passed + " diff = " +
		 * diff + " dis = " + dis + " intersect = " + intersect.size() +
		 * " perp diff = " + perpDiff);
		 */

		return passed;
	}

	/**
	 * Check to see if stroke is a triangle arrow
	 * 
	 * @return true if test passes; else false
	 */
	protected boolean checkTriangleArrow() {
		boolean passed = true;
		Stroke last = m_subStrokes.get(m_subStrokes.size() - 1);
		Stroke secondLast = m_subStrokes.get(m_subStrokes.size() - 2);
		Stroke thirdLast = m_subStrokes.get(m_subStrokes.size() - 3);
		Stroke fourthLast = m_subStrokes.get(m_subStrokes.size() - 4);
		double lastLength = StrokeFeatures.getStrokeLength(last);
		double secondLastLength = StrokeFeatures.getStrokeLength(secondLast);
		double thirdLastLength = StrokeFeatures.getStrokeLength(thirdLast);
		double fourthLastLength = StrokeFeatures.getStrokeLength(fourthLast);

		// test 1: last stroke and third to last stroke should be same size
		double diff = Math.abs(lastLength - thirdLastLength)
				/ (lastLength + thirdLastLength);
		if (diff > 0.5)
			passed = false;

		// test 2: two points at the "head" of the arrow should be close
		double dis = last.getLastPoint().distance(thirdLast.getFirstPoint())
				/ m_features.getStrokeLength();
		if (dis > 0.25)
			passed = false;

		// test 3: triangle arrow should be better fit than standard arrow
		if (m_standardPassed && dis > m_standardSum)
			passed = false;

		// test 4: second to last line of arrow head should intersect shaft of
		// arrow
		Line2D.Double line1 = new Line2D.Double(secondLast.getFirstPoint()
				.getX(), secondLast.getFirstPoint().getY(), secondLast
				.getLastPoint().getX(), secondLast.getLastPoint().getY());
		ArrayList<Point2D> intersect = StrokeFeatures.getIntersection(
				m_subStrokes.get(m_subStrokes.size() - 4), line1);
		if (intersect.size() <= 0)
			passed = false;
		Line2D.Double line2 = new Line2D.Double(fourthLast.getPoints()
				.get(fourthLast.getNumPoints() / 2).getX(), fourthLast
				.getPoints().get(fourthLast.getNumPoints() / 2).getY(),
				fourthLast.getLastPoint().getX(), fourthLast.getLastPoint()
						.getY());
		double perpDiff = Math.abs(getSlope(line1) - (1.0 / getSlope(line2)));
		if (perpDiff > 5)
			passed = false;

		// sometimes the last line of the arrow is broken incorrectly;
		// try combining last two strokes and repeat test
		if (!passed && m_subStrokes.size() >= 5) {
			passed = true;

			// test 1: last stroke and third to last stroke should be same size
			diff = Math.abs(lastLength + secondLastLength - fourthLastLength)
					/ (lastLength + secondLastLength + thirdLastLength);
			if (diff > 0.5)
				passed = false;

			// test 2: two points at the "head" of the arrow should be close
			dis = last.getLastPoint().distance(fourthLast.getFirstPoint())
					/ m_features.getStrokeLength();
			if (dis > 0.25)
				passed = false;

			// test 3: triangle arrow should be better fit than standard arrow
			if (m_standardPassed && dis > m_standardSum)
				passed = false;

			// test 4: line connecting tips of arrow head should be close to
			// perpendicular to the line it would intersect
			line1 = new Line2D.Double(thirdLast.getFirstPoint().getX(),
					thirdLast.getFirstPoint().getY(), thirdLast.getLastPoint()
							.getX(), thirdLast.getLastPoint().getY());
			Stroke fifthLast = m_subStrokes.get(m_subStrokes.size() - 5);
			intersect = StrokeFeatures.getIntersection(fifthLast, line1);
			if (intersect.size() <= 0)
				passed = false;
			line2 = new Line2D.Double(fifthLast.getPoints()
					.get(fifthLast.getNumPoints() / 2).getX(), fifthLast
					.getPoints().get(fifthLast.getNumPoints() / 2).getY(),
					fifthLast.getLastPoint().getX(), fifthLast.getLastPoint()
							.getY());
			perpDiff = Math.abs(getSlope(line1) - (1.0 / getSlope(line2)));
			if (perpDiff > 5)
				passed = false;

		}

		// if passed make a beautified standard arrow
		if (passed) {

			m_arrowType = Type.TRIANGLE;

			// TODO
			// create shape/beautified object
			/*
			 * m_shape = new GeneralPath(); try { computeBeautified(); } catch
			 * (Exception e) { log.error("Could not create shape object: " +
			 * e.getMessage()); }
			 */
		}

		System.out.println("TriangleArrow: passed = " + passed + " diff = "
				+ diff + " dis = " + dis + " intersect = " + intersect.size()
				+ " num substrokes = " + m_subStrokes.size() + " perp diff = "
				+ perpDiff);
		System.out.print("sizes: ");
		for (int i = 0; i < m_subStrokes.size(); i++)
			System.out.print(m_subStrokes.get(i).getNumPoints() + " ");
		System.out.println("");

		return passed;
	}

	/**
	 * Check to see if stroke is a diamond arrow
	 * 
	 * @return true if test passes; else false
	 */
	protected boolean checkDiamondArrow() {
		return false;
	}

	/**
	 * Get the slope of a line
	 * 
	 * @param line
	 *            line to compute slope for
	 * @return slope of line
	 */
	protected Double getSlope(Line2D line) {
		double x1, y1, x2, y2;
		if (line.getX1() < line.getX2()) {
			x1 = line.getX1();
			x2 = line.getX2();
			y1 = line.getY1();
			y2 = line.getY2();
		} else {
			x1 = line.getX2();
			x2 = line.getX1();
			y1 = line.getY2();
			y2 = line.getY1();
		}
		if (x1 == x2)
			return Double.POSITIVE_INFINITY;
		if (y1 == y2)
			return new Double(0);
		else
			return (y2 - y1) / (x2 - x1);
	}

	/**
	 * Size difference between last two strokes (the head)
	 * 
	 * @return the size difference between last two strokes (the head)
	 */
	public double getLastTwoDiff() {
		return m_lastTwoDiff;
	}

	/**
	 * Number of intersections between the ends of the head and the shaft
	 * 
	 * @return the numIntersect number of intersections
	 */
	public double getNumIntersect() {
		return m_numIntersect;
	}

	/**
	 * Distance between two points that meet at the head
	 * 
	 * @return the distance between two points that meet at the head
	 */
	public double getHeadDistance() {
		return m_headDistance;
	}
}
