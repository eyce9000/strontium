/**
 * ConstrainablePoint.java
 * 
 * Revision History:<br>
 * Sep 18, 2008 jbjohns - File created
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

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Point;
import srl.core.sketch.Shape;


/**
 * This class represents the implementation of IConstrainable for a single point
 * that will be passed to IConstraint implementations.
 * 
 * @author jbjohns
 */
public class ConstrainablePoint extends AbstractConstrainable {
	
	/**
	 * The type of shape that we're constraining--a point
	 * 
	 * @see ConstrainableFactory#POINT_SHAPE_TYPE
	 */
	public static final String POINT_SHAPE_TYPE = ConstrainableFactory.POINT_SHAPE_TYPE;
	
	/**
	 * The point that is being constrained
	 */
	private Point m_point;
	
	/**
	 * The bounding box for this constrainable shape.
	 */
	private BoundingBox m_boundingBox;
	
	
	/**
	 * Construct a constrainable point to constrain the given point. The given
	 * point cannot be null. Providing the parent shape of this constrainable
	 * point gives context from which the point was extracted. See
	 * {@link IConstrainable#getParentShape()}
	 * 
	 * @see #setPoint(Point)
	 * @param point
	 *            The point to constrain
	 * @param parentShape
	 *            The parent shape from which this point was extracted.
	 */
	public ConstrainablePoint(Point point, Shape parentShape) {
		super(parentShape);
		setPoint(point);
	}
	

	/**
	 * Construct a new constrainable point from the (x,y) coordinates.
	 * 
	 * @param x
	 *            The x value of the point to constrain
	 * @param y
	 *            The y value of the point to constrain
	 * @param parentShape
	 *            The parent shape from which this constrainable point was
	 *            extracted
	 */
	public ConstrainablePoint(double x, double y, Shape parentShape) {
		this(new Point(x, y), parentShape);
	}
	

	/**
	 * Set the point that's being constrained. The reference cannot be null or
	 * will throw a {@link NullPointerException}. This method updates the
	 * bounding box.
	 * 
	 * @param point
	 *            The point that's being constrained.
	 */
	public void setPoint(Point point) {
		if (point != null) {
			m_point = point;
		}
		else {
			throw new NullPointerException(
			        "You must specify an Point, cannot set to null.");
		}
		
		m_boundingBox = new BoundingBox(m_point.getX(), m_point.getY(), m_point
		        .getX(), m_point.getY());
	}
	

	/**
	 * Get the point that's being constrained
	 * 
	 * @return The point that's being constrained.
	 */
	public Point getPoint() {
		return m_point;
	}
	

	/**
	 * Get's the point's x value
	 * 
	 * @see Point#getX()
	 * @return The point's x value
	 */
	public double getX() {
		return m_point.getX();
	}
	

	/**
	 * Gets the point's y value.
	 * 
	 * @see Point#getY()
	 * @return The point's y value.
	 */
	public double getY() {
		return m_point.getY();
	}
	

	/**
	 * Get the point's time value
	 * 
	 * @see Point#getTime()
	 * @return The point's time value
	 */
	public long getTime() {
		return m_point.getTime();
	}
	

	/**
	 * Compute the distance between this point that's being constrained and the
	 * other point that's being constrained.
	 * 
	 * @see Point#distance(Point)
	 * @param otherPoint
	 *            The other point that's being constrained
	 * @return The distance between this point and the other point
	 */
	public double distance(ConstrainablePoint otherPoint) {
		return distance(otherPoint.getPoint());
	}
	

	/**
	 * Compute the distance between this {@link Point} that's being constrained
	 * and the other {@link Point}
	 * 
	 * @param otherPoint
	 *            The other {@link Point}
	 * @return The distance between this point and the other point
	 */
	public double distance(Point otherPoint) {
		return m_point.distance(otherPoint);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstrainable#getBoundingBox()
	 */
	public BoundingBox getBoundingBox() {
		return m_boundingBox;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstrainable#getShapeType()
	 */
	public String getShapeType() {
		return POINT_SHAPE_TYPE;
	}
	

	public String toString() {
		return "ConstrainablePoint(" + m_point + ")";
	}
	
}
