/**
 * PolylineCombinationSegmenter.java
 * 
 * Revision History:<br>
 * Oct 17, 2008 bpaulson - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import srl.core.exception.InvalidParametersException;
import srl.core.sketch.ISegmenter;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.recognition.paleo.LineFit;


/**
 * Segmenter that combines other polyline segmenters and chooses the "best"
 * segmentation. This segmenter is biased towards segmentations that produce the
 * lowest error with the fewest number of corners
 * 
 * @author bpaulson
 */
public class PolylineCombinationSegmenter implements ISegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "PolylineCombination";

	/**
	 * Alpha value for combiner
	 */
	public static double ALPHA = 0.055; // Changed from 0.2 after training

	/**
	 * Stroke to segment
	 */
	private Stroke m_stroke;

	/**
	 * Segmentations produced by the various segmenters
	 */
	private List<Segmentation> m_segmentations;

	/**
	 * Segmenters to combine
	 */
	private List<ISegmenter> m_segmenters;

	/**
	 * Segmentation comparator (compares based on the number of corners
	 * produced)
	 */
	private SegmentationNumCornerComparator m_cornerCompare = new SegmentationNumCornerComparator();

	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<Segmentation> m_threadedSegmentations = null;

	/**
	 * Default constructor
	 */
	public PolylineCombinationSegmenter() {
		this(false);
	}

	/**
	 * Default constructor (uses PaleoSegmenter, DouglasPeuckerSegmenter,
	 * ShortStrawSegmenter, and FSSCombinationSegmenter)
	 * 
	 * @param useSmoothing
	 *            flag denoting if graph smoothing should take place (used for
	 *            PaleoSegmenter)
	 */
	public PolylineCombinationSegmenter(boolean useSmoothing) {
		m_segmenters = new ArrayList<ISegmenter>();
		// m_segmenters.add(new PaleoSegmenter(useSmoothing));
		// m_segmenters.add(new ShortStrawSegmenter());
		// m_segmenters.add(new DouglasPeuckerSegmenter());
		// m_segmenters.add(new SezginSegmenter());
		// m_segmenters.add(new KimSquaredSegmenter());

		/*
		 * NOTE: Segmenter now gets the above segmentations from the FSS
		 * segmenter to avoid re-computation
		 */
		m_segmenters.add(new FSSCombinationSegmenter2(useSmoothing));
	}

	/**
	 * Constructor for combination polyline segmenter
	 * 
	 * @param segmenters
	 *            list of segmenters to combine (its assumes they are polyline
	 *            segmenters)
	 */
	public PolylineCombinationSegmenter(List<ISegmenter> segmenters) {
		m_segmenters = segmenters;
	}

	/**
	 * Constructor for combination polyline segmenter
	 * 
	 * @param segmenters
	 *            list of segmenters to combine (its assumes they are polyline
	 *            segmenters)
	 * @param stroke
	 *            stroke to segment
	 */
	public PolylineCombinationSegmenter(List<ISegmenter> segmenters,
			Stroke stroke) {
		m_segmenters = segmenters;
		setStroke(stroke);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getName()
	 */
	public String getName() {
		String name = S_SEGMENTER_NAME;
		/*
		 * if (m_segmenters != null) { name += " ("; for (ISegmenter s :
		 * m_segmenters) name += s.getName() + " "; name += ")"; }
		 */
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getSegmentations()
	 */
	public List<Segmentation> getSegmentations()
			throws InvalidParametersException {
		if (m_stroke == null)
			return null;
		if (m_segmentations == null)
			doSegmentation();
		return m_segmentations;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.segmentation.ISegmenter#setStroke(edu.tamu.core.sketch.Stroke )
	 */
	public void setStroke(Stroke stroke) {
		m_segmentations = null;
		m_stroke = stroke;
	}

	/**
	 * Perform segmentation ranking/selection
	 */
	private void doSegmentation() {
		List<SegmentationResult> results = new ArrayList<SegmentationResult>();
		if (m_segmenters.size() <= 0)
			return;

		// add segmentations for all segmenters
		for (ISegmenter segmenter : m_segmenters) {
			try {
				segmenter.setStroke(m_stroke);
				List<Segmentation> segmentations = segmenter.getSegmentations();

				// add segmentations of other corner finders
				if (segmenter instanceof FSSCombinationSegmenter2) {
					FSSCombinationSegmenter2 fss = (FSSCombinationSegmenter2) segmenter;
					if (fss.getDPSegs() != null)
						segmentations.addAll(fss.getDPSegs());
					if (fss.getKimSegs() != null)
						segmentations.addAll(fss.getKimSegs());
					if (fss.getPaleoSegs() != null)
						segmentations.addAll(fss.getPaleoSegs());
					if (fss.getSezginSegs() != null)
						segmentations.addAll(fss.getSezginSegs());
					if (fss.getShortStrawSegs() != null)
						segmentations.addAll(fss.getShortStrawSegs());
				}

				if (segmentations != null) {
					for (Segmentation seg : segmentations) {
						double err = getSegError(seg);
						seg.setSegmenterName(S_SEGMENTER_NAME);
						seg.label = (segmenter.getName());
						SegmentationResult r = new SegmentationResult(seg, err,
								segmenter.getName());
						results.add(r);
					}
				}
			} catch (InvalidParametersException e) {
				System.err.println("Unable to add segmentations for "
						+ segmenter.getName() + ". " + e.getMessage());
			}
		}

		// sort by corners first, in event of tie the one with lowest error
		// should appear first
		Collections.sort(results, m_cornerCompare);

		// continue to swap top result with better result until no change is
		// made
		boolean swapMade = true;
		while (swapMade && results.size() > 1) {
			swapMade = false;
			for (int i = 1; i < results.size() && !swapMade; i++) {
				double cornerDiff = Math.abs(results.get(0).getSegmentation()
						.getSegmentedStrokes().size()
						- results.get(i).getSegmentation()
								.getSegmentedStrokes().size());
				if (results.get(0).getError() > results.get(i).getError()
						* (cornerDiff + 1) * (1.0 + ALPHA)) {
					SegmentationResult r = results.remove(i);
					results.add(0, r);

					// change made so loop back to beginning
					swapMade = true;
				}
			}
		}

		// continue to swap and sort results until no change is made
		/*
		 * for (int i = 0; i < results.size() - 1; i++) {
		 * 
		 * double cornerDiff = Math.abs(results.get(i).getSegmentation()
		 * .getSegmentedStrokes().size() - results.get(i + 1).getSegmentation()
		 * .getSegmentedStrokes().size());
		 * 
		 * // swap if error of one with fewer corners is more than twice of the
		 * // error of the one with more corners if (results.get(i).getError() >
		 * results.get(i + 1).getError() (cornerDiff + 1) (1.0 + ALPHA)) {
		 * SegmentationResult r = results.remove(i + 1); results.add(i, r);
		 * 
		 * // change made so loop back to beginning i = -1; } }
		 */

		/*
		 * for (int i = 0; i < results.size(); i++) {
		 * System.out.println("size = " + results.get(i).getSegmentation()
		 * .getSegmentedStrokes().size() + " err = " + results.get(i).getError()
		 * + " name = " + results.get(i).getName()); }
		 */

		m_segmentations = new ArrayList<Segmentation>();
		for (SegmentationResult r : results)
			m_segmentations.add(r.getSegmentation());
	}

	/**
	 * Get the error of a segmentation
	 * 
	 * @param seg
	 *            segmentation to get error for
	 * @return error
	 */
	private double getSegError(Segmentation seg) {
		double err = 0.0;
		for (Stroke s : seg.getSegmentedStrokes()) {
			double lsqe = LineFit.getLineLSQE(s);
			err += lsqe;
		}
		return err;
	}
}
