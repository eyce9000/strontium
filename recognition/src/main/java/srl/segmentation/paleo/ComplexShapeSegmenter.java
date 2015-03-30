/**
 * ComplexShapeSegmenter.java
 * 
 * Revision History:<br>
 * Jul 2, 2008 bpaulson - File created
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


import srl.core.sketch.ISegmenter;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.recognition.paleo.CurveFit;
import srl.recognition.paleo.Fit;
import srl.recognition.paleo.OrigPaleoSketchRecognizer;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PolylineFit;
import srl.recognition.paleo.StrokeFeatures;


/**
 * Segmenter used when segmenting a complex shape into a set of PaleoSketch
 * primitives
 * 
 * @author bpaulson
 */
public class ComplexShapeSegmenter implements ISegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "ComplexShape";

	/**
	 * Features of the stroke we are segmenting
	 */
	private StrokeFeatures m_features;

	/**
	 * List of best substrokes for the complex fit
	 */
	private List<Stroke> m_best_substrokes;

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
	 * Constructor for segmenter
	 * 
	 * @param features
	 *            features of the stroke to segment
	 */
	public ComplexShapeSegmenter(StrokeFeatures features) {
		m_features = features;
	}

	/**
	 * Default constructor
	 */
	public ComplexShapeSegmenter() {
		this(false);
	}

	/**
	 * Constructor
	 * 
	 * @param useSmoothing
	 *            flag denoting whether direction graph should be smoothed
	 *            before segmenting
	 */
	public ComplexShapeSegmenter(boolean useSmoothing) {
		m_useSmoothing = useSmoothing;
	}

	/**
	 * Get the features of the stroke used in the complex segmentation
	 * 
	 * @return features of the stroke
	 */
	public StrokeFeatures getFeatures() {
		return m_features;
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
		if (m_segmentation == null) {
			doSegmentation();
			m_segmentation = new Segmentation();
			m_segmentation.setSegmentedStrokes(m_best_substrokes);
			((Segmentation) m_segmentation).setSegmenterName(S_SEGMENTER_NAME);
		}
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
	 */
	private void doSegmentation() {
		m_best_substrokes = calcTheBestSubStrokes();
		if (m_best_substrokes.size() <= 2)
			return;
		for (int i = 0; i < m_best_substrokes.size() - 1; i++) {
			Stroke s1 = m_best_substrokes.get(i);
			Stroke s2 = m_best_substrokes.get(i + 1);
			Stroke s = combine(s1, s2);
			OrigPaleoSketchRecognizer r = new OrigPaleoSketchRecognizer(s,
					PaleoConfig.basicPrimsOnly());
			r.recognize();
			List<Fit> f = r.getFits();
			if (f.size() == 0)
				continue;
			if (f.get(0) instanceof PolylineFit || f.get(0) instanceof CurveFit)
				continue;
			m_best_substrokes.remove(i + 1);
			m_best_substrokes.remove(i);
			m_best_substrokes.add(i, s);
			i--;
		}
	}

	/**
	 * Calculate the list of best possible sub-strokes; this is a separate
	 * function because its recursive
	 * 
	 * @return list of best possible sub-strokes
	 */
	private List<Stroke> calcTheBestSubStrokes() {
		ArrayList<Stroke> best = new ArrayList<Stroke>();
		if (m_features.getNumPoints() == 2) {
			best.add(m_features.getSubStroke(0, m_features.getNumPoints() - 1));
			for (Stroke substroke : best) {
				substroke.setParent(m_features.getOrigStroke());
			}

			return best;
		} else if (m_features.getNumPoints() < 2) {
			for (Stroke substroke : best) {
				substroke.setParent(m_features.getOrigStroke());
			}

			return best;
		}

		Stroke s1 = m_features.getSubStroke(0, m_features.getMaxCurvIndex());
		Stroke s2 = m_features.getSubStroke(m_features.getMaxCurvIndex(),
				m_features.getNumPoints() - 1);
		if (s1.getNumPoints() <= 2 || s2.getNumPoints() <= 2) {
			best.add(m_features.getSubStroke(0, m_features.getNumPoints() - 1));
			for (Stroke substroke : best) {
				substroke.setParent(m_features.getOrigStroke());
			}

			return best;
		}

		OrigPaleoSketchRecognizer p1 = new OrigPaleoSketchRecognizer(s1,
				PaleoConfig.basicPrimsOnly());
		p1.recognize();
		List<Fit> f1 = p1.getFits();
		OrigPaleoSketchRecognizer p2 = new OrigPaleoSketchRecognizer(s2,
				PaleoConfig.basicPrimsOnly());
		p2.recognize();
		List<Fit> f2 = p2.getFits();

		if (f1.size() == 0) {
			StrokeFeatures sf = new StrokeFeatures(s1,
					m_features.isSmoothingOn());
			ComplexShapeSegmenter seg = new ComplexShapeSegmenter(sf);
			List<Stroke> tmp = seg.calcTheBestSubStrokes();
			for (Stroke s : tmp)
				best.add(s);
		} else {
			best.add(s1);
		}

		if (f2.size() == 0) {
			StrokeFeatures sf = new StrokeFeatures(s2,
					m_features.isSmoothingOn());
			ComplexShapeSegmenter seg = new ComplexShapeSegmenter(sf);
			List<Stroke> tmp = seg.calcTheBestSubStrokes();
			for (Stroke s : tmp)
				best.add(s);
		} else {
			best.add(s2);
		}

		// Set the parent
		for (Stroke substroke : best) {
			substroke.setParent(m_features.getOrigStroke());
		}

		return best;
	}

	/**
	 * Combine two strokes into one
	 * 
	 * @param s1
	 *            stroke one
	 * @param s2
	 *            stroke two
	 * @return new stroke containing stroke one followed by stroke two
	 */
	private Stroke combine(Stroke s1, Stroke s2) {
		Stroke newStroke = new Stroke();
		for (int i = 0; i < s1.getNumPoints(); i++)
			newStroke.addPoint(s1.getPoints().get(i));
		for (int i = 1; i < s2.getNumPoints(); i++)
			newStroke.addPoint(s2.getPoints().get(i));
		return newStroke;
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
}
