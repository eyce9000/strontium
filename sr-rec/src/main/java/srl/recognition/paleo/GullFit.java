/**
 * GullFit.java
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
import org.openawt.svg.SVGGroup;
import org.openawt.svg.SVGPath;

import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.segmentation.paleo.VSegmenter;


/**
 * Fit stroke to a "gull" (double arches - like the McDonalds arches)
 * 
 * @author bpaulson
 */
public class GullFit extends Fit {

	/**
	 * Wave segmentation
	 */
	protected Segmentation m_seg;

	/**
	 * Slope list
	 */
	protected List<Double> m_slopes;

	/**
	 * Determines if gull was drawn right to left or left to right
	 */
	protected boolean m_leftToRight;

	/**
	 * Smallest segment to sum ratio
	 */
	protected double m_smallSumRatio;

	/**
	 * Angle difference between middle segments
	 */
	protected double m_angle;

	/**
	 * Get the percentage of the horizontal alignment test that passed
	 */
	protected double m_pctHorizontalAlignmentPass;

	/**
	 * Average slope of first and last segment
	 */
	protected double m_slopeAvg;

	/**
	 * Percentage of slope test that passed
	 */
	protected double m_pctSlopeTest;

	/**
	 * Paleo config
	 */
	protected PaleoConfig m_config;

	/**
	 * Constructor for gull fit
	 * 
	 * @param features
	 *            feature of the stroke to fit
	 * @param waveSeg
	 *            wave segmentation of stroke
	 * @param polyFit
	 *            polyline fit
	 */
	public GullFit(StrokeFeatures features, Segmentation waveSeg,
			PolylineFit polyFit, PaleoConfig config) {
		super(features);
		m_slopes = new ArrayList<Double>();
		m_passed = true;
		m_seg = waveSeg;
		m_config = config;
		double largest = Double.MIN_VALUE;
		double smallest = Double.MAX_VALUE;

		if (m_passed) {

			// segmentation cant be null
			if (m_seg == null) {
				m_passed = false;
				m_fail = 0;
//				log.debug("GullFit: passed = " + m_passed + "(" + m_fail
//						+ ")  Segmentation did not complete.");
				return;
			}

			List<Stroke> tmpStrokes = new ArrayList<Stroke>();

			// test 1: should be 4 lines
			if (m_seg.getSegmentedStrokes().size() != 4
					&& m_seg.getSegmentedStrokes().size() != 5) {
				m_passed = false;
				m_fail = 1;
			}

			// test 2: lines of segmentation should alternate slopes
			if (m_passed) {
				double slope1 = getSlopeSign(m_seg.getSegmentedStrokes().get(0));
				double slope2 = getSlopeSign(m_seg.getSegmentedStrokes().get(1));
				double slope3 = getSlopeSign(m_seg.getSegmentedStrokes().get(2));
				double slope4 = getSlopeSign(m_seg.getSegmentedStrokes().get(3));

				// check for tails
				if (m_seg.getSegmentedStrokes().size() == 5) {
					double small = endptDistance(m_seg.getSegmentedStrokes()
							.get(0));
					int smallestIndex = 0;
					double sum = small;
					for (int i = 1; i < m_seg.getSegmentedStrokes().size(); i++) {
						double dis = endptDistance(m_seg.getSegmentedStrokes()
								.get(i));
						if (dis < small) {
							small = dis;
							smallestIndex = i;
						}
						sum += dis;
					}
					sum /= m_seg.getSegmentedStrokes().size();
					m_smallSumRatio = (small / sum);
					if (m_smallSumRatio > 0.55) {
						m_passed = false;
						m_fail = 2;
					}
					for (int i = 0; i < m_seg.getSegmentedStrokes().size(); i++) {
						if (i != smallestIndex) {
							tmpStrokes.add(m_seg.getSegmentedStrokes().get(i));
						}
					}
					slope1 = getSlopeSign(tmpStrokes.get(0));
					slope2 = getSlopeSign(tmpStrokes.get(1));
					slope3 = getSlopeSign(tmpStrokes.get(2));
					slope4 = getSlopeSign(tmpStrokes.get(3));
				} else {
					tmpStrokes.add(m_seg.getSegmentedStrokes().get(0));
					tmpStrokes.add(m_seg.getSegmentedStrokes().get(1));
					tmpStrokes.add(m_seg.getSegmentedStrokes().get(2));
					tmpStrokes.add(m_seg.getSegmentedStrokes().get(3));
				}

				m_slopes.add(slope1);
				m_slopes.add(slope2);
				m_slopes.add(slope3);
				m_slopes.add(slope4);

				// check left to right
				m_leftToRight = true;
				if (m_passed) {
					m_pctSlopeTest = validSlopes(slope1, slope2, slope3, slope4);
					if (m_pctSlopeTest != 1.0) {
						m_passed = false;
						m_fail = 3;
					}

					// check right to left
					if (!m_passed) {
						double tmp = validSlopesOpposite(slope1, slope2,
								slope3, slope4);
						if (tmp > m_pctSlopeTest)
							m_pctSlopeTest = tmp;
						if (m_pctSlopeTest == 1.0) {
							m_passed = true;
							m_leftToRight = false;
						}
					}
				}

				// make sure slope2 and slope3 aren't close to linear
				if (m_passed) {
					double s1 = getSlope(tmpStrokes.get(1));
					double s2 = getSlope(tmpStrokes.get(2));
					m_angle = angleBetween(s1, s2);
					// System.out.println("s1 = " + s1 + " s2 = " + s2 +
					// " angle = "
					// + angle);
					if (m_angle < 25.0) {
						m_passed = false;
						m_fail = 4;
					}
				}

				// make sure horizontal alignment is good
				if (m_passed) {
					double currX = m_seg.getSegmentedStrokes().get(0)
							.getFirstPoint().getX();
					double numTested = 0.0;
					double numPassed = 0.0;
					for (int i = 1; i < m_seg.getSegmentedStrokes().size(); i++) {
						double x = m_seg.getSegmentedStrokes().get(i)
								.getFirstPoint().getX();
						if ((m_leftToRight && x < currX)
								|| (!m_leftToRight && x > currX)) {
							m_passed = false;
							m_fail = 5;
						} else {
							numPassed++;
						}
						numTested++;
						currX = x;
					}
					m_pctHorizontalAlignmentPass = numPassed / numTested;
				}

				// make sure first and last slopes aren't near vertical
				if (m_passed) {
					double s1 = Math.abs(getSlope(tmpStrokes.get(0)));
					double s2 = Math.abs(getSlope(tmpStrokes.get(3)));
					double avg = (s1 + s2) / 2.0;
					if (Double.isInfinite(s1))
						avg = s2;
					if (Double.isInfinite(s2))
						avg = s1;
					// System.out.println("s1 = " + s1 + " s2 = " + s2 +
					// " avg = " + avg);
					m_slopeAvg = avg;
					if (avg > 9.0) {
						m_passed = false;
						m_fail = 6;
					}
				}
			}

			// test 3: lines should be similar in size
			if (m_passed) {
				for (int i = 0; i < tmpStrokes.size(); i++) {
					double dis = endptDistance(tmpStrokes.get(i));
					if (dis > largest)
						largest = dis;
					if (dis < smallest)
						smallest = dis;
				}
				if (smallest / largest < 0.15) {
					m_passed = false;
					m_fail = 7;
				}
			}

		}

		// generate beautified gull (not fully implemented yet)
		generateGull();
		try {
			computeBeautified();
			m_beautified.setShapes(polyFit.getSubShapes());
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("GullFit: passed = " + m_passed + "(" + m_fail
//				+ ")  error = " + m_err + "  dcr = " + m_features.getDCR()
//				+ " ndde = " + m_features.getNDDE() + "  sub strokes = "
//				+ m_seg.getSegmentedStrokes().size() + " endpt:sl ratio = "
//				+ m_features.getEndptStrokeLengthRatio() + " max curv = "
//				+ m_features.getMaxCurv() + " avg curv = "
//				+ m_features.getAvgCurvature() + " slopes = "
//				+ m_slopes.toString() + " largest = " + largest
//				+ " smallest = " + smallest + " ratio = "
//				+ (smallest / largest) + " left-right = " + m_leftToRight);
	}

	/**
	 * Get the ratio between the smallest segment and the sum
	 * 
	 * @return ratio
	 */
	public double getSmallSumRatio() {
		return m_smallSumRatio;
	}

	/**
	 * Get the angle between the middle segments
	 * 
	 * @return angle
	 */
	public double getAngle() {
		return m_angle;
	}

	/**
	 * Get the percentage of the horizontal alignment test that passed
	 * 
	 * @return percent
	 */
	public double getPctHorizontalAlignmentPass() {
		return m_pctHorizontalAlignmentPass;
	}

	/**
	 * Get average slope of first and last segment
	 * 
	 * @return average slope
	 */
	public double getSlopeAvg() {
		return m_slopeAvg;
	}

	/**
	 * Get percentage of slope test that passed
	 * 
	 * @return percent passed
	 */
	public double getPctSlopeTest() {
		return m_pctSlopeTest;
	}

	/**
	 * Creates beautified gull
	 */
	protected void generateGull() {
		try {
			VSegmenter vSeg = new VSegmenter(m_features);
			Segmentation seg = vSeg.getSegmentations().get(0);
			List<Stroke> str = seg.getSegmentedStrokes();
			ArcFit a1 = new ArcFit(new StrokeFeatures(str.get(0),
					m_features.isSmoothingOn()), m_config);
			ArcFit a2 = new ArcFit(new StrokeFeatures(str.get(1),
					m_features.isSmoothingOn()), m_config);
			m_shape = new SVGGroup();
			((SVGGroup)m_shape).addShape(a1.toSVGShape());
			((SVGGroup)m_shape).addShape(a2.toSVGShape());
			
		} catch (Exception e) {
			// use actual stroke as beautified version for the time being
			GeneralPath gull = new GeneralPath();
			gull.moveTo(m_features.getFirstOrigPoint()
					.getX(), m_features.getFirstOrigPoint().getY());
			for (int i = 1; i < m_features.getOrigPoints().size(); i++)
				gull.lineTo(m_features.getOrigPoints()
						.get(i).getX(), m_features.getOrigPoints().get(i)
						.getY());
			m_shape = new SVGPath(gull);
		}
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

	/**
	 * Get the slope of a stroke
	 * 
	 * @param str
	 *            stroke
	 * @return slope of stroke
	 */
	protected double getSlope(Stroke str) {
		Point p2 = str.getLastPoint();
		Point p1 = str.getFirstPoint();
		return (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
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
	 * Determine if the slopes given are valid for a gull fit
	 * 
	 * @param slope1
	 *            slope 1
	 * @param slope2
	 *            slope 2
	 * @param slope3
	 *            slope 3
	 * @param slope4
	 *            slope 4
	 * @return percent passed (need 1.0 for validity)
	 */
	protected double validSlopes(double slope1, double slope2, double slope3,
			double slope4) {
		double numBad = 0.0;
		if (slope1 != -1.0 && slope1 != 0.0)
			numBad++;
		if (slope2 != 1.0 && slope2 != 0.0)
			numBad++;
		if (slope3 != -1.0 && slope3 != 0.0)
			numBad++;
		if (slope4 != 1.0 && slope4 != 0.0)
			numBad++;
		return (4.0 - numBad) / 4.0;
	}

	/**
	 * Determine if the slopes given are valid for a gull fit drawn in the
	 * opposite direction (right to left)
	 * 
	 * @param slope1
	 *            slope 1
	 * @param slope2
	 *            slope 2
	 * @param slope3
	 *            slope 3
	 * @param slope4
	 *            slope 4
	 * @return true if valid, else false
	 */
	protected double validSlopesOpposite(double slope1, double slope2,
			double slope3, double slope4) {
		double numBad = 0.0;
		if (slope1 != 1.0 && slope1 != 0.0)
			numBad++;
		if (slope2 != -1.0 && slope2 != 0.0)
			numBad++;
		if (slope3 != 1.0 && slope3 != 0.0)
			numBad++;
		if (slope4 != -1.0 && slope4 != 0.0)
			numBad++;
		return (4.0 - numBad) / 4.0;
	}

	/**
	 * Determines the angle between two slopes
	 * 
	 * @param s1
	 *            slope 1
	 * @param s2
	 *            slope 2
	 * @return angle between slope1 and slope2
	 */
	protected double angleBetween(double s1, double s2) {
		double value = Math.atan((s1 - s2) / (1 + s1 * s2)) * (180 / Math.PI);
		if (Double.isNaN(value))
			value = 90.0;
		value = Math.abs(value);
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return Fit.GULL;
	}
}
