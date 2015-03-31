/**
 * EllipseFit.java
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

import java.util.ArrayList;
import java.util.List;

import org.openawt.geom.AffineTransform;
import org.openawt.geom.Ellipse2D;
import org.openawt.geom.GeneralPath;
import org.openawt.geom.Line2D;
import org.openawt.geom.Point2D;
import org.openawt.svg.SVGPath;
import org.openawt.svg.SVGShape;

import srl.core.sketch.Stroke;
import srl.core.util.IsAConstants;
import srl.math.FeatureArea;
import srl.math.PerpendicularBisector;


/**
 * Fit stroke to an ellipse
 * 
 * @author bpaulson
 */
public class EllipseFit extends Fit {

	public final static double CLOSED_THRESHOLD = .09;

	/**
	 * Major axis of the ellipse fit
	 */
	protected Line2D m_majorAxis;

	/**
	 * Minor axis of the ellipse fit
	 */
	protected Line2D m_minorAxis;

	/**
	 * Center point of the ellipse fit (center of the major axis)
	 */
	protected Point2D m_center;

	/**
	 * Length of the major axis
	 */
	protected double m_majorAxisLength;

	/**
	 * Length of the minor axis
	 */
	protected double m_minorAxisLength;

	/**
	 * Angle of the major axis (relative to [0,0])
	 */
	protected double m_majorAxisAngle;

	/**
	 * Perimeter:stroke length ratio
	 */
	protected double m_perimeterStrokeLengthRatio;

	/**
	 * Rendered shape
	 */
	protected GeneralPath m_rendered_shape;
	
	/**
	 * Constructor for ellipse fit
	 * 
	 * @param features
	 *            features of the stroke
	 */
	public EllipseFit(StrokeFeatures features) {
		super(features);

		// estimate best ellipse

		try {
			calcMajorAxis();
			calcCenter();
			calcMinorAxis();
		} catch (Exception e) {
			m_passed = false;
			m_fail = 0;
			return;
		}

		// test 1: stroke must be closed
		// if (!m_features.isClosed())
		// m_passed = false;

		// test 2: make sure NDDE is high (close to 1.0) or low (close to 0.0);
		// ignore small ellipses
		if (m_majorAxisLength > Thresholds.active.M_ELLIPSE_SMALL) {
			if (m_features.getNDDE() < 0.63) {
				m_passed = false;
				m_fail = 1;
			}

			// closed test but with looser thresholds
			if (m_features.getEndptStrokeLengthRatio() > 0.3
					&& m_features.getNumRevolutions() < 1.0) {
				m_passed = false;
				m_fail = 2;
			}
			
		} else {
			if (m_features.getNDDE() < 0.54) {
				m_passed = false;
				m_fail = 3;
			}

			// closed test but with looser thresholds
			if (m_features.getEndptStrokeLengthRatio() > 0.26
					&& m_features.getNumRevolutions() < 0.75) {
				m_passed = false;
				m_fail = 4;
			}
			
		}

		// test 3: feature area test (results used for error)
		if (!m_features.isOvertraced()) {
			m_err = calcFeatureArea();
			if (m_majorAxisLength > Thresholds.active.M_ELLIPSE_SMALL) {
				if (m_err > Thresholds.active.M_ELLIPSE_FEATURE_AREA) {
					m_passed = false;
					m_fail = 5;
				}
			} else {
				if (m_err > Thresholds.active.M_ELLIPSE_FEATURE_AREA
						&& (m_err > 0.7 || !m_features.dirWindowPassed())) {
					m_passed = false;
					m_fail = 6;
				}
			}
		}
		// overtraced so we need to compute feature area slightly differently
		else {
			List<Stroke> subStrokes = m_features.getRevSegmenter()
					.getSegmentations().get(0).getSegmentedStrokes();
			double subErr = 0.0;
			double centerDis = 0.0, axisDiff = 0.0;
			for (int i = 0; i < subStrokes.size() - 1; i++) {
				OrigPaleoSketchRecognizer paleo = new OrigPaleoSketchRecognizer(
						subStrokes.get(i), new PaleoConfig());
				EllipseFit ef = paleo.getEllipseFit();
				if (ef.getFailCode() != 0) {
					centerDis += m_center.distance(ef.getCenter());
					axisDiff += Math.abs(m_majorAxisLength
							- ef.getMajorAxisLength());
					subErr += ef.getError();
				}
			}
			m_err = subErr / (subStrokes.size() - 1);
			if (Double.isNaN(m_err))
				m_err = calcFeatureArea();
			if (m_majorAxisLength > Thresholds.active.M_ELLIPSE_SMALL) {
				if (m_err > Thresholds.active.M_ELLIPSE_FEATURE_AREA) {
					m_passed = false;
					m_fail = 7;
				}
			} else {
				if (m_err > Thresholds.active.M_ELLIPSE_FEATURE_AREA
						&& (m_err > 0.65 || !m_features.dirWindowPassed())) {
					m_passed = false;
					m_fail = 8;
				}
			}
		}

		double perimeter = m_features.getBounds().getPerimeter();
		m_perimeterStrokeLengthRatio = m_features.getStrokeLength() / perimeter;
		if (m_perimeterStrokeLengthRatio < 0.55) {
			m_passed = false;
			m_fail = 9;
		}

		if (m_features.getPctDirWindowPassed() <= 0.25
				&& m_features.getNumRevolutions() < 0.4) {
			m_passed = false;
			m_fail = 10;
		}

		if (m_features.getAvgCornerStrokeDistance() < 0.1) {
			m_passed = false;
			m_fail = 11;
		}

		if (m_features.getStrokeLengthPerimRatio() > 1.15) {
			m_passed = false;
			m_fail = 12;
		}

		// ANOTHER NEW TEST: moving window for direction graph
		// if (!m_features.dirWindowPassed())
		// m_passed = false;

		// create shape/beautified object
		Ellipse2D ellipse = new Ellipse2D.Double(m_center.getX() - m_majorAxisLength / 2,
				m_center.getY() - m_minorAxisLength / 2, m_majorAxisLength,
				m_minorAxisLength);
		
		GeneralPath rotatedEllipse = new GeneralPath();
		AffineTransform t = AffineTransform.getRotateInstance(m_majorAxisAngle, m_center.getX(), m_center.getY());
		rotatedEllipse.append(ellipse.getPathIterator(t), false);
		m_shape = new SVGPath(rotatedEllipse);
		try {
			computeBeautified();
			m_beautified.setAttribute(IsAConstants.CLOSED, "true");
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("EllipseFit: passed = " + m_passed + "(" + m_fail
//				+ ")  center = (" + m_center.getX() + "," + m_center.getY()
//				+ ")  major axis = (" + m_majorAxis.getX1() + ","
//				+ m_majorAxis.getY1() + ") (" + m_majorAxis.getX2() + ","
//				+ m_majorAxis.getY2() + ")  major axis length = "
//				+ m_majorAxisLength + "  minor axis length = "
//				+ m_minorAxisLength + "  closed = " + m_features.isClosed()
//				+ "  overtraced = " + m_features.isOvertraced() + "  NDDE = "
//				+ m_features.getNDDE() + "  revs = "
//				+ m_features.numRevolutions() + "  feature area err = " + m_err
//				+ "  DCR = " + m_features.getDCR() +
//				/*
//				 * " num corners = " + m_features.numFinalCorners() +
//				 */
//				" num revs = " + m_features.numRevolutions()
//				+ "  dir window passed = " + m_features.dirWindowPassed()
//				+ " % dir window pass = " + m_features.getPctDirWindowPassed()
//				+ " bounds = " + m_features.getBounds().getMinX() + ", "
//				+ m_features.getBounds().getMinY() + " "
//				+ m_features.getBounds().getMaxX() + ", "
//				+ m_features.getBounds().getMaxY() + " endpt:strokelength = "
//				+ m_features.getEndptStrokeLengthRatio()
//				+ " best fit dir line err = "
//				+ m_features.getBestFitDirGraphError() + " sl:perim ratio = "
//				+ m_features.getStrokeLengthPerimRatio() + " avg corn dis = "
//				+ m_features.getAvgCornerStrokeDistance() + " max corn dis = "
//				+ m_features.getMaxCornerStrokeDistance() + " min corn dis = "
//				+ m_features.getMinCornerStrokeDistance() + " std dev = "
//				+ m_features.getStdDevCornerStrokeDistance());
	}

	/**
	 * Get the length of the major axis for this ellipse estimation
	 * 
	 * @return length of the major axis
	 */
	public double getMajorAxisLength() {
		return m_majorAxisLength;
	}

	/**
	 * Get the length of the minor axis for this ellipse estimation
	 * 
	 * @return length of the minor axis
	 */
	public double getMinorAxisLength() {
		return m_minorAxisLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return ELLIPSE;
	}

	/**
	 * Get the center point of the ellipse
	 * 
	 * @return center point of the ellipse
	 */
	public Point2D getCenter() {
		return m_center;
	}

	/**
	 * Get the angle of the major axis relative to (0,0)
	 * 
	 * @return angle of the major axis
	 */
	public double getMajorAxisAngle() {
		return m_majorAxisAngle;
	}

	/**
	 * Get the major axis of the stroke
	 * 
	 * @return major axis of the stroke
	 */
	public Line2D getMajorAxis() {
		return m_majorAxis;
	}

	/**
	 * Estimate the major axis of the ellipse (this is done by finding the two
	 * points that are farthest apart and joining them with a line)
	 */
	protected void calcMajorAxis() {
		m_majorAxis = m_features.getMajorAxis();
		m_majorAxisLength = m_features.getMajorAxisLength();
		m_majorAxisAngle = m_features.getMajorAxisAngle();
	}

	/**
	 * Estimate the center point of the ellipse (average of all stroke points)
	 */
	protected void calcCenter() {
		/*
		 * double avgX = 0, avgY = 0; for (int i = 0; i <
		 * m_features.getNumPoints(); i++) { avgX +=
		 * m_features.getPoints().get(i).getX(); avgY +=
		 * m_features.getPoints().get(i).getY(); } avgX /=
		 * m_features.getNumPoints(); avgY /= m_features.getNumPoints();
		 * m_center = new Point2D.Double(avgX, avgY);
		 */

		m_center = new Point2D.Double(m_features.getBounds().getCenterX(),
				m_features.getBounds().getCenterY());
	}

	/**
	 * Estimate the minor axis of the ellipse (perpendicular bisector of the
	 * major axis; clipped where it intersects the stroke)
	 */
	protected void calcMinorAxis() {
		PerpendicularBisector bisect = new PerpendicularBisector(
				m_majorAxis.getP1(), m_majorAxis.getP2(),
				m_features.getBounds());
		ArrayList<Point2D> intersectPts = m_features.getIntersection(bisect
				.getBisector());
		if (intersectPts.size() < 2) {
			m_minorAxis = null;
			m_minorAxisLength = 0.0;
		} else {
			double d1, d2;
			d1 = m_center.distance(intersectPts.get(0));
			d2 = m_center.distance(intersectPts.get(1));
			m_minorAxis = new Line2D.Double(intersectPts.get(0).getX(),
					intersectPts.get(0).getY(), intersectPts.get(1).getX(),
					intersectPts.get(1).getY());
			m_minorAxisLength = d1 + d2;
		}
	}

	/**
	 * Calculate the feature area error of the ellipse
	 * 
	 * @return feature area error
	 */
	protected double calcFeatureArea() {
		double err1 = FeatureArea.toPoint(m_features.getPoints(), m_center);
		err1 /= (Math.PI * (m_minorAxisLength / 2.0) * (m_majorAxisLength / 2.0));
		err1 = Math.abs(1.0 - err1);
		if (Double.isInfinite(err1) || Double.isNaN(err1))
			err1 = Thresholds.active.M_ELLIPSE_FEATURE_AREA * 10.0;
		return err1;
	}

	/**
	 * Get the perimeter to stroke length ratio
	 * 
	 * @return perim:sl ratio
	 */
	public double getPerimStrokeLengthRatio() {
		return m_perimeterStrokeLengthRatio;
	}
	
	@Override
	public SVGShape toSVGShape(){
		return m_shape;
	}
}
