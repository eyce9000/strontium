/**
 * Vector2D.java
 * 
 * Revision History:<br>
 * Jul 18, 2008 jbjohns - File created
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
package srl.math.linear;

import org.openawt.geom.Point2D;

/**
 * This class contains the idea of a Vector as used in Linear Algebra for
 * performing geometrical computations on 2D objects.
 * 
 * @author jbjohns
 */
public class Vector2D {

	// ///////////////////////////////////////////
	// /////////////////////////////////////////// PRIVATE MEMBERS
	// ///////////////////////////////////////////

	/**
	 * Movement along the x axis
	 */
	private double m_x;

	/**
	 * Movement along the y axis
	 */
	private double m_y;

	// ///////////////////////////////////////////
	// /////////////////////////////////////////// CONSTRUCTORS
	// ///////////////////////////////////////////

	/**
	 * Create a zero-vector
	 */
	public Vector2D() {
		this(0, 0);
	}

	/**
	 * Create a vector with x and y components set to the same values in the
	 * point
	 * 
	 * @param p
	 *            Point to get the x and y components out of
	 */
	public Vector2D(Point2D p) {
		this(p.getX(), p.getY());
	}

	/**
	 * Create a 2D vector with the given x and y components
	 * 
	 * @param x
	 *            The x component
	 * @param y
	 *            The y component
	 */
	public Vector2D(double x, double y) {
		setX(x);
		setY(y);
	}

	// ///////////////////////////////////////////
	// /////////////////////////////////////////// GET / SET
	// ///////////////////////////////////////////

	/**
	 * Get the x component of the vector
	 * 
	 * @return the x component
	 */
	public double getX() {
		return m_x;
	}

	/**
	 * Get the y component of the vector
	 * 
	 * @return the y component
	 */
	public double getY() {
		return m_y;
	}

	/**
	 * Set the x component of the vector
	 * 
	 * @param x
	 *            the x component
	 */
	public void setX(double x) {
		m_x = x;
	}

	/**
	 * Set the y component of the vector
	 * 
	 * @param y
	 *            the y component
	 */
	public void setY(double y) {
		m_y = y;
	}

	// ///////////////////////////////////////////
	// /////////////////////////////////////////// HELPER METHODS
	// ///////////////////////////////////////////

	/**
	 * Compute the vector difference [this - other], which gives a vector
	 * pointing from other to this.
	 * <p>
	 * Calls {@link #differenceVector(Vector2D, Vector2D)} with v1==this and
	 * v2==other.
	 * 
	 * @param other
	 *            The other vector
	 * @return The vector resulting from the difference [this - other]
	 */
	public Vector2D differenceFrom(Vector2D other) {
		return differenceVector(this, other);
	}

	/**
	 * Add other vector to this vector and return the resultant vector. this +
	 * other. Since addition is commutative, order doesn't matter
	 * 
	 * @param other
	 *            The other vector to add to this vector
	 * @return The resultant sum vector.
	 */
	public Vector2D addTo(Vector2D other) {
		Vector2D ret = new Vector2D();
		ret.setX(this.getX() + other.getX());
		ret.setY(this.getY() + other.getY());
		return ret;
	}

	/**
	 * Calls {@link #dotProduct(Vector2D, Vector2D)} with v1==this, v2==other.
	 * &lt;this * other>. Since dot product is commutative, order doesn't matter
	 * 
	 * @param other
	 *            The other vector to compute the dot product with
	 * @return The scalar dot product value <this * other>
	 */
	public double dotProductWith(Vector2D other) {
		return dotProduct(this, other);
	}

	/**
	 * Multiply this vector by the scalar and return the results in a new vector
	 * 
	 * @param scalar
	 *            Scalar value to multiply this matrix by
	 * @return Resultant matrix
	 */
	public Vector2D scalarMultiply(double scalar) {
		Vector2D ret = new Vector2D();
		ret.setX(this.getX() * scalar);
		ret.setY(this.getY() * scalar);
		return ret;
	}

	/**
	 * Compute the norm (length) of this vector, the square root of the dot
	 * product of this vector with itself: sqrt(&lt;this*this>)
	 * 
	 * @return Norm (length) of this vector
	 */
	public double norm() {
		return Math.sqrt(this.dotProductWith(this));
	}

	/**
	 * Alias for {@link #norm()}
	 * 
	 * @return {@link #norm()}
	 */
	public double length() {
		return norm();
	}

	// ///////////////////////////////////////////
	// /////////////////////////////////////////// STATIC METHODS
	// ///////////////////////////////////////////

	/**
	 * Compute the vector difference [v1 - v2], which gives a vector pointing
	 * from v2 to v1
	 * 
	 * @param v1
	 *            The first vector
	 * @param v2
	 *            The second vector
	 * @return The vector resulting from the difference [v1 - v2]
	 */
	public static Vector2D differenceVector(Vector2D v1, Vector2D v2) {
		return differenceVector(v1.getX(), v1.getY(), v2.getX(), v2.getY());
	}

	/**
	 * Calculate and return the difference vector between the two points, which
	 * points from p2 to p1.
	 * 
	 * @see #differenceVector(double, double, double, double)
	 * @param p1
	 *            The first point
	 * @param p2
	 *            The second point
	 * @return Vector difference between the two points, pointing from second to
	 *         first point
	 */
	public static Vector2D differenceVector(Point2D p1, Point2D p2) {
		return differenceVector(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}

	/**
	 * Calculate and return the vector that is the difference of the two points
	 * represented by the coordinates (x1,y1) and (x2,y2). The resultant vector
	 * points from the second point to the first point.
	 * 
	 * @param x1
	 *            X coord of the first point
	 * @param y1
	 *            Y coord of the first point
	 * @param x2
	 *            X coord of the second point
	 * @param y2
	 *            Y coord of the second point
	 * @return The difference vector between the two points, pointing from the
	 *         second point to the first point
	 */
	public static Vector2D differenceVector(double x1, double y1, double x2,
			double y2) {
		Vector2D ret = new Vector2D();
		ret.setX(x1 - x2);
		ret.setY(y1 - y2);
		return ret;
	}

	/**
	 * Compute the dot product (inner product) between the two vectors
	 * 
	 * @param v1
	 *            First vector
	 * @param v2
	 *            Second vector
	 * @return Scalar value for the dot product
	 */
	public static double dotProduct(Vector2D v1, Vector2D v2) {
		double xProd = v1.getX() * v2.getX();
		double yProd = v1.getY() * v2.getY();
		return (xProd + yProd);
	}
}
