/**
 * SpiralFit.java
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

import org.openawt.geom.GeneralPath;
import org.openawt.geom.Point2D;
import org.openawt.svg.SVGPath;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Stroke;


/**
 * Fit stroke to a spiral (circle with semi-constant center but changing radius)
 * 
 * @author bpaulson
 */
public class SpiralFit extends Fit {

	/**
	 * Circle fit of the stroke
	 */
	protected CircleFit m_circleFit;

	/**
	 * Center of the spiral
	 */
	protected Point2D m_center;

	/**
	 * Average radius of each point to the center point
	 */
	protected double m_avgRadius;

	/**
	 * Flag denoting whether or not the radius test passes or fails
	 */
	protected boolean m_radiusTest;

	/**
	 * Flag denoting whether the spiral was drawn with descending or ascending
	 * circles
	 */
	protected boolean m_desc;

	/**
	 * Average radius to bounding box radius ratio
	 */
	protected double m_avgRadBBRadRatio;

	/**
	 * Distance between the center point and the farthest point from the center
	 * point
	 */
	protected double m_maxDistanceToCenter;

	/**
	 * Flag denoting whether or not the center closeness test passed or failed
	 */
	protected boolean m_centerClosenessTest;

	/**
	 * Perctange of radius test that passed
	 */
	protected double m_pctRadiusTestPassed;

	/**
	 * Constructor for spiral fit
	 * 
	 * @param features
	 *            features of the stroke
	 * @param circleFit
	 *            the circle fit for the stroke (needed so we don't have to
	 *            recompute values)
	 */
	public SpiralFit(StrokeFeatures features, CircleFit circleFit) {
		super(features);
		m_circleFit = circleFit;

		// estimate spiral
		calcCenter();
		calcAvgRadius();

		// test 1: stroke must be overtraced
		if (!m_features.isOvertraced()) {
			m_passed = false;
			m_fail = 0;
//			log.debug("SpiralFit: passed = " + m_passed + "(" + m_fail
//					+ ")  overtraced = " + m_features.isOvertraced()
//					+ "  numRevs = " + m_features.numRevolutions());
			return;
		}

		// test 2: make sure NDDE is high (close to 1.0) or low (close to 0.0)
		if (m_features.getNDDE() < Thresholds.active.M_NDDE_HIGH) {
			m_passed = false;
			m_fail = 1;
		}

		// calculate substrokes
		List<Stroke> subStrokes = m_features.getRevSegmenter()
				.getSegmentations().get(0).getSegmentedStrokes();
		List<ICircularFit> subStrokeFits = new ArrayList<ICircularFit>();

		// fit all substrokes to circles
		for (int i = 0; i < subStrokes.size() - 1; i++) {
			OrigPaleoSketchRecognizer paleo = new OrigPaleoSketchRecognizer(
					subStrokes.get(i), new PaleoConfig());
			subStrokeFits.add(paleo.getCircleFit());
		}

		// except the last one (make it an arc)
		OrigPaleoSketchRecognizer paleo = new OrigPaleoSketchRecognizer(
				subStrokes.get(subStrokes.size() - 1), new PaleoConfig());
		subStrokeFits.add(paleo.getArcFit());

		// test 3: make sure all radii are either descending or ascending
		m_radiusTest = true;
		if (subStrokeFits.size() > 1) {
			double first = subStrokeFits.get(0).getRadius();
			double next = subStrokeFits.get(1).getRadius();
			m_desc = false;
			if (next < first)
				m_desc = true;

			// subStrokes.size()-1 because last substroke may be incomplete
			double numTested = 0.0;
			double numPassed = 0.0;
			for (int i = 1; i < subStrokes.size() - 1; i++) {
				next = subStrokeFits.get(i).getRadius();
				if (m_desc && next > first) {
					m_passed = false;
					m_fail = 2;
				} else if (!m_desc && next < first) {
					m_passed = false;
					m_fail = 3;
				} else {
					numPassed++;
				}
				numTested++;
				first = next;
			}
			if (numTested > 0)
				m_pctRadiusTestPassed = numPassed / numTested;
			m_radiusTest = m_passed;
		}
		// since subStrokeFits.size() may be <= 1 we need to manually calculate
		// desc
		if (m_center.distance(m_features.getFirstOrigPoint().getX(), m_features
				.getFirstOrigPoint().getY()) > m_center.distance(m_features
				.getLastOrigPoint().getX(), m_features.getLastOrigPoint()
				.getY()))
			m_desc = true;
		else
			m_desc = false;

		// test 4: average radius should be less than radius based on bounding
		// box
		// helps to distiguish between spiral and overtraced shape
		double bbRadius = (m_features.getBounds().getHeight() / 2 + m_features
				.getBounds().getWidth() / 2) / 2;
		m_avgRadBBRadRatio = m_avgRadius / bbRadius;
		if (m_avgRadius / bbRadius > Thresholds.active.M_SPIRAL_RADIUS_RATIO) {
			m_passed = false;
			m_fail = 4;
		}

		// test 5: verify that centers are close to each other (and close to
		// average center)
		double sum = 0.0;
		double sum2 = 0.0;
		m_centerClosenessTest = true;
		if (subStrokeFits.size() > 0)
			sum2 += subStrokeFits.get(0).getCenter().distance(m_center);
		// subStrokes.size()-2 because last substroke may be incomplete
		for (int i = 0; i < subStrokes.size() - 2; i++) {
			sum += subStrokeFits.get(i).getCenter()
					.distance(subStrokeFits.get(i + 1).getCenter());
			sum2 += subStrokeFits.get(i + 1).getCenter().distance(m_center);
		}
		if (subStrokes.size() - 2 != 0) {
			sum /= (subStrokes.size() - 2);
			sum2 /= (subStrokes.size() - 2);
		} else {
			sum = 0.0;
			sum2 = 0.0;
		}
		m_err = sum / m_avgRadius;
		m_err /= m_features.numRevolutions();
		if (m_err > Thresholds.active.M_SPIRAL_CENTER_CLOSENESS) {
			m_passed = false;
			m_centerClosenessTest = false;
			m_fail = 5;
		}

		// test 6: max distance between two centers should not be greater than
		// diameter of spiral
		double maxDistance = 0;
		for (int i = 0; i < subStrokeFits.size(); i++) {
			for (int j = 0; j < subStrokeFits.size(); j++) {
				if (i != j) {
					double dis = subStrokeFits.get(i).getCenter()
							.distance(subStrokeFits.get(j).getCenter());
					if (dis > maxDistance)
						maxDistance = dis;
				}
			}
		}
		m_maxDistanceToCenter = maxDistance;
		if (maxDistance > 2 * m_avgRadius) {
			m_passed = false;
			m_fail = 6;
		}

		// test 7: endpoints of stroke should less than diameter
		double endptDis = m_features
				.getPoints()
				.get(0)
				.distance(
						m_features.getPoints().get(
								m_features.getNumPoints() - 1))
				/ m_features.getStrokeLength();
		if (endptDis > Thresholds.active.M_SPIRAL_DIAMETER_CLOSENESS)
			m_passed = false;

		// sliding window on direction graph test
		// if (!m_features.dirWindowPassed())
		// m_passed = false;

		// create shape/beautified object
		generateSpiral();
		try {
			computeBeautified();
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("SpiralFit: passed = " + m_passed + "(" + m_fail
//				+ ")  overtraced = " + m_features.isOvertraced()
//				+ "  radius test = " + m_radiusTest + "  NDDE = "
//				+ m_features.getNDDE() + "  maxDist = " + maxDistance
//				+ "  diameter = " + m_avgRadius * 2 + "  numRevs = "
//				+ m_features.numRevolutions() + "  center closeness error = "
//				+ m_err + " radius ratio = " + (m_avgRadius / bbRadius)
//				+ "  endpt dis = " + endptDis);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return SPIRAL;
	}

	/**
	 * Get the percentage of the radius test that passed
	 * 
	 * @return percent passed
	 */
	public double getPctRadiusTestPassed() {
		return m_pctRadiusTestPassed;
	}

	/**
	 * Get the max distance from a point to the center divided by the average
	 * radius
	 * 
	 * @return (described above)
	 */
	public double getMaxDistanceToCenterRadiusRatio() {
		return m_maxDistanceToCenter / m_avgRadius;
	}

	/**
	 * Get the circle fit for the stroke
	 * 
	 * @return circle fit for the stroke
	 */
	public CircleFit getCircleFit() {
		return m_circleFit;
	}

	/**
	 * Flag denoting whether or not the center closeness test passed
	 * 
	 * @return true if passed, else false
	 */
	public boolean centerClosenessTestPassed() {
		return m_centerClosenessTest;
	}

	/**
	 * Get the average radius to bounding box radius ratio
	 * 
	 * @return ratio
	 */
	public double getAvgRadBBRadRatio() {
		return m_avgRadBBRadRatio;
	}

	/**
	 * Get the average radius of the spiral
	 * 
	 * @return average radius
	 */
	public double getAvgRadius() {
		return m_avgRadius;
	}

	/**
	 * Estimate the center of the spiral. Here we use the center of the bounding
	 * box since the average of all points may yield a slightly more distorted
	 * center.
	 */
	protected void calcCenter() {
		m_center = new Point2D.Double(m_features.getBounds().getCenterX(),
				m_features.getBounds().getCenterY());
	}

	/**
	 * Calculate the average radius of the spiral
	 */
	protected void calcAvgRadius() {
		m_avgRadius = 0;
		for (int i = 0; i < m_features.getNumPoints(); i++)
			m_avgRadius += m_features.getPoints().get(i)
					.distance(m_center.getX(), m_center.getY());
		m_avgRadius /= m_features.getNumPoints();
	}

	/**
	 * Generates a beautified spiral
	 */
	protected void generateSpiral() {
		double m_tightness;
		double ox, oy;
		double radius;

		// determine the outer-most point (either first or last point)
		if (m_desc) {
			ox = m_features.getFirstOrigPoint().getX();
			oy = m_features.getFirstOrigPoint().getY();
		} else {
			ox = m_features.getLastOrigPoint().getX();
			oy = m_features.getLastOrigPoint().getY();
		}

		radius = m_center.distance(ox, oy);

		// determine the tightness value (how tight the spiral looks depends on
		// the number of rotations within the radius)
		if ((m_features.isClockwise() && m_desc)
				|| (!m_features.isClockwise() && !m_desc))
			m_tightness = radius
					/ ((Math.floor(m_features.numRevolutions()) + 0.5)
							* Math.PI * 2);
		else
			m_tightness = radius
					/ ((Math.floor(m_features.numRevolutions())) * Math.PI * 2);

		// start generating shape (from center point out)
		GeneralPath spiral = new GeneralPath();
		spiral.moveTo((float) m_center.getX(),
				(float) m_center.getY());
		double r = 0.0;
		double theta = 0.0;
		double startAngle = Math.atan2(oy - m_center.getY(),
				ox - m_center.getX());
		// generate Archimedes spiral with a = 0.0 and b = m_tightness
		while (Math.abs(r) <= radius) {
			if (m_features.isClockwise()) {
				if (m_desc)
					theta -= 0.1;
				else
					theta += 0.1;
			} else {
				if (m_desc)
					theta += 0.1;
				else
					theta -= 0.1;
			}
			r = m_tightness * theta;
			spiral.lineTo(
					(r * Math.cos(theta + startAngle) + m_center.getX()), (r
							* Math.sin(theta + startAngle) + m_center.getY()));
		}
		m_shape = new SVGPath(spiral);
	}

}
