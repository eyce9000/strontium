/**
 * LineFit.java
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

import org.openawt.geom.Line2D;
import org.openawt.svg.SVGLine;
import org.openawt.svg.SVGShape;

import srl.core.sketch.IBeautifiable;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.core.util.IsAConstants;
import srl.math.FeatureArea;
import srl.math.LeastSquares;


/**
 * Fit a strokes to a single line
 * 
 * @author bpaulson
 */
public class LineFit extends Fit {


	/**
	 * Least squares error
	 */
	protected double m_lsqe;

	/**
	 * Endpoint to stroke length ratio
	 */
	protected double m_ratio;

	/**
	 * Only used within LineFit class
	 */
	protected LineFit() {
		super();
	}

	/**
	 * Constructor for line fit
	 * 
	 * @param features
	 *            features of the stroke
	 * @param useEndpoints
	 *            flag specifying whether ideal line is computed using its
	 *            endpoints (true) or instead using the two points farthest
	 *            apart (false)
	 */
	public LineFit(StrokeFeatures features, boolean useEndpoints) {
		super(features);

		// if only two points then we have a line with no error
		if (m_features.getNumPoints() <= 2) {
//			log.debug("LineFit: stroke contains " + m_features.getNumPoints()
//					+ " points");
			m_err = 0.0;
			m_lsqe = 0.0;
			m_passed = true;
			return;
		}

		// ideal line (just connect the endpoints)
		if (useEndpoints)
			m_shape = new SVGLine(m_features.getFirstOrigPoint().getX(),
					m_features.getFirstOrigPoint().getY(), m_features
							.getLastOrigPoint().getX(), m_features
							.getLastOrigPoint().getY());
		else
			m_shape = new SVGLine(m_features.getMajorAxis());

		// test 1: least squares error between the stroke points and the line
		// formed by the endpoints
		m_lsqe = LeastSquares.error(m_features.getPoints(), (Line2D) m_shape.getShape());
		m_ratio = m_features.getEndptStrokeLengthRatio();
		if (m_features.getStrokeLength() > 25.0) {
			if (m_lsqe / m_features.getStrokeLength() > 1.4) {
				m_passed = false;
				m_fail = 0;

				// if line test was close and endpt ratio is high then go ahead
				// and pass
				if (m_lsqe / m_features.getStrokeLength() < Thresholds.active.M_LINE_LS_ERROR_FROM_ENDPTS + 0.1
						&& m_ratio > 0.98)
					m_passed = true;
			}
			if (m_lsqe / m_features.getStrokeLength() > 1.25 && m_ratio < 0.75
					&& m_features.getNumRevolutions() <= 0.5) {
				m_passed = false;
				m_fail = 1;
			}
		} else {
			if (m_lsqe / m_features.getStrokeLength() > 1.5 && m_ratio < 0.732) {
				m_passed = false;
				m_fail = 2;
			}
			if (m_lsqe / m_features.getStrokeLength() > 1.25 && m_ratio < 0.7
					&& m_features.getNumRevolutions() < 0.15) {
				m_passed = false;
				m_fail = 3;
			}
		}

		// test 2: verify that stroke is not overtraced
		if (m_features.isOvertraced()) {
			m_passed = false;
			m_fail = 4;
		}

		// test 3: test feature area (use as error for fit)
		m_err = FeatureArea.toLine(m_features.getPoints(), (Line2D) m_shape.getShape())
				/ m_features.getStrokeLength();
		if (m_err > Thresholds.active.M_LINE_FEATURE_AREA) {
			m_passed = false;
			m_fail = 5;
		}

		// test 4: ratio must be near 1.0
		// if (m_ratio > 0.99) // definitely a line
		// m_passed = true;
		if (m_ratio < 0.3) { // definitely not a line
			m_passed = false;
			m_fail = 6;
		}

		// compute beautified shape
		try {
			computeBeautified();
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("LineFit: passed = " + m_passed + "(" + m_fail
//				+ ") least sq error = "
//				+ (m_lsqe / m_features.getStrokeLength()) + "  overtraced = "
//				+ m_features.isOvertraced() + "  feature area error = " + m_err
//				+ "  endpts = (" + ((Line2D) m_shape.getShape()).getX1() + ","
//				+ ((Line2D) m_shape.getShape()).getY1() + ") ("
//				+ ((Line2D) m_shape.getShape()).getX2() + "," + ((Line2D) m_shape.getShape()).getY2()
//				+
//				/* ") corners = " + m_features.numFinalCorners() + */
//				"  best fit = (" + m_features.getBestFitLine().getX1() + ","
//				+ m_features.getBestFitLine().getY1() + ") ("
//				+ m_features.getBestFitLine().getX2() + ","
//				+ m_features.getBestFitLine().getY2() + ")  is closed = "
//				+ m_features.isClosed() + " ratio = " + m_ratio + " length = "
//				+ m_features.getStrokeLength() + " revs = "
//				+ m_features.getNumRevolutions());

	}

	/**
	 * Get the least squares error of the fit
	 * 
	 * @return least squares error of the fit
	 */
	public double getLSQE() {
		return m_lsqe;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return LINE;
	}

	/**
	 * Simply returns a line interpretation of a given stroke
	 * 
	 * @param str
	 *            stroke
	 * @return line interpretation
	 */
	public static Shape getLineFit(Stroke str) {
		Shape s = new Shape();
		s = new srl.core.sketch.Shape();
		s.setLabel(LINE);
		s.setAttribute(IsAConstants.PRIMITIVE, IsAConstants.PRIMITIVE);
		ArrayList<Stroke> strokeList = new ArrayList<Stroke>();
		strokeList.add(str);
		s.setStrokes(strokeList);
		SVGShape sh = new SVGLine(str.getFirstPoint().getX(), str
				.getFirstPoint().getY(), str.getLastPoint().getX(), str
				.getLastPoint().getY());
		if (s instanceof IBeautifiable) {
			((IBeautifiable) s).setBeautifiedShape(sh);
		}
		return s;
	}

	/**
	 * Simply get the least squares line error for a stroke (by-passes all other
	 * recognition)
	 * 
	 * @param str
	 *            stroke
	 * @return least squares error
	 */
	public static double getLineLSQE(Stroke str) {
		Shape s = getLineFit(str);
		org.openawt.Shape sh = ((IBeautifiable) s).getBeautifiedShape().getShape();
		return LeastSquares.error(str.getPoints(), (Line2D) sh);
	}

	/**
	 * Get the feature area error for a stroke to a line (by-passes all other
	 * recognition)
	 * 
	 * @param str
	 *            stroke
	 * @return feature area error
	 */
	public static double getLineFA(Stroke str) {
		Shape s = getLineFit(str);
		org.openawt.Shape sh = ((IBeautifiable) s).getBeautifiedShape().getShape();
		return FeatureArea.toLine(str.getPoints(), (Line2D) sh)
				/ str.getPathLength();
	}

	/**
	 * Computes a line fit on a stroke without having to compute all the other
	 * features
	 * 
	 * @param str
	 *            stroke
	 * @return line fit
	 */
	public static LineFit getFit(Stroke str) {
		LineFit fit = new LineFit();
		fit.m_passed = true;
		fit.m_beautified = getLineFit(str);
		fit.m_lsqe = getLineLSQE(str);
		fit.m_err = getLineFA(str);
		fit.m_ratio = str.getFirstPoint().distance(str.getLastPoint())
				/ str.getPathLength();
		double numRevs = getNumRevolutions(str);
		boolean overtraced = false;

		// overtraced check
		if (numRevs >= Thresholds.active.M_REVS_TO_BE_OVERTRACED)
			overtraced = true;
		else
			overtraced = false;

		// if only two points then we have a line with no error
		if (str.getNumPoints() <= 2) {
			fit.m_err = 0.0;
			fit.m_lsqe = 0.0;
			fit.m_passed = true;
			return fit;
		}

		// test 1: least squares error between the stroke points and the line
		// formed by the endpoints
		if (str.getPathLength() > 25.0) {
			if (fit.m_lsqe / str.getPathLength() > 1.4) {
				fit.m_passed = false;
				fit.m_fail = 0;

				// if line test was close and endpt ratio is high then go ahead
				// and pass
				if (fit.m_lsqe / str.getPathLength() < Thresholds.active.M_LINE_LS_ERROR_FROM_ENDPTS + 0.1
						&& fit.m_ratio > 0.98)
					fit.m_passed = true;
			}
			if (fit.m_lsqe / str.getPathLength() > 1.25 && fit.m_ratio < 0.75
					&& numRevs <= 0.5) {
				fit.m_passed = false;
				fit.m_fail = 1;
			}
		} else {
			if (fit.m_lsqe / str.getPathLength() > 1.5 && fit.m_ratio < 0.732) {
				fit.m_passed = false;
				fit.m_fail = 2;
			}
			if (fit.m_lsqe / str.getPathLength() > 1.25 && fit.m_ratio < 0.7
					&& numRevs < 0.15) {
				fit.m_passed = false;
				fit.m_fail = 3;
			}
		}

		// test 2: verify that stroke is not overtraced
		if (overtraced) {
			fit.m_passed = false;
			fit.m_fail = 4;
		}

		// test 3: test feature area (use as error for fit)
		if (fit.m_err > Thresholds.active.M_LINE_FEATURE_AREA) {
			fit.m_passed = false;
			fit.m_fail = 5;
		}

		// test 4: ratio must be near 1.0
		// if (m_ratio > 0.99) // definitely a line
		// m_passed = true;
		if (fit.m_ratio < 0.3) { // definitely not a line
			fit.m_passed = false;
			fit.m_fail = 6;
		}

		return fit;
	}

	/**
	 * Get the number of revolutions that a stroke makes
	 * 
	 * @param str
	 *            stroke
	 * @return number of revolutions
	 */
	public static double getNumRevolutions(Stroke str) {
		double sum = 0;
		double totalRot = 0;
		double numRevs = 0;
		double deltaX, deltaY, deltaX1, deltaY1;
		for (int i = 1; i < str.getNumPoints() - 1; i++) {
			deltaX = str.getPoints().get(i + 1).getX()
					- str.getPoints().get(i).getX();
			deltaY = str.getPoints().get(i + 1).getY()
					- str.getPoints().get(i).getY();
			deltaX1 = str.getPoints().get(i).getX()
					- str.getPoints().get(i - 1).getX();
			deltaY1 = str.getPoints().get(i).getY()
					- str.getPoints().get(i - 1).getY();

			// check for divide by zero; add or subtract PI/2 accordingly (this
			// is the limit of atan as it approaches infinity)
			if (deltaX * deltaX1 + deltaY * deltaY1 == 0) {
				if (deltaX * deltaY1 - deltaX1 * deltaY < 0) {
					sum += Math.PI / -2.0;
				} else if (deltaX * deltaY1 - deltaX1 * deltaY > 0) {
					sum += Math.PI / 2.0;
				}
			}

			// otherwise sum the rotation
			else
				sum += Math.atan2((deltaX * deltaY1 - deltaX1 * deltaY),
						(deltaX * deltaX1 + deltaY * deltaY1));
		}
		totalRot = sum;

		// num revolutions = total rotation divided by 2PI
		numRevs = Math.abs(totalRot) / (Math.PI * 2.0);

		// this just accounts for numerical instabilities
		if (numRevs < .0000001)
			numRevs = 0.0;

		return numRevs;
	}
}
