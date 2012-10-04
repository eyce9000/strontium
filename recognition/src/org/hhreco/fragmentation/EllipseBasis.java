/*
 * $Id: EllipseBasis.java,v 1.4 2003/11/21 00:24:47 hwawen Exp $
 *
 * Copyright (c) 2003-2004 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.fragmentation;


import org.hhreco.toolbox.Util;
import org.openawt.geom.Line2D;
import org.openawt.geom.Point2D;

/**
 * An EllipseBasis uses an ellipse to approximate a set of points. It is
 * represented with (a,b,c,d,e,f) parameters as in a conic equation:
 * ax^2+bxy+cy^2+dx+ey+f=0.
 * 
 * Many properties of the fitted ellipse have been calculated, however to
 * minimize the amount of computation, they can be turned off by not calling
 * calcProperties.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class EllipseBasis extends Basis {
	/**
	 * Data points on the ellipse which approximates the given set of points.
	 * _points[0] = x coordinate array _points[1] = y coordinate array
	 */
	private double _points[][] = null;

	/**
	 * Number of data points in the fitted elliptical segment.
	 */
	private int _numDataPoints = -1;

	private double _eccentricity;

	// approximated length of the perimeter of the ellipse
	private double _circumference;

	private Point2D _center;// center of the ellipse
	private Point2D _midPoint;// mid point on the elliptical segment
	private Line2D _majorAxis;
	private Line2D _minorAxis;
	private double _maxNorm;
	private double _minNorm;

	/**
	 * Create an ellipse basis where params give the parameters of an ellipse
	 * that best approximate the given set of points. The fit error will be
	 * calcuated in the constructor, it require interpolating the given sequence
	 * of points in order to get a better approximation of distance.
	 */
	public EllipseBasis(double xvals[], double yvals[], int num,
			double params[]) {
		super(xvals, yvals, num, Basis.TYPE_ELLIPSE, params, 0);// fiterror=0
																// for now
		double[] p = new double[params.length + 1];
		for (int i = 0; i < params.length; i++) {
			p[i + 1] = params[i];
		}
		if (!calcProperties(p)) {// not successful
			setFitError(Double.NaN);
		} else {
			if (!genPoints()) {
				setFitError(Double.NaN);
			} else {
				double totalMag = Util.pathLength(xvals, yvals, num);
				double halfMag = totalMag / 2;
				double distSoFar = 0;
				double len, tmp;
				for (int i = 1; i < num; i++) {
					len = Util.distance(xvals[i - 1], yvals[i - 1], xvals[i],
							yvals[i]);
					tmp = len + distSoFar;
					if (tmp < halfMag) {// keep going
						distSoFar = tmp;
					} else {// midpoint is somewhere in between this segment
						double ratio = Math.abs(halfMag - distSoFar) / len;
						double midx = xvals[i - 1] + (xvals[i] - xvals[i - 1])
								* ratio;
						double midy = yvals[i - 1] + (yvals[i] - yvals[i - 1])
								* ratio;
						_midPoint = new Point2D.Double(midx, midy);
						break;
					}
				}
			}
		}
	}

	private boolean calcProperties(double[] pvec) {
		// according to notation in Pavlidis' paper
		double a = pvec[1];
		double h = pvec[2] / 2;
		double b = pvec[3];
		double e = pvec[4] / 2;
		double g = pvec[5] / 2;
		double c = pvec[6];
		double Q[][] = new double[3][3];
		double Qinv[][] = new double[3][3];
		Q[1][1] = pvec[1];
		Q[1][2] = pvec[2] / 2;// h
		Q[2][1] = Q[1][2];
		Q[2][2] = pvec[3];
		FittingUtil.inverse(Q, Qinv, 2);
		double gv1 = pvec[4];// 2*e
		double gv2 = pvec[5];// 2*g
		double c1 = c
				- 0.25
				* (gv1 * gv1 * Qinv[1][1] + gv1 * gv2 * Qinv[2][1] + gv2 * gv1
						* Qinv[1][2] + gv2 * gv2 * Qinv[2][2]);
		double term2 = Math.sqrt(Math.abs(c1));
		double totalDist = 0;
		double[] xvals = getXvals();
		double[] yvals = getYvals();
		for (int j = 0; j < getNumPoints(); j++) {
			double xi = xvals[j];
			double yi = yvals[j];
			double val = a * xi * xi + 2 * h * xi * yi + b * yi * yi + gv1 * xi
					+ gv2 * yi + c;
			double tmp = Math.sqrt(val - c1) - term2;
			totalDist += Math.pow(tmp, 2);
		}
		if (Double.isNaN(totalDist)) {
			return false;
		}

		setFitError(totalDist);
		double eval[] = new double[3];
		double evec[][] = new double[3][3];
		FittingUtil.eigen(Q, eval, evec);
		double minval, maxval;
		if (eval[1] < eval[2]) {
			minval = eval[1];
			maxval = eval[2];
		} else {
			minval = eval[2];
			maxval = eval[1];
		}
		_maxNorm = 2 * Math.sqrt(-c1 / minval);
		_minNorm = 2 * Math.sqrt(-c1 / maxval);
		_eccentricity = _minNorm / _maxNorm;

		double denom = a * b - Math.pow(h, 2);
		double cx = (-e * b + g * h) / denom;
		double cy = (-a * g + e * h) / denom;
		_center = new Point2D.Double(cx, cy);
		if (eval[1] < eval[2]) {// index 1 = major axis, index 2 = minor axis
			// max eigenvalue(eigenvector) corresponds to minor axis
			double k = Math.pow(_minNorm, 2);
			double m = evec[2][2] / evec[1][2];// slope of the minor axis, max
												// eigenvalue, index 2
			double tmp = Math.sqrt(k / (m * m + 1));
			double x1 = cx + tmp;
			double x2 = cx - tmp;
			double y1 = m * (x1 - cx) + cy;
			double y2 = m * (x2 - cx) + cy;
			_minorAxis = new Line2D.Double(x1, y1, x2, y2);
			k = Math.pow(_maxNorm, 2);
			m = evec[2][1] / evec[1][1];// slope of the major axis, min
										// eigenvalue, index 1
			tmp = Math.sqrt(k / (m * m + 1));
			x1 = cx + tmp;
			x2 = cx - tmp;
			y1 = m * (x1 - cx) + cy;
			y2 = m * (x2 - cx) + cy;
			_majorAxis = new Line2D.Double(x1, y1, x2, y2);
		} else {// index 2 = major axis, index 1 = minor axis
			double k = Math.pow(_minNorm, 2);
			double m = evec[2][1] / evec[1][1];// slope of the minor axis, max
												// eigenvalue, index 2
			double tmp = Math.sqrt(k / (m * m + 1));
			double x1 = cx + tmp;
			double x2 = cx - tmp;
			double y1 = m * (x1 - cx) + cy;
			double y2 = m * (x2 - cx) + cy;
			_minorAxis = new Line2D.Double(x1, y1, x2, y2);
			k = Math.pow(_maxNorm, 2);
			m = evec[2][2] / evec[1][2];// slope of the major axis, min
										// eigenvalue, index 1
			tmp = Math.sqrt(k / (m * m + 1));
			x1 = cx + tmp;
			x2 = cx - tmp;
			y1 = m * (x1 - cx) + cy;
			y2 = m * (x2 - cx) + cy;
			_majorAxis = new Line2D.Double(x1, y1, x2, y2);
		}
		return true;
	}

	/**
	 * return false if the process is not successful. ex. all points generated
	 * are (-1,-1).
	 */
	private boolean genPoints() {
		int npts = 50;
		double XY[][] = new double[3][npts + 1];
		double pvec[] = new double[7];
		double params[] = getParams();
		for (int i = 1; i <= 6; i++) {
			pvec[i] = params[i - 1];
		}
		double thetas[] = new double[npts + 1];
		FittingUtil.draw_conic(pvec, npts, XY, thetas);

		double xvals[] = getXvals();
		double yvals[] = getYvals();
		int s = closest(xvals[0], yvals[0], XY, npts);
		int m = closest(xvals[getNumPoints() / 2], yvals[getNumPoints() / 2],
				XY, npts);
		int e = closest(xvals[getNumPoints() - 1], yvals[getNumPoints() - 1],
				XY, npts);
		// System.out.println("s="+s+", m="+m+", e="+e);
		if ((s == -1 && m == -1 && e == -1)) {
			return false;
		}

		int dir = 1;// increment or decrement
		if (s > m & m > e)
			dir = -1;
		else if (s < m & m < e)
			dir = 1;
		else if (s > m & m < e & s > e)
			dir = 1;
		else if (s < m & m > e & s < e)
			dir = -1;
		else if (s > m & m < e & e > s)
			dir = -1;
		else if (s < m & m > e & e < s)
			dir = 1;
		else
			dir = 1;

		double tmp[][] = new double[2][npts];
		int ct = 0;
		int i = s;
		while (true) {
			if (XY[1][i] != -1 || XY[2][i] != -1) {// keep point only if it's
													// not (-1,-1)
				tmp[0][ct] = XY[1][i];// x
				tmp[1][ct] = XY[2][i];// y
				ct++;
			}
			i += dir;
			if (i > npts)// wrap around to the beginning
				i = 1;
			else if (i <= 0)// wrap around to the end
				i = npts;
			if (i == e)
				break;
		}
		if (ct < 3) {
			return false;
		}
		_points = new double[2][ct];
		for (i = 0; i < ct; i++) {
			_points[0][i] = tmp[0][i];
			_points[1][i] = tmp[1][i];
		}
		_numDataPoints = ct;
		_circumference = Util
				.pathLength(_points[0], _points[1], _numDataPoints);
		return true;
	}

	/**
	 * Generate points from p1 to p2 in the direction of the stroke. The number
	 * of points is specified by "npts". Return 0 if the process is not
	 * successful. Otherwise return the number of points generated.
	 */
	public int genPoints(Point2D p1, Point2D p2, double xpath[],
			double ypath[], int npts) {
		double XY[][] = new double[3][npts + 1];
		double pvec[] = new double[7];
		double params[] = getParams();
		for (int i = 1; i <= 6; i++) {
			pvec[i] = params[i - 1];
		}
		double thetas[] = new double[npts + 1];
		FittingUtil.draw_conic(pvec, npts, XY, thetas);

		double xvals[] = getXvals();
		double yvals[] = getYvals();
		int s = closest(p1.getX(), p1.getY(), XY, npts);// start
		int m = closest(xvals[getNumPoints() / 2], yvals[getNumPoints() / 2],
				XY, npts);// middle
		int e = closest(p2.getX(), p2.getY(), XY, npts);// end
		// System.out.println("s="+s+", m="+m+", e="+e);
		if ((s == -1 && m == -1 && e == -1)) {
			return 0;
		}

		int dir = 1;// increment or decrement
		if (s > m & m > e)
			dir = -1;
		else if (s < m & m < e)
			dir = 1;
		else if (s > m & m < e & s > e)
			dir = 1;
		else if (s < m & m > e & s < e)
			dir = -1;
		else if (s > m & m < e & e > s)
			dir = -1;
		else if (s < m & m > e & e < s)
			dir = 1;
		else
			dir = 1;

		double tmp[][] = new double[2][npts];
		int ct = 0;
		int i = s;
		while (true) {
			if (XY[1][i] != -1 || XY[2][i] != -1) {// keep point only if it's
													// not (-1,-1)
				tmp[0][ct] = XY[1][i];// x
				tmp[1][ct] = XY[2][i];// y
				ct++;
			}
			i += dir;
			if (i > npts)// wrap around to the beginning
				i = 1;
			else if (i <= 0)// wrap around to the end
				i = npts;
			if (i == e)
				break;
		}
		if (ct < 3) {
			return 0;
		}

		for (i = 0; i < ct; i++) {
			xpath[i] = tmp[0][i];
			ypath[i] = tmp[1][i];
		}
		return ct;
	}

	/**
	 * Return the index of the closest point in the arr to the given (x,y) where
	 * the point is not -1. arr is 3x51, index 0 is not valid.
	 */
	private int closest(double x, double y, double arr[][], int npts) {
		double minVal = Double.MAX_VALUE;
		int minIndex = -1;
		double dist;
		for (int i = 1; i < npts; i++) {
			if ((arr[1][i] != -1) && (arr[2][i] != -1)) {
				dist = Util.distance(x, y, arr[1][i], arr[2][i]);
				if (dist < minVal) {
					minVal = dist;
					minIndex = i;
				}
			}
		}
		return minIndex;
	}

	/** Return the center of the ellipse */
	public Point2D getCenter() {
		return _center;
	}

	/** Return the mid point on the original stroke points */
	public Point2D getMidpoint() {
		return _midPoint;
	}

	/**
	 * Return the approximated circumference of this ellipse (the entier
	 * ellipse, not just the segment)
	 */
	public double getCircumference() {
		return _circumference;
	}

	/** Return the ratio of the norms of the two axes of the ellipse */
	public double getEccentricity() {
		return _eccentricity;
	}

	/**
	 * Return the data points of the elliptical segment that approximates the
	 * given set of points. _points[0][] = xvals _points[1][] = yvals
	 */
	public double[][] getEllipticalPoints() {
		return _points;
	}

	public Line2D getMajorAxis() {
		return _majorAxis;
	}

	public Line2D getMinorAxis() {
		return _minorAxis;
	}

	/**
	 * Return the magnitude of the major axis.
	 */
	public double getMaxNorm() {
		return _maxNorm;
	}

	/**
	 * Return the magnitude of the minor axis.
	 */
	public double getMinNorm() {
		return _minNorm;
	}

	/**
	 * Return the number of data points used to represent the elliptical
	 * segment.
	 */
	public int getNumEllipticalDataPoints() {
		return _numDataPoints;
	}
}
