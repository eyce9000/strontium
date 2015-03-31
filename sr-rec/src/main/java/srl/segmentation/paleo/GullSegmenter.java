/**
 * GullSegmenter.java
 * 
 * Revision History:<br>
 * Dec 1, 2008 bpaulson - File created
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
package srl.segmentation.paleo;

import java.util.ArrayList;
import java.util.List;

import srl.core.exception.InvalidParametersException;
import srl.core.sketch.ISegmenter;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.recognition.paleo.StrokeFeatures;


/**
 * Wave segmenter. Segments stroke using two VSegmenters
 * 
 * @author bpaulson
 */
public class GullSegmenter implements ISegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "GullSegmenter";

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
	 * Default constructor
	 */
	public GullSegmenter() {
		this(false);
	}

	/**
	 * Constructor
	 * 
	 * @param useSmoothing
	 *            flag denoting whether direction graph should be smoothed
	 *            before segmenting
	 */
	public GullSegmenter(boolean useSmoothing) {
		m_useSmoothing = useSmoothing;
	}

	/**
	 * Constructor for segmenter
	 * 
	 * @param features
	 *            features of the stroke to segment
	 */
	public GullSegmenter(StrokeFeatures features) {
		m_useSmoothing = features.isSmoothingOn();
		m_features = features;
	}

	/**
	 * Get the features of the stroke used in the gull segmentation
	 * 
	 * @return features of the stroke
	 */
	public StrokeFeatures getFeatures() {
		return m_features;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getSegmentations()
	 */
	public List<Segmentation> getSegmentations()
			throws InvalidParametersException {
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
		try {
			m_threadedSegmentations = null;
			m_threadedSegmentations = getSegmentations();
		} catch (InvalidParametersException ipe) {
			ipe.printStackTrace();
		}
	}

	/**
	 * Perform segmentation
	 * 
	 * @return segmentation interpretation
	 * @throws InvalidParametersException
	 */
	private Segmentation doSegmentation() throws InvalidParametersException {
		Segmentation seg = new Segmentation();

		List<Stroke> substrokes = new ArrayList<Stroke>();
		if (m_features.getDir().length < 2) {
			substrokes.add(m_features.getOrigStroke());
			for (Stroke substroke : substrokes) {
				substroke.setParent(m_features.getOrigStroke());
			}
			seg.setSegmentedStrokes(substrokes);
			return seg;
		}

		VSegmenter vSeg = new VSegmenter(m_features);
		Segmentation seg1 = vSeg.getSegmentations().get(0);
		if (seg1.getSegmentedStrokes().size() != 2) {
			substrokes.add(m_features.getOrigStroke());
			for (Stroke substroke : substrokes) {
				substroke.setParent(m_features.getOrigStroke());
			}
			seg.setSegmentedStrokes(substrokes);
			return seg;
		}

		Stroke sub1 = seg1.getSegmentedStrokes().get(0);
		Stroke sub2 = seg1.getSegmentedStrokes().get(1);
		vSeg = new VSegmenter(new StrokeFeatures(sub1,
				m_features.isSmoothingOn()));
		seg1 = vSeg.getSegmentations().get(0);
		if (seg1.getSegmentedStrokes().size() != 2) {
			substrokes.add(m_features.getOrigStroke());
			for (Stroke substroke : substrokes) {
				substroke.setParent(m_features.getOrigStroke());
			}
			seg.setSegmentedStrokes(substrokes);
			return seg;
		}

		substrokes.addAll(seg1.getSegmentedStrokes());
		vSeg = new VSegmenter(new StrokeFeatures(sub2,
				m_features.isSmoothingOn()));
		seg1 = vSeg.getSegmentations().get(0);
		if (seg1.getSegmentedStrokes().size() != 2) {
			substrokes.add(m_features.getOrigStroke());
			for (Stroke substroke : substrokes) {
				substroke.setParent(m_features.getOrigStroke());
			}
			seg.setSegmentedStrokes(substrokes);
			return seg;
		}

		substrokes.addAll(seg1.getSegmentedStrokes());
		for (Stroke substroke : substrokes) {
			substroke.setParent(m_features.getOrigStroke());
		}
		seg.setSegmentedStrokes(substrokes);
		seg.setSegmenterName(S_SEGMENTER_NAME);
		return seg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.segmentation.ISegmenter#setStroke(edu.tamu.core.sketch.Stroke )
	 */
	public void setStroke(Stroke stroke) {
		m_features = new StrokeFeatures(stroke, m_useSmoothing);
		m_segmentation = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.core.sketch.ISegmenter#getName()
	 */
	@Override
	public String getName() {
		return S_SEGMENTER_NAME;
	}

}
