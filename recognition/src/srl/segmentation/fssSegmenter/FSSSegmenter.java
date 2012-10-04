package srl.segmentation.fssSegmenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import srl.core.exception.InvalidParametersException;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.segmentation.combination.objectiveFunctions.IObjectiveFunction;
import srl.segmentation.combination.objectiveFunctions.PolylineMSEObjectiveFunction;
import srl.segmentation.sezgin.SezginSegmenter;
import srl.segmentation.shortstraw.IShortStrawThresholds;


/**
 * Feature subset selection corner finder. Uses an oversegmented stroke to begin
 * with, similar to MergeCF's technique. Uses corners as features with five
 * possible algorithms: SFS, SBS, SFFS, and SBFS.
 * 
 * @author awolin
 */
public class FSSSegmenter extends SezginSegmenter {

	/**
	 * Enumeration storing the algorithms that can be used for combining the
	 * corner find
	 * 
	 * @author awolin
	 */
	public enum SubsetSelectionAlgorithm {

		/**
		 * Sequential Forward Search
		 */
		SFS,

		/**
		 * Sequential Backward Search
		 */
		SBS,

		/**
		 * Sequential Forward Floating Search
		 */
		SFFS,

		/**
		 * Sequential Backward Floating Search
		 */
		SBFS
	}

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "FSS Segmenter";

	/**
	 * SBFS threshold
	 */
	// private static final double S_THRESHOLD = 1.5805;
	// private static final double S_THRESHOLD = 2.02998; // FSS Comb
	private static final double S_THRESHOLD = 2.718; // FSS all
	// private static final double S_THRESHOLD = 2.60964; // FSS SS Over

	/**
	 * Subset selection algorithm
	 */
	private SubsetSelectionAlgorithm m_fssType = SubsetSelectionAlgorithm.SBFS;

	/**
	 * Objective function to use when calculating the error of a fit.
	 */
	private IObjectiveFunction m_objFunction = new PolylineMSEObjectiveFunction();

	/**
	 * Stroke to segment
	 */
	// private Stroke m_stroke = null;
	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<Segmentation> m_threadedSegmentations = null;

	/**
	 * Threshold for the elbow. Lower bound, i.e. before removing a key corner
	 */
	public static double S_ELBOW_THRESHOLD_LOWER_BOUND = -1.0;

	/**
	 * Threshold for the elbow. Upper bound, i.e. after removing a key corner
	 */
	public static double S_ELBOW_THRESHOLD_UPPER_BOUND = -1.0;

	/**
	 * Default constructor
	 */
	public FSSSegmenter() {
	}

	/**
	 * Constructor that takes a subset algorithm type
	 * 
	 * @param fssType
	 *            Subset selection type to use for combining the corners
	 */
	public FSSSegmenter(SubsetSelectionAlgorithm fssType) {
		m_fssType = fssType;
	}

	/**
	 * Constructor that takes in an objective function to use when calculating
	 * the total fit errors
	 * 
	 * @param objFunction
	 *            Objective function to use during the corner subset selection
	 *            process
	 */
	public FSSSegmenter(IObjectiveFunction objFunction) {
		m_objFunction = objFunction;
	}

	/**
	 * Constructor that takes a subset algorithm type and an objective function
	 * 
	 * @param fssType
	 *            Subset selection type to use for combining the corners
	 * @param objFunction
	 *            Objective function to use during the corner subset selection
	 *            process
	 */
	public FSSSegmenter(SubsetSelectionAlgorithm fssType,
			IObjectiveFunction objFunction, boolean useSmoothing) {
		m_fssType = fssType;
		m_objFunction = objFunction;
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

		// Check that we have a stroke
		if (m_stroke == null) {
			throw new InvalidParametersException();
		}

		List<Integer> allCorners = getInitialCorners();
		List<Integer> subset = new ArrayList<Integer>();

		// Find the best subset of corners using the given feature subset
		// selection algorithm
		switch (m_fssType) {
		case SFS:
			subset = sfs(allCorners, m_stroke, m_objFunction);
			break;
		case SBS:
			subset = sbs(allCorners, m_stroke, m_objFunction);
			break;
		case SFFS:
			subset = sffs(allCorners, m_stroke, m_objFunction);
			break;
		case SBFS:
			subset = sbfs(allCorners, m_stroke, m_objFunction);
			break;
		default:
			subset = sbs(allCorners, m_stroke, m_objFunction);
		}

		List<Segmentation> combinedSegmentations = segmentStroke(m_stroke,
				subset, S_SEGMENTER_NAME, 0.80);

		return combinedSegmentations;
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
		int i = 1;

		// Remove similar corners
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

		// Pixel distance threshold close to the endpoints
		double hookDistThreshold = 10.0;

		// How many indices away from the endpoint we should be
		int endPtThreshold = 3;

		// Calculate the average speed
		double avgSpeed = 0.0;
		for (double s : speeds) {
			avgSpeed += s;
		}
		avgSpeed /= speeds.length;

		// Remove corners too close to the stroke endpoints
		i = 1;
		while (i < corners.size() - 1) {
			if (pathLengths[corners.get(i)] < hookDistThreshold
					|| corners.get(i) < endPtThreshold
					|| corners.get(i) > pathLengths.length - endPtThreshold) {
				corners.remove(i);
			} else {
				i++;
			}
		}

		// Speed filter
		i = 1;
		while (i < corners.size() - 1) {
			if (speeds[corners.get(i)] > avgSpeed
					* S_STRICT_AVG_SPEED_THRESHOLD) {
				corners.remove(i);
			} else {
				i++;
			}
		}
	}

	/**
	 * Gets a set of initial corners to use in conjunction with the FSS
	 * algorithm.
	 * 
	 * @return an initial corner set
	 * @throws InvalidParametersException
	 *             if a segmenter has not received the proper input.
	 */
	private List<Integer> getInitialCorners() throws InvalidParametersException {
		// return getInitialCorners_SezginOversegment();
		return getInitialCorners_ShortStrawOversegment();
		// return getInitialCorners_AllPoints();
	}

	/**
	 * Gets an oversegmented set of corners using loose speed and curvature
	 * thresholds.
	 * 
	 * @return an initial corner set
	 * @throws InvalidParametersException
	 *             if a segmenter has not received the proper input.
	 */
	private List<Integer> getInitialCorners_SezginOversegment()
			throws InvalidParametersException {

		List<Integer> allCorners = new ArrayList<Integer>();

		double[] pathLengths = calcPathLengths(m_stroke);
		double[] directions = calcDirections(true);
		double[] curvatures = calcCurvatures(pathLengths, directions, true);
		double[] speeds = calcSpeeds(pathLengths, true);

		// Get the corners from curvature and speed
		// This has VERY loose thresholds
		List<Integer> Fc = generateCornersFromCurvature(curvatures, 0.1);
		List<Integer> Fs = generateCornersFromSpeed(speeds, 3.0);

		// Calculate an initial fit for the corners
		allCorners.addAll(Fc);
		allCorners.addAll(Fs);

		Collections.sort(allCorners);

		// Filter out similar corners
		filterCorners(allCorners, curvatures, speeds, pathLengths);

		return allCorners;
	}

	/**
	 * Gets an oversegmented set of corners using loose ShortStraw thresholds.
	 * 
	 * @return an initial corner set
	 * @throws InvalidParametersException
	 *             if a segmenter has not received the proper input.
	 */
	private List<Integer> getInitialCorners_ShortStrawOversegment()
			throws InvalidParametersException {

		// Store the original stroke
		Stroke resampledStroke = new Stroke(m_stroke);

		// Initial processing (filtering, resampling) on the stroke
		resampledStroke = cleanStroke(resampledStroke);

		double resampledSpacing = determineResampleSpacing(m_stroke.getPoints());

		// The stroke is too short to resample
		if (resampledStroke.getPathLength() < S_SHORT_STROKE_THRESHOLD
				|| resampledStroke.getNumPoints() < IShortStrawThresholds.S_MIN_PTS_THRESHOLD
				|| resampledSpacing < IShortStrawThresholds.S_SMALL_RESAMPLE_SPACING_THRESHOLD) {
			// Do nothing...
		}

		// Resample the stroke
		else {
			List<Point> points = resamplePoints(resampledStroke,
					resampledSpacing);
			resampledStroke = new Stroke(points);
		}

		final double MEDIAN_PERCENTAGE = 1.0;

		// Initialize necessary variables
		double[] straws = new double[resampledStroke.getNumPoints()];
		double[] sortedStraws = new double[resampledStroke.getNumPoints()
				- (IShortStrawThresholds.S_WINDOW * 2)];

		// Calculate the straws
		for (int i = IShortStrawThresholds.S_WINDOW; i < resampledStroke
				.getNumPoints() - IShortStrawThresholds.S_WINDOW; i++) {

			straws[i] = resampledStroke.getPoint(
					i - IShortStrawThresholds.S_WINDOW).distance(
					resampledStroke
							.getPoint(i + IShortStrawThresholds.S_WINDOW));

			// For finding the median
			sortedStraws[i - IShortStrawThresholds.S_WINDOW] = straws[i];
		}

		// Initialize the corner array, with the start point
		List<Integer> corners = new ArrayList<Integer>();
		corners.add(0);

		// Calculate our local minimum thresholds
		Arrays.sort(sortedStraws);
		double medianDist = sortedStraws[sortedStraws.length / 2];
		double threshold = MEDIAN_PERCENTAGE * medianDist;

		// Find the shortest straws
		for (int i = IShortStrawThresholds.S_WINDOW; i < straws.length
				- IShortStrawThresholds.S_WINDOW; i++) {

			// If we are below the median threshold
			if (straws[i] < threshold) {

				double localMinimum = Double.POSITIVE_INFINITY;
				int localMinimumIndex = i;

				// Find only the local minimum
				/*
				 * while (i < straws.length - IShortStrawThresholds.S_WINDOW &&
				 * straws[i] < threshold) { if (straws[i] < localMinimum) {
				 * localMinimum = straws[i]; localMinimumIndex = i; }
				 * 
				 * i++; }
				 */

				// Add the index of the local minimum to our list
				corners.add(new Integer(localMinimumIndex));
			}
		}

		// Add the end point
		corners.add(resampledStroke.getNumPoints() - 1);

		// Map the resampled corners to original points
		List<Integer> allCorners = getOriginalStrokeCorners(corners,
				resampledStroke, m_stroke);

		return allCorners;
	}

	/**
	 * Gets an oversegmented set of corners using loose ShortStraw thresholds.
	 * 
	 * @return an initial corner set
	 * @throws InvalidParametersException
	 *             if a segmenter has not received the proper input.
	 */
	private List<Integer> getInitialCorners_AllPoints()
			throws InvalidParametersException {

		List<Integer> allCorners = new ArrayList<Integer>();

		for (int i = 0; i < m_stroke.getNumPoints(); i++) {
			allCorners.add(i);
		}

		return allCorners;
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

		double spacing = diagonal / IShortStrawThresholds.S_PTS_PER_DIAGONAL;

		return spacing;
	}

	/**
	 * Sequential Forward Selection. Deprecated due to lack of testing.
	 * 
	 * @param corners
	 *            All corners found for the stroke
	 * @param stroke
	 *            Stroke that contains the corners
	 * @param objFunction
	 *            Objective function that measures the error of a given subset
	 *            of corners
	 * @return Subset of corners found
	 */
	@Deprecated
	private List<Integer> sfs(List<Integer> corners, Stroke stroke,
			IObjectiveFunction objFunction) {
		if (corners.size() <= 2)
			return corners;

		List<Integer> cornerSubset = new ArrayList<Integer>();
		cornerSubset.add(0);
		cornerSubset.add(stroke.getNumPoints() - 1);

		List<Double> errorList = new ArrayList<Double>();

		for (int i = 0; i < corners.size() - 2; i++) {

			List<Object> results = nextBestCorner(cornerSubset, corners,
					stroke, objFunction);

			int corner = (Integer) results.get(0);
			double error = (Double) results.get(1);

			cornerSubset.add(corner);
			errorList.add(error);
		}

		double bestError = errorList.get(errorList.size() - 1);
		List<Integer> bestSubset = new ArrayList<Integer>();
		for (int i = errorList.size() - 1; i >= 0; i--) {
			if (errorList.get(i) > bestError * 1.5) {
				bestSubset = cornerSubset.subList(0, i + 1);
				break;
			}
		}
		Collections.sort(bestSubset);

		return bestSubset;
	}

	/**
	 * Sequential Backward Selection. Deprecated due to lack of testing.
	 * 
	 * @param corners
	 *            All corners found for the stroke
	 * @param stroke
	 *            Stroke that contains the corners
	 * @param objFunction
	 *            Objective function that measures the error of a given subset
	 *            of corners
	 * @return Subset of corners found
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	private List<Integer> sbs(List<Integer> corners, Stroke stroke,
			IObjectiveFunction objFunction) {

		if (corners.size() <= 2)
			return corners;

		List<List<Integer>> cornerSubsetList = new ArrayList<List<Integer>>();
		List<Double> errorList = new ArrayList<Double>();

		List<Integer> cornerSubset = new ArrayList<Integer>(corners);

		for (int i = 0; i < corners.size(); i++) {

			List<Object> results = prevBestSubset(cornerSubset, stroke,
					objFunction);

			List<Integer> bestSubset = (List<Integer>) results.get(0);
			double error = (Double) results.get(1);

			cornerSubsetList.add(bestSubset);
			errorList.add(error);

			cornerSubset = bestSubset;
		}

		double bestError = errorList.get(0);
		List<Integer> bestSubset = new ArrayList<Integer>();
		for (int i = 1; i < errorList.size(); i++) {
			if (errorList.get(i) > bestError * 1.5) {
				bestSubset = cornerSubsetList.get(i - 1);
				break;
			}
		}
		Collections.sort(bestSubset);

		return bestSubset;
	}

	/**
	 * Sequential Forward Floating Selection. Deprecated due to lack of testing.
	 * 
	 * @param corners
	 *            All corners found for the stroke
	 * @param stroke
	 *            Stroke that contains the corners
	 * @param objFunction
	 *            Objective function that measures the error of a given subset
	 *            of corners
	 * @return Subset of corners found
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	private List<Integer> sffs(List<Integer> corners, Stroke stroke,
			IObjectiveFunction objFunction) {

		if (corners.size() <= 2)
			return corners;

		double currError = Double.MAX_VALUE;
		List<Integer> cornerSubset = new ArrayList<Integer>();
		cornerSubset.add(0);
		cornerSubset.add(stroke.getNumPoints() - 1);

		List<Double> errorList = new ArrayList<Double>();
		errorList.add(objFunction.solve(cornerSubset, stroke));

		List<List<Integer>> cornerSubsetList = new ArrayList<List<Integer>>();
		cornerSubsetList.add(new ArrayList<Integer>(cornerSubset));

		List<List<Integer>> backOnSubset = new ArrayList<List<Integer>>();

		int n = 0;

		while (cornerSubset.size() < corners.size()) {

			// Go forward
			List<Object> forwardResults = nextBestCorner(cornerSubset, corners,
					stroke, objFunction);
			int forwardCorner = (Integer) forwardResults.get(0);
			double forwardError = (Double) forwardResults.get(1);

			// Go backward (if possible)
			int backCorner = -1;
			double backError = Double.MAX_VALUE;
			List<Integer> backSubset = null;
			if (cornerSubset.size() < corners.size() - 1) {
				List<Object> backResults = prevBestSubset(cornerSubset, stroke,
						objFunction);
				backSubset = (List<Integer>) backResults.get(0);
				backError = (Double) backResults.get(1);
			}

			if (backCorner != -1 && backError < errorList.get(n - 1)
					&& !alreadySeenSubset(cornerSubset, backOnSubset)) {

				backOnSubset.add(cornerSubset);
				cornerSubset = new ArrayList<Integer>(backSubset);
				currError = backError;
				n--;
			} else {
				cornerSubset.add(forwardCorner);
				currError = forwardError;
				n++;
			}

			if (cornerSubsetList.size() <= n) {
				cornerSubsetList.add(new ArrayList<Integer>(cornerSubset));
				errorList.add(currError);
			} else {
				cornerSubsetList.set(n, new ArrayList<Integer>(cornerSubset));
				errorList.set(n, currError);
			}
		}

		List<Integer> bestSubset = null;

		double d1Errors[] = new double[errorList.size()];
		for (int i = 0; i < errorList.size() - 1; i++) {
			double deltaError = errorList.get(i + 1) / errorList.get(i);
			d1Errors[i] = deltaError;
		}

		double d2Errors[] = new double[errorList.size()];
		for (int i = 0; i < errorList.size() - 2; i++) {
			double deltaDeltaError = d1Errors[i + 1] / d1Errors[i];
			d2Errors[i] = deltaDeltaError;
		}

		for (int i = 0; i < d2Errors.length - 3; i++) {
			if (d2Errors[i] > S_THRESHOLD) {
				bestSubset = cornerSubsetList.get(i + 1);
				break;
			}
		}

		if (bestSubset == null)
			bestSubset = cornerSubsetList.get(0);

		Collections.sort(bestSubset);

		return bestSubset;
	}

	/**
	 * Sequential Backward Floating Selection
	 * 
	 * @param corners
	 *            All corners found for the stroke
	 * @param stroke
	 *            Stroke that contains the corners
	 * @param objFunction
	 *            Objective function that measures the error of a given subset
	 *            of corners
	 * @return Subset of corners found
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> sbfs(List<Integer> corners, Stroke stroke,
			IObjectiveFunction objFunction) {

		if (corners.size() <= 2)
			return corners;

		double currError = Double.MAX_VALUE;
		List<Integer> cornerSubset = new ArrayList<Integer>(corners);
		List<List<Integer>> cornerSubsetList = new ArrayList<List<Integer>>(
				cornerSubset.size());
		List<Double> errorList = new ArrayList<Double>(cornerSubset.size());

		List<List<Integer>> forwardOnSubset = new ArrayList<List<Integer>>();

		int n = -1;

		while (cornerSubset.size() > 2) {

			// Go backward
			List<Object> backResults = prevBestSubset(cornerSubset, stroke,
					objFunction);
			List<Integer> backSubset = (List<Integer>) backResults.get(0);
			double backError = (Double) backResults.get(1);

			// Go forward (if possible)
			int forwardCorner = -1;
			double forwardError = Double.MAX_VALUE;
			if (cornerSubset.size() < corners.size() - 1) {
				List<Object> forwardResults = nextBestCorner(cornerSubset,
						corners, stroke, objFunction);
				forwardCorner = (Integer) forwardResults.get(0);
				forwardError = (Double) forwardResults.get(1);
			}

			// Go forward if the error is better, otherwise continue backward
			if (forwardCorner != -1 && forwardError < errorList.get(n - 1)
					&& !alreadySeenSubset(cornerSubset, forwardOnSubset)) {

				forwardOnSubset.add(new ArrayList<Integer>(cornerSubset));
				cornerSubset.add(forwardCorner);
				Collections.sort(cornerSubset);

				currError = forwardError;
				n--;
			} else {
				cornerSubset = backSubset;
				currError = backError;
				n++;
			}

			// Update the list of best subsets for n corners
			if (cornerSubsetList.size() <= n) {
				cornerSubsetList.add(new ArrayList<Integer>(cornerSubset));
				errorList.add(currError);
			} else {
				cornerSubsetList.set(n, new ArrayList<Integer>(cornerSubset));
				errorList.set(n, currError);
			}
		}

		List<Integer> bestSubset = null;

		double d1Errors[] = new double[errorList.size()];
		for (int i = 1; i < errorList.size(); i++) {
			double deltaError = errorList.get(i) / errorList.get(i - 1);
			d1Errors[i] = deltaError;
		}

		for (int i = 2; i < d1Errors.length; i++) {
			if (d1Errors[i] > S_THRESHOLD) {
				bestSubset = cornerSubsetList.get(i - 1);
				break;
			}
		}

		if (bestSubset == null)
			bestSubset = cornerSubsetList.get(0);

		Collections.sort(bestSubset);

		return bestSubset;
	}

	/**
	 * Find the next best corner according to an objective function
	 * 
	 * @param cornerSubset
	 *            Current subset of found corners
	 * @param corners
	 *            Total set of corners to pool from
	 * @param stroke
	 *            Stroke that contains the corners
	 * @param objFunction
	 *            Objective function that measures the error of a given subset
	 *            of corners
	 * @return Best corner and the corresponding error
	 */
	private List<Object> nextBestCorner(List<Integer> cornerSubset,
			List<Integer> corners, Stroke stroke, IObjectiveFunction objFunction) {

		List<Double> errorValues = new ArrayList<Double>();
		Collections.sort(cornerSubset);

		for (int i = 0; i < corners.size(); i++) {
			Integer currCorner = corners.get(i);

			if (!cornerSubset.contains(currCorner)) {

				List<Integer> tempSubset = new ArrayList<Integer>(cornerSubset);
				tempSubset.add(currCorner);

				double value = objFunction.solve(tempSubset, stroke);
				errorValues.add(value);
			} else {
				errorValues.add(Double.MAX_VALUE);
			}
		}

		Double minError = Collections.min(errorValues);
		int index = errorValues.indexOf(minError);
		int bestCorner = corners.get(index);

		List<Object> returnList = new ArrayList<Object>();
		returnList.add(bestCorner);
		returnList.add(minError);

		return returnList;
	}

	/**
	 * Find the next best corner according to an objective function
	 * 
	 * @param cornerSubset
	 *            Current subset of found corners
	 * @param stroke
	 *            Stroke that contains the corners
	 * @param objFunction
	 *            Objective function that measures the error of a given subset
	 *            of corners
	 * @return Best previous subset
	 */
	private List<Object> prevBestSubset(List<Integer> cornerSubset,
			Stroke stroke, IObjectiveFunction objFunction) {

		List<Double> errorValues = new ArrayList<Double>();
		Collections.sort(cornerSubset);

		for (int i = 0; i < cornerSubset.size(); i++) {
			Integer currCorner = cornerSubset.get(i);

			if (currCorner != 0 && currCorner != stroke.getNumPoints() - 1) {

				List<Integer> tempSubset = new ArrayList<Integer>(cornerSubset);
				tempSubset.remove(currCorner);

				double value = objFunction.solve(tempSubset, stroke);
				errorValues.add(value);
			} else {
				errorValues.add(Double.MAX_VALUE);
			}
		}

		double minError = Collections.min(errorValues);
		int index = errorValues.indexOf(minError);
		int worstCorner = cornerSubset.get(index);

		List<Integer> bestSubset = new ArrayList<Integer>(cornerSubset);
		bestSubset.remove(new Integer(worstCorner));

		List<Object> returnList = new ArrayList<Object>();
		returnList.add(bestSubset);
		returnList.add(minError);

		return returnList;
	}

	/**
	 * Returns true if the given subset is in a list of previously seen subsets
	 * 
	 * @param subset
	 *            Subset to check for
	 * @param prevSeenSubsets
	 *            List of subsets to check in
	 * @return True if subset is a component in the previously seen subsets,
	 *         false otherwise
	 */
	private boolean alreadySeenSubset(List<Integer> subset,
			List<List<Integer>> prevSeenSubsets) {

		Collections.sort(subset);

		for (List<Integer> seenSubset : prevSeenSubsets) {
			Collections.sort(seenSubset);

			if (subset.size() != seenSubset.size()) {
				continue;
			} else {

				boolean allValsEqual = true;
				for (int i = 0; i < subset.size(); i++) {
					if (subset.get(i).intValue() != seenSubset.get(i)
							.intValue()) {
						allValsEqual = false;
						break;
					}
				}

				if (allValsEqual) {
					return true;
				}
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getSegmentations()
	 */
	public List<Segmentation> getSegmentationsForCVTraining(int knownNumCorners)
			throws InvalidParametersException {

		// Check that we have a stroke
		if (m_stroke == null) {
			throw new InvalidParametersException();
		}

		List<Integer> allCorners = getInitialCorners();
		List<Integer> subset = new ArrayList<Integer>();

		// Find the best subset of corners using the given feature subset
		// selection algorithm
		subset = sbfsForCVTraining(allCorners, m_stroke, m_objFunction,
				knownNumCorners);

		List<Segmentation> combinedSegmentations = segmentStroke(m_stroke,
				subset, S_SEGMENTER_NAME, 0.80);

		return combinedSegmentations;
	}

	/**
	 * Runs the SBFS algorithm with a known number of corners in order to
	 * calculate the elbow threshold
	 * 
	 * @param corners
	 *            All corners found for the stroke
	 * @param stroke
	 *            Stroke that contains the corners
	 * @param objFunction
	 *            Objective function that measures the error of a given subset
	 *            of corners
	 * @param knownNumCorners
	 *            Known number of corners in the stroke
	 * @return Subset of corners found
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> sbfsForCVTraining(List<Integer> corners,
			Stroke stroke, IObjectiveFunction objFunction, int knownNumCorners) {

		S_ELBOW_THRESHOLD_LOWER_BOUND = -1.0;
		S_ELBOW_THRESHOLD_UPPER_BOUND = -1.0;

		double currError = Double.MAX_VALUE;

		List<Integer> cornerSubset = new ArrayList<Integer>(corners);
		List<List<Integer>> cornerSubsetList = new ArrayList<List<Integer>>(
				cornerSubset.size());
		List<Double> errorList = new ArrayList<Double>(cornerSubset.size());

		List<List<Integer>> forwardOnSubset = new ArrayList<List<Integer>>();

		int n = -1;

		while (cornerSubset.size() > 2) {

			// Go backward
			List<Object> backResults = prevBestSubset(cornerSubset, stroke,
					objFunction);
			List<Integer> backSubset = (List<Integer>) backResults.get(0);
			double backError = (Double) backResults.get(1);

			// Go forward (if possible)
			int forwardCorner = -1;
			double forwardError = Double.MAX_VALUE;
			if (cornerSubset.size() < corners.size() - 1) {
				List<Object> forwardResults = nextBestCorner(cornerSubset,
						corners, stroke, objFunction);
				forwardCorner = (Integer) forwardResults.get(0);
				forwardError = (Double) forwardResults.get(1);
			}

			if (forwardCorner != -1 && forwardError < errorList.get(n - 1)
					&& !alreadySeenSubset(cornerSubset, forwardOnSubset)) {

				forwardOnSubset.add(new ArrayList<Integer>(cornerSubset));
				cornerSubset.add(forwardCorner);
				Collections.sort(cornerSubset);

				currError = forwardError;
				n--;
			} else {
				cornerSubset = backSubset;
				currError = backError;
				n++;
			}

			if (cornerSubsetList.size() <= n) {
				cornerSubsetList.add(new ArrayList<Integer>(cornerSubset));
				errorList.add(currError);
			} else {
				cornerSubsetList.set(n, new ArrayList<Integer>(cornerSubset));
				errorList.set(n, currError);
			}
		}

		List<Integer> bestSubset = null;

		double d1Errors[] = new double[errorList.size()];
		for (int i = 1; i < errorList.size(); i++) {
			double deltaError = errorList.get(i) / errorList.get(i - 1);
			d1Errors[i] = deltaError;
		}

		for (int i = 1; i < cornerSubsetList.size(); i++) {
			if (cornerSubsetList.get(i).size() == knownNumCorners) {
				S_ELBOW_THRESHOLD_LOWER_BOUND = d1Errors[i];
				S_ELBOW_THRESHOLD_UPPER_BOUND = d1Errors[i + 1];
				bestSubset = cornerSubsetList.get(i);
			}
		}

		if (bestSubset == null) {
			bestSubset = cornerSubsetList.get(0);
		}

		Collections.sort(bestSubset);

		return bestSubset;
	}

	/**
	 * Gets the segmentations using a given threshold and the SBFS algorithm.
	 * 
	 * @param threshold
	 *            Elbow threshold to use
	 * @return A list of segmentations
	 * @throws InvalidParametersException
	 */
	public List<Segmentation> getSegmentationsForCVTesting(double threshold)
			throws InvalidParametersException {

		// Check that we have a stroke
		if (m_stroke == null) {
			throw new InvalidParametersException();
		}

		List<Integer> allCorners = getInitialCorners();
		List<Integer> subset = new ArrayList<Integer>();

		// Find the best subset of corners using the given feature subset
		// selection algorithm
		subset = sbfsForCVTesting(allCorners, m_stroke, m_objFunction,
				threshold);

		List<Segmentation> combinedSegmentations = segmentStroke(m_stroke,
				subset, S_SEGMENTER_NAME, 0.80);

		return combinedSegmentations;
	}

	/**
	 * Runs the SBFS algorithm with a given threshold in order to test the
	 * threshold's accuracy.
	 * 
	 * @param corners
	 *            All corners found for the stroke
	 * @param stroke
	 *            Stroke that contains the corners
	 * @param objFunction
	 *            Objective function that measures the error of a given subset
	 *            of corners
	 * @param threshold
	 *            Given elbow threshold
	 * @return Subset of corners found
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> sbfsForCVTesting(List<Integer> corners,
			Stroke stroke, IObjectiveFunction objFunction, double threshold) {

		double currError = Double.MAX_VALUE;

		List<Integer> cornerSubset = new ArrayList<Integer>(corners);
		List<List<Integer>> cornerSubsetList = new ArrayList<List<Integer>>(
				cornerSubset.size());
		List<Double> errorList = new ArrayList<Double>(cornerSubset.size());

		List<List<Integer>> forwardOnSubset = new ArrayList<List<Integer>>();

		int n = -1;

		while (cornerSubset.size() > 2) {

			// Go backward
			List<Object> backResults = prevBestSubset(cornerSubset, stroke,
					objFunction);
			List<Integer> backSubset = (List<Integer>) backResults.get(0);
			double backError = (Double) backResults.get(1);

			// Go forward (if possible)
			int forwardCorner = -1;
			double forwardError = Double.MAX_VALUE;
			if (cornerSubset.size() < corners.size() - 1) {
				List<Object> forwardResults = nextBestCorner(cornerSubset,
						corners, stroke, objFunction);
				forwardCorner = (Integer) forwardResults.get(0);
				forwardError = (Double) forwardResults.get(1);
			}

			if (forwardCorner != -1 && forwardError < errorList.get(n - 1)
					&& !alreadySeenSubset(cornerSubset, forwardOnSubset)) {

				forwardOnSubset.add(new ArrayList<Integer>(cornerSubset));
				cornerSubset.add(forwardCorner);
				Collections.sort(cornerSubset);

				currError = forwardError;
				n--;
			} else {
				cornerSubset = backSubset;
				currError = backError;
				n++;
			}

			if (cornerSubsetList.size() <= n) {
				cornerSubsetList.add(new ArrayList<Integer>(cornerSubset));
				errorList.add(currError);
			} else {
				cornerSubsetList.set(n, new ArrayList<Integer>(cornerSubset));
				errorList.set(n, currError);
			}
		}

		List<Integer> bestSubset = null;

		double d1Errors[] = new double[errorList.size()];
		for (int i = 1; i < errorList.size(); i++) {
			double deltaError = errorList.get(i) / errorList.get(i - 1);
			d1Errors[i] = deltaError;
		}

		for (int i = 1; i < d1Errors.length; i++) {
			if (d1Errors[i] > threshold) {
				bestSubset = cornerSubsetList.get(i - 1);
				break;
			}
		}

		if (bestSubset == null) {
			bestSubset = cornerSubsetList.get(0);
		}

		Collections.sort(bestSubset);

		return bestSubset;
	}
}
