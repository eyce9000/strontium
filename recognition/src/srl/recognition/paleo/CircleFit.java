/**
 * CircleFit.java
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

import org.openawt.geom.Ellipse2D;
import org.openawt.geom.Point2D;
import org.openawt.svg.SVGCircle;
import org.openawt.svg.SVGEllipse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Stroke;
import srl.core.util.IsAConstants;
import srl.math.FeatureArea;


/**
 * Fit stroke to a circle
 * 
 * @author bpaulson
 */
public class CircleFit extends Fit implements ICircularFit {

	/**
	 * Ellipse fit for the stroke (used so we don't have to re-compute shared
	 * values)
	 */
	protected EllipseFit m_ellipseFit;

	/**
	 * Radius of the estimated circle
	 */
	protected double m_radius;

	/**
	 * Length ratio between the major and minor axis. For circles this ratio
	 * should be close to 1.0
	 */
	protected double m_axisRatio;

	/**
	 * Constructor for circle fit
	 * 
	 * @param features
	 *            features of the stroke
	 * @param ellipseFit
	 *            the ellipse fit for the stroke (needed so we don't have to
	 *            recompute values)
	 */
	public CircleFit(StrokeFeatures features, EllipseFit ellipseFit) {
		super(features);
		m_ellipseFit = ellipseFit;

		// estimate the radius of the circle
		calcRadius();

		// test 1: stroke must be closed
		if (!m_features.isClosed()) {
			m_passed = false;
			m_fail = 0;
		}

		// test 2: make sure NDDE is high (close to 1.0) or low (close to 0.0);
		// ignore small circles
		if (m_features.getNDDE() < Thresholds.active.M_NDDE_HIGH && m_radius > Thresholds.active.M_CIRCLE_SMALL) {
			m_passed = false;
			m_fail = 1;
		}

		// test 3: check major axis to minor axis ratio
		m_axisRatio = Math.abs(1.0 - m_ellipseFit.getMajorAxisLength()
				/ m_ellipseFit.getMinorAxisLength());
		if (Double.isInfinite(m_axisRatio) || Double.isNaN(m_axisRatio))
			m_axisRatio = 1.0;
		if (m_axisRatio > Thresholds.active.M_AXIS_RATIO_TO_BE_CIRCLE) {
			m_passed = false;
			m_fail = 2;
		}

		// test 4: feature area test (results used for error)
		if (!m_features.isOvertraced()) {
			m_err = calcFeatureArea();
			if (m_err > Thresholds.active.M_CIRCLE_FEATURE_AREA) {
				m_passed = false;
				m_fail = 3;
			}
		}

		// overtraced so we need to compute feature area slightly differently
		else {
			List<Stroke> subStrokes = m_features.getRevSegmenter()
					.getSegmentations().get(0).getSegmentedStrokes();
			double subErr = 0.0;
			double centerDis = 0.0, radDiff = 0.0;
			for (int i = 0; i < subStrokes.size(); i++) {
				if (i != subStrokes.size() - 1) {
					OrigPaleoSketchRecognizer paleo = new OrigPaleoSketchRecognizer(
							subStrokes.get(i), new PaleoConfig());
					CircleFit cf = paleo.getCircleFit();
					centerDis += m_ellipseFit.getCenter().distance(
							cf.getCenter());
					radDiff += Math.abs(m_radius - cf.getRadius());
					subErr += cf.getError();
				} else {
					// fit last substroke to an arc (part of a circle)
					OrigPaleoSketchRecognizer paleo = new OrigPaleoSketchRecognizer(
							subStrokes.get(i), new PaleoConfig());
					ArcFit af = paleo.getArcFit();
					centerDis += m_ellipseFit.getCenter().distance(
							af.getCenter());
					radDiff += Math.abs(m_radius - af.getRadius());
				}
			}
			if (subStrokes.size() > 1)
				m_err = subErr / (subStrokes.size() - 1);
			if (m_err > Thresholds.active.M_CIRCLE_FEATURE_AREA) {
				m_passed = false;
				m_fail = 4;
			}
		}

		// sliding window on direction graph test
		// if (!m_features.dirWindowPassed())
		// m_passed = false;

		// create shape/beautified object
		m_shape = new SVGCircle(m_features.getBounds().getCenterX()
				, m_features.getBounds().getCenterY(),
				m_radius);
		try {
			computeBeautified();
			m_beautified.setAttribute(IsAConstants.CLOSED, "true");
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("CircleFit: passed = " + m_passed + "(" + m_fail
//				+ ")  center = (" + m_ellipseFit.getCenter().getX() + ","
//				+ m_ellipseFit.getCenter().getY() + ")  radius = " + m_radius
//				+ "  closed = " + m_features.isClosed() + "  overtraced = "
//				+ m_features.isOvertraced() + "  NDDE = "
//				+ m_features.getNDDE() + "  axis ratio = " + m_axisRatio
//				+ "  feature area err = " + m_err + "  num revs = "
//				+ m_features.numRevolutions() + " DCR = " + m_features.getDCR()
//				+ "  dir window passed = " + m_features.dirWindowPassed());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return CIRCLE;
	}

	/**
	 * Get the major axis to minor axis length ratio
	 * 
	 * @return axis ratio
	 */
	public double getAxisRatio() {
		return m_axisRatio;
	}

	/**
	 * Get the ellipse fit for the stroke
	 * 
	 * @return ellipse fit for the stroke
	 */
	public EllipseFit getEllipseFit() {
		return m_ellipseFit;
	}

	/**
	 * Return the center of the estimated circle
	 * 
	 * @return center of the estimated circle
	 */
	public Point2D getCenter() {
		return m_ellipseFit.getCenter();
	}

	/**
	 * Get the radius of the estimated circle
	 * 
	 * @return radius of the estimated circle
	 */
	public double getRadius() {
		return m_radius;
	}

	/**
	 * Estimate the best radius for the circle fit
	 */
	protected void calcRadius() {
		// calc average radius (average of all points)
		m_radius = 0.0;
		if (m_ellipseFit.getCenter() != null) {
			for (int i = 0; i < m_features.getNumPoints(); i++)
				m_radius += m_features
						.getPoints()
						.get(i)
						.distance(m_ellipseFit.getCenter().getX(),
								m_ellipseFit.getCenter().getY());
			m_radius /= m_features.getNumPoints();
		}
	}

	/**
	 * Compute the feature area error of the circle
	 * 
	 * @return feature area error of the circle
	 */
	protected double calcFeatureArea() {
		double err1 = FeatureArea.toPoint(m_features.getPoints(),
				m_ellipseFit.getCenter());
		err1 /= (Math.PI * m_radius * m_radius);
		err1 = Math.abs(1.0 - err1);
		if (Double.isInfinite(err1) || Double.isNaN(err1))
			err1 = Thresholds.active.M_CIRCLE_FEATURE_AREA * 10.0;
		return err1;
	}

}
