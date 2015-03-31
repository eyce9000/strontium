/**
 * WaveFit.java
 * 
 * Revision History:<br>
 * Nov 20, 2008 bpaulson - File created
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

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;


/**
 * Fit stroke to a wave (sinusoidal)
 * 
 * @author bpaulson
 */
public class WaveFit extends Fit{

	/**
	 * Wave segmentation
	 */
	protected Segmentation m_seg;

	/**
	 * Slope list
	 */
	protected List<Double> m_slopes;

	/**
	 * Percentage of the slope test passed
	 */
	protected double m_pctSlopePassed;

	/**
	 * Ratio of smallest to largest segment
	 */
	protected double m_smallLargeRatio;

	/**
	 * Constructor for wave fit
	 * 
	 * @param features
	 *            feature of the stroke to fit
	 * @param seg
	 *            wave segmentation for stroke
	 */
	public WaveFit(StrokeFeatures features, Segmentation waveSeg) {
		super(features);
		m_slopes = new ArrayList<Double>();
		m_passed = true;
		m_seg = waveSeg;
		double largest = Double.MIN_VALUE;
		double smallest = Double.MAX_VALUE;

		// test 1: low dcr
		if (m_features.getDCR() > 7.0) {
			// m_passed = false;
			m_fail = 0;
		}

		// test 2: low average curvature
		// if (m_features.getAvgCurvature() > 0.15)
		// m_passed = false;

		// test 3: endpoint stroke length ratio should be near 0.5
		if (Math.abs(m_features.getEndptStrokeLengthRatio() - 0.5) > 0.4) {
			m_passed = false;
			m_fail = 1;
		}

		if (m_passed) {

			// segmentation cant be null
			if (m_seg == null) {
				m_passed = false;
				m_fail = 2;
				log.debug("WaveFit: passed = " + m_passed + "(" + m_fail
						+ ")  Segmentation did not complete.");
				return;
			}

			// test 4: at least 5 lines
			if (m_seg.getSegmentedStrokes().size() < 5) {
				m_fail = 3;
				m_passed = false;
			}

			// test 5: lines of segmentation should alternate slopes
			if (m_passed) {
				double slopePrev = getSlopeSign(m_seg.getSegmentedStrokes()
						.get(0));
				m_slopes.add(slopePrev);
				double numPassed = 0.0;
				double numTested = 0.0;
				for (int i = 1; i < m_seg.getSegmentedStrokes().size(); i++) {
					double slopeNow = getSlopeSign(m_seg.getSegmentedStrokes()
							.get(i));
					m_slopes.add(slopeNow);
					if (slopeNow == slopePrev) {
						m_passed = false;
						m_fail = 4;
					} else {
						numPassed++;
					}
					slopePrev = slopeNow;
					numTested++;
				}
				m_pctSlopePassed = numPassed / numTested;
			}

			// test 6: lines should be similar in size
			if (m_passed) {
				for (int i = 0; i < m_seg.getSegmentedStrokes().size(); i++) {
					double dis = endptDistance(m_seg.getSegmentedStrokes().get(
							i));
					if (dis > largest && m_slopes.get(i) != 0)
						largest = dis;
					if (dis < smallest && m_slopes.get(i) != 0)
						smallest = dis;
				}
				m_smallLargeRatio = smallest / largest;
				if (m_smallLargeRatio < 0.06) {
					m_passed = false;
					m_fail = 5;
				}
			}

		}

		// generate beautified wave (not fully implemented yet)
		try {
			generateWave();
			computeBeautified();
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("WaveFit: passed = " + m_passed + "(" + m_fail + ") error = "
//				+ m_err + "  dcr = " + m_features.getDCR() + " ndde = "
//				+ m_features.getNDDE() + "  sub strokes = "
//				+ m_seg.getSegmentedStrokes().size() + " endpt:sl ratio = "
//				+ m_features.getEndptStrokeLengthRatio() + " max curv = "
//				+ m_features.getMaxCurv() + " avg curv = "
//				+ m_features.getAvgCurvature() + " slopes = "
//				+ m_slopes.toString() + " largest = " + largest
//				+ " smallest = " + smallest + " ratio = "
//				+ (smallest / largest));
	}

	/**
	 * Get the percentage of the slope test that passed
	 * 
	 * @return pass percent
	 */
	public double getPctSlopePassed() {
		return m_pctSlopePassed;
	}

	/**
	 * Get the ratio between the smallest and largest segment of the wave
	 * segmentation
	 * 
	 * @return ratio between smallest and largest
	 */
	public double getSmallLargeRatio() {
		return m_smallLargeRatio;
	}

	/**
	 * Creates beautified wave
	 */
	protected void generateWave() {
		double numSteps = 100;
		BoundingBox bounds = m_features.getOrigStroke().getBoundingBox();
		boolean backwards = m_features.getFirstOrigPoint().getX() > m_features
				.getLastOrigPoint().getX();
		double xAxis = bounds.getCenterY();
		double amplitude = bounds.height / 2.0;
		double offset = (m_features.getFirstOrigPoint().getY() - xAxis)
				/ amplitude;
		if (backwards)
			offset = (m_features.getLastOrigPoint().getY() - xAxis) / amplitude;
		double phase = Math.asin(offset);
		double time = (m_seg.getSegmentedStrokes().size() / 2.0) * Math.PI
				* 2.0;
		double step = time / numSteps;
		double currTime = 0.0;
		double slopeSignFirst = getSlopeSign(m_seg.getSegmentedStrokes().get(0));

		// if slope is backwards then we need to add a bit more to the phase;
		// use hillclimbing to find other phase value
		if (slopeSignFirst == -1.0) {
			double s = 0.0;
			double target = Math.sin(phase);
			double prevBest = Double.MAX_VALUE;
			double diff = 0.0;
			double best = phase;
			for (int i = 1; i < 1000; i++) {
				s = (i / 1000.0) * Math.PI + phase;
				diff = Math.abs(target - Math.sin(s));
				if (diff <= prevBest) {
					prevBest = diff;
					best = s;
				}
			}
			phase = best;
		}

		// find best time value estimate for end points
		double endMag = m_features.getLastOrigPoint().getY() - xAxis;
		if (backwards)
			endMag = m_features.getFirstOrigPoint().getY() - xAxis;
		double endTime = Math.asin(endMag / amplitude) - phase;
		int numRevs = m_seg.getSegmentedStrokes().size() / 2;
		double t1 = endTime + (Math.PI * 2.0 * (numRevs));
		double t2 = endTime + (Math.PI * 2.0 * (numRevs + 1));
		// System.out.println("endMag = " + endMag + " endTime = " + endTime
		// + " time = " + time + " t1 = " + t1 + " t2 = " + t2);
		if (Math.abs(t1 - time) < Math.abs(t2 - time))
			time = t1;
		else
			time = t2;
		step = time / numSteps;

		// generate wave
		GeneralPath wave = new GeneralPath();
		if (!backwards)
			wave.moveTo(m_features.getFirstOrigPoint()
					.getX(), m_features.getFirstOrigPoint().getY());
		else
			wave.moveTo(
					m_features.getLastOrigPoint().getX(), m_features
							.getLastOrigPoint().getY());
		for (int i = 0; i < numSteps; i++) {
			currTime += step;
			double mag = amplitude * Math.sin(currTime + phase);
			double x = ((currTime / time) * bounds.width) + bounds.getLeft();
			double y = mag + xAxis;
			wave.lineTo(x, y);
		}
		
		m_shape = new SVGPath(wave);
	}

	/**
	 * Get the distance between the endpoints of a stroke
	 * 
	 * @param str
	 *            stroke
	 * @return distance between endpoints of stroke
	 */
	protected double endptDistance(Stroke str) {
		return str.getLastPoint().distance(str.getFirstPoint());
	}

	/**
	 * Get the sign of the slope of a given stroke (assumed to be a line)
	 * 
	 * @param str
	 *            stroke to compute sign of slope for
	 * @return sign of the slope of the stroke
	 */
	protected double getSlopeSign(Stroke str) {
		Point p2 = str.getLastPoint();
		Point p1 = str.getFirstPoint();
		if (p2.getX() - p1.getX() == 0)
			return 0;
		double slope = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
		return Math.signum(slope);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return Fit.WAVE;
	}

}
