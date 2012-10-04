/*
 * $Id: ZernikeMoments.java,v 1.4 2003/08/18 22:17:23 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;

import java.util.ArrayList;

/**
 * This class provides a set of methods for computing Zernike moments.
 * <p>
 * This code originally came from the Xite project written in C++.
 * (http://www.ifi.uio.no/forskning/grupper/dsb/Software/Xite/)
 * <p>
 * It has been translated to Java by Lijun Tang in his zernike.java source.
 * http://www1.cs.columbia.edu/~ljtang/research.html
 * <p>
 * Here I adapt it into my program. The basic functionality remains the same.
 * The modifications are in the interface and data structure, and the
 * reconstruction methods have been removed.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class ZernikeMoments {

	/**
	 * zer_pol_R() computes the radial polynomial, Rnm(p), in the definition of
	 * V(n,m,x,y). [1]
	 * 
	 * @return the value of Rnm(p)
	 */
	public static double zer_pol_R(int n, int m_in, double x, double y) {
		int i;
		int m;
		int s;
		int a; // (n-s)!
		int b; // s!
		int c; // [(n+|m|)/2-s]!
		int d; // [(n-|m|)/2-s]!
		int sign;

		m = Math.abs(m_in);

		if ((n - m) % 2 != 0) {
			throw new IllegalArgumentException("zer_pol_R: n-|m| is odd");
		}

		double res = 0;
		if ((x * x + y * y) <= 1.0) {
			sign = 1;
			a = 1;
			for (i = 2; i <= n; i++) {
				a *= i;
			}
			b = 1;
			c = 1;
			for (i = 2; i <= (n + m) / 2; i++) {
				c *= i;
			}
			d = 1;
			for (i = 2; i <= (n - m) / 2; i++) {
				d *= i;
			}
			// Before the loop is entered, all the integer variables
			// (sign, a, b, c, d) have their correct values for the
			// s=0 case.
			for (s = 0; s <= (n - m) / 2; s++) {
				res += sign * (a * 1.0 / (b * c * d))
						* Math.pow((x * x + y * y), (n / 2.0) - s);
				// Now update the integer variables before the next
				// iteration of the loop.
				if (s < (n - m) / 2) {
					sign = -sign;
					a = a / (n - s);
					b = b * (s + 1);
					c = c / ((n + m) / 2 - s);
					d = d / ((n - m) / 2 - s);
				}
			}
		}
		return res;
	}

	/**
	 * zer_pol() computes the Zernike basis function V(n,m,x,y).
	 * 
	 * @return res[1] is the dcomplex for V(n,m,x,y)
	 */
	public static Complex zer_pol(int n, int m, double x, double y) {
		if ((x * x + y * y) > 1.0) {
			return new Complex(0.0, 0.0);
		} else {
			double r = zer_pol_R(n, m, x, y);
			double arg = m * Math.atan2(y, x);
			double real = r * Math.cos(arg);
			double imag = r * Math.sin(arg);
			return new Complex(real, imag);
		}
	}

	/*
	 * ________________________________________________________________
	 * 
	 * zer_mom ________________________________________________________________
	 * 
	 * Name: zer_mom, zer_pol, zer_rec, zer_con - Zernike moments
	 * 
	 * Syntax: | #include <xite/zernike.h> | | int zer_mom( BufferedImage
	 * inband, int n, int m, | DCOMPLEX *res ); | | int zer_pol( int n, int m,
	 * double x, double y, | DCOMPLEX *res ); | | int zer_con( int n,
	 * BufferedImage inband, BufferedImage outband ); | | int zer_rec( int
	 * order, BufferedImage inband, BufferedImage outband ); |
	 * 
	 * Description: The Zernike moment of order 'n' and repetition 'm' of an
	 * image f(x,y) is defined as follows: | n+1 * | A(n,m) = ---- Sum Sum
	 * f(x,y)[V(n,m, x,y)] | pi x y | | where x^2+y^2 <= 1 | The image V(n,m,
	 * x,y) is the Zernike basis images of order 'n' and repetition 'm'. These
	 * basis images are complex and orthogonal. The Zernike moments are
	 * essentially the projections of the input image onto these basis images.
	 * 
	 * The original image can be reconstructed from the Zernike moments. The
	 * N-th order approximation is given by | ^ N | f(x,y) = Sum Sum A(n,m)
	 * V(n,m, x,y) | n=0 m | The contribution or information content of the n-th
	 * order moments is | | I(x,y, n) = Sum A(n,m) V(n,m, x,y) | m
	 * 
	 * 'zer_mom' computes A(n,m).
	 * 
	 * 'zer_pol' computes the zernike basis function V(n,m,x,y).
	 * 
	 * 'zer_con' computes the absolute value of the contribution of the n-th
	 * order moments, i.e. the absolute value of I(x,y, n).
	 * 
	 * 'zer_rec' demonstrates how the input image can be reconstructed from the
	 * Zernike basis functions and the Zernike moments.
	 * 
	 * Return value: | 0 : OK.
	 * 
	 * Author: Aivind Due Trier, Dept. Informatics, Univ. Oslo
	 * ________________________________________________________________
	 */

	/**
	 * Computes the width, height, and centroid of the shape, and call
	 * zer_mom(double[] xvals, double[] yvals, int npoints, double ww, double
	 * hh, double cx, double cy, int n, int m)
	 */
	public static Complex zer_mom(double[] xvals, double[] yvals, int npoints,
			int n, int m) {
		int diff = n - Math.abs(m);
		if ((n < 0) || (Math.abs(m) > n) || (diff % 2 != 0)) {
			throw new IllegalArgumentException("zer_mom: n=" + n + ", m=" + m
					+ ", n-|m|=" + diff);
		}
		double xmin = Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double xmax = Double.MIN_VALUE;
		double ymax = Double.MIN_VALUE;
		for (int i = 0; i < npoints; i++) {
			xmin = Math.min(xmin, xvals[i]);
			xmax = Math.max(xmax, xvals[i]);
			ymin = Math.min(ymin, yvals[i]);
			ymax = Math.max(ymax, yvals[i]);
		}
		double ww = xmax - xmin;// width
		double hh = ymax - ymin;// height
		double cx = xmin + ww / 2;
		double cy = ymin + hh / 2;
		return zer_mom(xvals, yvals, npoints, ww, hh, cx, cy, n, m);
	}

	/**
	 * xvals and yvals are the coordinates of the shape. npoints indicates the
	 * total number of points in the digitized shape. ww is the width and hh is
	 * the height of the bounding box of the shape. (cx, cy) is the centroid
	 * point of the shape. n and m are the moment order. 'zer_mom' computes
	 * A(n,m).
	 */
	public static Complex zer_mom(double[] xvals, double[] yvals, int npoints,
			double ww, double hh, double cx, double cy, int n, int m) {
		double i_0, j_0;
		double i_scale, j_scale;
		double x, y;
		Complex v;
		double isize, jsize;

		int diff = n - Math.abs(m);
		if ((n < 0) || (Math.abs(m) > n) || (diff % 2 != 0)) {
			throw new IllegalArgumentException("zer_mom: n=" + n + ", m=" + m
					+ ", n-|m|=" + diff);
		}
		isize = ww;
		jsize = hh;
		i_0 = cx;
		j_0 = cy;
		double radius = ww / 2;
		i_scale = Math.sqrt(2) * radius;
		radius = hh / 2;
		j_scale = Math.sqrt(2) * radius; // note we want to construct a circle
											// to contain the rectangle
		Complex res = new Complex();
		for (int i = 0; i < npoints; i++) {
			x = (xvals[i] - i_0) / i_scale;
			y = (yvals[i] - j_0) / j_scale;
			if (((x * x + y * y) <= 1.0)) {// we ignore (x,y) not in the unit
											// circle
				v = zer_pol(n, m, x, y);
				res.setRE(res.getRE() + v.getRE());
				res.setIM(res.getIM() + v.getIM());
			}
		}
		res.setRE(res.getRE() * (n + 1) / Math.PI);
		res.setIM(res.getIM() * (n + 1) / Math.PI);
		return res;
	}

	/**
	 * Return the set of Zernike's moments up to the specified order. The
	 * moments are returned in the order of (0,0)(1,1)(2,0)(2,2)(3,1)(3,3) and
	 * so on, where m<=n and n = 0...order.
	 * 
	 * xvals and yvals are the coordinates of the shape. npoints indicates the
	 * total number of points in the digitized shape. ww is the width and hh is
	 * the height of the bounding box of the shape. (cx, cy) is the centroid
	 * point of the shape. n and m are the moment order. 'zer_mom' computes
	 * A(n,m).
	 */
	public static Complex[] zer_mmts(int order, double[] xvals, double[] yvals,
			int npoints, double ww, double hh, double cx, double cy) {
		ArrayList list = new ArrayList(order);
		int ct = 0;
		for (int n = 0; n <= order; n++) {
			for (int m = 0; m <= n; m++) {
				if ((n - Math.abs(m)) % 2 == 0) {
					Complex v = zer_mom(xvals, yvals, npoints, ww, hh, cx, cy,
							n, m);
					list.add(ct, v);
					ct++;
				}
			}
		}
		Complex[] mmts = new Complex[ct];
		for (int i = 0; i < ct; i++) {
			mmts[i] = (Complex) list.get(i);
		}
		return mmts;
	}

	public static Complex[] zer_mmts(int order, double[] xvals, double[] yvals,
			int npoints) {
		double xmin = Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double xmax = Double.MIN_VALUE;
		double ymax = Double.MIN_VALUE;
		for (int i = 0; i < npoints; i++) {
			xmin = Math.min(xmin, xvals[i]);
			xmax = Math.max(xmax, xvals[i]);
			ymin = Math.min(ymin, yvals[i]);
			ymax = Math.max(ymax, yvals[i]);
		}
		double ww = xmax - xmin;// width
		double hh = ymax - ymin;// height
		double cx = xmin + ww / 2;
		double cy = ymin + hh / 2;
		return zer_mmts(order, xvals, yvals, npoints, ww, hh, cx, cy);
	}

	/**
	 * Data structure for a complex number with real and imaginary parts.
	 */
	public static class Complex {
		private double _re = 0;
		private double _im = 0;

		/**
		 * Create a complex number with real and imaginary parts set to 0.
		 */
		public Complex() {
		}

		/**
		 * Create a complex number with the specified real and imaginary values.
		 */
		public Complex(double re, double im) {
			_re = re;
			_im = im;
		}

		/** Set the real part. */
		public void setRE(double re) {
			_re = re;
		}

		/** Set the imaginary part. */
		public void setIM(double im) {
			_im = im;
		}

		/** Return the real part. */
		public double getRE() {
			return _re;
		}

		/** Return the imaginary part. */
		public double getIM() {
			return _im;
		}

		/** Return the magnitude of this complex number. */
		public double getMagnitude() {
			double sum = _re * _re + _im * _im;
			return Math.sqrt(sum);
		}

		/** Text representation of this complex number. */
		public String toString() {
			return "RE=" + _re + ", IM=" + _im;
		}
	}
}
