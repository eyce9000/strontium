/**
 * ArcFit.java
 * 
 * Revision History:<br>
 * Jun 24, 2008 bpaulson - File created
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

import org.openawt.Shape;
import org.openawt.geom.Arc2D;
import org.openawt.geom.Line2D;
import org.openawt.geom.Point2D;
import org.openawt.svg.SVGPath;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Point;
import srl.math.FeatureArea;
import srl.math.PerpendicularBisector;


/**
 * Fit stroke to an arc (incomplete circle)
 * 
 * @author bpaulson
 */
public class ArcFit extends Fit implements ICircularFit {


	/**
	 * Radius of the arc
	 */
	protected double m_radius;

	/**
	 * Center point of the arc
	 */
	protected Point2D m_center;

	/**
	 * Area of the ideal arc
	 */
	protected double m_arcArea;

	/**
	 * Angle of the line between the endpoints
	 */
	protected Double m_angle;

	/**
	 * Constructor for arc fit
	 * 
	 * @param features
	 *            features of that stroke
	 * @param config
	 *            paleo configuration (used to get heuristics)
	 */
	public ArcFit(StrokeFeatures features, PaleoConfig config) {
		super(features);

		// if only two points then we dont have an arc
		if (m_features.getNumPoints() <= 2) {
			m_err = 10000.0;
			m_passed = false;
			m_fail = 0;
			return;
		}

		// estimate best arc
		calcCenter();
		calcRadius();

		// generate ideal arc
		generateArc();

		// test 1: stroke must not be closed or overtraced
		if (m_features.isClosed() || m_features.isOvertraced()) {
			m_passed = false;
			m_fail = 1;
		}

		// test 2: make sure NDDE is high (close to 1.0);
		// ignore if this is a small arc because NDDE is unstable with small
		// arcs
		if (m_radius > Thresholds.active.M_ARC_SMALL) {
			if (m_features.getNDDE() < 0.7
					&& (!m_features.dirWindowPassed() || m_features
							.getMaxCurvToAvgCurvRatio() > 3.0)) {
				m_passed = false;
				m_fail = 2;
			}

		} else {
			if (m_features.getNDDE() < 0.6
					&& (!m_features.dirWindowPassed() || m_features
							.getMaxCurvToAvgCurvRatio() > 3.0)) {
				m_passed = false;
				m_fail = 3;
			}
		}

		// test 3: dcr must be low
		if (m_radius > Thresholds.active.M_ARC_SMALL) {
			if (m_features.getDCR() > Thresholds.active.M_DCR_TO_BE_POLYLINE) {
				m_passed = false;
				m_fail = 4;
			}
		} else {
			// if (m_features.getDCR() > 3.65)// 2.75)
			// m_passed = false;
		}

		if (m_features.getEndptStrokeLengthRatio() < 0.1) {
			m_passed = false;
			m_fail = 5;
		}

		// NEW TEST: area of ideal arc must be close to the area of the actual
		// stroke
		double arcAreaRatio = 0;
		double area = m_features.getBounds().getHeight()
				* m_features.getBounds().getWidth();
		if (m_arcArea < area)
			arcAreaRatio = m_arcArea / area;
		else
			arcAreaRatio = area / m_arcArea;
		// TODO: test this before using
		/*
		 * if (arcAreaRatio < M_ARC_AREA_RATIO) m_passed = false;
		 */

		// test 4: feature area test (results used for error)
		m_err = calcFeatureArea();
		if (m_radius > Thresholds.active.M_ARC_SMALL) {

			// use feature area for large arcs
			if (m_err > Thresholds.active.M_ARC_FEATURE_AREA && !m_features.dirWindowPassed()) {
				m_passed = false;
				m_fail = 7;
			}
		} else {

			// use arc area ratio for small arcs
			if (m_err > Thresholds.active.M_ARC_FEATURE_AREA && arcAreaRatio > 0.55) {
				m_passed = false;
				m_fail = 8;
			}
		}

		// check to see if arcs should point down
		if (config.getHeuristics().ARC_DOWN) {
			Line2D endpt = new Line2D.Double(m_features.getFirstOrigPoint()
					.getX(), m_features.getFirstOrigPoint().getY(), m_features
					.getLastOrigPoint().getX(), m_features.getLastOrigPoint()
					.getY());
			Point midpt = m_features.getPoints().get(
					m_features.getNumPoints() / 2);
			calcAngle();
			// System.out.println("angle = " + angle);
			if (m_angle > 30.0 || midpt.getY() > endpt.getY1()) {
				m_passed = false;
				m_fail = 9;
			}
		}

		// ANOTHER NEW TEST: moving window
		// if (!m_features.dirWindowPassed())
		// m_passed = false;

		// create shape/beautified object
		try {
			computeBeautified();
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("ArcFit: passed = " + m_passed + " (" + m_fail
//				+ ")  center = (" + m_center.getX() + "," + m_center.getY()
//				+ ")  radius = " + m_radius + "  closed = "
//				+ m_features.isClosed() + "  overtraced = "
//				+ m_features.isOvertraced() + "  length = "
//				+ m_features.getStrokeLength() + "  NDDE = "
//				+ m_features.getNDDE() + "  DCR = " + m_features.getDCR()
//				+ "  feature area err = " + m_err + " radius = " + m_radius
//				+ " num revs = " + m_features.numRevolutions()
//				+ " arc area ratio = " + arcAreaRatio + " dir window passed = "
//				+ m_features.dirWindowPassed() + " clockwise = "
//				+ m_features.isClockwise() + " endpt:sl ratio = "
//				+ m_features.getEndptStrokeLengthRatio() + " avg curv = "
//				+ m_features.getAvgCurvature() + " curv ratio = "
//				+ m_features.getMaxCurvToAvgCurvRatio());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return ARC;
	}

	/**
	 * Get the angle between the endpoints
	 * 
	 * @return angle between endpoints
	 */
	public double getAngle() {
		if (m_angle == null)
			calcAngle();
		return m_angle;
	}

	/**
	 * Calculate the angle between the midpoints
	 */
	protected void calcAngle() {
		Line2D endpt = new Line2D.Double(m_features.getFirstOrigPoint().getX(),
				m_features.getFirstOrigPoint().getY(), m_features
						.getLastOrigPoint().getX(), m_features
						.getLastOrigPoint().getY());
		double angle = Math.atan2(endpt.getY2() - endpt.getY1(), endpt.getX2()
				- endpt.getX1());
		angle *= 180.0 / Math.PI;
		angle = Math.abs(angle);
		if (angle > 90.0)
			angle = Math.abs(180.0 - angle);
		m_angle = angle;
	}

	/**
	 * Estimate the best center point for the arc. The first attempt tries to
	 * find the center point using a series of perpendicular bisectors. If this
	 * fails then the center is denoted as the center of mass (this will likely
	 * only be the case in non-arcs though).
	 */
	protected void calcCenter() {
		Point first = m_features.getFirstOrigPoint();
		Point last = m_features.getLastOrigPoint();
		if (m_features.getBounds() == null)
			m_features.calcBounds();

		// compute bisector of stroke
		PerpendicularBisector pb = new PerpendicularBisector(first, last,
				m_features.getBounds());

		// find intersection of bisector with stroke (should be midpoint)
		ArrayList<Point2D> intersects = m_features.getIntersection(pb
				.getBisector());
		if (intersects.size() > 0) {
			Point2D pbIntersect = intersects.get(0);
			if (first != null && last != null && pbIntersect != null) {

				// compute bisectors of each 1/2 of the stroke
				PerpendicularBisector pb1 = new PerpendicularBisector(first,
						pbIntersect, m_features.getBounds());
				PerpendicularBisector pb2 = new PerpendicularBisector(
						pbIntersect, last, m_features.getBounds());

				// center is where these two bisectors intersect
				m_center = StrokeFeatures.getIntersectionPt(pb1.getBisector(),
						pb2.getBisector());
			}
			// something went wrong; use alternative method
			if (m_center == null)
				calcAvgCenter();
		} else {
			// something went wrong; use alternative method
			calcAvgCenter();
		}
	}

	/**
	 * Get the center of the arc estimation
	 * 
	 * @return center of the arc estimation
	 */
	public Point2D getCenter() {
		return m_center;
	}

	/**
	 * Get the estimated radius of the arc
	 * 
	 * @return radius of the arc
	 */
	public double getRadius() {
		return m_radius;
	}

	/**
	 * Compute the center point as the average x, y value of all points
	 */
	protected void calcAvgCenter() {
		double avgX = 0, avgY = 0;
		for (int i = 0; i < m_features.getNumPoints(); i++) {
			avgX += m_features.getPoints().get(i).getX();
			avgY += m_features.getPoints().get(i).getY();
		}
		avgX /= m_features.getNumPoints();
		avgY /= m_features.getNumPoints();
		m_center = new Point2D.Double(avgX, avgY);
	}

	/**
	 * Compute the best fit radius (average distance between all points and the
	 * center point)
	 */
	protected void calcRadius() {
		double sum = 0.0;
		for (int i = 0; i < m_features.getNumPoints(); i++)
			sum += m_features.getPoints().get(i)
					.distance(m_center.getX(), m_center.getY());
		if (m_features.getNumPoints() == 0)
			m_radius = 0.0;
		else
			m_radius = sum / m_features.getNumPoints();
	}

	/**
	 * Calculate the feature area of the arc fit
	 * 
	 * @return feature area of the arc fit
	 */
	protected double calcFeatureArea() {
		double err1 = FeatureArea.toPoint(m_features.getPoints(), m_center);
		err1 /= (Math.PI * m_radius * m_radius * m_features.numRevolutions());
		err1 = Math.abs(1.0 - err1);
		if (Double.isInfinite(err1) || Double.isNaN(err1))
			err1 = Thresholds.active.M_ARC_FEATURE_AREA * 10.0;
		return err1;
	}

	/**
	 * Get the arc to area ratio
	 * 
	 * @return arc:area ratio
	 */
	public double getArcArea() {
		return m_arcArea;
	}

	/**
	 * Generates an ideal arc
	 */
	protected void generateArc() {
		double startX = m_features.getFirstOrigPoint().getX();
		double startY = m_features.getFirstOrigPoint().getY();
		double endX = m_features.getLastOrigPoint().getX();
		double endY = m_features.getLastOrigPoint().getY();

		// swap if stroke is counter-clockwise
		if (m_features.isClockwise()) {
			double tx = m_features.getFirstOrigPoint().getX();
			double ty = m_features.getFirstOrigPoint().getY();
			startX = m_features.getLastOrigPoint().getX();
			startY = m_features.getLastOrigPoint().getY();
			endX = tx;
			endY = ty;
		}
		double startAngle = 360 - (Math.atan2(startY - m_center.getY(), startX
				- m_center.getX()) * (180.0 / Math.PI));
		double endAngle = 360 - (Math.atan2(endY - m_center.getY(), endX
				- m_center.getX()) * (180.0 / Math.PI));
		while (startAngle < 0)
			startAngle += 360.0;
		while (endAngle < 0)
			endAngle += 360.0;
		while (startAngle >= 360.0)
			startAngle -= 360.0;
		while (endAngle >= 360.0)
			endAngle -= 360.0;
		if (endAngle < startAngle)
			endAngle += 360.0;
		double extent = endAngle - startAngle;
		double m_radius = Point2D.distance(m_center.getX(), m_center.getY(),
				startX, startY);
		Arc2D arc = new Arc2D.Double(m_center.getX() - m_radius, m_center.getY()
				- m_radius, m_radius * 2, m_radius * 2, startAngle, extent,
				Arc2D.OPEN);
		m_shape = new SVGPath(arc.getPathIterator(null));
		m_arcArea = arc.getWidth()
				* arc.getHeight();
	}

}
