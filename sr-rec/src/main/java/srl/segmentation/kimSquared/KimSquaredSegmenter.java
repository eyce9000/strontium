package srl.segmentation.kimSquared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import srl.core.exception.InvalidParametersException;
import srl.core.sketch.ISegmenter;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.segmentation.AbstractSegmenter;


/**
 * My Kim-Kim corner finder implementation
 * 
 * @author Aaron Wolin
 */
public class KimSquaredSegmenter extends AbstractSegmenter implements
		ISegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "KimSquared";

	/**
	 * How many points should be on a diagonal line
	 */
	private static final double S_DEFAULT_RESAMPLE_SPACING = 5.0;

	/**
	 * How many points should be on a diagonal line
	 */
	private static final double S_PTS_PER_DIAGONAL = 50.0;

	/**
	 * Original stroke
	 */
	private Stroke m_origStroke;

	/**
	 * Stroke to segment
	 */
	private Stroke m_stroke;

	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<Segmentation> m_threadedSegmentations = null;

	/**
	 * Default constructor
	 */
	public KimSquaredSegmenter() {
		// Do nothing
	}

	/**
	 * Constructor that takes a stroke
	 * 
	 * @param stroke
	 *            Stroke to segment
	 */
	public KimSquaredSegmenter(Stroke stroke) {
		setStroke(stroke);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.IDebuggableSegmenter#setStroke(org.ladder.core
	 * .sketch.Stroke)
	 */
	public void setStroke(Stroke stroke) {
		m_origStroke = stroke;
		m_stroke = cleanStroke(stroke);

		// Resample the points
		double resampleSpacing = determineResampleSpacing(m_stroke.getPoints());
		List<Point> pts = resamplePoints(m_stroke, resampleSpacing);

		// Original Kim algorithm had 5.0 pixel resample spacing
		// List<Point> pts = resamplePoints(m_stroke,
		// S_DEFAULT_RESAMPLE_SPACING);

		stroke = new Stroke(pts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getName()
	 */
	public String getName() {
		return S_SEGMENTER_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getSegmentations()
	 */
	public List<Segmentation> getSegmentations()
			throws InvalidParametersException {

		List<Segmentation> segmentations = new ArrayList<Segmentation>();

		// If the stroke is exceptionally short, creates a dummy copy of
		// m_origStroke and uses it for a substroke
		if (m_stroke.getPathLength() < S_SHORT_STROKE_THRESHOLD
				|| m_stroke.getNumPoints() < 5) {

			List<Integer> endPoints = new ArrayList<Integer>();
			endPoints.add(0);
			endPoints.add(m_origStroke.getNumPoints() - 1);

			// TODO - better confidence value
			segmentations = segmentStroke(m_origStroke, endPoints,
					S_SEGMENTER_NAME, 0.95);
		} else {
			// Get the arc length at each point
			double[] arcLengths = calcArcLengths(m_stroke);
			double[] directions = calcKimDirections(m_stroke);
			double[] curvatures = calcKimCurvatures(directions, 3);

			List<Integer> corners = getKimFit(curvatures);
			filterCorners(curvatures, arcLengths, corners);

			corners = getOriginalStrokeCorners(corners, m_stroke, m_origStroke);

			segmentations = segmentStroke(m_origStroke, corners,
					S_SEGMENTER_NAME, 0.8);
		}

		return segmentations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getThreadedSegmentations()
	 */
	public List<Segmentation> getThreadedSegmentations() {
		return m_threadedSegmentations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#run()
	 */
	public void run() {
		try {
			m_threadedSegmentations = null;
			m_threadedSegmentations = getSegmentations();
		} catch (InvalidParametersException ipe) {
			ipe.printStackTrace();
		}
	}

	/**
	 * Determines the resample spacing for the stroke
	 * 
	 * @param pts
	 *            A list of points to determine the spacing for
	 * @return The distance that each point should be resampled
	 */
	private double determineResampleSpacing(List<Point> pts) {

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < pts.size() - 1; i++) {
			double x = pts.get(i).getX();
			double y = pts.get(i).getY();

			if (x < minX)
				minX = x;
			if (x > maxX)
				maxX = x;
			if (y < minY)
				minY = y;
			if (y > maxY)
				maxY = y;
		}

		double diagonal = Math.sqrt(Math.pow(maxX - minX, 2)
				+ Math.pow(maxY - minY, 2));

		double spacing = diagonal / S_PTS_PER_DIAGONAL;

		return Math.max(spacing, S_DEFAULT_RESAMPLE_SPACING);
	}

	/**
	 * Resample the points of the stroke, based on the method described by
	 * Wobbrock in $1
	 * 
	 * @param s
	 *            Stroke to resample
	 * @param interspacing
	 *            Interspacing distance between points
	 * @return A new list of points for the stroke
	 */
	private List<Point> resamplePoints(Stroke s, double interspacing) {
		List<Point> points = s.getPoints();

		ArrayList<Point> newPoints = new ArrayList<Point>();
		newPoints.add(points.get(0));

		double D = 0;

		for (int i = 1; i < points.size(); i++) {

			// Get the current distance distance between the two points
			double d = points.get(i - 1).distance(points.get(i));

			if (D + d >= interspacing) {
				double q_x = points.get(i - 1).getX()
						+ (((interspacing - D) / d) * (points.get(i).getX() - points
								.get(i - 1).getX()));

				double q_y = points.get(i - 1).getY()
						+ (((interspacing - D) / d) * (points.get(i).getY() - points
								.get(i - 1).getY()));

				long q_t = points.get(i - 1).getTime()
						+ (long) (((interspacing - D) / d) * (points.get(i)
								.getTime() - points.get(i - 1).getTime()));

				Point q = new Point(q_x, q_y, q_t);

				newPoints.add(q);
				points.add(i, q);

				D = 0;
			} else {
				D = D + d;
			}
		}

		return newPoints;
	}

	/*
	 * Feature functions
	 */

	/**
	 * Calculates the arc length for each point
	 * 
	 * @param pts
	 *            Stroke points
	 * @return Arc length values at each point
	 */
	private double[] calcArcLengths(Stroke stroke) {
		int numPts = stroke.getNumPoints();

		double[] arcLengths = new double[numPts];

		arcLengths[0] = 0.0;

		for (int i = 1; i < numPts; i++) {
			arcLengths[i] = arcLengths[i - 1]
					+ stroke.getPoint(i - 1).distance(stroke.getPoint(i));
		}

		return arcLengths;
	}

	/**
	 * Calculates the directions - Kim style
	 * 
	 * @param Stroke
	 *            to find the Kim directions for
	 * @return Direction array of the stroke
	 */
	private double[] calcKimDirections(Stroke stroke) {
		int numPts = stroke.getNumPoints();
		double[] deltaXY = new double[numPts];

		// Set the initial direction point
		deltaXY[0] = 0.0;

		// Calculate the direction value for each point
		for (int i = 1; i < numPts; i++) {
			double d = direction(stroke.getPoints(), i);

			// Make sure there are no large jumps in direction - ensures graph
			// continuity
			if (Math.abs(d - deltaXY[i - 1]) > (0.95 * Math.PI * 2)) {
				if (deltaXY[i - 1] < 0)
					d -= (Math.PI * 2);
				else
					d += (Math.PI * 2);
			}

			deltaXY[i] = d;
		}

		double[] direction = new double[numPts];

		for (int i = 1; i < numPts; i++) {
			direction[i] = deltaXY[i - 1] - deltaXY[i];
		}

		return direction;
	}

	/**
	 * Calculates the direction at a point
	 * 
	 * @param pts
	 *            Points of the stroke
	 * @param index
	 *            Index of the point to check
	 * @return The direction at the index point
	 */
	private double direction(List<Point> pts, int index) {
		if (index - 1 >= 0) {
			double dy = pts.get(index).getY() - pts.get(index - 1).getY();
			double dx = pts.get(index).getX() - pts.get(index - 1).getX();

			return Math.atan2(dy, dx);
		} else
			return 0.0;
	}

	/**
	 * Calculates the curvatures for the stroke - Kim style
	 * 
	 * @param direction
	 *            Direction array for the stroke
	 * @param window
	 *            Window of points around an index
	 * @return Curvature array for the stroke
	 */
	private double[] calcKimCurvatures(double[] direction, int window) {
		double[] curvature = new double[direction.length];

		for (int i = 0; i < direction.length; i++) {
			curvature[i] = localMonotonicity(direction, i, window);
		}

		return curvature;
	}

	/**
	 * Calculates the local convexity
	 * 
	 * @param curvature
	 *            Curvature array of the stroke
	 * @param index
	 *            Index of the point to get the convexity for
	 * @param window
	 *            Window around the point
	 * @return The local convexity
	 */
	@SuppressWarnings("unused")
	private double localConvexity(double[] curvature, int index, int window) {
		/*
		 * WTF is u? if (Math.abs(curvature[index]) < u) return 0.0;
		 */

		int start = index - window;
		if (start < 0)
			start = 0;

		int end = index + window;
		if (end > curvature.length)
			end = curvature.length - 1;

		double c = curvature[index];

		for (int i = index + 1; i <= end; i++) {
			if (areSignsEqual(curvature[i - 1], curvature[i]))
				c += curvature[i];
		}

		for (int i = index - 1; i >= start; i--) {
			if (areSignsEqual(curvature[i + 1], curvature[i]))
				c += curvature[i];
		}

		return c;
	}

	/**
	 * Calculates the local monotonicity
	 * 
	 * @param curvature
	 *            Curvature array of the stroke
	 * @param index
	 *            Index of the point to get the convexity for
	 * @param window
	 *            Window around the point
	 * @return Local monotonicity
	 */
	private double localMonotonicity(double[] curvature, int index, int window) {

		/*
		 * WTF is u? if (Math.abs(curvature[index]) < u) return 0.0;
		 */

		int start = index - window;
		if (start < 0)
			start = 0;

		int end = index + window;
		if (end > curvature.length - 1)
			end = curvature.length - 1;

		double c = curvature[index];
		double min = Math.abs(c);

		for (int i = index + 1; i <= end; i++) {
			if (areSignsEqual(curvature[i - 1], curvature[i])
					&& Math.abs(curvature[i]) < min) {
				c += curvature[i];
				min = Math.abs(curvature[i]);
			} else
				break;
		}

		for (int i = index - 1; i >= start; i--) {
			if (areSignsEqual(curvature[i + 1], curvature[i])
					&& Math.abs(curvature[i]) < min) {
				c += curvature[i];
				min = Math.abs(curvature[i]);
			} else
				break;
		}

		return c;
	}

	/**
	 * Checks to see if two doubles have equal signs
	 * 
	 * @param a
	 *            First double
	 * @param b
	 *            Second double
	 * @return True if the signs are equal
	 */
	private boolean areSignsEqual(double a, double b) {
		if (a <= 0.0 && b <= 0.0)
			return true;
		else if (a >= 0.0 && b >= 0.0)
			return true;

		return false;
	}

	/*
	 * Get the fit for the corner
	 */

	/**
	 * Gets the corners of the stroke, based on Kim's curvature
	 * 
	 * @param curvature
	 *            Curvature array of the stroke
	 * @return Corners for the stroke
	 */
	private ArrayList<Integer> getKimFit(double[] curvature) {
		double avgCurvMag = 0.0;
		double maxCurvature = Double.NEGATIVE_INFINITY;
		double minCurvature = Double.POSITIVE_INFINITY;

		for (int i = 0; i < curvature.length; i++) {
			double currCurv = curvature[i];

			if (currCurv > maxCurvature)
				maxCurvature = currCurv;

			if (currCurv < minCurvature)
				minCurvature = currCurv;

			avgCurvMag += Math.abs(curvature[i]);
		}

		avgCurvMag /= (double) curvature.length;

		ArrayList<Integer> corners = new ArrayList<Integer>();

		// Curvature values above this threshold will be considered for corners
		double threshold = Math.max(Math.abs(minCurvature), maxCurvature) * 0.25;

		// Find corners where our curvature is over the average curvature
		// threshold
		for (int i = 0; i < curvature.length; i++) {
			// Find only the local maximum
			if (curvature[i] > 0 && curvature[i] > threshold) {
				double localMaximum = Double.NEGATIVE_INFINITY;
				int localMaximumIndex = i;

				while (i < curvature.length && curvature[i] > threshold) {
					if (curvature[i] > localMaximum) {
						localMaximum = curvature[i];
						localMaximumIndex = i;
					}

					i++;
				}

				corners.add(new Integer(localMaximumIndex));
			}

			// Find only the local minimum
			else if (curvature[i] < 0 && curvature[i] < -threshold) {
				double localMinimum = Double.POSITIVE_INFINITY;
				int localMinimumIndex = i;

				while (i < curvature.length && curvature[i] < -threshold) {
					if (curvature[i] < localMinimum) {
						localMinimum = curvature[i];
						localMinimumIndex = i;
					}

					i++;
				}

				corners.add(new Integer(localMinimumIndex));
			}
		}

		// Add the endpoints
		if (!corners.contains(0))
			corners.add(0);
		if (!corners.contains(curvature.length - 1))
			corners.add(curvature.length - 1);

		// Sort the corners
		Collections.sort(corners);

		// Return the list of corners (indices)
		return corners;
	}

	/**
	 * Filters corners
	 * 
	 * @param curvature
	 *            Curvature array for the stroke
	 * @param arcLength
	 *            Arc length array for the stroke
	 * @param corners
	 *            Corners to filter
	 */
	private void filterCorners(double[] curvature, double[] arcLengths,
			List<Integer> corners) {
		int i = 1;

		// Remove similar corners
		while (i < corners.size()) {
			int corner1 = corners.get(i - 1);
			int corner2 = corners.get(i);

			if (areCornersSimilar(arcLengths, corner1, corner2)) {
				if (corner1 == 0)
					corners.remove(i);
				else if (corner2 == arcLengths.length - 1)
					corners.remove(i - 1);
				else if (curvature[corner1] < curvature[corner2])
					corners.remove(i - 1);
				else
					corners.remove(i);
			} else {
				i++;
			}
		}

		// Collinear test for the lines
		for (int j = 1; j < corners.size() - 1; j++) {
			int c1 = corners.get(j - 1);
			int c3 = corners.get(j + 1);

			if (isLine(c1, c3, m_stroke, arcLengths, 0.90)) {
				corners.remove(j);
				j--;
			}
		}

		// Pixel distance threshold close to the endpoints
		double distThreshold = 10.0;

		// How many indices away from the endpoint we should be
		int endPtThreshold = 3;

		i = 1;

		// Remove corners too close to the stroke endpoints
		while (i < corners.size() - 1) {
			if (arcLengths[corners.get(i)] < distThreshold
					|| corners.get(i) < endPtThreshold
					|| corners.get(i) > arcLengths.length - endPtThreshold) {
				corners.remove(i);
			} else {
				i++;
			}
		}
	}

	/**
	 * Checks if two corners are similar to each other through a distance
	 * threshold
	 * 
	 * @param arcLength
	 *            Array of arc length values for points
	 * @param index1
	 *            Index of corner 1
	 * @param index2
	 *            Index of corner 2
	 * @return True if the two corners are similar to one another
	 */
	private boolean areCornersSimilar(double[] arcLength, int index1, int index2) {

		// Pixel threshold to see if corners are too close
		double distThreshold = 15.0;

		// Index threshold to see if the corners are too close
		int pointIndexThreshold = 2;

		// Are the indices the same or too close?
		if (index1 == index2 || index2 - index1 <= pointIndexThreshold)
			return true;

		// Are the two corners close to each other?
		if (arcLength[index2] - arcLength[index1] < distThreshold)
			return true;

		return false;
	}

}
