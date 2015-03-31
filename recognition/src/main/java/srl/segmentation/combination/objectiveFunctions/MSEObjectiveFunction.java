package srl.segmentation.combination.objectiveFunctions;

import java.util.Collections;
import java.util.List;

import org.openawt.geom.Line2D;
import org.openawt.geom.Rectangle2D;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;
import srl.math.LeastSquares;
import srl.math.PerpendicularBisector;
import Jama.Matrix;

/**
 * Objective function for the FSS Combination algorithm that uses a mean-squared
 * error line and arc fit
 * 
 * @author awolin
 */
public class MSEObjectiveFunction implements IObjectiveFunction {

	/**
	 * Threshold for line tests
	 */
	private static final double S_LINETHRESHOLD = 0.95;

	/**
	 * Default constructor
	 */
	public MSEObjectiveFunction() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.segmentation.combination.objectiveFunctions.IObjectiveFunction
	 * #solve(java.util.List, edu.tamu.core.sketch.Stroke)
	 */
	public double solveCurve(Stroke stroke) {

		double totalError = 0.0;

		List<Point> actualSegment = stroke.getPoints();

		double err = curveFitError(actualSegment, 3);
		totalError += err;

		totalError = totalError / stroke.getPathLength();

		return totalError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.segmentation.combination.objectiveFunctions.IObjectiveFunction
	 * #solve(java.util.List, edu.tamu.core.sketch.Stroke)
	 */
	public double solve(List<Integer> corners, Stroke stroke) {

		Collections.sort(corners);
		double[] pathLengths = calcPathLengths(stroke);
		double totalError = 0.0;

		for (int c = 1; c < corners.size(); c++) {

			List<Point> actualSegment = stroke.getPoints().subList(
					corners.get(c - 1), corners.get(c));

			double err = 0.0;

			if (isLine(corners.get(c - 1), corners.get(c), stroke, pathLengths,
					S_LINETHRESHOLD)) {

				Point corner1 = stroke.getPoint(corners.get(c - 1));
				Point corner2 = stroke.getPoint(corners.get(c));

				Line2D.Double optimalLine = new Line2D.Double();
				optimalLine.setLine(corner1.getX(), corner1.getY(),
						corner2.getX(), corner2.getY());

				double lineErr = LeastSquares.error(actualSegment, optimalLine);

				err = lineErr;
			} else {

				double curveErr = curveFitError(actualSegment, 4);
				double arcErr = arcOrthogonalDistanceSquared(actualSegment);

				err = Math.min(curveErr, arcErr);
			}

			totalError += err;
		}

		totalError = totalError / stroke.getNumPoints();

		return totalError;
	}

	/**
	 * Calculate the path lengths at each point, mainly for use in the
	 * {@link #isLine(Stroke, int, int, double[])} test.
	 * 
	 * @param stroke
	 *            Stroke to calculate the path lengths for
	 * @return An array of doubles corresponding to points in the stroke, where
	 *         each double is the path length of the stroke up to the point in
	 *         that array's index
	 */
	private double[] calcPathLengths(Stroke stroke) {

		double[] pathLengths = new double[stroke.getNumPoints()];
		pathLengths[0] = 0.0;

		for (int i = 1; i < stroke.getNumPoints(); i++) {
			pathLengths[i] = pathLengths[i - 1]
					+ stroke.getPoint(i - 1).distance(stroke.getPoint(i));
		}

		return pathLengths;
	}

	/**
	 * Check to see if the stroke segment between two points is a line
	 * 
	 * @param p1
	 *            Index for point 1
	 * @param p2
	 *            Index for point 2
	 * @param stroke
	 *            Stroke that contains the segment
	 * @param pathLengths
	 *            Path length array storing the total stroke path length at each
	 *            index of the point
	 * @param lineVsArcThreshold
	 *            The threshold for the straight-line distance / path distance
	 * @return True if the segment of the stroke is a line, false otherwise
	 */
	private boolean isLine(int p1, int p2, Stroke stroke, double[] pathLengths,
			final double lineVsArcThreshold) {

		// Local thresholds that shouldn't need changing.
		// Correspond to the size of the segment
		final double sizeThreshold = 10;
		final double pointThreshold = 5;

		Point pt1 = stroke.getPoint(p1);
		Point pt2 = stroke.getPoint(p2);

		double straightLineDistance = pt1.distance(pt2);
		double pathDistance = pathLengths[p2] - pathLengths[p1];

		double lengthRatio = straightLineDistance / pathDistance;

		// Check whether the straight/path ratio is greater than a threshold
		if (lengthRatio > lineVsArcThreshold || pathDistance < sizeThreshold
				|| p2 - p1 < pointThreshold) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Find the curve parameters for the stroke
	 * 
	 * @param points
	 *            Points to fit a curve to
	 * @param k
	 *            Degree of the curve
	 * @return Curve parameters
	 * @throws java.lang.RuntimeException
	 *             Thrown if the matrix is singular
	 */
	private Matrix curveFitParameters(List<Point> points, int k)
			throws java.lang.RuntimeException {

		double[] x = new double[points.size()];
		double[] y = new double[points.size()];

		for (int i = 0; i < points.size(); i++) {
			x[i] = points.get(i).getX();
			y[i] = points.get(i).getY();
		}

		Matrix X = new Matrix(x.length, k + 1);
		for (int r = 0; r < X.getRowDimension(); r++) {
			for (int c = 0; c < X.getColumnDimension(); c++) {
				X.set(r, c, Math.pow(x[r], c));
			}
		}

		Matrix Y = new Matrix(y.length, 1);
		for (int r = 0; r < Y.getRowDimension(); r++) {
			Y.set(r, 0, y[r]);
		}

		Matrix XTrans = X.transpose();

		Matrix A = ((XTrans.times(X)).inverse()).times(XTrans).times(Y);

		return A;
	}

	/**
	 * Find the y value for a given x value and a matrix representing a curve
	 * fit
	 * 
	 * @param x
	 *            x-coordinate value
	 * @param A
	 *            Matrix representation of a curve
	 * @return y-coordinate value in the curve
	 */
	private double curveFitY(double x, Matrix A) {

		int k = A.getRowDimension();

		double y = 0.0;
		for (int i = 0; i < k; i++) {
			y += A.get(i, 0) * Math.pow(x, i);
		}

		return y;
	}

	/**
	 * MSE for curves
	 * 
	 * @param points
	 *            Points to find the MSE error for
	 * @param k
	 *            Degree of the curve
	 * @return Error value
	 */
	private double curveFitError(List<Point> points, int k) {

		try {
			Matrix A = curveFitParameters(points, k);
			double error = 0.0;

			for (Point p : points) {
				double aY = p.getY();
				double cY = curveFitY(p.getX(), A);

				error += Math.abs(aY - cY);
			}

			return error;
		} catch (RuntimeException e) {
			return Double.MAX_VALUE;
		}
	}

	/**
	 * Compute the ODSQ of an arc using the original stroke and an optimal line
	 * between two points
	 * 
	 * ODSQ = orthogonal distance squared
	 * 
	 * @param pts
	 *            Points of the stroke
	 * @param p1
	 *            End point one of the line (corner 1)
	 * @param p2
	 *            End point two of the line (corner 2)
	 * @return The ODSQ value for the segment
	 */
	private double arcOrthogonalDistanceSquared(List<Point> pts) {

		double error = 0.0;

		Point p1 = pts.get(0);
		Point p2 = pts.get(pts.size() - 1);

		double minX = Double.MAX_VALUE;
		double maxX = 0.0;
		double minY = Double.MAX_VALUE;
		double maxY = 0.0;

		for (Point p : pts) {
			if (p.getX() < minX)
				minX = p.getX();
			if (p.getX() > maxX)
				maxX = p.getX();
			if (p.getY() < minY)
				minY = p.getY();
			if (p.getY() > maxY)
				maxY = p.getY();
		}
		Rectangle2D.Double bounds = new Rectangle2D.Double(minX, minY, maxX,
				maxY);

		// Set the line between the corners
		PerpendicularBisector perpBisector = new PerpendicularBisector(p1, p2,
				bounds);

		Point midPoint = new Point(perpBisector.getMidPoint().getX(),
				perpBisector.getMidPoint().getY());

		double slope = perpBisector.getBisectorSlope();
		double yInt = perpBisector.getBisectorYIntercept();

		Point pbPt1;
		Point pbPt2;

		// Convoluted way to ensure that our perpendicular bisector line will
		// definitely go through our stroke
		// It's long because there are many checks
		// TODO: Get rid of magic numbers
		if (Double.isInfinite(slope)) {
			pbPt1 = new Point(midPoint.getX(), 0.0);
			pbPt2 = new Point(midPoint.getX(), 10000.0);
		} else if (slope == 0.0) {
			pbPt1 = new Point(0, yInt);
			pbPt2 = new Point(10000.0, yInt);
		} else {
			if (yInt < 0.0) {
				pbPt1 = new Point((-yInt / slope), 0.0);
				pbPt2 = new Point((10000.0 - yInt) / slope, 10000.0);
			} else {
				double xInt = -yInt / slope;

				if (xInt < 0.0) {
					pbPt2 = new Point(0, yInt);
					pbPt1 = new Point(10000.0, (10000.0 * slope) + yInt);
				} else {
					pbPt1 = new Point(xInt, 0.0);
					pbPt2 = new Point(0, yInt);
				}
			}
		}

		Point p3 = midPoint;

		// Get a third point that intersects the _stroke_ around its midpoint
		for (int i = 0; i < pts.size() - 2; i += 2) {
			if (isIntersection(pts.get(i), pts.get(i + 2), pbPt1, pbPt2)) {
				double newX = (pts.get(i + 2).getX() + pts.get(i).getX()) / 2.0;
				double newY = (pts.get(i + 2).getY() + pts.get(i).getY()) / 2.0;
				p3 = new Point(newX, newY);

				i = pts.size();
			}
		}

		// http://mcraefamily.com/MathHelp/
		// GeometryConicSectionCircleEquationGivenThreePoints.htm
		double a = p1.getX();
		double b = p1.getY();
		double c = p3.getX();
		double d = p3.getY();
		double e = p2.getX();
		double f = p2.getY();

		double k = (0.5 * ((((a * a) + (b * b)) * (e - c))
				+ (((c * c) + (d * d)) * (a - e)) + (((e * e) + (f * f)) * (c - a))))
				/ ((b * (e - c)) + (d * (a - e)) + (f * (c - a)));

		double h = (0.5 * ((((a * a) + (b * b)) * (f - d))
				+ (((c * c) + (d * d)) * (b - f)) + (((e * e) + (f * f)) * (d - b))))
				/ ((a * (f - d)) + (c * (b - f)) + (e * (d - b)));

		// If we're actually a line
		if (Double.isInfinite(k) || Double.isInfinite(h) || Double.isNaN(k)
				|| Double.isNaN(h)) {

			return Double.MAX_VALUE;
		}

		// Set the circle's center and radius
		Point center = new Point(h, k);
		double radius = Math.sqrt(((a - h) * (a - h)) + ((b - k) * (b - k)));

		// Get the orthogonal distance between each point in the stroke and the
		// line
		for (int i = 0; i < pts.size(); i++) {
			double euc = pts.get(i).distance(center);
			double dist = radius - euc;

			error += Math.abs(dist);
		}

		return error;
	}

	/**
	 * http://www.mema.ucl.ac.be/~wu/FSA2716/Exercise1.htm
	 * 
	 * @param pt1
	 *            Endpoint 1 for line 1
	 * @param pt2
	 *            Endpoint 2 for line 1
	 * @param pt3
	 *            Endpoint 1 for line 2
	 * @param pt4
	 *            Endpoint 2 for line 2
	 * @return Whether the two lines intersect
	 */
	private boolean isIntersection(Point pt1, Point pt2, Point pt3, Point pt4) {
		// A
		double Ax = pt1.getX();
		double Ay = pt1.getY();

		// B
		double Bx = pt2.getX();
		double By = pt2.getY();

		// C
		double Cx = pt3.getX();
		double Cy = pt3.getY();

		// D
		double Dx = pt4.getX();
		double Dy = pt4.getY();

		double denom = (((Bx - Ax) * (Dy - Cy)) - ((By - Ay) * (Dx - Cx)));

		// AB and CD are parallel
		if (denom == 0.0)
			return false;

		double numR = (((Ay - Cy) * (Dx - Cx)) - ((Ax - Cx) * (Dy - Cy)));
		double r = numR / denom;

		double numS = (((Ay - Cy) * (Bx - Ax)) - ((Ax - Cx) * (By - Ay)));
		double s = numS / denom;

		// An intersection exists
		if (r >= 0.0 && r <= 1.0 && s >= 0.0 && s <= 1.0)
			return true;

		return false;
	}

}
