/*
 * $Id: Basis.java,v 1.1 2003/06/01 17:15:50 hwawen Exp $
 *
 * Copyright (c) 2003-2004 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.fragmentation;

/**
 * A basis represents a basic unit of a shape, minimal yet perceptually
 * relevant.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class Basis {
	/** Line segment type */
	public static int TYPE_LINE = 1;
	/** Elliptical arc type */
	public static int TYPE_ELLIPSE = 2;
	/** The set of original points that this basis approximates */
	private double _xvals[];
	private double _yvals[];
	/** The number of original points */
	private int _num;
	/** The type of this basis, line or ellipse. */
	private int _type;
	/**
	 * parameters of the primitive. ax+by+c=0 for a line,
	 * ax^2+bxy+cy^2+dx+ey+f=0 for an ellipse
	 */
	private double _params[];
	/** sum of squared distance of points from the fitted line or curve. */
	private double _fitError;

	/**
	 * Create a Basis object and initialize its attributes, the original points,
	 * number of points, the type of basis, parameters for the basis, and the
	 * fitting error resulting from the approximation of the original points
	 * with this basis.
	 */
	public Basis(double xvals_a[], double yvals_a[], int num_a, int type_a,
			double params_a[], double fitError_a) {
		_xvals = xvals_a;
		_yvals = yvals_a;
		_num = num_a;
		_type = type_a;
		_params = params_a;
		_fitError = fitError_a;
	}

	/**
	 * Return the X coordinates of the original points.
	 */
	public double[] getXvals() {
		return _xvals;
	}

	/**
	 * Return the Y coordinates of the original points.
	 */
	public double[] getYvals() {
		return _yvals;
	}

	/**
	 * Return the total number of original points.
	 */
	public int getNumPoints() {
		return _num;
	}

	/**
	 * Return the type of this basis object.
	 */
	public int getType() {
		return _type;
	}

	/**
	 * Return the parameters of this basis.
	 */
	public double[] getParams() {
		return _params;
	}

	/**
	 * Return the fit error.
	 */
	public double getFitError() {
		return _fitError;
	}

	/**
	 * Set the fit error.
	 */
	protected void setFitError(double val) {
		_fitError = val;
	}

	/**
	 * Text representation of this basis including its type, parameters, and fit
	 * error.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (getType() == TYPE_ELLIPSE) {
			buf.append("E  ");
		} else {
			buf.append("L  ");
		}
		buf.append(getFitError() + "\n");
		buf.append("params:\n");
		double[] params = getParams();
		for (int i = 0; i < params.length; i++) {
			buf.append("\t" + params[i] + "\n");
		}
		return buf.toString();
	}
}
