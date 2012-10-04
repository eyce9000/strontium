package srl.segmentation.combination;

import java.util.ArrayList;
import java.util.List;


import srl.core.exception.InvalidParametersException;
import srl.core.sketch.ISegmenter;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.patternrec.UnivariateKDE;
import srl.segmentation.AbstractSegmenter;
import srl.segmentation.douglaspeucker.DouglasPeuckerSegmenter;
import srl.segmentation.kimSquared.KimSquaredSegmenter;
import srl.segmentation.paleo.PaleoSegmenter;
import srl.segmentation.sezgin.SezginSegmenter;
import srl.segmentation.shortstraw.ShortStrawSegmenter;


/**
 * Currently uses ShortStraw, Douglas-Peucker, and Paleo
 * 
 * @author Aaron Wolin
 */
public class KDECombinationSegmenter extends AbstractSegmenter implements
		ISegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "KDE Combination Segmenter";

	/**
	 * Flag for whether we should output the data in Matlab format to the
	 * console. Used for graphing.
	 */
	private static boolean S_MATLABGRAPHS = false;

	/**
	 * Stroke to segment
	 */
	private Stroke m_stroke;

	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<Segmentation> m_threadedSegmentations = null;

	/**
	 * Flag denoting if direction graph smoothing should take place of not
	 * (needed for PaleoSegmenter)
	 */
	private boolean m_useSmoothing;

	/**
	 * Default constructor
	 */
	public KDECombinationSegmenter() {
		this(false);
	}

	/**
	 * Constructor taking a smoothing factor for Paleo
	 * 
	 * @param useSmoothing
	 *            Flag denoting if direction graph smoothing should take place
	 *            of not (needed for PaleoSegmenter)
	 */
	public KDECombinationSegmenter(boolean useSmoothing) {
		m_useSmoothing = useSmoothing;
	}

	/**
	 * Constructor that takes in a stroke
	 * 
	 * @param stroke
	 *            Stroke to segment
	 * @param useSmoothing
	 *            Flag denoting if direction graph smoothing should take place
	 *            of not (needed for PaleoSegmenter)
	 */
	public KDECombinationSegmenter(Stroke stroke, boolean useSmoothing) {
		m_useSmoothing = useSmoothing;
		setStroke(stroke);
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

		List<Integer> allCorners = new ArrayList<Integer>();

		ISegmenter segmenter;

		// ShortStraw
		segmenter = new ShortStrawSegmenter(m_stroke);
		List<Segmentation> shortStrawSegs = segmenter.getSegmentations();
		allCorners.addAll(getCornersFromSegmentations(shortStrawSegs));

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

		// Run a KDE over the corner indices
		UnivariateKDE cornerKDE = new UnivariateKDE();

		// Merge the corner data
		List<Double> X = new ArrayList<Double>();
		for (Integer c : allCorners) {
			X.add(new Double(c));
		}

		// Set a default bandwidth
		double h = 4.0;

		// Create an empty list for later graphical output
		List<Double> Y = new ArrayList<Double>();
		List<Double> P = cornerKDE.compute(X, h, Y);

		if (S_MATLABGRAPHS) {
			generateMatlabGraphCode(Y, P);
		}

		List<Integer> possibleCorners = localMaxima(Y, P);

		List<Segmentation> segmentations = this.segmentStroke(m_stroke,
				possibleCorners, S_SEGMENTER_NAME, 0.80);

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
	 * Finds the local maxima in the KDE plot
	 * 
	 * @param Y
	 *            Range of values (indices)
	 * @param P
	 *            KDE probabilities
	 * @return Local maxima in the P_KDE plot
	 */
	private List<Integer> localMaxima(List<Double> Y, List<Double> P) {

		List<Integer> maxima = new ArrayList<Integer>();

		boolean findMaxima = true;
		for (int i = 1; i < P.size(); i++) {
			if (findMaxima) {
				if (P.get(i) < P.get(i - 1)) {
					maxima.add((int) Math.round(Y.get(i - 1)));
					findMaxima = false;
				}
			} else if (P.get(i) > P.get(i - 1)) {
				findMaxima = true;
			}
		}

		return maxima;
	}

	/**
	 * Print the data range and P_KDE values in a Matlab-readable format. Prints
	 * to the console.
	 * 
	 * @param Y
	 *            Data values
	 * @param P
	 *            P_KDE for the data
	 */
	private void generateMatlabGraphCode(List<Double> Y, List<Double> P) {

		String yString = "Y = [";
		String pString = "P = [";
		for (int i = 0; i < Y.size(); i++) {
			yString += Y.get(i);
			pString += P.get(i);

			if (i != Y.size() - 1) {
				yString += ",";
				pString += ",";
			}
		}

		yString += "];";
		pString += "];";

		String plotString = "plot(Y, P);";

		System.out.println(yString);
		System.out.println(pString);
		System.out.println(plotString);
	}
}
