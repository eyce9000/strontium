/**
 * BoundingBox.java
 * 
 * Revision History:<br>
 * (5/23/08) bpaulson - class created<br>
 * (5/24/08) jbjohns - Default and Parameter constructors<Br>
 * (6/08/08) jbjohns - Add helper method to get the center of the bounding box <br>
 * (7/03/08) bpaulson - Added method to get the distance to the bounding box
 * from a given point <br>
 * 16 July 2008 : jbjohns : remove todo, simplify getCenterPoint, contract and
 * expand methods, toString <br>
 * 23 July 2008 : jbjohns : distance to point returns 0 if pt is inside,
 * distance to line and other bounding boxes, getTopSegment and other methods
 * and refactoring other methods to use them <br>
 * 18 Sept 2008 : jbjohns : get all the different points (topLeft, topRight,
 * etc) on the edge of the bounding box <br>
 * 
 * <p>
 * 
 * <pre>
 *    This work is released under the BSD License:
 *    (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 *    All rights reserved.
 *    
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *        * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University
 *          nor the names of its contributors may be used to endorse or promote
 *          products derived from this software without specific prior written
 *          permission.
 *    
 *    THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *    DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 *    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package srl.core.sketch;

import java.util.List;

import org.openawt.geom.Line2D;
import org.openawt.geom.Point2D;
import org.openawt.geom.Rectangle2D;

import srl.math.linear.LineComputations;



/**
 * Extension of Rectangle2D but with added functionality to make it more of a
 * bounding box and less of a stupid rectangle.
 * 
 * @author bpaulson
 * 
 */
public class BoundingBox extends Rectangle2D.Double implements
		Comparable<BoundingBox>, Cloneable {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = -4471373493020082765L;

	/**
	 * Construct a default bounding box at point (0, 0) with width and height
	 * both == 0.
	 */
	public BoundingBox() {
		super();
	}

	/**
	 * Construct a bounding box with location set to (minX, minY), width set to
	 * (maxX-minX), and height set to (maxY-minY).
	 * 
	 * @param minX
	 *            x-coordinate of the location.
	 * @param minY
	 *            y-coordinate of the location.
	 * @param maxX
	 *            x-coordinate to determine rectangle width.
	 * @param maxY
	 *            y-coordinate to determine rectangle height.
	 */
	public BoundingBox(double minX, double minY, double maxX, double maxY) {
		super();

		double x = Math.min(minX, maxX);
		double y = Math.min(minY, maxY);
		double width = Math.abs(maxX - minX);
		double height = Math.abs(maxY - minY);

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Construct a bounding box for the given strokes.
	 * 
	 * @param strokes
	 *            the strokes to get the bounding box for.
	 */
	public BoundingBox(List<Stroke> strokes) {
		super();

		double minX = java.lang.Double.POSITIVE_INFINITY;
		double maxX = java.lang.Double.NEGATIVE_INFINITY;
		double minY = java.lang.Double.POSITIVE_INFINITY;
		double maxY = java.lang.Double.NEGATIVE_INFINITY;

		for (Stroke stroke : strokes) {
			if (stroke.getBoundingBox().getMinX() < minX) {
				minX = stroke.getBoundingBox().getMinX();
			}
			if (stroke.getBoundingBox().getMaxX() > maxX) {
				maxX = stroke.getBoundingBox().getMaxX();
			}
			if (stroke.getBoundingBox().getMinY() < minY) {
				minY = stroke.getBoundingBox().getMinY();
			}
			if (stroke.getBoundingBox().getMaxY() > maxY) {
				maxY = stroke.getBoundingBox().getMaxY();
			}

		}

		double x = Math.min(minX, maxX);
		double y = Math.min(minY, maxY);
		double width = Math.abs(maxX - minX);
		double height = Math.abs(maxY - minY);

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Get the maximum X value.
	 * 
	 * @return the maximum X value.
	 */
	public double getMaxX() {
		return x + width;
	}

	/**
	 * Get the maximum Y value.
	 * 
	 * @return the maximum Y value.
	 */
	public double getMaxY() {
		return y + height;
	}

	/**
	 * Get the minimum X value.
	 * 
	 * @return the minimum X value.
	 */
	public double getMinX() {
		return x;
	}

	/**
	 * Get the minimum Y value.
	 * 
	 * @return the minimum Y value.
	 */
	public double getMinY() {
		return y;
	}

	/**
	 * Get the area of the bounding box.
	 * 
	 * @return the area of the bounding box.
	 */
	public double getArea() {
		return width * height;
	}

	/**
	 * Get the perimeter of the bounding box.
	 * 
	 * @return the perimeter of the bounding box.
	 */
	public double getPerimeter() {
		return 2 * width + 2 * height;
	}

	/**
	 * Get the line segment along the top edge of this box.
	 * 
	 * @return the segment along the top edge of this box.
	 */
	public Line2D getTopSegment() {
		return new Line2D.Double(getMinX(), getMinY(), getMaxX(), getMinY());
	}

	/**
	 * Get the y value for the top of the box.
	 * 
	 * @return the y value for the top of the box.
	 */
	public double getTop() {
		return getMinY();
	}

	/**
	 * Get the y value for the bottom of the box.
	 * 
	 * @return the y value for the bottom of the box.
	 */
	public double getBottom() {
		return getMaxY();
	}

	/**
	 * Get the x value for the left of the box.
	 * 
	 * @return the x value for the left of the box.
	 */
	public double getLeft() {
		return getMinX();
	}

	/**
	 * Get the x value for the right of the box.
	 * 
	 * @return the x value for the right of the box.
	 */
	public double getRight() {
		return getMaxX();
	}

	/**
	 * This bounding box is above the given {@code y} value
	 * 
	 * @param y
	 *            the y value to compare against.
	 * @return {@code true} if the bottom edge of the bounding box is above the
	 *         given {@code y} value; {@code false} otherwise.
	 */
	public boolean isAbove(double y) {
		return getBottom() < y;
	}

	/**
	 * This bounding box is below the given {@code y} value.
	 * 
	 * @param y
	 *            the y value to compare against.
	 * @return {@code true} if the top edge of the bounding box is below the
	 *         given {@code y} value; {@code false} otherwise.
	 */
	public boolean isBelow(double y) {
		return getTop() > y;
	}

	/**
	 * This bounding box is to the left of the given {@code x} value.
	 * 
	 * @param x
	 *            the x value to compare against.
	 * @return {@code true} if the right edge of the bounding box is to the left
	 *         of the given {@code x} value; {@code false} otherwise.
	 */
	public boolean isLeftOf(double x) {
		return getRight() < x;
	}

	/**
	 * This bounding box is to the right of the given {@code x} value.
	 * 
	 * @param x
	 *            the x value to compare against.
	 * @return {@code true} if the left edge of the bounding box is to the right
	 *         of the given {@code x} value; {@code false} otherwise.
	 */
	public boolean isRightOf(double x) {
		return getLeft() > x;
	}

	/**
	 * Get the line segment along the bottom edge of this box.
	 * 
	 * @return The segment along the bottom edge of this box.
	 */
	public Line2D getBottomSegment() {
		return new Line2D.Double(getMinX(), getMaxY(), getMaxX(), getMaxY());
	}

	/**
	 * Get the line segment along the left edge of this box.
	 * 
	 * @return The segment along the left edge of this box.
	 */
	public Line2D getLeftSegment() {
		return new Line2D.Double(getMinX(), getMinY(), getMinX(), getMaxY());
	}

	/**
	 * Get the line segment along the right edge of this box.
	 * 
	 * @return The segment along the right edge of this box.
	 */
	public Line2D getRightSegment() {
		return new Line2D.Double(getMaxX(), getMinY(), getMaxX(), getMaxY());
	}

	/**
	 * Get the distance of a point {@code (x, y)} to the bounding box. If this
	 * bounding box contains the point, return 0.
	 * 
	 * @param x
	 *            point's x value.
	 * @param y
	 *            point's y value.
	 * @return distance between point and bounding box.
	 */
	public double distance(double x, double y) {
		if (this.contains(x, y)) {
			return 0;
		}

		// Line2D l1 = new Line2D.Double(getMinX(), getMinY(), getMaxX(),
		// getMinY());
		// Line2D l2 = new Line2D.Double(getMaxX(), getMinY(), getMaxX(),
		// getMaxY());
		// Line2D l3 = new Line2D.Double(getMaxX(), getMaxY(), getMinX(),
		// getMaxY());
		// Line2D l4 = new Line2D.Double(getMinX(), getMaxY(), getMinX(),
		// getMinY());
		// double minDistance = l1.ptSegDist(x, y);
		// if (l2.ptSegDist(x, y) < minDistance)
		// minDistance = l2.ptSegDist(x, y);
		// if (l3.ptSegDist(x, y) < minDistance)
		// minDistance = l3.ptSegDist(x, y);
		// if (l4.ptSegDist(x, y) < minDistance)
		// minDistance = l4.ptSegDist(x, y);

		double topDistance = getTopSegment().ptSegDist(x, y);
		double bottomDistance = getBottomSegment().ptSegDist(x, y);
		double leftDistance = getLeftSegment().ptSegDist(x, y);
		double rightDistance = getRightSegment().ptSegDist(x, y);

		double minDist = Math.min(Math.min(topDistance, bottomDistance),
				Math.min(leftDistance, rightDistance));

		return minDist;
	}

	/**
	 * Compute the minimum distance between the given segment and the line.
	 * 
	 * @param line
	 *            the line to compute the min distance from the bounding box.
	 * @return the min distance between the given line and the bounding box.
	 */
	public double distance(Line2D line) {

		// is the line fully contained?
		if (this.contains(line.getP1()) && this.contains(line.getP2())) {
			return 0;
		}

		// 4 segments that define the bounding box
		Line2D topLine = getTopSegment();
		Line2D bottomLine = getBottomSegment();
		Line2D leftLine = getLeftSegment();
		Line2D rightLine = getRightSegment();

		// distance from the given segment to the 4 segments of the bounding box
		double topDist = LineComputations.minimumDistanceBetweenSegments(line,
				topLine);
		double bottomDist = LineComputations.minimumDistanceBetweenSegments(
				line, bottomLine);
		double leftDist = LineComputations.minimumDistanceBetweenSegments(line,
				leftLine);
		double rightDist = LineComputations.minimumDistanceBetweenSegments(
				line, rightLine);

		double minDist = Math.min(Math.min(topDist, bottomDist),
				Math.min(leftDist, rightDist));
		// return the minimum distance
		return minDist;
	}

	/**
	 * The minimum distance between this bounding box and the other bounding
	 * box. If the bounding boxes overlap, or if one bounding box contains the
	 * other, the distance returned is 0.
	 * 
	 * @param other
	 *            the other bounding box.
	 * @return the minimum distance between the boundign boxes.
	 */
	public double distance(BoundingBox other) {

		// have to use java.lang since Double is a subtype for Rectangle2D
		// The compiler confuses them unless we're explicit that we want lang
		double dist = java.lang.Double.POSITIVE_INFINITY;

		// are they contained within each other or do they overlap
		if (this.contains(other) || other.contains(this)
				|| this.intersects(other)) {
			dist = 0;
		}
		// we'll consider this as the center of the universe and move other
		// around it to figure out which distances we want to compute. A fixed
		// point frame of reference is easier to wrap the old brain around.
		// Since we know the boxes are not in each other, and the rectangles
		// do not intersect, other must lie some distance away from this.
		// Determine which direction, first. And then how far.
		else {
			// not contains and not intersects

			// this below other's bottom, dist from our top to other's bottom
			if (this.isBelow(other.getBottom())) {
				double tempDist = LineComputations
						.minimumDistanceBetweenSegments(this.getTopSegment(),
								other.getBottomSegment());
				dist = (tempDist < dist) ? tempDist : dist;
			} else
			// this above other's top, dist from our bottom to other's top
			if (this.isAbove(other.getTop())) {
				double tempDist = LineComputations
						.minimumDistanceBetweenSegments(
								this.getBottomSegment(), other.getTopSegment());
				dist = (tempDist < dist) ? tempDist : dist;
			} else
			// this is left of other, dist from this right to other left
			if (this.isLeftOf(other.getLeft())) {
				double tempDist = LineComputations
						.minimumDistanceBetweenSegments(this.getRightSegment(),
								other.getLeftSegment());
				dist = (tempDist < dist) ? tempDist : dist;
			} else
			// this is right of other, dist from this left to other right
			if (this.isRightOf(other.getRight())) {
				double tempDist = LineComputations
						.minimumDistanceBetweenSegments(this.getLeftSegment(),
								other.getRightSegment());
				dist = (tempDist < dist) ? tempDist : dist;
			} else
				dist = 0;
		}

		return dist;
	}

	/**
	 * Get the percent of the bounding box that is contained within the other
	 * bounding box. If this bounding box is outside of other's bounding box
	 * then we return 0.0. If it is fully contained within other's bounding box
	 * then we return 1.0. If it is partially contained in other's bounding box
	 * then we return a value between 0.0 and 1.0 denoting the percentage of the
	 * bounding box that is contained within other's bounding box.
	 * 
	 * @param other
	 *            bounding box of other shape
	 * @return percentage of bounding box that is contained within other's
	 *         bounding box
	 */
	public double getPercentContained(BoundingBox other) {
		if (other.contains(this))
			return 1.0;
		if (!other.intersects(this))
			return 0.0;
		Rectangle2D intersection = new Rectangle2D.Double();
		Rectangle2D.intersect(this, other, intersection);
		double area = intersection.getWidth() * intersection.getHeight();
		return area / getArea();
	}

	/**
	 * Get the length of the diagonal of the bounding box.
	 * 
	 * @return length of the diagonal of the bounding box.
	 */
	public double getDiagonalLength() {
		return Math.sqrt(width * width + height * height);
	}

	/**
	 * Get the angle of the diagonal of the bounding box, i.e. the angle between
	 * the line along the bottom of the bounding box and the line through the
	 * bottom left and top right corners of the box.
	 * 
	 * @return angle of the diagonal of the bounding box.
	 */
	public double getDiagonalAngle() {
		return Math.atan2(height, width);
	}

	/**
	 * Compare two bounding boxes based on area, & get difference between areas
	 * of the boxes (for sorting based on size of bounding box)
	 * 
	 * @param bb
	 *            bounding box to compare to.
	 * @return difference in area: area of this bounding box minus area of bb
	 *         (signed value, can be negative).
	 */
	public int compareTo(BoundingBox bb) {
		return (int) (this.getArea() - bb.getArea());
	}

	/**
	 * Determine if two bounding boxes are equal (are same size & in the same
	 * place).
	 * 
	 * @param bb
	 *            bounding box to compare to.
	 * @return {@code true} if the same; else {@code false}.
	 */
	public boolean equals(BoundingBox bb) {
		if ((this == bb)
				|| (width == bb.width && height == bb.height && x == bb.x && y == bb.y))
			return true;
		else
			return false;
	}

	/**
	 * Get an IPOint that corresponds to the center of this bounding box. The
	 * only thing that will be stored in this Point are X and Y values. Thus,
	 * you should /only/ use the interface's getX() and getY() methods, and
	 * should not cast the point to a concrete implementation. Time of the point
	 * is set to 0.
	 * 
	 * @return the Point with X and Y coordinates set to the center of this
	 *         bounding box.
	 */
	public Point getCenterPoint() {
		return new Point(getCenterX(), getCenterY());
	}

	/**
	 * Get the Point corresponding to the top-left corner of this bounding box.
	 * This method assumes screen coordinates.
	 * 
	 * @return the top left point of this bounding box.
	 */
	public Point getTopLeftPoint() {
		return new Point(getLeft(), getTop());
	}

	/**
	 * Get the Point corresponding to the top-center point on this bounding box
	 * (the midpoint of {@link #getTopSegment()}.
	 * 
	 * @return the top-center point of this bounding box, or center point on the
	 *         top edge of this bounding box.
	 */
	public Point getTopCenterPoint() {
		return new Point(getCenterX(), getTop());
	}

	/**
	 * Get the Point corresponding to the top-right corner of this bounding box.
	 * 
	 * @return The top-right corner of this bounding box.
	 */
	public Point getTopRightPoint() {
		return new Point(getRight(), getTop());
	}

	/**
	 * Get the Point corresponding to the center of the left edge of this
	 * bounding box.
	 * 
	 * @return the Point that is the center of the left edge of this bounding
	 *         box.
	 */
	public Point getCenterLeftPoint() {
		return new Point(getLeft(), getCenterY());
	}

	/**
	 * Get the Point corresponding to the center point of the right edge of this
	 * bounding box.
	 * 
	 * @return the point that is the center of the right edge of this bounding
	 *         box.
	 */
	public Point getCenterRightPoint() {
		return new Point(getRight(), getCenterY());
	}

	/**
	 * Get the Point corresponding to the bottom left corner of this bounding
	 * box.
	 * 
	 * @return the point that is the bottom left corner of this bounding box.
	 */
	public Point getBottomLeftPoint() {
		return new Point(getLeft(), getBottom());
	}

	/**
	 * Get the Point corresponding to the center point on the bottom edge of
	 * this bounding box.
	 * 
	 * @return the point that is the center of the bottom edge of this bounding
	 *         box.
	 */
	public Point getBottomCenterPoint() {
		return new Point(getCenterX(), getBottom());
	}

	/**
	 * Get the Point corresponding to the bottom right corner of this bounding
	 * box.
	 * 
	 * @return the point that is the bottom right corner of this bounding box.
	 */
	public Point getBottomRightPoint() {
		return new Point(getRight(), getBottom());
	}

	/**
	 * Make a bigger bounding box that is {@code amt} larger in all 4
	 * directions. So the net change in x and y directions is both 2 {@code amt}
	 * . {@code amt} is not allowed to be negative and uses the absolute value.
	 * 
	 * @param amt
	 *            the amount to expand ({@code minX} - {@code amt}, {@code maxX}
	 *            + {@code amt}, etc.).
	 * @return the expanded bounding box.
	 */
	public BoundingBox expand(double amt) {
		double a = Math.abs(amt);
		return new BoundingBox(getMinX() - a, getMinY() - a, getMaxX() + a,
				getMaxY() + a);
	}

	/**
	 * Make a bigger bounding box using a multiplicative factor. The center is
	 * still in the same spot {@code newWidth} = getWidth() * {@code factor};
	 * same for height.
	 * 
	 * @param factor
	 *            the amount to expand.
	 * @return the expanded bounding box.
	 */
	public BoundingBox grow(double factor) {
		return new BoundingBox(getMinX() - getWidth() * factor / 2.0, getMinY()
				- getHeight() * factor / 2.0, getMaxX() + getWidth() * factor
				/ 2.0, getMaxY() + getHeight() * factor / 2.0);
	}

	/**
	 * Make a bigger bounding box using a multiplicative factor. The center is
	 * still in the same spot {@code newWidth} = getWidth() * {@code factor}.
	 * 
	 * @param factor
	 *            the amount to expand.
	 * @return the expanded bounding box.
	 */
	public BoundingBox growWidth(double factor) {

		double newMinX = getMinX() - getWidth() * factor / 2.0;
		double newMaxX = getMaxX() + getWidth() * factor / 2.0;

		return new BoundingBox(newMinX, getMinY(), newMaxX, getMaxY());
	}

	/**
	 * Make a bigger bounding box using a multiplicative factor. The center is
	 * still in the same spot {@code newHeight} = getHeight() * {@code factor}.
	 * 
	 * @param factor
	 *            the amount to expand.
	 * @return the expanded bounding box.
	 */
	public BoundingBox growHeight(double factor) {

		double newMinY = getMinY() - getHeight() * factor / 2.0;
		double newMaxY = getMaxY() + getHeight() * factor / 2.0;

		return new BoundingBox(getMinX(), newMinY, getMaxX(), newMaxY);
	}

	/**
	 * Make a smaller bounding box that is {@code amt} smaller in all 4
	 * directions. The net change in x and y directions is 2 * {@code amt}.
	 * {@code amt} is not allowed to be negative, and this method uses the
	 * absolute value. If the bounding box is too small and contracting by
	 * {@code amt} would result in a bounding box of size < 0, this method
	 * returns a bounding box of size 0 at the center of this bounding box.
	 * 
	 * @param amt
	 *            the amount to contract by (> 0).
	 * @return the contracted bounding box.
	 */
	public BoundingBox contract(double amt) {
		double a = Math.abs(amt);

		BoundingBox ret = null;

		// too small to shrink
		if (this.height < 2 * a || this.width < 2 * a) {
			ret = new BoundingBox(this.getCenterX(), this.getCenterY(),
					this.getCenterX(), this.getCenterY());
		} else {
			ret = new BoundingBox(this.getMinX() + a, this.getMinY() + a,
					this.getMaxX() - a, this.getMaxY() - a);
		}

		return ret;
	}

	/**
	 * Contains method that takes an Point instead of a Point2D.Double.
	 * 
	 * @param pt
	 *            the Point to check for containment.
	 * @return {@code true} if this BoundingBox contains the given {@code pt},
	 *         {@code false} otherwise.
	 */
	public boolean contains(Point pt) {
		return this.contains(new Point2D.Double(pt.getX(), pt.getY()));
	}

	/**
	 * Checks whether a given Point is within the BoundingBox's minimum and
	 * maximum x-axis parameters.
	 * 
	 * @param pt
	 *            the Point to check for containment.
	 * @return {@code true} if the given {@code pt} is within the BoundingBox's
	 *         width, {@code false} otherwise.
	 */
	public boolean withinWidth(Point pt) {

		if (pt.getX() >= this.x && pt.getX() <= this.x + this.width) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks whether a given Point is within the BoundingBox's minimum and
	 * maximum y-axis parameters.
	 * 
	 * @param pt
	 *            the Point to check for containment.
	 * @return {@code true} if the given {@code pt} is within the BoundingBox's
	 *         height, {@code false} otherwise.
	 */
	public boolean withinHeight(Point pt) {
		if (pt.getY() >= this.y && pt.getY() <= this.y + this.height) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Return a string representation of the bounding box, a set of 2 coordinate
	 * pairs representing min corner and max corner.
	 * 
	 * @return the string representation of the bounding box.
	 */
	public String toString() {
		return "[<" + getMinX() + ", " + getMinY() + ">, <" + getMaxX() + ", "
				+ getMaxY() + ">]";
	}

	public BoundingBox increment() {
		return new BoundingBox(this.getMinX() - 1, this.getMinY() - 1,
				this.getMaxX() + 1, this.getMaxY() + 1);
	}
}
