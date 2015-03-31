/**
 * CurveFit.java
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

import Jama.Matrix;

/**
 * Fits stroke to an n-degree Bezier curve; currently this is limited to 5th
 * degree
 * 
 * @author bpaulson
 */
public class CurveFit extends Fit {

	/**
	 * Sometimes curves are best left alone rather than beautified
	 */
	protected static final boolean m_useOrigStrokesToBeautify = true;

	/**
	 * Curve fitting error
	 */
	protected double m_fitErr;

	/**
	 * Bezier control points
	 */
	protected Point2D[] m_P;

	/**
	 * Current degree curve we are generating
	 */
	protected int m_degree = 4;

	/**
	 * Minimum degree to try
	 */
	protected final int m_minDegree = 4;

	/**
	 * Maximum degree to try
	 */
	protected final int m_maxDegree = 5;

	/**
	 * Flag denoting if a condition for a curve fails or not
	 */
	protected boolean m_curveFailed = false;

	/**
	 * Constructor for curve fit
	 * 
	 * @param features
	 *            features of the stroke to fit a curve to
	 */
	public CurveFit(StrokeFeatures features) {
		super(features);

		boolean allFailed = true;
		double minErr = Double.MAX_VALUE;
		int minErrDegree = m_minDegree;

		// try to find best curve fit within the range of degrees given
		for (m_degree = m_minDegree; m_degree <= m_maxDegree; m_degree++) {
			calcControlPts();
			if (m_curveFailed)
				continue;
			m_err = calcError();
			if (m_err < minErr) {
				minErr = m_err;
				minErrDegree = m_degree;
				allFailed = false;
			}
		}

		// all curve estimations failed
		if (allFailed) {
			m_passed = false;
			m_fail = 0;
//			log.debug("CurveFit: passed = " + m_passed + "(" + m_fail
//					+ ")  Failed to calculate control points!");
		} else {
			m_err = minErr;
			m_degree = minErrDegree;

			// test 1: stroke must not be closed
			if (m_features.isClosed()) {
				m_passed = false;
				m_fail = 1;
			}

			// test 2: dcr must be low
			if (m_features.getDCR() > Thresholds.active.M_DCR_TO_BE_POLYLINE) {
				m_passed = false;
				m_fail = 2;
			}

			// test 4: make sure error between stroke and curve is low
			if (m_err > Thresholds.active.M_CURVE_ERROR) {
				m_passed = false;
				m_fail = 3;
			}

			if (m_err == Double.MAX_VALUE)
				m_err = Thresholds.active.M_CURVE_ERROR * 10.0;

			// create shape/beautified object
			generateCurve();
			try {
				computeBeautified();
			} catch (Exception e) {
				log.error("Could not create shape object: " + e.getMessage(),e);
				e.printStackTrace();
			}

//			log.debug("CurveFit: passed = " + m_passed + "(" + m_fail
//					+ ")  NDDE = " + m_features.getNDDE() + " fitErr = "
//					+ m_fitErr + " degree = " + m_degree + " err = " + m_err
//					+ " dcr = " + m_features.getDCR() + " max curv = "
//					+ m_features.getMaxCurv());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return CURVE;
	}

	/**
	 * Method used to generate the beautified curve
	 */
	protected void generateCurve() {
		GeneralPath curve;
		if (!m_useOrigStrokesToBeautify) {
			int degree = m_P.length - 1;
			curve = new GeneralPath();
			double delta = 0.001;
			double t = delta;
			if (m_P == null)
				return;
			if (m_P.length == 0)
				return;
			if (m_P[0] == null)
				return;
			curve.moveTo((float) m_P[0].getX(),
					(float) m_P[0].getY());
			while (t < 1.0) {
				double x = 0.0, y = 0.0;
				for (int i = 0; i < m_P.length; i++) {
					double b = Math.pow(1 - t, degree - i) * Math.pow(t, i)
							* binomialCoeff(degree, i);
					x += m_P[i].getX() * b;
					y += m_P[i].getY() * b;
				}
				curve.lineTo((float) x, (float) y);
				t += delta;
			}
			curve.moveTo((float) m_P[degree].getX(),
					(float) m_P[degree].getY());
			curve.closePath();
		}

		// making beautified shape the actual stroke instead of beautified curve
		// (since the estimation is not completely accurate)
		else {
			curve = new GeneralPath();
			curve.moveTo(m_features.getFirstOrigPoint()
					.getX(), m_features.getFirstOrigPoint().getY());
			for (int i = 1; i < m_features.getOrigPoints().size(); i++)
				curve.lineTo(m_features.getOrigPoints()
						.get(i).getX(), m_features.getOrigPoints().get(i)
						.getY());
		}
		m_shape = new SVGPath(curve);
	}

	/**
	 * Calculate the control points of the Bezier curve
	 * 
	 * Note: Bezier curve formula B(t) = P0*(1-t)^3 + P1*3t(1-t)^2 +
	 * P2*3t^2(1-t) + P3t^3
	 * 
	 * where t = parametirc value, B(t) are the actual curve points, and P0...P4
	 * are the control points
	 */
	protected void calcControlPts() {
		double[] lengthSoFar = m_features.getLengthSoFar();
		Point2D[] B = new Point2D[m_degree + 1];
		double[] tvals = new double[m_degree + 1];
		for (int i = 0; i < tvals.length; i++)
			tvals[i] = Double.NaN;
		m_P = new Point2D[m_degree + 1];

		// control point for t = 0
		B[0] = new Point2D.Double(m_features.getPoints().get(0).getX(),
				m_features.getPoints().get(0).getY());
		tvals[0] = 0.0;
		if (lengthSoFar == null) {
			m_curveFailed = true;
			return;
		}
		// find points closest to t = 1/degree and t = (degree-1)/degree
		for (int i = 0; i < lengthSoFar.length; i++) {
			for (int d = 1; d < m_degree; d++) {
				if (lengthSoFar[i] / m_features.getStrokeLength() > ((double) d / (double) m_degree)
						&& Double.isNaN(tvals[d])) {
					tvals[d] = lengthSoFar[i] / m_features.getStrokeLength();
					B[d] = new Point2D.Double(m_features.getPoints().get(i)
							.getX(), m_features.getPoints().get(i).getY());
				}

			}
		}
		// control point for t = 1
		B[m_degree] = new Point2D.Double(m_features.getPoints()
				.get(m_features.getNumPoints() - 1).getX(), m_features
				.getPoints().get(m_features.getNumPoints() - 1).getY());
		tvals[m_degree] = 1.0;

		// find control points
		Matrix A = new Matrix(m_degree + 1, m_degree + 1);
		for (int i = 0; i < m_degree + 1; i++) {
			double t = tvals[i];
			for (int j = 0; j < m_degree + 1; j++) {
				double val = Math.pow(1 - t, m_degree - j) * Math.pow(t, j)
						* binomialCoeff(m_degree, j);
				A.set(i, j, val);
			}
		}
		Matrix bX = new Matrix(m_degree + 1, 1);
		Matrix bY = new Matrix(m_degree + 1, 1);
		try {
			for (int i = 0; i < m_degree + 1; i++) {
				bX.set(i, 0, B[i].getX());
				bY.set(i, 0, B[i].getY());
			}
			Matrix XX = A.solve(bX);
			Matrix YY = A.solve(bY);
			for (int i = 0; i < m_degree + 1; i++)
				m_P[i] = new Point2D.Double(XX.get(i, 0), YY.get(i, 0));
		} catch (Exception e) {
			m_curveFailed = true;
		}
	}

	/**
	 * Calculate a binomial coefficient - n!/(k!(n-k)!)
	 * 
	 * @param n
	 * @param k
	 * @return binomial coefficient
	 */
	public static double binomialCoeff(int n, int k) {
		return factorial(n) / (factorial(k) * factorial(n - k));
	}

	/**
	 * Calculate the factorial of a given integer
	 * 
	 * @param x
	 *            integer to calculate factorial of
	 * @return factorial
	 */
	public static int factorial(int x) {
		if (x <= 1)
			return 1;
		return x * factorial(x - 1);
	}

	/**
	 * Calculate the orthogonal distance squared error between the stroke and
	 * the ideal curve
	 * 
	 * @return error
	 */
	public double calcError() {
		double err = 0.0;
		int degree = m_P.length - 1;
		double[] length = m_features.getLengthSoFar();
		for (int i = 0; i < length.length; i++) {
			double t = length[i] / m_features.getStrokeLength();
			double x = 0.0, y = 0.0;
			for (int j = 0; j < m_P.length; j++) {
				double b = Math.pow(1 - t, degree - j) * Math.pow(t, j)
						* binomialCoeff(degree, j);
				x += m_P[j].getX() * b;
				y += m_P[j].getY() * b;
			}
			err += (m_features.getPoints().get(i).distance(x, y) * m_features
					.getPoints().get(i).distance(x, y));
		}
		return Math.sqrt(err) / m_features.getStrokeLength();
	}

}
