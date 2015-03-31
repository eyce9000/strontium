/**
 * PerpendicularBisector.java
 *
 * Revision History:<br>
 * Jun 25, 2008 bpaulson - File created
 *
 * <p>
 *
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 *
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
 *
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
package srl.math;

import org.openawt.geom.Line2D;
import org.openawt.geom.Point2D;
import org.openawt.geom.Rectangle2D;

import srl.core.sketch.Point;


/**
 * Class used to calculate the perpendicular bisector of a line (at the
 * midpoint) within a given bounds. Class originally by Mark Eaton, but slightly
 * modified.
 * 
 * @author bpaulson
 */
public class PerpendicularBisector {

	/**
	 * Bisector for the input line
	 */
	private Line2D.Double bisector;

	/**
	 * Mid point of the line (where the bisector will intersect)
	 */
	private Point2D midPoint;

	/**
	 * Slope of the bisector
	 */
	private double bisectorSlope;

	/**
	 * Y-intercept of the bisector
	 */
	private double bisectorYIntercept;

	/**
	 * Constructor for perpendicular bisector
	 * 
	 * @param p1
	 *            first endpoint of the line
	 * @param p2
	 *            second endpoint of the line
	 * @param bounds
	 *            maximum bounds for bisector
	 */
	public PerpendicularBisector(Point2D p1, Point2D p2, Rectangle2D bounds) {
		double xMid = ((p2.getX() - p1.getX()) / 2) + p1.getX();
		double yMid = ((p2.getY() - p1.getY()) / 2) + p1.getY();
		midPoint = new Point2D.Double(xMid, yMid);

		if ((p2.getY() - p1.getY()) == 0) {
			// line is horizontal, slope 0
			// perpendicular bisector is slope infinity
			bisectorSlope = Double.NaN;
		} else if ((p2.getX() - p1.getX()) == 0) {
			// line is vertical, slope infinity
			// perpendicular bisector is slope 0
			bisectorSlope = 0;
		} else {
			bisectorSlope = -1 * (p2.getX() - p1.getX())
					/ (p2.getY() - p1.getY());
		}

		double x1, y1, x2, y2;
		if (Double.isNaN(bisectorSlope)) {
			// vertical
			bisectorYIntercept = Double.NaN;
			y1 = bounds.getMinY();
			y2 = bounds.getMaxY();
			x1 = midPoint.getX();
			x2 = midPoint.getX();
		} else if (bisectorSlope == 0) {
			// horizontal
			bisectorYIntercept = midPoint.getY();
			y1 = midPoint.getY();
			y2 = midPoint.getY();
			x1 = bounds.getMinX();
			x2 = bounds.getMaxX();
		} else {
			// solve for y intercept
			// y = mx + b
			double y = midPoint.getY();
			double x = midPoint.getX();
			double m = bisectorSlope;
			double b = y - (m * x);
			bisectorYIntercept = b;

			x1 = bounds.getMinX();
			x2 = bounds.getMaxX();

			if (Double.isNaN(m)) {
				y1 = bounds.getMinY();
				y2 = bounds.getMaxY();
			} else {
				y1 = m * x1 + b;
				y2 = m * x2 + b;
			}
		}
		bisector = new Line2D.Double(x1, y1, x2, y2);
	}

	/**
	 * Constructor for perpendicular bisector
	 * 
	 * @param p1
	 *            first endpoint of the line
	 * @param p2
	 *            second endpoint of the line
	 * @param bounds
	 *            maximum bounds for bisector
	 */
	public PerpendicularBisector(Point p1, Point p2, Rectangle2D bounds) {
		this(new Point2D.Double(p1.getX(), p1.getY()), new Point2D.Double(
				p2.getX(), p2.getY()), bounds);
	}

	/**
	 * Constructor for perpendicular bisector
	 * 
	 * @param p1
	 *            first endpoint of the line
	 * @param p2
	 *            second endpoint of the line
	 * @param bounds
	 *            maximum bounds for bisector
	 */
	public PerpendicularBisector(Point p1, Point2D p2, Rectangle2D bounds) {
		this(new Point2D.Double(p1.getX(), p1.getY()), p2, bounds);
	}

	/**
	 * Constructor for perpendicular bisector
	 * 
	 * @param p1
	 *            first endpoint of the line
	 * @param p2
	 *            second endpoint of the line
	 * @param bounds
	 *            maximum bounds for bisector
	 */
	public PerpendicularBisector(Point2D p1, Point p2, Rectangle2D bounds) {
		this(p1, new Point2D.Double(p2.getX(), p2.getY()), bounds);
	}

	/**
	 * Get the midpoint of the bisector
	 * 
	 * @return midpoint
	 */
	public Point2D getMidPoint() {
		return midPoint;
	}

	/**
	 * Get the slope of the bisector
	 * 
	 * @return slope of bisector
	 */
	public double getBisectorSlope() {
		return bisectorSlope;
	}

	/**
	 * Get the Y-intercept of the bisector
	 * 
	 * @return Y-intercept
	 */
	public double getBisectorYIntercept() {
		return bisectorYIntercept;
	}

	/**
	 * Get the bisector found
	 * 
	 * @return bisector found
	 */
	public Line2D.Double getBisector() {
		return bisector;
	}
}
