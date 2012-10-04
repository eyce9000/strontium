/**
 * LeastSquares.java
 * 
 * Revision History:<br>
 * Jun 24, 2008 bpaulson - File created
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

import java.util.List;

import org.openawt.geom.Line2D;
import org.openawt.geom.Point2D;
import org.openawt.geom.Rectangle2D;

import srl.core.sketch.Point;

import Jama.Matrix;

/**
 * Class containing static methods used to conduct least squares tests
 * 
 * @author bpaulson
 */
public class LeastSquares {

	/**
	 * Perform a least squares fit with the input values
	 * 
	 * @param sumX
	 *            sum of the x values
	 * @param sumX2
	 *            sum of the x values squared
	 * @param sumY
	 *            sum of the y values
	 * @param sumXY
	 *            sum of the x*y values
	 * @param n
	 *            number of values
	 * @return 2x1 matrix containing the least squares fit y = a + bx; first
	 *         value in matrix will contain the y-intercept, the second will
	 *         contain the slope
	 */
	public static Matrix fit(double sumX, double sumX2, double sumY,
			double sumXY, int n) {
		Matrix A = new Matrix(2, 2);
		Matrix b = new Matrix(2, 1);
		A.set(0, 0, n);
		A.set(1, 0, sumX);
		A.set(0, 1, sumX);
		A.set(1, 1, sumX2);
		b.set(0, 0, sumY);
		b.set(1, 0, sumXY);
		return A.solve(b);
	}

	/**
	 * Perform a least squares fit with the input values and return a line
	 * within the given bounds
	 * 
	 * @param sumX
	 *            sum of the x values
	 * @param sumX2
	 *            sum of the x values squared
	 * @param sumY
	 *            sum of the y values
	 * @param sumXY
	 *            sum of the x*y values
	 * @param n
	 *            number of values
	 * @param bounds
	 *            rectangular bounds of best fit line
	 * @return best fit line of the least squares fit y = a + bx
	 */
	public static Line2D bestFitLine(double sumX, double sumX2, double sumY,
			double sumXY, int n, Rectangle2D bounds) {
		Matrix result = new Matrix(1, 0);
		result = fit(sumX, sumX2, sumY, sumXY, n);
		double a = result.get(0, 0);
		double b = result.get(1, 0);
		double minX = bounds.getMinX();
		double maxX = bounds.getMaxX();
		double minY = a + b * minX;
		double maxY = a + b * maxX;
		return new Line2D.Double(minX, minY, maxX, maxY);
	}

	/**
	 * Compute the least sqaures solution (linear least squares regression) to
	 * the problem y = x'b. Y is the output, x is the input (including a column
	 * of 1's to accomodate an intercept value), and b is the model that maps
	 * input to output. This method solves for b.
	 * 
	 * @param x
	 *            (n by d+1) matrix for input of n examples of dimensionality d,
	 *            with one column of all ones for an intercept (examples are row
	 *            vectors in the matrix)
	 * @param y
	 *            (n by 1) column vector for outputs/observations, one per
	 *            input.
	 * @return The coefficients b of the model that minimizes the least squared
	 *         error of y = x'b (x' is transpose).
	 */
	public static Matrix bestFitMatrix(Matrix x, Matrix y) {
		if (x == null || y == null) {
			throw new NullPointerException(
					"Input/Output matrices cannot be null");
		}
		Matrix x_transpose = x.transpose();
		Matrix b = (x_transpose.times(x)).inverse().times(x_transpose).times(y);
		return b;
	}

	/**
	 * Return the total least squares error between the array of points and the
	 * input line
	 * 
	 * @param x
	 *            x values
	 * @param y
	 *            y values
	 * @param line
	 *            line to find the LSE to
	 * @return total least squares error between the input points and line
	 */
	public static double error(double[] x, double[] y, Line2D line) {
		double err = 0.0;
		for (int i = 0; i < x.length; i++)
			err += line.ptSegDist(x[i], y[i]);
		return err;
	}

	/**
	 * Return the total least squares error between the array of points and the
	 * input line
	 * 
	 * @param points
	 *            points
	 * @param line
	 *            line to find the LSE to
	 * @return total least squares error between the input points and line
	 */
	public static double error(Point2D[] points, Line2D line) {
		double err = 0.0;
		for (int i = 0; i < points.length; i++)
			err += line.ptSegDist(points[i]);
		return err;
	}

	/**
	 * Return the total least squares error between the array of points and the
	 * input line
	 * 
	 * @param points
	 *            points
	 * @param line
	 *            line to find the LSE to
	 * @return total least squares error between the input points and line
	 */
	public static double error(List<Point> points, Line2D line) {
		double err = 0.0;
		for (int i = 0; i < points.size(); i++)
			err += line.ptSegDist(points.get(i).getX(), points.get(i).getY());
		return err;
	}

	/**
	 * Return the total least squares error squared between the array of points
	 * and the input line
	 * 
	 * @param x
	 *            x values
	 * @param y
	 *            y values
	 * @param line
	 *            line to find the LSE to
	 * @return total least squares error squared between the input points and
	 *         line
	 */
	public static double squaredError(double[] x, double[] y, Line2D line) {
		double err = 0.0;
		for (int i = 0; i < x.length; i++)
			err += line.ptSegDistSq(x[i], y[i]);
		return err;
	}

	/**
	 * Return the total least squares error between the array of points and the
	 * input line
	 * 
	 * @param points
	 *            points
	 * @param line
	 *            line to find the LSE to
	 * @return total least squares error between the input points and line
	 */
	public static double squaredError(List<Point> points, Line2D line) {
		double err = 0.0;
		for (int i = 0; i < points.size(); i++)
			err += line.ptSegDistSq(points.get(i).getX(), points.get(i).getY());
		return err;
	}
}
