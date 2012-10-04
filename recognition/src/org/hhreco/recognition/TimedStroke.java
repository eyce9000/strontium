/*
 * $Id: TimedStroke.java,v 1.3 2003/08/08 00:04:53 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;

import org.openawt.geom.AffineTransform;
import org.openawt.geom.Rectangle2D;

/**
 * TimedStroke is a collection of points taken in the duration of a mouse
 * pressed event and a mouse released event. A TimedStroke object contains basic
 * stroke path information such as the points and the timestamps in the path.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class TimedStroke {

	/** x coordinates */
	private double _xvals[];

	/** y coordinates */
	private double _yvals[];

	/**
	 * An array storing the timestamps of the points in the stroke path.
	 */
	private long _timestamps[];

	/** Number of points in the stroke. */
	private int _npoints = 0;

	/**
	 * Construct a timed stroke object with an empty stroke path.
	 */
	public TimedStroke() {
		this(10);
	}

	/**
	 * Copy constructor for efficient copying of TimedStroke
	 */
	public TimedStroke(TimedStroke in) {
		_npoints = in._npoints;
		_xvals = new double[_npoints];
		System.arraycopy(in._xvals, 0, _xvals, 0, _npoints);
		_yvals = new double[_npoints];
		System.arraycopy(in._yvals, 0, _yvals, 0, _npoints);
		_timestamps = new long[_npoints];
		System.arraycopy(in._timestamps, 0, _timestamps, 0, _npoints);
	}

	/**
	 * Construct a timed stroke object with an empty stroke path of the given
	 * initial size.
	 */
	public TimedStroke(int initSize) {
		_npoints = 0;
		_xvals = new double[initSize];
		_yvals = new double[initSize];
		_timestamps = new long[initSize];
	}

	/**
	 * Add a pair of x, y coordinates in the stroke path and the corresponding
	 * timestamp.
	 */
	public void addVertex(double x, double y, long timestamp) {
		if (_npoints >= _xvals.length) {// expand array
			double[] tmpX = new double[_npoints * 2];
			System.arraycopy(_xvals, 0, tmpX, 0, _npoints);
			_xvals = tmpX;
			double[] tmpY = new double[_npoints * 2];
			System.arraycopy(_yvals, 0, tmpY, 0, _npoints);
			_yvals = tmpY;
			long[] tmpT = new long[_npoints * 2];
			System.arraycopy(_timestamps, 0, tmpT, 0, _npoints);
			_timestamps = tmpT;
		}

		_xvals[_npoints] = x;
		_yvals[_npoints] = y;
		_timestamps[_npoints] = timestamp;
		_npoints++;
	}

	/**
	 * Get the bounds of the polyline in Rectangle2D.Double.
	 */
	public Rectangle2D getBounds2D() {
		if (_npoints <= 0) {
			return new Rectangle2D.Double();
		}
		double x1 = _xvals[0];
		double y1 = _yvals[0];
		double x2 = x1;
		double y2 = y1;
		for (int i = 1; i < _npoints; i++) {
			if (_xvals[i] < x1) {
				x1 = _xvals[i];
			} else if (_xvals[i] > x2) {
				x2 = _xvals[i];
			}
			if (_yvals[i] < y1) {
				y1 = _yvals[i];
			} else if (_yvals[i] > y2) {
				y2 = _yvals[i];
			}
		}
		return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * Return the x coordinate at the specified index.
	 */
	public double getX(int i) {
		return _xvals[i];
	}

	/**
	 * Return the y coordinate at the specified index.
	 */
	public double getY(int i) {
		return _yvals[i];
	}

	/**
	 * Return the timestamp of the point at the given index.
	 */
	public long getTimestamp(int i) {
		return _timestamps[i];
	}

	/**
	 * Return the number of points in the stroke.
	 */
	public int getVertexCount() {
		return _npoints;
	}

	/**
	 * Transform the polyline with the given transform.
	 */
	public void transform(AffineTransform at) {
		int n = _npoints * 2;
		double[] coords = new double[n];
		int k = 0;
		for (int i = 0; i < _npoints; i++) {
			coords[k++] = _xvals[i];
			coords[k++] = _yvals[i];
		}
		at.transform(coords, 0, coords, 0, _npoints);
		k = 0;
		for (int i = 0; i < _npoints; i++) {
			_xvals[i] = coords[k++];
			_yvals[i] = coords[k++];
		}
	}

	/**
	 * Translate the polyline with the given distance.
	 */
	public void translate(double x, double y) {
		for (int i = 0; i < _npoints; i++) {
			_xvals[i] += x;
			_yvals[i] += y;
		}
	}
}
