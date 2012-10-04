/*
 * $Id: LineBasis.java,v 1.2 2003/09/30 16:30:05 hwawen Exp $
 *
 * Copyright (c) 2003-2004 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.fragmentation;

import org.openawt.geom.Line2D;

/**
 * A LineBasis is used to represent a set of points that are highly colinear.
 * The line is denoted with (a,b,c) parameters as in ax+by+c=0.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class LineBasis extends Basis {
	/**
	 * The line segment that best represents the sequence of points we're
	 * approximating.
	 */
	private Line2D _line;

	/**
	 * Create a line basis and initialize its attributes. "line" is the best
	 * approximated line fit to the set of points (xvals, yvals) using total
	 * regression.
	 */
	public LineBasis(double xvals[], double yvals[], int num, double params[],
			double fitError, Line2D line) {
		super(xvals, yvals, num, Basis.TYPE_LINE, params, fitError);
		_line = line;
		// double
		// len1=FEUtilities.distance(_line.getX1(),_line.getY1(),_line.getX2(),_line.getY2());
		// double len2=FEUtilities.pathLength(xvals,yvals,num);
		// _lenRatio=len1/len2;
	}

	/**
	 * Return the line segment used to approximate the given set of points.
	 */
	public Line2D getLine() {
		return _line;
	}

}
