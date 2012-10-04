/*
 * $Id: ZernikeFE.java,v 1.3 2003/08/18 22:17:23 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;

import org.hhreco.toolbox.Util;

/**
 * Compute Zernike moments of the specified order [1]. This is a global feature
 * of a shape and is independent of stroke-number, -order, and -direction. This
 * feature (specifically the magnitude of the moment) is invariant to rotation,
 * but not invariant to scale and translation. Therefore, it is important to
 * normalize the shape before extracting the moment features. All moment
 * features up to the specified order are returned except for order 0 and 1.
 * This is because for shapes that have been scale- and translation- normalized,
 * these values are the same across all shapes and therefore they are not useful
 * for distinguishing shapes. If desired, one can call zer_mom directly to
 * obtain these moment values.
 * <p>
 * 
 * [1] 'A. Khotanzad and Y.H. Hong' "Invariant image recognition by Zernike
 * Moments", IEEE trans. on Pattern Analysis and Machine Intelligence, vol.12,
 * no.5, pp.489-487, May 1990.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class ZernikeFE implements FeatureExtractor {
	/**
	 * The default order of Zernike moments is set to 8. This will compute
	 * moments from order 2 up to order 8.
	 */
	public static int DEFAULT_ORDER = 8;

	/**
	 * The order of moment.
	 */
	private int _order = DEFAULT_ORDER;

	/**
	 * Create a Zernike feature extractor with the default order.
	 */
	public ZernikeFE() {
	}

	/**
	 * Create a Zernike feature extractor with the specified order.
	 */
	public ZernikeFE(int order) {
		_order = order;
	}

	/**
	 * Return the magnitudes of the Zernike moments of the default order.
	 */
	public double[] apply(TimedStroke[] s) {
		return zernikeMoments(s, _order);
	}

	/**
	 * Return the magnitudes of the Zernike moments of the specified order.
	 */
	public double[] apply(TimedStroke[] s, int order) {
		return zernikeMoments(s, order);
	}

	/**
	 * Set the moment order (appears as 'n' in the equations [1])
	 */
	public void setOrder(int order) {
		_order = order;
	}

	/**
	 * Return the moment order.
	 */
	public int getOrder() {
		return _order;
	}

	/**
	 * Return the name of this feature extractor.
	 */
	public String getName() {
		return "Zernike Moments";
	}

	/**
	 * Compute Zernike moments of the stroke.
	 */
	public static double[] zernikeMoments(TimedStroke[] s) {
		return zernikeMoments(s, DEFAULT_ORDER);
	}

	/**
	 * Compute Zernike moments of the specified order and return the magnitudes
	 * of the moments.
	 */
	public static double[] zernikeMoments(TimedStroke[] s, int order) {
		int nstrokes = s.length;
		double xvector[][] = new double[nstrokes][];
		double yvector[][] = new double[nstrokes][];
		int npoints[] = new int[nstrokes];
		Util.strokesToArrays(s, xvector, yvector, npoints);
		int numOrigPoints = 0;
		for (int i = 0; i < nstrokes; i++) {
			numOrigPoints += npoints[i];
		}
		double[] origx = new double[numOrigPoints];
		double[] origy = new double[numOrigPoints];
		int origcur = 0;
		for (int i = 0; i < nstrokes; i++) {
			// pack orignal points into one array
			System.arraycopy(xvector[i], 0, origx, origcur, npoints[i]);
			System.arraycopy(yvector[i], 0, origy, origcur, npoints[i]);
			origcur += npoints[i];
		}
		ZernikeMoments.Complex[] vals = ZernikeMoments.zer_mmts(order, origx,
				origy, numOrigPoints);
		// remove the first two elements, they are m00 and m11
		double[] mag = new double[vals.length - 2];
		for (int i = 0; i < vals.length - 2; i++) {
			mag[i] = vals[i + 2].getMagnitude();
		}
		return mag;
	}
}
