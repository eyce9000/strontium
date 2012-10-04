/**
 * MergeCFSegmenter.java
 * 
 * Revision History:<br>
 * August 27, 2008 awolin - File created
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
package srl.segmentation.combination;

import org.openawt.geom.Line2D;
import org.openawt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import srl.core.exception.InvalidParametersException;
import srl.core.sketch.ISegmenter;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.math.LeastSquares;
import srl.math.PerpendicularBisector;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;
import srl.segmentation.AbstractSegmenter;
import srl.segmentation.sezgin.SezginSegmenter;

import Jama.Matrix;

/**
 * MergeCF segmentation algorithm. This algorithm is built upon many of the
 * features described in Sezgin et al.'s algorithm, and therefore uses many
 * {@link SezginSegmenter} methods. The actual corner detection algorithm uses
 * Paulson's {@link PaleoSketchRecognizer} to computer fit errors, and the
 * fitting of the corners in MergeCF varies heavily from Sezgin's.
 * 
 * @author awolin
 */
public class KDEMergeSegmenter extends AbstractSegmenter implements ISegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "KDEMerge";

	/**
	 * Stroke to segment
	 */
	private Stroke m_stroke;

	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<Segmentation> m_threadedSegmentations = null;

	/**
	 * Configuration file for what primitives we support
	 */
	private PaleoConfig m_paleoConfig;

	/**
	 * Default constructor
	 */
	public KDEMergeSegmenter() {

		// Intialize the Paleo configuration file
		m_paleoConfig = PaleoConfig.allOff();
		m_paleoConfig.setArcTestOn(true);
		m_paleoConfig.setLineTestOn(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.IDebuggableSegmenter#setStroke(org.ladder.core
	 * .sketch.Stroke)
	 */
	@Override
	public void setStroke(Stroke stroke) {
		m_stroke = stroke;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getSegmentations()
	 */
	@Override
	public List<Segmentation> getSegmentations()
			throws InvalidParametersException {

		if (m_stroke == null)
			throw new InvalidParametersException();

		List<Segmentation> segmentations = new ArrayList<Segmentation>();

		KDECombinationSegmenter kdeSegmenter = new KDECombinationSegmenter();
		kdeSegmenter.setStroke(m_stroke);
		List<Segmentation> kdeSegs = kdeSegmenter.getSegmentations();

		List<Integer> corners = getCornersFromSegmentations(kdeSegs);
		List<Integer> mergeCorners = mergeFit(corners);

		// TODO - better confidence
		segmentations = segmentStroke(m_stroke, mergeCorners, S_SEGMENTER_NAME,
				0.80);

		return segmentations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getName()
	 */
	@Override
	public String getName() {
		return S_SEGMENTER_NAME;
	}

	/**
	 * Original merge fit algorithm
	 * 
	 * @param Fc
	 *            Corners found from curvature
	 * @param Fs
	 *            Corners found from speed
	 * @return A list of merged corners
	 */
	private List<Integer> mergeFit(List<Integer> corners) {

		double[] pathLengths = calcPathLengths(m_stroke);

		double error0 = mseFitError(corners.get(0),
				corners.get(corners.size() - 1), pathLengths);
		double thisFitError = error0;
		double prevError = error0;

		double run = 0.0;

		boolean everythingChecked = false;

		do {
			prevError = thisFitError;
			run++;

			TreeMap<Double, Integer> segmentRatios = getSegmentRatios(corners,
					pathLengths);

			double avgSegmentRatio = 1.0 / (double) corners.size();

			double segmentRatioThreshold = (avgSegmentRatio / 2.0) * run;

			ArrayList<Integer> toRemove = new ArrayList<Integer>();

			double fitWindow = 2.0;

			while (!segmentRatios.isEmpty()
					&& segmentRatios.firstKey() < segmentRatioThreshold) {
				Map.Entry<Double, Integer> smallestSegment = segmentRatios
						.pollFirstEntry();

				int segmentIndex = smallestSegment.getValue();

				int c1 = corners.get(segmentIndex);
				int c2 = corners.get(segmentIndex + 1);

				if (c1 > 0 && c2 < corners.get(corners.size() - 1)) {
					int c0 = corners.get(segmentIndex - 1);
					int c3 = corners.get(segmentIndex + 2);

					double fitLeft = mseFitError(c0, c2, pathLengths);
					double fitRight = mseFitError(c1, c3, pathLengths);

					double currFitMiddle = mseFitError(c1, c2, pathLengths);
					double currFitLeft = mseFitError(c0, c1, pathLengths)
							+ currFitMiddle;
					double currFitRight = currFitMiddle
							+ mseFitError(c2, c3, pathLengths);

					if (fitLeft < fitRight
							&& (fitLeft < currFitLeft * fitWindow)) {
						toRemove.add(new Integer(c1));
					} else if (fitRight < currFitRight * fitWindow) {
						toRemove.add(new Integer(c2));
					}
				} else if (c1 == 0 && c2 < corners.get(corners.size() - 1)) {
					int c3 = corners.get(segmentIndex + 2);

					double fitRight = mseFitError(c1, c3, pathLengths);
					double currFit = mseFitError(c1, c2, pathLengths)
							+ mseFitError(c2, c3, pathLengths);

					if (fitRight < currFit * fitWindow) {
						toRemove.add(new Integer(c2));
					}
				} else if (c1 > 0 && c2 == corners.get(corners.size() - 1)) {
					int c0 = corners.get(segmentIndex - 1);

					double fitLeft = mseFitError(c0, c2, pathLengths);
					double currFit = mseFitError(c0, c1, pathLengths)
							+ mseFitError(c1, c2, pathLengths);

					if (fitLeft < currFit * fitWindow) {
						toRemove.add(new Integer(c1));
					}
				}
			}

			// Remove corners to be culled
			while (!toRemove.isEmpty()) {
				Integer removeCorner = toRemove.remove(0);

				if (corners.contains(removeCorner))
					corners.remove(removeCorner);
			}

			thisFitError = mseFitError(corners.get(0),
					corners.get(corners.size() - 1), pathLengths);

			if (segmentRatios.isEmpty())
				everythingChecked = true;
		} while (thisFitError < prevError * 3.0 && !everythingChecked);

		return corners;
	}

	/**
	 * Calculate the segment's error using PaleoSketch. TODO - less
	 * computationally intensive
	 * 
	 * @param p1
	 *            Index of the first corner
	 * @param p2
	 *            Index of the second corner
	 * @return An error for the stroke segment
	 */
	private double mseFitError(int p1, int p2, double[] pathLengths) {

		double err = Double.POSITIVE_INFINITY;

		List<Point> actualSegment = m_stroke.getPoints().subList(p1, p2);

		if (isLine(p1, p2, m_stroke, pathLengths, 0.92)) {

			Point corner1 = m_stroke.getPoint(p1);
			Point corner2 = m_stroke.getPoint(p2);

			Line2D.Double optimalLine = new Line2D.Double();
			optimalLine.setLine(corner1.getX(), corner1.getY(), corner2.getX(),
					corner2.getY());

			double lineErr = LeastSquares.error(actualSegment, optimalLine);

			err = lineErr;
		} else {
			double curveErr = curveFitError(actualSegment, 4);
			double arcErr = arcOrthogonalDistanceSquared(actualSegment);

			err = Math.min(curveErr, arcErr);
		}

		return err;
	}

	/**
	 * Gets the length of each segment as a percentage of the total stroke
	 * length, sorted into a TreeMap.
	 * 
	 * @param corners
	 *            Corners of the stroke
	 * @param pathLengths
	 *            Path lengths of the points
	 * @return Sorted segment ratios
	 */
	private TreeMap<Double, Integer> getSegmentRatios(List<Integer> corners,
			double[] pathLengths) {

		TreeMap<Double, Integer> segmentRatios = new TreeMap<Double, Integer>();

		double totalLength = pathLengths[pathLengths.length - 1];

		for (int i = 0; i < corners.size() - 1; i++) {
			double ratio = (pathLengths[corners.get(i + 1)] - pathLengths[corners
					.get(i)]) / totalLength;
			segmentRatios.put(ratio, i);
		}

		return segmentRatios;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.core.sketch.ISegmenter#getThreadedSegmentations()
	 */
	public List<Segmentation> getThreadedSegmentations() {
		return m_threadedSegmentations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.core.sketch.ISegmenter#run()
	 */
	public void run() {
		// TODO Auto-generated method stub
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
