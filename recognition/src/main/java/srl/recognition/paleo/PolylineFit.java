/**
 * PolylineFit.java
 * 
 * Revision History:<br>
 * Jun 24, 2008 bpaulson - File created
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
package srl.recognition.paleo;

import java.util.ArrayList;
import java.util.List;

import org.openawt.geom.Line2D;
import org.openawt.geom.Point2D;
import org.openawt.svg.SVGLine;
import org.openawt.svg.SVGPolyline;

import srl.core.exception.InvalidParametersException;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.core.util.IsAConstants;
import srl.segmentation.paleo.VSegmenter;


/**
 * Fit stroke to a polyline
 * 
 * @author bpaulson
 */
public class PolylineFit extends Fit {

	/**
	 * Flag denoting whether or not all sub-strokes passed a line test
	 */
	protected boolean m_allLinesPassed;

	/**
	 * Least squares error of polyline fit
	 */
	protected double m_lsqe;

	/**
	 * Number of sub-strokes that passed a line test
	 */
	protected int m_numPassed;

	/**
	 * Segmentation used for polyline fit
	 */
	protected Segmentation m_segmentation;

	/**
	 * List of subshapes for the polyline fit
	 */
	protected List<Shape> m_subshapes;

	/**
	 * Substrokes of polyline
	 */
	protected List<Stroke> m_subStrokes;

	/**
	 * Paleo config
	 */
	protected PaleoConfig m_config;

	/**
	 * Average angular direction
	 */
	protected double m_avgAngularDirection;

	/**
	 * Constructor for polyline fit
	 * 
	 * @param features
	 *            features of the stroke
	 * @param segmentation
	 *            segmentation to use for polyline fit
	 * @param config
	 *            paleo configuration (used to get heuristics)
	 */
	public PolylineFit(StrokeFeatures features, Segmentation segmentation,
			PaleoConfig config) {
		super(features);
		m_config = config;
		m_segmentation = segmentation;
		m_allLinesPassed = true;
		m_err = 0;
		m_lsqe = 0;
		m_subStrokes = m_segmentation.getSegmentedStrokes();
		m_subshapes = new ArrayList<Shape>();

		// check to see if polyline is actually an overtraced line
		if (config.getHeuristics().OVERTRACED_LINE_COMBINE) {
			boolean overtracedLine = combineOvertracedLine(m_subStrokes,
					m_features);

			// we have a line!
			if (overtracedLine) {
				try {
					computeBeautified();
					m_beautified.setShapes(m_subshapes);
					m_beautified.setLabel(Fit.POLYLINE + " (1)");
				} catch (Exception e) {
//					log.error("Could not create shape object: " + e.getMessage(),e);
					e.printStackTrace();
				}
//				log.debug("PolylineFit: passed = " + m_passed
//						+ " OVERTRACED LINE lsqe = " + m_lsqe
//						/ m_features.getStrokeLength() + " err = " + m_err);
				return;
			}
		}

		/*
		 * System.out.println("sizes:"); for (Stroke s : m_subStrokes)
		 * System.out.println(s.getNumPoints()); System.out.println("slopes:");
		 * for (Stroke s : m_subStrokes) System.out.println(getSlope(s));
		 */

		// System.out.println("num substrokes = " + m_subStrokes.size());
		// check for small, extraneous lines in interpretation
		if (config.getHeuristics().SMALL_POLYLINE_COMBINE
				&& m_subStrokes.size() < 10) {
			m_subStrokes = combineSmallLines(m_subStrokes);
		}

		// System.out.println("num substrokes = " + m_subStrokes.size());

		// check for consecutive lines with similar slopes, combine if similar
		if (config.getHeuristics().SIM_SLOPE_POLYLINE_COMBINE) {
			m_subStrokes = combineSimilarSlopes(m_subStrokes);
		}

		// System.out.println("num substrokes = " + m_subStrokes.size());

		// check for consecutive lines that, when combined, pass a line test
		if (config.getHeuristics().LINE_TEST_COMBINE && m_subStrokes.size() > 2) {
			m_subStrokes = combineLineTest(m_subStrokes);
		}

		// System.out.println("num substrokes = " + m_subStrokes.size());

		// test 1: we need at least 2 substrokes
		if (m_subStrokes.size() < 2) {
			m_passed = false;
			m_fail = 0;
		} else {
			// test 2: run line test on all sub strokes and get a cumulative
			// error
			for (int i = 0; i < m_subStrokes.size(); i++) {

				// if we have 2 or fewer points we automatically have a line
				if (m_subStrokes.get(i).getNumPoints() > 1) {
					LineFit lf = LineFit.getFit(m_subStrokes.get(i));
					m_err += lf.getError();
					m_lsqe += lf.getLSQE();
					m_subshapes.add(lf.getShape());
					if (!lf.passed()) {
						m_allLinesPassed = false;
						m_passed = false;
						m_fail = 1;
					} else
						m_numPassed++;
				} else
					m_numPassed++;
			}
			m_err /= m_features.getStrokeLength();
			m_lsqe /= m_features.getStrokeLength();

			// dcr should be high
			if (m_features.getDCR() < Thresholds.active.M_DCR_TO_BE_POLYLINE) {
				m_passed = false;
				m_fail = 2;
			}

			// test 3.2: if total lsqe is low then accept even if not all
			// substrokes passed polyline test
			if (m_lsqe < Thresholds.active.M_POLYLINE_LS_ERROR) {// || m_allLinesPassed)
				m_passed = true;
				m_fail = 3;
			}
		}

		// calcular avg angular direction
		double sum = 0.0;
		for (Stroke s : m_subStrokes) {
			sum += Math.abs(angularDirection(s));
		}
		m_avgAngularDirection = (sum * 180.0) / (m_subStrokes.size() * Math.PI);

		// generate beautified polyline
		generatePolyline();
		try {
			computeBeautified();
			m_beautified.getAttributes().remove(IsAConstants.PRIMITIVE);
			m_beautified.setShapes(m_subshapes);
			m_beautified.setLabel(Fit.POLYLINE + " (" + m_subshapes.size()
					+ ")");
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

		/*
		 * System.out.println("sizes:"); for (Stroke s : m_subStrokes)
		 * System.out.println(s.getNumPoints()); System.out.println("slopes:");
		 * for (Stroke s : m_subStrokes) System.out.println(getSlope(s));
		 */

//		log.debug("PolylineFit: passed = " + m_passed + "(" + m_fail
//				+ ")  error = " + m_err + "  lsqe = " + m_lsqe + "  dcr = "
//				+ m_features.getDCR() + " ndde = " + m_features.getNDDE()
//				+ "  sub strokes = " + m_subStrokes.size()
//				+ "  corners used = " + m_segmentation.toString()
//				+ " lines passed = " + m_allLinesPassed + " endpt:sl ratio = "
//				+ m_features.getEndptStrokeLengthRatio() + " avg ang dir = "
//				+ m_avgAngularDirection);

	}

	/**
	 * Check list of strokes to see if any can be combined by checking to see if
	 * together they pass a line test
	 * 
	 * @param strokes
	 *            strokes to check
	 * @return merged/combined strokes
	 */
	private List<Stroke> combineLineTest(List<Stroke> strokes) {
		if (strokes.size() <= 1)
			return strokes;
		for (int i = 1; i < strokes.size(); i++) {
			Stroke newStroke = combine(strokes.get(i - 1), strokes.get(i));
			StrokeFeatures newFeatures;
			if (m_config.getHeuristics().FILTER_DIR_GRAPH)
				newFeatures = new StrokeFeatures(newStroke, true);
			else
				newFeatures = new StrokeFeatures(newStroke, false);
			LineFit lineFit = new LineFit(newFeatures, true);
			if (lineFit.passed()) {
				// System.out.println("line fit: " + lineFit.m_err + " lsqe = "
				// + lineFit.m_lsqe/lineFit.m_features.getStrokeLength() +
				// " ratio = " + lineFit.m_ratio);
				strokes.remove(i - 1);
				strokes.remove(i - 1);
				strokes.add(i - 1, newStroke);
				strokes = combineLineTest(strokes);
			}
		}
		return strokes;
	}

	/**
	 * Check list of strokes to see if any can be combined by checking to see if
	 * there is a small change in slope between consecutive lines
	 * 
	 * @param strokes
	 *            strokes to check
	 * @return merged/combined strokes
	 */
	private List<Stroke> combineSimilarSlopes(List<Stroke> strokes) {
		if (strokes.size() <= 1)
			return strokes;
		for (int i = 1; i < strokes.size(); i++) {
			double s1 = getSlope(strokes.get(i));
			double s2 = getSlope(strokes.get(i - 1));
			// double slopeDiff = Math.abs(s1 - s2);
			double value = Math.atan((s1 - s2) / (1 + s1 * s2))
					* (180 / Math.PI);
			if (Double.isNaN(value))
				value = 90.0;
			// System.out.println("angle diff = " + value);
			value = Math.abs(value);
			double ySignS1 = Math.signum(strokes.get(i).getLastPoint().getY()
					- strokes.get(i).getFirstPoint().getY());
			double xSignS1 = Math.signum(strokes.get(i).getLastPoint().getX()
					- strokes.get(i).getFirstPoint().getX());
			double ySignS2 = Math.signum(strokes.get(i - 1).getLastPoint()
					.getY()
					- strokes.get(i - 1).getFirstPoint().getY());
			double xSignS2 = Math.signum(strokes.get(i - 1).getLastPoint()
					.getX()
					- strokes.get(i - 1).getFirstPoint().getX());
			/*
			 * double length = strokes.get(i).getFirstPoint().distance(
			 * strokes.get(i).getLastPoint()) + strokes.get(i -
			 * 1).getFirstPoint().distance( strokes.get(i - 1).getLastPoint());
			 */

			/*
			 * System.out.println("xSignS1 = " + xSignS1 + " ySignS1 = " +
			 * ySignS1 + " xSignS2 = " + xSignS2 + " ySignS2 = " + ySignS2 +
			 * " value = " + value + " length = " + length + " value*length = "
			 * + value length);
			 */

			if (value < 25.0 && value > 0 && ySignS1 == ySignS2
					&& xSignS1 == xSignS2 /* slopeDiff < (2.0 / 3.0) */) {
				Stroke newStroke = combine(strokes.get(i - 1), strokes.get(i));
				strokes.remove(i - 1);
				strokes.remove(i - 1);
				strokes.add(i - 1, newStroke);
				strokes = combineSimilarSlopes(strokes);
				// System.out.println("fail = " + value);
			}
		}
		return strokes;
	}

	/**
	 * Check list of strokes to see if any can be removed as insignificant
	 * substrokes (small percentage of total stroke)
	 * 
	 * @param strokes
	 *            strokes to check
	 * @return merged/combined strokes
	 */
	private List<Stroke> combineSmallLines(List<Stroke> strokes) {
		if (strokes.size() <= 1)
			return strokes;
		double numTotal = 0.0;
		for (Stroke s : strokes)
			numTotal += s.getNumPoints();
		// System.out.println("numPoints = " + numTotal);
		for (int i = 0; i < strokes.size(); i++) {
			// System.out.println("ratio = " + strokes.get(i).getNumPoints()
			// / numTotal);
			if (strokes.get(i).getNumPoints() / numTotal < 0.06) {
				// last stroke
				if (i == strokes.size() - 1) {
					if (i != 0) {
						Stroke newStroke = combine(strokes.get(i - 1),
								strokes.get(i));
						strokes.remove(i - 1);
						strokes.remove(i - 1);
						strokes.add(i - 1, newStroke);
					}
				}
				// middle stroke
				else {
					Stroke newStroke = combine(strokes.get(i),
							strokes.get(i + 1));
					strokes.remove(i);
					strokes.remove(i);
					strokes.add(i, newStroke);
				}
				strokes = combineSmallLines(strokes);
			}
		}
		return strokes;
	}

	/**
	 * Check list of strokes to see if any can be combined into an overtraced
	 * line
	 * 
	 * @param strokes
	 *            strokes to check
	 * @param features
	 *            features of original stroke
	 * @return true if overtraced line present, else false
	 */
	private boolean combineOvertracedLine(List<Stroke> strokes,
			StrokeFeatures features) {
		boolean passed = true;
		LineFit lineFit = new LineFit(features, false);
		double err = lineFit.getLSQE();
		double fa = lineFit.getError();
		if (err / m_features.getStrokeLength() > 1.29)
			passed = false;
		// System.out.println("line fit: passed = " + passed + " err = " + m_err
		// + " lsqe = " + m_lsqe / m_features.getStrokeLength());

		// compute slopes
		if (passed) {
			double maxSlopeDiff = Double.MIN_VALUE;
			List<Double> slopes = new ArrayList<Double>();
			for (Stroke str : strokes)
				slopes.add(getSlope(str));
			double diff;
			for (int i = 0; i < slopes.size(); i++) {
				for (int j = 0; j < slopes.size(); j++) {
					if (i != j) {
						diff = angleBetween(slopes.get(i), slopes.get(j));
						if (diff > maxSlopeDiff)
							maxSlopeDiff = diff;
					}
				}
			}
			if (maxSlopeDiff > 90.0)
				maxSlopeDiff -= 90.0;
			// System.out.println("max slope diff = " + maxSlopeDiff);
			if (maxSlopeDiff > 10.0)
				passed = false;
		}

		// set values
		if (passed) {
			m_allLinesPassed = true;
			m_subStrokes.clear();
			m_subStrokes.add(features.getOrigStroke());
			m_subshapes.clear();
			m_subshapes.add(lineFit.getShape());
			m_numPassed = 1;
			m_passed = true;
			m_shape = new SVGLine(m_features.getMajorAxis());
			m_err = fa;
			m_lsqe = err;
		}
		return passed;
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
	protected Stroke combine(Stroke s1, Stroke s2) {
		Stroke newStroke = new Stroke();
		for (int i = 0; i < s1.getNumPoints(); i++)
			newStroke.addPoint(s1.getPoints().get(i));
		for (int i = 1; i < s2.getNumPoints(); i++)
			newStroke.addPoint(s2.getPoints().get(i));

		newStroke.setParent(s1.getParent());

		return newStroke;
	}

	/**
	 * Get the slope of a given Stroke
	 * 
	 * @param s
	 *            stroke to find slope of
	 * @return slope of stroke (assumed to be a line)
	 */
	private double getSlope(Stroke s) {
		Point p1 = s.getFirstPoint();
		Point p2 = s.getLastPoint();
		return (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
	}

	/**
	 * Manually construct a polyline fit from a "V" segmentation
	 * 
	 * @param vseg
	 *            "V" segmentation
	 */
	public PolylineFit(VSegmenter vseg) {
		super(vseg.getFeatures());
		try {
			m_segmentation = vseg.getSegmentations().get(0);
		} catch (InvalidParametersException e1) {
		}
		m_allLinesPassed = true;
		m_err = 0;
		m_lsqe = 0;
		m_subStrokes = m_segmentation.getSegmentedStrokes();
		m_subshapes = new ArrayList<Shape>();

		// test 1: we need at least 2 substrokes
		if (m_subStrokes.size() < 2)
			m_passed = false;
		else {
			// test 2: run line test on all sub strokes and get a cumulative
			// error
			for (int i = 0; i < m_subStrokes.size(); i++) {

				// if we have 2 or fewer points we automatically have a line
				if (m_subStrokes.get(i).getNumPoints() > 2) {
					LineFit lf = LineFit.getFit(m_subStrokes.get(i));
					m_err += lf.getError();
					m_lsqe += lf.getLSQE();
					m_subshapes.add(lf.getShape());
					if (!lf.passed()) {
						m_allLinesPassed = false;
					} else
						m_numPassed++;
				} else
					m_numPassed++;
			}
			m_err /= m_features.getStrokeLength();
			m_lsqe /= m_features.getStrokeLength();
		}

		// generate beautified polyline
		generatePolyline();
		try {
			computeBeautified();
			m_beautified.getAttributes().remove(IsAConstants.PRIMITIVE);
			m_beautified.setShapes(m_subshapes);
			m_beautified.setLabel(Fit.POLYLINE + " (" + m_subshapes.size()
					+ ")");
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("VPolylineFit: passed = " + m_passed + "  error = " + m_err
//				+ "  lsqe = " + m_lsqe + "  dcr = " + m_features.getDCR()
//				+ "  sub strokes = " + m_subStrokes.size()
//				+ "  corners used = " + m_segmentation.toString()
//				+ " lines passed = " + m_allLinesPassed);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return POLYLINE;
	}

	/**
	 * Get the number of sub stroke lines in the polyline interpretation
	 * 
	 * @return number of sub strokes
	 */
	public int getNumSubStrokes() {
		return m_subStrokes.size();
	}

	/**
	 * Get the percentage of substrokes that passed a line test
	 * 
	 * @return percentage of passing strokes
	 */
	public double getPctPassed() {
		return m_numPassed / (double) getNumSubStrokes();
	}

	/**
	 * Get the sub stroke lines
	 * 
	 * @return sub strokes
	 */
	public List<Stroke> getSubStrokes() {
		return m_subStrokes;
	}

	/**
	 * Get the list of sub shapes for the fit
	 * 
	 * @return list of sub shapes
	 */
	public List<Shape> getSubShapes() {
		return m_subshapes;
	}

	/**
	 * Flag denoting whether or not all sub strokes passed a line test
	 * 
	 * @return true if all passed; else false
	 */
	public boolean allLinesPassed() {
		return m_allLinesPassed;
	}

	/**
	 * Get the least squares error
	 * 
	 * @return lsqe
	 */
	public double getLSQE() {
		return m_lsqe;
	}

	/**
	 * Get average angular direction
	 * 
	 * @return avg angular direction
	 */
	public double getAvgAngularDirection() {
		return m_avgAngularDirection;
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

	/**
	 * Get the angular direction of a stroke
	 * 
	 * @param str
	 *            stroke
	 * @return angular direction
	 */
	protected double angularDirection(Stroke str) {
		double angle;
		angle = Math.atan2(str.getLastPoint().getY()
				- str.getFirstPoint().getY(), str.getLastPoint().getX()
				- str.getFirstPoint().getX());
		return angle;
	}

	/**
	 * Generates the beautified polyline shape
	 */
	protected void generatePolyline() {
		List<Stroke> subStrokes = m_subStrokes;

		// just a single line
		if (subStrokes.size() <= 1) {
			m_shape = new SVGLine(new Line2D.Double(m_features.getFirstOrigPoint().toPoint2D(),
					m_features.getLastOrigPoint().toPoint2D()));
			
			return;
		}

		// multiple lines; ensure original endpoints are intact
		List<Point2D> points = new ArrayList<Point2D>();
		points.add(m_features.getFirstOrigPoint().toPoint2D());
		for(int i=0; i<subStrokes.size() - 1; i++){
			points.add(subStrokes.get(i).getPoints()
					.get(subStrokes.get(i).getNumPoints() - 1).toPoint2D());
		}
		points.add(m_features.getLastOrigPoint().toPoint2D());
		m_shape = new SVGPolyline(points);

	}
}
