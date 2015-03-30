/**
 * ConstrainableLine.java
 * 
 * Revision History:<br>
 * Sep 17, 2008 jbjohns - File created
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
package srl.recognition.constraint.constrainable;

import java.util.List;

import org.openawt.geom.Line2D;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Point;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;


/**
 * This class represents the implementation of IConstrainable that wraps a
 * simple line. Lines are defined by two endpoints.
 * <p>
 * This class always assumes that the Left-most (or in the case of vertical
 * lines, bottom-most) point is End1. There is an alias method called EndLB that
 * returns End1. The other endpoint, the right-/top-most point, is End2/EndTR.
 * 
 * @author jbjohns
 */
public class ConstrainableLine extends AbstractConstrainable {
	
	/**
	 * The shape typs this constrainable shape always returns when querying
	 * {@link IConstrainable#getShapeType()}.
	 * 
	 * @see ConstrainableFactory#LINE_SHAPE_TYPE
	 */
	public static final String LINE_SHAPE_TYPE = ConstrainableFactory.LINE_SHAPE_TYPE;
	
	/**
	 * The first endpoint. This endpoint is on the left/bottom.
	 */
	private ConstrainablePoint m_end1;
	
	/**
	 * The second endpoint. This endpoint is on the right/top.
	 */
	private ConstrainablePoint m_end2;
	
	/**
	 * The topmost endpoint. 
	 */
	private ConstrainablePoint m_top;
	
	/**
	 * The bottommost endpoint. 
	 */
	private ConstrainablePoint m_bottom;
	
	/**
	 * The leftmost endpoint. 
	 */
	private ConstrainablePoint m_left;
	
	/**
	 * The rightmost endpoint. 
	 */
	private ConstrainablePoint m_right;
	
	/**
	 * The first endpoint added, irrespective of which one is designated end1 or
	 * end2
	 */
	private ConstrainablePoint m_first;
	
	/**
	 * The second endpoint added, irrespective of which one is designated end1
	 * or end2
	 */
	private ConstrainablePoint m_last;
	
	/**
	 * Cache this so we don't compute it every time. Not too hard, but saves a
	 * little time at the cost of negligible amortized memory (since you'll be
	 * wanting the bounding box anyway).
	 */
	private BoundingBox m_boundingBox;
	
	
	/**
	 * Construct the constrainable line between the two endpoints. See
	 * {@link #setEndPoints(ConstrainablePoint, ConstrainablePoint)} for the
	 * contract of end point ordering and naming.
	 * 
	 * @param end1
	 *            The first endpoint
	 * @param end2
	 *            The second endpoint
	 * @param parentShape
	 *            See {@link AbstractConstrainable#getParentShape()} for a
	 *            discussion on parent shapes.
	 */
	public ConstrainableLine(ConstrainablePoint end1, ConstrainablePoint end2,
	        Shape parentShape) {
		
		super(parentShape);
		m_boundingBox = null;
		setEndPoints(end1, end2);
	}
	

	/**
	 * Construct the constrainable line between the two first point of the first
	 * stroke and last point of the last stroke in an IShape.
	 * <p>
	 * See
	 * {@link ConstrainableLine#ConstrainableLine(ConstrainablePoint, ConstrainablePoint, IShape)}
	 * 
	 * @param shape
	 *            The IShape
	 */
	public ConstrainableLine(Shape shape) {
		this(shape.getFirstStroke().getFirstPoint(), 
				shape.getLastStroke().getLastPoint(), shape);
	}
	

	/**
	 * Construct the constrainable line between the two endpoints of an IStroke.
	 * See {@link #setEndPoints(ConstrainablePoint, ConstrainablePoint)} for the
	 * contract of end point ordering and naming.
	 * 
	 * @param stroke
	 *            The IStroke
	 * @param parentShape
	 *            The parent shape of this constrainable line
	 */
	public ConstrainableLine(Stroke stroke, Shape parentShape) {
		this(stroke.getFirstPoint(), stroke.getLastPoint(), parentShape);
	}
	

	/**
	 * Construct the constrainable line between the two endpoints.
	 * 
	 * @see ConstrainablePoint#ConstrainablePoint(IPoint, IShape)
	 * @see ConstrainableLine#ConstrainableLine(ConstrainablePoint,
	 *      ConstrainablePoint, IShape)
	 * @param end1
	 *            The first endpoint.
	 * @param end2
	 *            The second endpoint
	 * @param parentShape
	 *            The parent shape of this constrainable line.
	 */
	public ConstrainableLine(Point end1, Point end2, Shape parentShape) {
		this(new ConstrainablePoint(end1, parentShape), new ConstrainablePoint(
		        end2, parentShape), parentShape);
	}
	

	/**
	 * Set the two endpoints associated with the line. Even though you specify
	 * the arguments as end1 and end2, this will not necessarily be the way the
	 * points are assigned to the class's members.
	 * <p>
	 * The two endpoints you specify will be compared. The leftmost endpoint
	 * will be assigned and returned with {@link #getEnd1()} (or bottom for
	 * vertical lines). The rightmost endpoint will be assigned and returned
	 * with {@link #getEnd2()} (or top for vertical lines).
	 * 
	 * @param pt1
	 *            The first point that defines this line
	 * @param pt2
	 *            The second point that defines this line
	 */
	public void setEndPoints(ConstrainablePoint pt1, ConstrainablePoint pt2) {
		if (pt1 == null || pt2 == null) {
			throw new NullPointerException("Endpoints cannot be null");
		}
		
		m_first = pt1;
		m_last = pt2;
		
		// are the lines vertical? vertical == same x coord.
		double xDiff = Math.abs(pt1.getX() - pt2.getX());
		
		// how different do we allow the x values before declaring something
		// vertical?
		final double verticalEpsilon = 0;
		
		if (xDiff <= verticalEpsilon) {
			// which one is on top? top means lower y val, since for screen
			// coordinates origin is at top of screen and values increase moving
			// down the monitor
			if (pt1.getY() <= pt2.getY()) {
				// pt1 is on the top
				m_end1 = pt2;
				m_end2 = pt1;
			}
			else {
				// pt2 is on the top
				m_end1 = pt1;
				m_end2 = pt2;
			}
		}
		else {
			// we're not vertical. So we'll compare the x values and see which
			// one is on the left. Using screen coordinates, lower values of
			// X are on the left
			if (pt1.getX() <= pt2.getX()) {
				// pt1 has lower x so it's on the left
				m_end1 = pt1;
				m_end2 = pt2;
			}
			else {
				// pt2 has lower x so it's on the left
				m_end1 = pt2;
				m_end2 = pt1;
			}
		}
		
		if (pt1.getY() <= pt2.getY()) {
			// pt1 is on the top
			m_top = pt1;
			m_bottom = pt2;
		}
		else {
			// pt2 is on the top
			m_top = pt2;
			m_bottom = pt1;
		}
		if (pt1.getX() <= pt2.getX()) {
			// pt1 has lower x so it's on the left
			m_left = pt1;
			m_right = pt2;
		}
		else {
			// pt2 has lower x so it's on the left
			m_left = pt2;
			m_right = pt1;
		}
		
		// we've changed the endpoints, so null out any cached value for
		// bounding box
		m_boundingBox = null;
	}
	
	/**
	 * Standard, easy definition of a slope.
	 * @return
	 */
	public double getSlope()
	{
		if (m_top.getX() == m_bottom.getX()) 
			return Double.MAX_VALUE;
		else
			// must make negative because of the way the screen's coordinate system works
			return -((m_top.getY() - m_bottom.getY()) / (m_top.getX() - m_bottom.getX()));
	}
	
	/**
	 * Get the topmost endpoint
	 * 
	 * @return The topmost endpoint
	 */
	public ConstrainablePoint getTop() {
		return m_top;
	}
	
	/**
	 * Get the bottommost endpoint
	 * 
	 * @return The bottommost endpoint
	 */
	public ConstrainablePoint getBottom() {
		return m_bottom;
	}
	
	/**
	 * Get the leftmost endpoint
	 * 
	 * @return The leftmost endpoint
	 */
	public ConstrainablePoint getLeft() {
		return m_left;
	}
	
	/**
	 * Get the rightmost endpoint
	 * 
	 * @return The rightmost endpoint
	 */
	public ConstrainablePoint getRight() {
		return m_right;
	}

	/**
	 * Get the first endpoint, which is the leftmost (or for vertical lines,
	 * bottom) endpoint.
	 * 
	 * @return The first endpoint
	 */
	public ConstrainablePoint getEnd1() {
		return m_end1;
	}
	

	/**
	 * Get the leftmost (or for vertical lines, bottom) endpoint.
	 * 
	 * @see #getEnd1()
	 * @return Said endpoint
	 */
	public ConstrainablePoint getEndLB() {
		return getEnd1();
	}
	

	/**
	 * Get the second endpoint, which is the rightmost (or for vertical lines,
	 * top) endpoint.
	 * 
	 * @return The second endpoint
	 */
	public ConstrainablePoint getEnd2() {
		return m_end2;
	}
	

	/**
	 * Get the rightmost (or for vertical lines, the top) endpoint
	 * 
	 * @see #getEnd2()
	 * @return Said endpoint
	 */
	public ConstrainablePoint getEndRT() {
		return getEnd2();
	}
	

	/**
	 * Get the bottom most end point of the line
	 * 
	 * @return bottom most end point of the line
	 */
	public ConstrainablePoint getBottomMostEnd() {
		if (m_end1.getY() > m_end2.getY())
			return m_end1;
		else
			return m_end2;
	}
	

	/**
	 * Get the top most end point of the line
	 * 
	 * @return top most end point of the line
	 */
	public ConstrainablePoint getTopMostEnd() {
		if (m_end1.getY() <= m_end2.getY())
			return m_end1;
		else
			return m_end2;
	}
	

	/**
	 * Get the right most end point of the line
	 * 
	 * @return right most end point of the line
	 */
	public ConstrainablePoint getRightMostEnd() {
		if (m_end1.getX() < m_end2.getX())
			return m_end2;
		else
			return m_end1;
	}
	

	/**
	 * Get the left most end point of the line
	 * 
	 * @return left most end point of the line
	 */
	public ConstrainablePoint getLeftMostEnd() {
		if (m_end1.getX() >= m_end2.getX())
			return m_end2;
		else
			return m_end1;
	}
	

	/**
	 * Get an array containing exactly two elements. return[0] is the first
	 * endpoint returned by {@link #getEnd1()}. return[1] is the second endpoint
	 * returned by {@link #getEnd2()}. We return an array here rather than a
	 * {@link List} because it's more lightweight and easy to deal with.
	 * 
	 * @return An array with two elements: the two endpoints of this line.
	 */
	public ConstrainablePoint[] getEndpoints() {
		return new ConstrainablePoint[] { getEnd1(), getEnd2() };
	}
	

	/**
	 * Get the point that is the midpoint (center) of this line. The midpoint of
	 * this line is defined as the point exactly between the two endpoints.
	 * 
	 * @return The midpoint of the line.
	 */
	public ConstrainablePoint getMidpoint() {
		// midpoint of straight line is average of endpoints
		double x = (m_end1.getX() + m_end2.getX()) / 2.0;
		double y = (m_end1.getY() + m_end2.getY()) / 2.0;
		
		return new ConstrainablePoint(x, y, getParentShape());
	}
	

	/**
	 * Get the angle of the line between the two endpoints. The angle is
	 * returned in radians and will be between the values [0...2*PI] (0 and 360
	 * degrees, if converted). If the endpoints are in the same (x,y) location,
	 * returns 0.
	 * <p>
	 * The angle of the line is computed perceptually, as it would look on the
	 * screen. Since screen coordinates increase Y value as you move down the
	 * screen, this means we have to "flip" the angle to align the values with
	 * Cartesian coordinates, or how the user sees things on the screen and
	 * expects them to be. This means that a line moving from a point on the
	 * screen up and to the right, even though Y values are decreasing since
	 * it's moving up the screen and the angle should be negative value or large
	 * value close to 2*PI (decreasing Y values), will have a positive angle <
	 * Pi/2.
	 * <p>
	 * The notion of "first" and "last" endpoints are used here to preserve
	 * stroke direction and therefore angle. This way, things don't always look
	 * positive.
	 * 
	 * @return The angle of the line in radians.
	 */
	public double getAngleInRadians() {
		final double TWOPI = 2 * Math.PI;
		
		// radians
		double deltaY = -m_first.getY() + m_last.getY();
		double deltaX = -m_first.getX() + m_last.getX();
		
		// negate the angle because with screen coords, increasing Y values go
		// down. ATAN is expecting cartesian, where increasing Y goes up.
		// negating the angle flips from cartesian to screen orientation.
		double angle = -Math.atan2(deltaY, deltaX);
		// rotate to 0...2*PI
		while (angle < 0) {
			angle += TWOPI;
		}
		while (angle >= TWOPI) {
			angle -= TWOPI;
		}
		
		return angle;
	}
	

	/**
	 * Compute the angle in radians, then convert it to degrees. Because of the
	 * computations in {@link #getAngleInRadians()}, the angle returned here
	 * will be in the range [0...360].
	 * <p>
	 * See {@link #getAngleInRadians()} for a discussion about the angle
	 * returned. The angle is based on perception--what the user sees on the
	 * monitor--rather than strictly on the x and y values of the line
	 * endpoints.
	 * 
	 * @return Get the angle of the line in degrees.
	 */
	public double getAngleInDegrees() {
		// get the angle in radians
		double angle = getAngleInRadians();
		// to degrees
		angle = angle * 180 / Math.PI;
		return angle;
	}
	

	/**
	 * Get the angle between this line and the other line, in radians. The angle
	 * will be between [0...PI].
	 * 
	 * @param otherLine
	 *            The other line
	 * @return The angle between the two lines, in radians, value in [0...PI].
	 */
	public double getAngleBetweenInRadians(ConstrainableLine otherLine) {
		this.setOrientation(otherLine);
		otherLine.setOrientation(this);
		
		double angle = Math.abs(this.getAngleInRadians()
		                        - otherLine.getAngleInRadians());
		// never < 0 since we ABS it
		while (angle > Math.PI) {
			angle -= 2 * Math.PI;
		}
		return Math.abs(angle);
	}
	

	/**
	 * Get the angle between this line and the other line in degrees. The angle
	 * will be between [0...180].
	 * 
	 * @param otherLine
	 *            The other line
	 * @return The angle between the two lines, in degrees, value in [0...180]
	 */
	public double getAngleBetweenInDegrees(ConstrainableLine otherLine) {
		this.setOrientation(otherLine);
		otherLine.setOrientation(this);
		
		double angle = Math.abs(this.getAngleInDegrees()
		                        - otherLine.getAngleInDegrees());
		// never < 0 since we ABS it
		while (angle > 180) {
			angle -= 360;
		}
		return Math.abs(angle);
	}
	

	/**
	 * Get the {@link Line2D.Double} representation of this line. The returned
	 * line object extends from {@link #getEnd1()} to {@link #getEnd2()}.
	 * 
	 * @return The {@link Line2D.Double} representation of this line.
	 */
	public Line2D.Double getLine2DDouble() {
		return new Line2D.Double(getEnd1().getX(), getEnd1().getY(), getEnd2()
		        .getX(), getEnd2().getY());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstrainable#getBoundingBox()
	 */
	public BoundingBox getBoundingBox() {
		if (m_boundingBox == null) {
			double minx = Math.min(m_end1.getX(), m_end2.getX());
			double miny = Math.min(m_end1.getY(), m_end2.getY());
			double maxx = Math.max(m_end1.getX(), m_end2.getX());
			double maxy = Math.max(m_end1.getY(), m_end2.getY());
			
			m_boundingBox = new BoundingBox(minx, miny, maxx, maxy);
		}
		
		return m_boundingBox;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstrainable#getShapeType()
	 */
	public String getShapeType() {
		return LINE_SHAPE_TYPE;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ConstrainableLine( from " + m_end1 + " to " + m_end2 + ")";
	}
	

	/**
	 * Compute the length of this line--distance between endpoints
	 * 
	 * @return The length of this line.
	 */
	public double getPixelLength() {
		return m_end1.distance(m_end2);
	}
	

	/**
	 * Set the first point of the line
	 * 
	 * @param end
	 *            Alias of the end point
	 */
	public void setFirstPoint(String end) {
		if (end.equalsIgnoreCase("End1")) {
			m_first = m_end1;
			m_last = m_end2;
		}
		else {
			m_first = m_end2;
			m_last = m_end1;
			
		}
		
	}
	

	/**
	 * Sets the orientation of the line
	 * 
	 * @param referenceLine
	 *            Line to examine when setting the line's orientation
	 */
	public void setOrientation(ConstrainableLine referenceLine) {
		if (Line2D.Double.ptLineDist(referenceLine.getEnd1().getX(),
		        referenceLine.getEnd1().getY(), referenceLine.getEnd2().getX(),
		        referenceLine.getEnd2().getY(), this.getEnd1().getX(), this
		                .getEnd1().getY()) > Line2D.Double.ptLineDist(
		        referenceLine.getEnd1().getX(), referenceLine.getEnd1().getY(),
		        referenceLine.getEnd2().getX(), referenceLine.getEnd2().getY(),
		        this.getEnd2().getX(), this.getEnd2().getY())) {
			this.setFirstPoint("End2");
		}
		else if (Line2D.Double.ptLineDist(referenceLine.getEnd1().getX(),
		        referenceLine.getEnd1().getY(), referenceLine.getEnd2().getX(),
		        referenceLine.getEnd2().getY(), this.getEnd1().getX(), this
		                .getEnd1().getY()) < Line2D.Double.ptLineDist(
		        referenceLine.getEnd1().getX(), referenceLine.getEnd1().getY(),
		        referenceLine.getEnd2().getX(), referenceLine.getEnd2().getY(),
		        this.getEnd2().getX(), this.getEnd2().getY())) {
			this.setFirstPoint("End1");
		}
		else if (Line2D.Double.ptSegDist(referenceLine.getEnd1().getX(),
		        referenceLine.getEnd1().getY(), referenceLine.getEnd2().getX(),
		        referenceLine.getEnd2().getY(), this.getEnd1().getX(), this
		                .getEnd1().getY()) > Line2D.Double.ptSegDist(
		        referenceLine.getEnd1().getX(), referenceLine.getEnd1().getY(),
		        referenceLine.getEnd2().getX(), referenceLine.getEnd2().getY(),
		        this.getEnd2().getX(), this.getEnd2().getY())) {
			this.setFirstPoint("End2");
		}
		else {
			this.setFirstPoint("End1");
		}
	}
}
