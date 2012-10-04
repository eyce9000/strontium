package srl.segmentation.stahovich;

import org.openawt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import srl.core.exception.InvalidParametersException;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.recognition.paleo.PaleoConfig;
import srl.segmentation.sezgin.SezginSegmenter;


public class StahovichSegmenter extends SezginSegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "Stahovich";

	/**
	 * Configuration file for what primitives we support
	 */
	private PaleoConfig m_paleoConfig;

	/**
	 * Default constructor
	 */
	public StahovichSegmenter() {

		// Initialize the Paleo configuration file
		m_paleoConfig = PaleoConfig.allOff();
		m_paleoConfig.setArcTestOn(true);
		m_paleoConfig.setLineTestOn(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getSegmentations()
	 */
	@Override
	public List<Segmentation> getSegmentations()
			throws InvalidParametersException {

		if (m_origStroke == null)
			throw new InvalidParametersException();

		List<Segmentation> segmentations = new ArrayList<Segmentation>();

		// If the stroke is exceptionally short, creates a dummy copy of
		// m_origStroke and uses it for a substroke
		if (m_stroke.getPathLength() < S_SHORT_STROKE_THRESHOLD) {

			List<Integer> endPoints = new ArrayList<Integer>();
			endPoints.add(0);
			endPoints.add(m_origStroke.getNumPoints() - 1);

			// TODO - better confidence value
			segmentations = segmentStroke(m_origStroke, endPoints,
					S_SEGMENTER_NAME, 0.95);
		} else {

			List<Integer> corners = getCorners();

			// TODO - better confidence
			segmentations = segmentStroke(m_origStroke, corners,
					S_SEGMENTER_NAME, 0.80);
		}

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
	 * Finds the corners for a stroke
	 * 
	 * @param s
	 *            Stroke to find the corners for
	 * @return Corners of a stroke
	 */
	private List<Integer> getCorners() {

		double[] pathLengths = calcPathLengths(m_stroke);
		double[] directions = calcDirections(true);
		double[] curvatures = calcCurvatures(pathLengths, directions, true);
		double[] speeds = calcSpeeds(pathLengths, true);

		// Get the corners from curvature and speed
		List<Integer> Ic = generateCorners(curvatures, speeds, 0.75, 0.8);

		// Calculate an initial fit for the corners
		List<Integer> corners = stahovichFit(curvatures, speeds, pathLengths,
				directions, Ic);

		// Map the corners back to the original points (i.e., non-cleaned)
		List<Integer> origCorners = getOriginalStrokeCorners(corners, m_stroke,
				m_origStroke);

		return origCorners;
	}

	/**
	 * Finds possible corners from the curvature values of points. Uses the
	 * average curvature for a cutoff threshold.
	 * 
	 * @param curvatures
	 *            Curvature values for each point
	 * @param speeds
	 *            Curvature values for each point
	 * @return An ArrayList of indices indicating corners
	 */
	private List<Integer> generateCorners(double[] curvatures, double[] speeds,
			final double CURVATURE_THRESHOLD, final double SPEED_THRESHOLD) {

		List<Integer> corners = new ArrayList<Integer>();

		double minSpeed = Double.POSITIVE_INFINITY;
		double maxSpeed = Double.NEGATIVE_INFINITY;
		double avgSpeed = 0.0;

		// Calculate the average, minimum, and maximum speed
		for (int i = 0; i < speeds.length; i++) {
			avgSpeed += speeds[i];

			if (speeds[i] < minSpeed)
				minSpeed = speeds[i];

			if (speeds[i] > maxSpeed)
				maxSpeed = speeds[i];
		}

		avgSpeed /= (double) speeds.length;

		// Speed values below this threshold will be considered for corners
		double speedThreshold = avgSpeed * SPEED_THRESHOLD;

		// Find corners where our speed is under the average curvature threshold
		for (int i = 0; i < speeds.length; i++) {

			// Find only the local minimum
			if (speeds[i] < speedThreshold
					&& curvatures[i] < CURVATURE_THRESHOLD) {

				double localSpeedMinimum = Double.POSITIVE_INFINITY;
				double localCurvatureMaximum = Double.NEGATIVE_INFINITY;
				int localIndex = i;

				while (i < speeds.length && speeds[i] < speedThreshold
						&& curvatures[i] > CURVATURE_THRESHOLD) {

					if (speeds[i] < localSpeedMinimum
							&& curvatures[i] > localCurvatureMaximum) {
						localSpeedMinimum = speeds[i];
						localCurvatureMaximum = curvatures[i];
						localIndex = i;
					}

					i++;
				}

				corners.add(new Integer(localIndex));
			}
		}

		// Add the endpoints
		if (!corners.contains(0))
			corners.add(0);
		if (!corners.contains(speeds.length - 1))
			corners.add(speeds.length - 1);

		// Sort the corners
		Collections.sort(corners);

		// Return the list of corners (indices)
		return corners;
	}

	/**
	 * Original merge fit algorithm
	 * 
	 * @param Fc
	 * @param Fs
	 * @return
	 */
	private List<Integer> stahovichFit(double[] curvatures, double[] speeds,
			double[] pathLengths, double[] directions, List<Integer> Ic) {

		// Get all corners
		List<Integer> allCorners = new ArrayList<Integer>();
		allCorners.addAll(Ic);

		Collections.sort(allCorners);

		// Filter out similar corners
		filterCorners(allCorners, curvatures, speeds, pathLengths);

		List<Integer> corners = new ArrayList<Integer>();
		corners.addAll(allCorners);

		// Merging
		for (int i = 0; i < corners.size() - 1; i++) {

			int c0 = -1;
			int c1 = -1;
			int c2 = -1;
			int c3 = -1;

			if (i - 1 > 0) {
				c0 = corners.get(i - 1);
			}

			c1 = corners.get(i);
			c2 = corners.get(i + 1);

			if (i + 2 < corners.size()) {
				c3 = corners.get(i + 2);
			}

			double path0 = Double.POSITIVE_INFINITY;
			double path1 = Double.POSITIVE_INFINITY;
			double path2 = Double.POSITIVE_INFINITY;

			double err0 = Double.POSITIVE_INFINITY;
			double err1 = Double.POSITIVE_INFINITY;
			double err2 = Double.POSITIVE_INFINITY;

			if (c0 != -1) {
				path0 = getSegmentPathLength(c0, c1);
				err0 = arcOrthogonalDistanceSquared(m_stroke.getPoints(), c0,
						c1);
			}

			path1 = getSegmentPathLength(c1, c2);
			err1 = arcOrthogonalDistanceSquared(m_stroke.getPoints(), c1, c2);

			if (c3 != -1) {
				path2 = getSegmentPathLength(c2, c3);
				err2 = arcOrthogonalDistanceSquared(m_stroke.getPoints(), c2,
						c3);
			}

			double mergeLeft = Double.POSITIVE_INFINITY;
			double mergeRight = Double.POSITIVE_INFINITY;

			double pathRatioLeft = Double.POSITIVE_INFINITY;
			double pathRatioRight = Double.POSITIVE_INFINITY;

			if (c0 != -1) {
				mergeLeft = arcOrthogonalDistanceSquared(m_stroke.getPoints(),
						c0, c2);
				pathRatioLeft = path1 / path0;
			}

			if (c3 != -1) {
				mergeRight = arcOrthogonalDistanceSquared(m_stroke.getPoints(),
						c1, c3);
				pathRatioRight = path1 / path2;
			}

			double errRatioLeft = mergeLeft / (err0 + err1);
			double errRatioRight = mergeRight / (err1 + err2);

			if (errRatioLeft < errRatioRight && (errRatioLeft - 1.0) < 0.1) {
				// && pathRatioLeft < 0.2) {
				corners.remove(i);
				i--;
			} else if ((errRatioRight - 1.0) < 0.1) { // && pathRatioRight <
														// 0.2) {
				corners.remove(i + 1);
				i--;
			}
		}

		// Splitting
		// Find corners where the curvature changes
		// double[] signedCurvatures = calcSignedCurvatures(pathLengths,
		// directions, true);
		//
		// int[] signs = new int[signedCurvatures.length];
		// List<Integer> signCorners = new ArrayList<Integer>();
		// for (int i = 0; i < signedCurvatures.length; i++) {
		//
		// if (signedCurvatures[i] < -0.1) {
		// signs[i] = -1;
		// } else if (signedCurvatures[i] > 0.1) {
		// signs[i] = 1;
		// } else {
		// signs[i] = 0;
		// }
		// }
		//
		// for (int i = 1; i < signs.length; i++) {
		//
		// if (Math.abs(signs[i] - signs[i - 1]) > 0) {
		// signCorners.add(i);
		// }
		// }
		//
		// for (int i = 0; i < corners.size() - 1; i++) {
		//
		// int c0 = corners.get(i);
		// int c1 = corners.get(i+1);
		// double err0 = arcOrthogonalDistanceSquared(m_stroke.getPoints(), c0,
		// c1);
		//
		// double minErr = Double.POSITIVE_INFINITY;
		// int bestSplitCorner = c0;
		//
		// for (int j = 0; j < signCorners.size(); j++) {
		// int cs = signCorners.get(j);
		// if (signCorners.get(j) > c0 && signCorners.get(j) < c1) {
		// double errLeft = arcOrthogonalDistanceSquared(m_stroke.getPoints(),
		// c0, cs);
		// double errRight = arcOrthogonalDistanceSquared(m_stroke.getPoints(),
		// cs, c1);
		//
		// if (errLeft + errRight < minErr) {
		// minErr = errLeft + errRight;
		// bestSplitCorner = cs;
		// }
		// }
		// }
		//
		// if (minErr < 0.65 * err0) {
		// corners.add(i+1, bestSplitCorner);
		// Collections.sort(corners);
		// }
		// }
		//
		// // Check lines to remove them
		// int i = 1;
		// while (i < corners.size() - 1) {
		//
		// int corner0 = corners.get(i - 1);
		// int corner1 = corners.get(i);
		// int corner2 = corners.get(i + 1);
		//
		// if (isLine(corner0, corner1, m_stroke, pathLengths,
		// S_LINE_VS_ARC_THRESHOLD)
		// && isLine(corner1, corner2, m_stroke, pathLengths,
		// S_LINE_VS_ARC_THRESHOLD)) {
		//
		// Point p0 = m_stroke.getPoint(corner0);
		// Point p1 = m_stroke.getPoint(corner1);
		// Point p2 = m_stroke.getPoint(corner2);
		//
		// double dx1 = p1.getX() - p0.getX();
		// double dy1 = p1.getY() - p0.getY();
		//
		// double angle1 = Math.atan2(dy1, dx1);
		//
		// double dx2 = p2.getX() - p1.getX();
		// double dy2 = p2.getY() - p1.getY();
		//
		// double angle2 = Math.atan2(dy2, dx2);
		//
		// if (Math.abs(angle1 - angle2) < Math.PI / 9.0) {
		// corners.remove(new Integer(corner1));
		// } else {
		// i++;
		// }
		// } else {
		// i++;
		// }
		// }

		return corners;
	}

	/**
	 * Filters corners
	 * 
	 * @param curvatures
	 *            Curvature array of the stroke
	 * @param speeds
	 *            Speed array of the stroke
	 * @param pathLengths
	 *            Arc length array of the stroke
	 * @param corners
	 *            Corners to filter corners
	 */
	private void filterCorners(List<Integer> corners, double[] curvatures,
			double[] speeds, double[] pathLengths) {

		// // Remove small segments at ends of a stroke (hooks)
		// for (int i = 1; i < corners.size() - 1; i++) {
		// if (corners.get(i) < 15
		// || corners.get(i) > (pathLengths.length - 15)) {
		// corners.remove(i);
		// i--;
		// }
		// }

		// Remove corners too close to the stroke endpoints
		int i = 1;
		double hookDistThreshold = 15.0;

		while (i < corners.size() - 1) {
			if (pathLengths[corners.get(i)] < hookDistThreshold
					|| m_stroke.getPathLength() - pathLengths[corners.get(i)] < hookDistThreshold) {
				corners.remove(i);
			} else {
				i++;
			}
		}

		// Remove similar corners
		i = 1;
		while (i < corners.size()) {
			int corner1 = corners.get(i - 1);
			int corner2 = corners.get(i);

			if (areCornersSimilar(pathLengths, corner1, corner2)) {
				if (corner1 == 0)
					corners.remove(i);
				else if (corner2 == pathLengths.length - 1)
					corners.remove(i - 1);
				else if (curvatures[corner1] < curvatures[corner2])
					corners.remove(i - 1);
				else
					corners.remove(i);
			} else {
				i++;
			}
		}

		// Check lines to remove them
		i = 1;
		while (i < corners.size() - 1) {

			int corner0 = corners.get(i - 1);
			int corner1 = corners.get(i);
			int corner2 = corners.get(i + 1);

			if (isLine(corner0, corner1, m_stroke, pathLengths,
					S_LINE_VS_ARC_THRESHOLD)
					&& isLine(corner1, corner2, m_stroke, pathLengths,
							S_LINE_VS_ARC_THRESHOLD)) {

				Point p0 = m_stroke.getPoint(corner0);
				Point p1 = m_stroke.getPoint(corner1);
				Point p2 = m_stroke.getPoint(corner2);

				double dx1 = p1.getX() - p0.getX();
				double dy1 = p1.getY() - p0.getY();

				double angle1 = Math.atan2(dy1, dx1);

				double dx2 = p2.getX() - p1.getX();
				double dy2 = p2.getY() - p1.getY();

				double angle2 = Math.atan2(dy2, dx2);

				if (Math.abs(angle1 - angle2) < Math.PI / 9.0) {
					corners.remove(new Integer(corner1));
				} else {
					i++;
				}
			} else {
				i++;
			}
		}
	}

	/**
	 * Checks if two corners are similar to each other through a distance
	 * threshold
	 * 
	 * @param pathLengths
	 *            Array of path length values for points
	 * @param index1
	 *            Index of corner 1
	 * @param index2
	 *            Index of corner 2
	 * @return True if the two corners are similar to one another
	 */
	private boolean areCornersSimilar(double[] pathLengths, int index1,
			int index2) {

		// Pixel threshold to see if corners are too close
		double distThreshold = m_stroke.getBoundingBox().getDiagonalLength()
				* S_CLOSE_POINT_DIST_THRESHOLD;

		// Index threshold to see if the corners are too close
		int pointIndexThreshold = S_CLOSE_POINT_INDICES_THRESHOLD;

		// Are the indices the same or too close?
		if (index1 == index2 || index2 - index1 <= pointIndexThreshold)
			return true;

		// Are the two corners close to each other?
		if (pathLengths[index2] - pathLengths[index1] < distThreshold)
			return true;

		return false;
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
	@Override
	protected double arcOrthogonalDistanceSquared(List<Point> pts, int p1,
			int p2) {

		double error = 0.0;

		Point pt1 = pts.get(p1);
		Point pt2 = pts.get(p2);

		// Set the line between the corners
		Line2D.Double line = new Line2D.Double();
		line.setLine(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());

		double lineLength = pt1.distance(pt2);
		double slope = Math.tan(line.getBounds2D().getHeight()
				/ line.getBounds2D().getWidth());
		double midX = Math.cos(slope) * (lineLength / 2.0);
		double midY = Math.sin(slope) * (lineLength / 2.0);

		Point midPoint = new Point(midX, midY);

		// Stolen from the old TLine
		double perpSlope = Math.atan2(line.getY2() - line.getY1(), line.getX2()
				- line.getX1())
				+ Math.PI / 2;
		double perpLine[] = { midX, midY,
				midX + Math.cos(perpSlope) * lineLength,
				midY + Math.sin(perpSlope) * lineLength };

		Line2D.Double perpBisector = new Line2D.Double();
		perpBisector
				.setLine(perpLine[0], perpLine[1], perpLine[2], perpLine[3]);

		// Also stolen from TLine
		double yInt = perpBisector.getP1().getY() - perpSlope
				* perpBisector.getP1().getX();

		Point pbPt1;
		Point pbPt2;

		// Convoluted way to ensure that our perpendicular bisector line will
		// definitely go through our stroke
		// It's long because there are many checks
		// TODO: Get rid of magic numbers
		if (Double.isInfinite(perpSlope)) {
			pbPt1 = new Point(midPoint.getX(), 0.0);
			pbPt2 = new Point(midPoint.getX(), 10000.0);
		} else if (perpSlope == 0.0) {
			pbPt1 = new Point(0, yInt);
			pbPt2 = new Point(10000.0, yInt);
		} else {
			if (yInt < 0.0) {
				pbPt1 = new Point((-yInt / perpSlope), 0.0);
				pbPt2 = new Point((10000.0 - yInt) / perpSlope, 10000.0);
			} else {
				double xInt = -yInt / perpSlope;

				if (xInt < 0.0) {
					pbPt2 = new Point(0, yInt);
					pbPt1 = new Point(10000.0, (10000.0 * perpSlope) + yInt);
				} else {
					pbPt1 = new Point(xInt, 0.0);
					pbPt2 = new Point(0, yInt);
				}
			}
		}

		Point p3 = midPoint;

		// Get a third point that intersects the _stroke_ around its midpoint
		for (int i = p1; i < p2 - 1; i += 2) {
			if (isIntersection(pts.get(i), pts.get(i + 2), pbPt1, pbPt2)) {
				double newX = (pts.get(i + 2).getX() + pts.get(i).getX()) / 2.0;
				double newY = (pts.get(i + 2).getY() + pts.get(i).getY()) / 2.0;
				p3 = new Point(newX, newY);

				i = p2;
			}
		}

		// http://mcraefamily.com/MathHelp/
		// GeometryConicSectionCircleEquationGivenThreePoints.htm
		double a = pts.get(p1).getX();
		double b = pts.get(p1).getY();
		double c = p3.getX();
		double d = p3.getY();
		double e = pts.get(p2).getX();
		double f = pts.get(p2).getY();

		double k = (0.5 * ((((a * a) + (b * b)) * (e - c))
				+ (((c * c) + (d * d)) * (a - e)) + (((e * e) + (f * f)) * (c - a))))
				/ ((b * (e - c)) + (d * (a - e)) + (f * (c - a)));

		double h = (0.5 * ((((a * a) + (b * b)) * (f - d))
				+ (((c * c) + (d * d)) * (b - f)) + (((e * e) + (f * f)) * (d - b))))
				/ ((a * (f - d)) + (c * (b - f)) + (e * (d - b)));

		// If we're actually a line
		if (Double.isInfinite(k) || Double.isInfinite(h) || Double.isNaN(k)
				|| Double.isNaN(h))
			return orthogonalDistanceSquared(pts, p1, p2);

		// Set the circle's center and radius
		Point center = new Point(h, k);
		double radius = Math.sqrt(((a - h) * (a - h)) + ((b - k) * (b - k)));

		double circumference = 2.0 * Math.PI * radius;
		double ptsPathLength = getSegmentPathLength(p1, p2);

		// We won't be using the circle fit is less than 10%
		if (ptsPathLength / circumference < 0.10) {
			return orthogonalDistanceSquared(pts, p1, p2);
		}

		// Get the orthogonal distance between each point in the stroke and the
		// line
		for (int i = p1; i <= p2; i++) {
			double euc = pts.get(i).distance(center);
			double dist = radius - euc;

			error += (dist * dist);
		}

		return error;
	}

	/**
	 * Get the path length of the segment between p1 and p2.
	 * 
	 * @param p1
	 *            first point.
	 * @param p2
	 *            second point.
	 * @return the path length between p1 and p2.
	 */
	private double getSegmentPathLength(int p1, int p2) {
		double ptsPathLength = 0.0;
		for (int i = p1; i < p2; i++) {
			ptsPathLength += m_stroke.getPoint(i).distance(
					m_stroke.getPoint(i + 1));
		}
		return ptsPathLength;
	}

	/**
	 * Calculates the curvature values at each point
	 * 
	 * @param pathLengths
	 *            Path lengths of the points
	 * @param directions
	 *            Direction (angles) of the points
	 * @param smooth
	 *            Should an average filter be applied?
	 * @return The curvature value at each point
	 */
	private double[] calcSignedCurvatures(double[] pathLengths,
			double[] directions, boolean smooth) {

		double[] curvature = new double[m_stroke.getNumPoints()];

		curvature[0] = 0.0;

		// Calculate the curvature value for each point
		for (int i = 1; i < m_stroke.getNumPoints() - 1; i++) {
			double curv = curvatureSigned(pathLengths, directions, i, 3);

			// Hack to check if we have a divide-by-0 error
			if (curv != -1.0)
				curvature[i] = curv;
			else
				curvature[i] = curvature[i - 1];
		}

		// Average filtering
		if (smooth) {
			double[] smoothCurvature = new double[m_stroke.getNumPoints()];

			for (int i = 1; i < m_stroke.getNumPoints() - 1; i++) {
				smoothCurvature[i] = (curvature[i - 1] + curvature[i] + curvature[i + 1]) / 3.0;
			}

			curvature = smoothCurvature;
		}

		return curvature;
	}

	/**
	 * Finds the curvature at a given point index, given a point window, the arc
	 * lengths of the points, and the directions of the points
	 * 
	 * @param pathLengths
	 *            Path lengths of the points
	 * @param directions
	 *            Direction (angles) of the points
	 * @param index
	 *            Corner index
	 * @param window
	 *            Neighborhood window of points
	 * @return The curvature value for a point at the index
	 */
	private double curvatureSigned(double[] pathLengths, double[] directions,
			int index, int window) {

		int start = index - window;
		if (index - window < 0)
			start = 0;

		int end = index + window;
		if (end + window > pathLengths.length)
			end = pathLengths.length - 1;

		double segmentLength = pathLengths[end] - pathLengths[start];

		if (segmentLength > 0.0) {
			double dirChanges = 0.0;

			for (int i = start + 1; i <= end; i++) {
				dirChanges += (directions[i] - directions[i - 1]);
			}

			return dirChanges / segmentLength;
		} else {
			return 0.0;
		}
	}
}
