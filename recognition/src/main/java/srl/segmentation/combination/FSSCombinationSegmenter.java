package srl.segmentation.combination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import srl.core.exception.InvalidParametersException;
import srl.core.sketch.ISegmenter;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.segmentation.AbstractSegmenter;
import srl.segmentation.combination.objectiveFunctions.IObjectiveFunction;
import srl.segmentation.combination.objectiveFunctions.PolylineMSEObjectiveFunction;
import srl.segmentation.douglaspeucker.DouglasPeuckerSegmenter;
import srl.segmentation.kimSquared.KimSquaredSegmenter;
import srl.segmentation.mergecf.MergeCFSegmenter;
import srl.segmentation.paleo.PaleoSegmenter;
import srl.segmentation.sezgin.SezginSegmenter;
import srl.segmentation.shortstraw.ShortStrawSegmenter;


/**
 * Feature subset selection corner finding combination algorithm. Uses corners
 * as features with five possible algorithms: SFS, SBS, SFFS, and SBFS.
 * 
 * @author awolin
 */
public class FSSCombinationSegmenter extends AbstractSegmenter implements
		ISegmenter {

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
	private static final String S_SEGMENTER_NAME = "FSS Combination Segmenter";

	/**
	 * SBFS threshold
	 */
	private static final double S_THRESHOLD = 1.9887473405191085; // 2.0299882732432564;

	// 1-fold on Training Set:
	// Without MergeCF:
	// With MergeCF: 1.9887473405191085

	// Without MergeCF 2.020747995; //1.7637226;
	// for new Actual Error 1.3848;
	// for old Actual Error 1.5805;
	// With SS and DP 1.100

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
	private Stroke m_stroke = null;

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
	 * Flag denoting if direction graph smoothing should take place of not
	 * (needed for PaleoSegmenter)
	 */
	private boolean m_useSmoothing;

	/**
	 * Default constructor
	 */
	public FSSCombinationSegmenter() {
		this(false);
	}

	/**
	 * Constructor that takes in a smoothing factor
	 * 
	 * @param useSmoothing
	 *            Flag denoting if direction graph smoothing should take place
	 *            of not (needed for PaleoSegmenter)
	 */
	public FSSCombinationSegmenter(boolean useSmoothing) {
		m_useSmoothing = useSmoothing;
	}

	/**
	 * Constructor that takes a subset algorithm type
	 * 
	 * @param fssType
	 *            Subset selection type to use for combining the corners
	 * 
	 * @param useSmoothing
	 *            Flag denoting if direction graph smoothing should take place
	 *            of not (needed for PaleoSegmenter)
	 */
	public FSSCombinationSegmenter(SubsetSelectionAlgorithm fssType,
			boolean useSmoothing) {
		this(useSmoothing);
		m_fssType = fssType;
	}

	/**
	 * Constructor that takes in an objective function to use when calculating
	 * the total fit errors
	 * 
	 * @param objFunction
	 *            Objective function to use during the corner subset selection
	 *            process
	 * @param useSmoothing
	 *            Flag denoting if direction graph smoothing should take place
	 *            of not (needed for PaleoSegmenter)
	 */
	public FSSCombinationSegmenter(IObjectiveFunction objFunction,
			boolean useSmoothing) {
		this(useSmoothing);
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
	 * @param useSmoothing
	 *            Flag denoting if direction graph smoothing should take place
	 *            of not (needed for PaleoSegmenter)
	 */
	public FSSCombinationSegmenter(SubsetSelectionAlgorithm fssType,
			IObjectiveFunction objFunction, boolean useSmoothing) {
		this(useSmoothing);
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
	 * @see
	 * edu.tamu.segmentation.ISegmenter#setStroke(edu.tamu.core.sketch.Stroke )
	 */
	public void setStroke(Stroke stroke) {
		m_stroke = stroke;
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
	 * Gets a set of initial corners to use in conjunction with the FSS
	 * algorithm.
	 * 
	 * @return an initial corner set
	 * @throws InvalidParametersException
	 *             if a segmenter has not received the proper input.
	 */
	private List<Integer> getInitialCorners() throws InvalidParametersException {

		List<Integer> allCorners = new ArrayList<Integer>();

		ISegmenter segmenter;

		// ShortStraw
		segmenter = new ShortStrawSegmenter(m_stroke);
		List<Segmentation> shortStrawSegs = segmenter.getSegmentations();
		allCorners.addAll(getCornersFromSegmentations(shortStrawSegs));

		// MergeCF
		segmenter = new MergeCFSegmenter(m_stroke);
		List<Segmentation> mergeCFSegs = segmenter.getSegmentations();
		// allCorners.addAll(getCornersFromSegmentations(mergeCFSegs));

		// Douglas-Peucker
		segmenter = new DouglasPeuckerSegmenter(m_stroke);
		List<Segmentation> dpSegs = segmenter.getSegmentations();
		allCorners.addAll(getCornersFromSegmentations(dpSegs));

		// Paleo
		segmenter = new PaleoSegmenter(m_useSmoothing);
		segmenter.setStroke(m_stroke);
		List<Segmentation> paleoSegs = segmenter.getSegmentations();
		allCorners.addAll(getCornersFromSegmentations(paleoSegs));

		// Sezgin
		segmenter = new SezginSegmenter();
		segmenter.setStroke(m_stroke);
		List<Segmentation> sezginSegs = segmenter.getSegmentations();
		allCorners.addAll(getCornersFromSegmentations(sezginSegs));

		// Kim
		segmenter = new KimSquaredSegmenter();
		segmenter.setStroke(m_stroke);
		List<Segmentation> kimSegs = segmenter.getSegmentations();
		allCorners.addAll(getCornersFromSegmentations(kimSegs));

		// Remove overlapping corners
		Collections.sort(allCorners);
		for (int i = 1; i < allCorners.size(); i++) {
			if (allCorners.get(i).equals(allCorners.get(i - 1))) {
				allCorners.remove(i);
				i--;
			}
		}

		return allCorners;
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

		// double d2Errors[] = new double[errorList.size()];
		// for (int i = 2; i < errorList.size(); i++) {
		// double deltaDeltaError = d1Errors[i] / d1Errors[i - 1];
		// d2Errors[i] = deltaDeltaError;
		// }

		// for (int i = 3; i < d2Errors.length; i++) {
		// if (d2Errors[i] > S_THRESHOLD) {
		// bestSubset = cornerSubsetList.get(i - 1);
		// break;
		// }
		// }

		for (int i = 2; i < d1Errors.length; i++) {
			if (d1Errors[i] > S_THRESHOLD) {
				bestSubset = cornerSubsetList.get(i - 1);
				break;
			}
		}

		if (bestSubset == null)
			bestSubset = cornerSubsetList.get(0);

		Collections.sort(bestSubset);

		// printArrayToMatlab(errorList);
		// System.out.println(errorList.size());

		// printArrayToMatlab(d1Errors);
		// System.out.println(d1Errors.length);

		// double[] cornerArray = new double[cornerSubsetList.size()];
		// for (int i = 0; i < cornerSubsetList.size(); i++) {
		// cornerArray[i] = cornerSubsetList.get(i).size();
		// }
		// printArrayToMatlab(cornerArray);
		// System.out.println(cornerArray.length);

		boolean tryMax = true;
		if (tryMax) {

			if (d1Errors.length > 1) {
				double maxValue = d1Errors[1];
				List<Integer> bestMaxSubset = cornerSubsetList.get(0);

				for (int i = 2; i < d1Errors.length; i++) {
					if (d1Errors[i] > maxValue) {
						maxValue = d1Errors[i];
						bestMaxSubset = cornerSubsetList.get(i - 1);
					}
				}
			}
		}

		// return bestMaxSubset;
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

	private void printArrayToMatlab(double[] a) {

		String aString = "[";

		for (int i = 0; i < a.length; i++) {
			aString += a[i];

			if (i != a.length - 1) {
				aString += ",";
			}
		}

		aString += "]";

		System.out.println(aString);
	}

	private void printArrayToMatlab(List<Double> a) {

		String aString = "[";

		for (int i = 0; i < a.size(); i++) {
			aString += a.get(i);

			if (i != a.size() - 1) {
				aString += ",";
			}
		}

		aString += "]";

		System.out.println(aString);
	}
}
