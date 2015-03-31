/**
 * NBCFit.java
 * 
 * Revision History:<br>
 * Jan 7, 2009 bpaulson - File created
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
package srl.recognition.paleo;

import java.util.ArrayList;
import java.util.List;

import org.openawt.geom.GeneralPath;
import org.openawt.svg.SVGPath;

import srl.core.sketch.Segmentation;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;


/**
 * Fit stroke to an NBC symbol
 * 
 * @author bpaulson
 */
public class NBCFit extends Fit {

	/**
	 * Segmentation of stroke
	 */
	protected Segmentation m_seg;

	/**
	 * Ratio between largest and smallest segment
	 */
	protected double m_sizeRatio;

	/**
	 * Density of sub dot
	 */
	protected double m_dotDensity;

	/**
	 * Number of revolutions of sub dot
	 */
	protected double m_dotRevs;

	/**
	 * Subshapes (dot/line)
	 */
	protected List<Shape> m_subShapes = new ArrayList<Shape>();

	/**
	 * Constructor for NBC fit
	 * 
	 * @param features
	 *            features of the stroke to fit
	 * @param waveSeg
	 *            wave segmentation of stroke
	 */
	public NBCFit(StrokeFeatures features, Segmentation waveSeg) {
		super(features);
		m_passed = true;
		m_seg = waveSeg;

		// segmentation cant be null
		if (m_seg == null) {
			m_passed = false;
			m_fail = 0;
//			log.debug("NBCFit: passed = " + m_passed + "(" + m_fail
//					+ ")  Segmentation did not complete.");
			return;
		}

		// need sufficient segments
		if (m_seg.getSegmentedStrokes().size() < 5) {
			m_passed = false;
			m_fail = 1;
		}

		double disFirst = -1;
		double disLast = -1;
		m_sizeRatio = -1;
		if (m_passed) {

			// determine if first or last segment is longest (this is the tail)
			Stroke first = m_seg.getSegmentedStrokes().get(0);
			Stroke last = m_seg.getSegmentedStrokes().get(
					m_seg.getSegmentedStrokes().size() - 1);
			disFirst = endptDistance(first);
			disLast = endptDistance(last);

			// determine size ratio between largest and smallest
			if (disFirst > disLast)
				m_sizeRatio = disLast / disFirst;
			else
				m_sizeRatio = disFirst / disLast;

			// size ratio should be small
			if (m_sizeRatio > 0.4) {
				m_passed = false;
				m_fail = 2;
			}

			// endpt:strokelength distance should be larger
			if (m_features.getEndptStrokeLengthRatio() < 0.1) {
				m_passed = false;
				m_fail = 3;
			}

			if (m_passed) {

				// see if dot can be found
				List<Stroke> sub = new ArrayList<Stroke>();
				Stroke line = new Stroke();
				if (disFirst > disLast) {
					sub = m_seg.getSegmentedStrokes().subList(1,
							m_seg.getSegmentedStrokes().size());
					line = m_seg.getSegmentedStrokes().get(0);
				} else {
					sub = m_seg.getSegmentedStrokes().subList(0,
							m_seg.getSegmentedStrokes().size() - 1);
					line = m_seg.getSegmentedStrokes().get(
							m_seg.getSegmentedStrokes().size() - 1);
				}
				Stroke newStroke = combineAll(sub);
				StrokeFeatures newFeatures = new StrokeFeatures(newStroke,
						m_features.isSmoothingOn());
				DotFit dotFit = new DotFit(newFeatures);
				m_dotDensity = dotFit.getDensity();
				m_dotRevs = newFeatures.getNumRevolutions();
				if (dotFit.getDensity() < .15
						|| newFeatures.getNumRevolutions() < 1.0) {
					m_passed = false;
					m_fail = 4;
				}
				m_subShapes.add(dotFit.getShape());
				LineFit lineFit = new LineFit(new StrokeFeatures(line,
						m_features.isSmoothingOn()), true);
				m_subShapes.add(lineFit.getShape());
			}

		}

		// generate shape
		generateNBC();
		try {
			computeBeautified();
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("NBCFit: passed = " + m_passed + "(" + m_fail
//				+ ") num segs = " + m_seg.getSegmentedStrokes().size()
//				+ " dis first = " + disFirst + " dis last = " + disLast
//				+ " size ratio = " + m_sizeRatio + " endpt:sl ratio = "
//				+ m_features.getEndptStrokeLengthRatio());
	}

	/**
	 * Density of sub dot
	 * 
	 * @return density
	 */
	public double getDotDensity() {
		return m_dotDensity;
	}

	/**
	 * Number of revolutions of sub dot
	 * 
	 * @return num revs
	 */
	public double getDotRevs() {
		return m_dotRevs;
	}

	/**
	 * Ratio between largest and smallest segment
	 * 
	 * @return ratio
	 */
	public double getSizeRatio() {
		return m_sizeRatio;
	}

	/**
	 * Creates beautified gull
	 */
	private void generateNBC() {
		// TODO: beautify properly

		// use actual stroke as beautified version for the time being
		GeneralPath nbc = new GeneralPath();
		nbc.moveTo(m_features.getFirstOrigPoint().getX(),
				m_features.getFirstOrigPoint().getY());
		for (int i = 1; i < m_features.getOrigPoints().size(); i++)
			nbc.lineTo(m_features.getOrigPoints().get(i)
					.getX(), m_features.getOrigPoints().get(i).getY());
		m_shape = new SVGPath(nbc);
	}

	/**
	 * Get the distance between the endpoints of a stroke
	 * 
	 * @param str
	 *            stroke
	 * @return distance between endpoints of stroke
	 */
	private double endptDistance(Stroke str) {
		return str.getLastPoint().distance(str.getFirstPoint());
	}

	/**
	 * Combine multiple strokes into a single stroke (these are assumed to be in
	 * order already)
	 * 
	 * @param strokes
	 *            stroke to combine
	 * @return combined stroke
	 */
	private Stroke combineAll(List<Stroke> strokes) {
		Stroke newStroke = new Stroke();
		for (Stroke s : strokes) {
			for (int i = 0; i < s.getNumPoints(); i++)
				newStroke.addPoint(s.getPoint(i));
		}
		return newStroke;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return Fit.NBC;
	}
}
