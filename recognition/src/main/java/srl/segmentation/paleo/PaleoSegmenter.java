/**
 * PaleoSegmenter.java
 * 
 * Revision History:<br>
 * Jun 30, 2008 bpaulson - File created
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
package srl.segmentation.paleo;

import java.util.ArrayList;
import java.util.List;


import srl.core.sketch.ISegmenter;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.recognition.paleo.Thresholds;
import srl.recognition.paleo.StrokeFeatures;


/**
 * Polyline segmenter used by PaleoSketch
 * 
 * @author bpaulson
 */
public class PaleoSegmenter implements ISegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "Paleo";

	/**
	 * Features of the stroke
	 */
	private StrokeFeatures m_features;

	/**
	 * Segmentation produced by the segmenter
	 */
	private Segmentation m_segmentation;

	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<Segmentation> m_threadedSegmentations = null;

	/**
	 * Flag denoting if direction graph smoothing should take place
	 */
	private boolean m_useSmoothing;

	/**
	 * Default Constructor
	 */
	public PaleoSegmenter() {
		this(false);
	}

	/**
	 * Constructor
	 * 
	 * @param useSmoothing
	 *            flag denoting whether direction graph should be smoothed
	 *            before segmenting
	 */
	public PaleoSegmenter(boolean useSmoothing) {
		m_useSmoothing = useSmoothing;
	}

	/**
	 * Constructor for the segmenter
	 * 
	 * @param features
	 *            features of that stroke
	 */
	public PaleoSegmenter(StrokeFeatures features) {
		m_useSmoothing = features.isSmoothingOn();
		m_features = features;
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
	public List<Segmentation> getSegmentations() {
		if (m_features == null)
			return null;
		if (m_segmentation == null)
			m_segmentation = doSegmentation();
		ArrayList<Segmentation> segs = new ArrayList<Segmentation>();
		segs.add(m_segmentation);
		return segs;
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
		m_threadedSegmentations = getSegmentations();
	}

	/**
	 * Performs the actual segmentation
	 * 
	 * @return segmentation
	 */
	private Segmentation doSegmentation() {
		int neighborhood = (int) (m_features.getNumPoints() * Thresholds.active.M_NEIGHBORHOOD_PCT);
		if (neighborhood < 5)
			neighborhood = 5;
		ArrayList<Integer> m_paleoCorners = new ArrayList<Integer>();
		m_paleoCorners.add(0);

		// find preliminary corners
		Stroke currStroke = new Stroke();
		currStroke.addPoint(m_features.getPoints().get(0));
		for (int i = 1; i < m_features.getNumPoints(); i++) {
			currStroke.addPoint(m_features.getPoints().get(i));
			if (currStroke.getNumPoints() > neighborhood
					&& currStroke.getNumPoints() > 5) {
				if (StrokeFeatures.getEndptStrokeLengthRatio(currStroke) < Thresholds.active.M_ENDPT_STROKE_LENGTH_RATIO) {
					m_paleoCorners.add(i - 2);
					currStroke = new Stroke();
					currStroke.setParent(m_features.getOrigStroke());
					currStroke.addPoint(m_features.getPoints().get(i - 1));
				}
			}
		}
		m_paleoCorners.add(m_features.getNumPoints() - 1);

		// merge until not done
		boolean done = false;
		while (!done) {
			int prevSize = m_paleoCorners.size();

			// observe neighborhood and find corner with highest curvature
			// within neighborhood
			for (int i = 0; i < m_paleoCorners.size(); i++) {
				int curr = m_paleoCorners.get(i);
				if (i > 0) {
					if (curr < m_paleoCorners.get(i - 1)) {
						m_paleoCorners.remove(i);
						continue;
					}
				}
				if (curr - neighborhood < 0
						|| curr + neighborhood > m_features.getCurvature().length - 1)
					continue;
				else {
					int maxIndex = curr - neighborhood;
					double maxCurv = m_features.getCurvature()[maxIndex];
					boolean stop = false;
					for (int j = curr - neighborhood; j < curr + neighborhood
							&& !stop; j++) {
						if (j >= m_features.getCurvature().length)
							stop = true;
						else if (m_features.getCurvature()[j] > maxCurv) {
							maxCurv = m_features.getCurvature()[j];
							maxIndex = j;
						}
					}
					if (!stop
							&& !m_paleoCorners
									.contains((Integer) (maxIndex + 1))) {
						m_paleoCorners.add(i, maxIndex + 1);
						m_paleoCorners.remove(i + 1);
					}
				}
			}

			// done when merging no longer produces new corner set
			if (m_paleoCorners.size() != prevSize) {

				// merge corners within a neighborhood of each other (leave end
				// points intact)
				m_paleoCorners = doMerge(neighborhood + 1, m_paleoCorners);
			} else {
				done = true;
			}
		}

		Segmentation seg = new Segmentation();
		List<Stroke> substrokes = m_features.getSubStrokes(m_paleoCorners);
		for (Stroke substroke : substrokes) {
			substroke.setParent(m_features.getOrigStroke());
		}
		seg.setSegmentedStrokes(substrokes);
		seg.setSegmenterName(S_SEGMENTER_NAME);
		return seg;
	}

	/**
	 * Method used to merge corners that are within the same neighborhood
	 * 
	 * @param neighborhood
	 *            neighborhood size
	 * @param paleoCorners
	 *            integer list of corner indexes
	 * @return new list of corners (after merging)
	 */
	private ArrayList<Integer> doMerge(int neighborhood,
			ArrayList<Integer> paleoCorners) {
		ArrayList<Integer> newCorners = new ArrayList<Integer>();
		newCorners.add(0);
		int prev = 0;
		int sum = 0;
		int num = 1;
		for (int i = 1; i < paleoCorners.size(); i++) {
			// no longer in the neighborhood; see if corners are too close - if
			// they are then take the middle of the two
			if (paleoCorners.get(i) - prev >= neighborhood) {
				if (Math.round((float) sum / num) > neighborhood)
					newCorners.add(Math.round((float) sum / num));
				prev = paleoCorners.get(i);
				num = 1;
				sum = paleoCorners.get(i);
			}
			// still in the neighborhood
			else {
				num++;
				sum += paleoCorners.get(i);
			}
		}

		if (!newCorners.contains((Integer) (m_features.getNumPoints() - 1))) {

			if ((m_features.getNumPoints() - 1)
					- newCorners.get(newCorners.size() - 1) <= neighborhood) {
				newCorners.remove(newCorners.size() - 1);
			}
			newCorners.add(m_features.getNumPoints() - 1);
		}

		return newCorners;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.IDebuggableSegmenter#setStroke(org.ladder.core
	 * .sketch.Stroke)
	 */
	public void setStroke(Stroke stroke) {
		m_features = new StrokeFeatures(stroke, m_useSmoothing);
		m_segmentation = null;
	}

}
