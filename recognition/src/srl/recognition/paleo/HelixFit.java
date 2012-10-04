/**
 * HelixFit.java
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Fit stroke to a helix (circle with semi-constant radius but moving center)
 * 
 * @author bpaulson
 */
public class HelixFit extends Fit {

	/**
	 * Spiral fit of the stroke
	 */
	protected SpiralFit m_spiralFit;

	/**
	 * Average radius of stroke points to major axis line
	 */
	protected double m_avgRadius;

	/**
	 * Constructor for helix fit
	 * 
	 * @param features
	 *            features of the stroke
	 * @param spiralFit
	 *            the spiral fit for the stroke (needed so we don't have to
	 *            recompute values)
	 */
	public HelixFit(StrokeFeatures features, SpiralFit spiralFit) {
		super(features);
		m_spiralFit = spiralFit;

		calcAvgRadius();

		// test 1: stroke must be overtraced
		if (!m_features.isOvertraced()) {
			m_passed = false;
			m_fail = 0;
//			log.debug("HelixFit: passed = " + m_passed + "(" + m_fail
//					+ ")  overtraced = " + m_features.isOvertraced()
//					+ "  numRevs = " + m_features.numRevolutions());
			return;
		}

		// test 2: make sure NDDE is high (close to 1.0) or low (close to 0.0)
		if (m_features.getNDDE() < Thresholds.active.M_NDDE_HIGH) {
			m_passed = false;
			m_fail = 1;
		}

		// test 3: make sure endpoints are not close (opposite spiral)
		double endptDis = m_features
				.getPoints()
				.get(0)
				.distance(
						m_features.getPoints().get(
								m_features.getNumPoints() - 1))
				/ m_features.getStrokeLength();
		if (endptDis < Thresholds.active.M_SPIRAL_DIAMETER_CLOSENESS) {
			m_passed = false;
			m_fail = 2;
		}

		// sliding window on direction graph test
		// if (!m_features.dirWindowPassed())
		// m_passed = false;

		// generate beautified helix
		generateHelix();
		try {
			computeBeautified();
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("HelixFit: passed = " + m_passed + "(" + m_fail
//				+ ")  overtraced = " + m_features.isOvertraced() + "  NDDE = "
//				+ m_features.getNDDE() + "  numRevs = "
//				+ m_features.numRevolutions() + "  center closeness test = "
//				+ !m_spiralFit.centerClosenessTestPassed() + " closed = "
//				+ m_features.isClosed() + " endpt dis = " + endptDis
//				+ "  dcr = " + m_features.getDCR());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return HELIX;
	}

	/**
	 * Computes the average radius of the stroke to the major axis
	 */
	protected void calcAvgRadius() {
		double sum = 0.0;
		for (int i = 0; i < m_features.getPoints().size(); i++) {
			sum += m_spiralFit
					.getCircleFit()
					.getEllipseFit()
					.getMajorAxis()
					.ptSegDist(m_features.getPoints().get(i).getX(),
							m_features.getPoints().get(i).getY());
		}
		m_avgRadius = sum / m_features.getPoints().size();
	}

	/**
	 * Generates the beautified helix
	 */
	protected void generateHelix() {
		GeneralPath helix = new GeneralPath();
		double m_startX = m_features.getFirstOrigPoint().getX();
		double m_startY = m_features.getFirstOrigPoint().getY();
		double m_endX = m_features.getLastOrigPoint().getX();
		double m_endY = m_features.getLastOrigPoint().getY();
		int m_revolutions = (int) m_features.numRevolutions();
		helix.moveTo((float) m_startX, (float) m_startY);
		double theta = 0.0;
		double x, y, t = 0.0;
		double c_startX = m_startX, c_startY = m_startY, c_endX = m_endX, c_endY = m_endY;
		while (Point2D.distance(c_startX, c_startY, m_startX, m_startY) < m_avgRadius
				&& m_endX != m_startX && m_endY != m_startY) {
			t += 0.001;
			c_startX = m_startX + (m_endX - m_startX) * t;
			c_startY = m_startY + (m_endY - m_startY) * t;
		}
		t = 1.0;
		while (Point2D.distance(c_endX, c_endY, m_endX, m_endY) < m_avgRadius
				&& m_endX != m_startX && m_endY != m_startY) {
			t -= 0.001;
			c_endX = m_startX + (m_endX - m_startX) * t;
			c_endY = m_startY + (m_endY - m_startY) * t;
		}
		t = 0.0;
		double startAngle = Math
				.atan2(m_startY - c_startY, m_startX - c_startX);
		while (Math.abs(theta) < ((m_revolutions + 0.5) * Math.PI * 2.0)) {
			t = Math.abs(theta) / ((m_revolutions + 0.5) * Math.PI * 2.0);
			x = c_startX + (c_endX - c_startX) * t;
			y = c_startY + (c_endY - c_startY) * t;
			helix.lineTo(
					(float) (m_avgRadius * (Math.cos(theta + startAngle)) + x),
					(float) (m_avgRadius * (Math.sin(theta + startAngle)) + y));
			if (m_features.isClockwise())
				theta += 0.1;
			else
				theta -= 0.1;
		}
		helix.lineTo((float) m_endX, (float) m_endY);
		m_shape = new SVGPath(helix);
	}

}
