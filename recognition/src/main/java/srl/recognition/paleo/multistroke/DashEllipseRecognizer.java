/**
 * DashEllipseRecognizer.java
 * 
 * Revision History:<br>
 * Feb 23, 2009 bpaulson - File created
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
package srl.recognition.paleo.multistroke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.core.sketch.comparators.TimePeriodComparator;
import srl.core.util.IsAConstants;
import srl.recognition.paleo.ArcFit;
import srl.recognition.paleo.EllipseFit;
import srl.recognition.paleo.Fit;
import srl.recognition.paleo.OrigPaleoSketchRecognizer;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.Thresholds;


/**
 * Dashed ellipse recognizer
 * 
 * @author bpaulson
 */
public class DashEllipseRecognizer {

	/**
	 * Input shapes
	 */
	private List<Shape> m_input;

	/**
	 * Output shapes
	 */
	private List<Shape> m_output;

	/**
	 * Current stroke
	 */
	private Stroke m_currStroke = new Stroke();

	/**
	 * Paleo config
	 */
	private PaleoConfig m_config = PaleoConfig.allOff();

	/**
	 * Paleo recognizer
	 */
	private OrigPaleoSketchRecognizer m_paleo;

	/**
	 * Slope of the direction graph of the previous stroke
	 */
	private double m_slopeDirPrev = Double.NaN;

	/**
	 * Constructor
	 * 
	 * @param input
	 *            input shapes
	 */
	public DashEllipseRecognizer(List<Shape> input) {
		m_input = input;
		Collections.sort(m_input, new TimePeriodComparator());
		m_config.setEllipseTestOn(true);
		m_config.setArcTestOn(true);
		m_config.getHeuristics().ARC_DOWN = false;
		m_paleo = new OrigPaleoSketchRecognizer(m_config);
	}

	/**
	 * Recognize dashed lines within a list of shapes (given in constructor)
	 * 
	 * @return new set of shapes
	 */
	public List<Shape> recognize() {
		m_output = new ArrayList<Shape>();
		if (m_input.size() <= 0)
			return m_output;
		List<Shape> possibleDash = new ArrayList<Shape>();

		// populate m_output
		for (Shape shape : m_input) {
			// get lines of a dashed line
			if (shape.hasAttribute(IsAConstants.DASHED)
					&& shape.getInterpretation().label
							.equalsIgnoreCase(Fit.LINE)) {
				for (Shape ss : shape.getShapes())
					addShape(ss, possibleDash);
			} else
				addShape(shape, possibleDash);
		}
		Shape last = m_input.get(m_input.size() - 1);
		m_currStroke.addPoint(last.getFirstStroke().getFirstPoint());
		m_currStroke.addPoint(last.getFirstStroke().getPoint(
				last.getFirstStroke().getNumPoints() / 2));
		m_currStroke.addPoint(last.getFirstStroke().getLastPoint());
		generateDash(possibleDash);
		Collections.sort(m_output, new TimePeriodComparator());
		return m_output;
	}

	/**
	 * Add shape to list of possible dashes
	 * 
	 * @param shape
	 *            shape to add
	 * @param possibleDash
	 *            list to add to
	 */
	private void addShape(Shape shape, List<Shape> possibleDash) {
		// non-lines go into output
		if (!shape.getInterpretation().label.equals(Fit.LINE)
				&& !shape.getInterpretation().label.startsWith(Fit.POLYLINE)
				&& !shape.getInterpretation().label.equals(Fit.ARC)) {
			m_output.add(shape);
		}

		// handle the line
		else {

			// first line in list
			if (possibleDash.size() == 0) {
				possibleDash.add(shape);
				m_currStroke = new Stroke();
				m_currStroke.addPoint(shape.getFirstStroke().getFirstPoint());
				m_currStroke.addPoint(shape.getFirstStroke().getPoint(
						shape.getFirstStroke().getNumPoints() / 2));
				m_currStroke.addPoint(shape.getFirstStroke().getLastPoint());
				m_slopeDirPrev = Double.NaN;
			}

			// dash continues
			else if (isDashContinuation(possibleDash, shape)) {
				possibleDash.add(shape);
			}

			// we have a line but its not part of the current dash
			else {
				// create dash shape
				generateDash(possibleDash);
				possibleDash.clear();
				possibleDash.add(shape);
				m_currStroke = new Stroke();
				m_currStroke.addPoint(shape.getFirstStroke().getFirstPoint());
				m_currStroke.addPoint(shape.getFirstStroke().getPoint(
						shape.getFirstStroke().getNumPoints() / 2));
				m_currStroke.addPoint(shape.getFirstStroke().getLastPoint());
				m_slopeDirPrev = Double.NaN;
			}
		}
	}

	/**
	 * Generates a dash shape and adds it to output
	 */
	private void generateDash(List<Shape> possibleDash) {
		// remove last 3 points from currStroke (the stroke that
		// "broke the camel's back")
		if (m_currStroke.getNumPoints() > 2) {
			for (int i = 0; i < 3; i++)
				m_currStroke.getPoints()
						.remove(m_currStroke.getNumPoints() - 1);
		}

		if (m_currStroke.getNumPoints() <= 0) {
			for (Shape s : possibleDash)
				m_output.add(s);
			return;
		}

		// stroke must pass ellipse test
		m_paleo.setStrokeRecalc(m_currStroke);
		EllipseFit ellFit = m_paleo.getEllipseFit();
		double dist = m_currStroke.getLastPoint().distance(
				m_currStroke.getFirstPoint());
		double ratio = dist / m_currStroke.getPathLength();

		// dash must have at least 5 lines to be dashed ellipse
		if (possibleDash.size() > 4 && ellipseTestPassed(ellFit)
				&& ratio < 0.21) {
			Shape newShape = new Shape();
			newShape.setLabel(Fit.ELLIPSE);
			newShape.setAttribute(IsAConstants.PRIMITIVE,
					IsAConstants.PRIMITIVE);
			newShape.setAttribute(IsAConstants.DASHED, IsAConstants.DASHED);
			newShape.setAttribute(IsAConstants.CLOSED, IsAConstants.CLOSED);
			// newShape.setRecognitionTime(System.currentTimeMillis());
			List<Stroke> subStrokes = new ArrayList<Stroke>();
			List<Shape> subShapes = new ArrayList<Shape>();
			double conf = 0.0;
			for (Shape s : possibleDash) {
				subShapes.add(s);
				subStrokes.addAll(s.getStrokes());
				conf += s.getInterpretation().confidence;
			}
			conf /= (double) possibleDash.size();
			newShape.setStrokes(subStrokes);
			newShape.setShapes(subShapes);
			newShape.getInterpretation().confidence = (conf);
			m_output.add(newShape);
		} else {
			for (Shape s : possibleDash)
				m_output.add(s);
		}
	}

	/**
	 * Determines if new shape is a continuation of the dashed lines in current
	 * 
	 * @param current
	 *            list of current dashed line
	 * @param newShape
	 *            candidate for dash continuation
	 * @return true if possible candidate else false
	 */
	private boolean isDashContinuation(List<Shape> current, Shape newShape) {
		Shape lastShape = current.get(current.size() - 1);
		double distanceToLast = lastShape.getLastStroke().getLastPoint()
				.distance(newShape.getFirstStroke().getFirstPoint());
		double lengthPrev = lastShape.getLastStroke().getPathLength()
				+ distanceToLast;
		double strokeLength = lastShape.getLastStroke().getPathLength()
				+ newShape.getFirstStroke().getPathLength();
		double endptDis = lastShape.getLastStroke().getFirstPoint()
				.distance(newShape.getFirstStroke().getLastPoint());
		double lastToLast = lastShape.getLastStroke().getLastPoint()
				.distance(newShape.getLastStroke().getLastPoint());
		double avgStrokeLength = avgStrokeLength(current, newShape);
		double ratio = endptDis / (strokeLength + distanceToLast);
		double ratio2 = distanceToLast / avgStrokeLength;
		double ratio4 = (1.0 - ratio + ratio2) / 2.0;
		double ratio5 = (lastToLast - distanceToLast)
				/ newShape.getFirstStroke().getPathLength();
		m_currStroke.addPoint(newShape.getFirstStroke().getFirstPoint());
		m_currStroke.addPoint(newShape.getFirstStroke().getPoint(
				newShape.getFirstStroke().getNumPoints() / 2));
		m_currStroke.addPoint(newShape.getFirstStroke().getLastPoint());
		m_paleo.setStrokeRecalc(m_currStroke);
		ArcFit arcFit = m_paleo.getArcFit();
		EllipseFit ellFit = m_paleo.getEllipseFit();
		double slopeDirGraph = m_paleo.getFeatures().getSlopeDirGraph();
		boolean slopeDirCheck = false;
		if (Math.signum(slopeDirGraph) == Math.signum(m_slopeDirPrev)
				|| Double.isNaN(m_slopeDirPrev))
			slopeDirCheck = true;
		m_slopeDirPrev = slopeDirGraph;
		if ((arcTestPassed(arcFit) || arcFit.getError() < Thresholds.active.M_ARC_FEATURE_AREA || ellFit
				.passed())
				&& ratio > 0.52
				&& distanceToLast < endptDis
				&& distanceToLast < lastToLast) {
			if (ratio5 <= 0.525
					&& !newShape.getInterpretation().label.equals(Fit.DOT))
				return false;
			if (ratio4 >= 1.8 && current.size() > 1)
				return false;
			return true;
		}
		return false;
	}

	/**
	 * Determines if arc test passed (have to remove some conditions used in
	 * ArcFit)
	 * 
	 * @param af
	 *            arc fit to test
	 * @return true if passed, else false
	 */
	private boolean arcTestPassed(ArcFit af) {
		boolean passed = true;

		// test 1: stroke must not be closed or overtraced
		if (af.getFeatures().isClosed() || af.getFeatures().isOvertraced()) {
			passed = false;
		}

		// test 2: make sure NDDE is high (close to 1.0);
		// ignore if this is a small arc because NDDE is unstable with small
		// arcs
		/*
		 * if (af.getRadius() > M_ARC_SMALL) { if (af.getFeatures().getNDDE() <
		 * 0.7 && (!af.getFeatures().dirWindowPassed() || af.getFeatures()
		 * .getMaxCurvToAvgCurvRatio() > 3.0)) { passed = false; }
		 * 
		 * } else { if (af.getFeatures().getNDDE() < 0.6 &&
		 * (!af.getFeatures().dirWindowPassed() || af.getFeatures()
		 * .getMaxCurvToAvgCurvRatio() > 3.0)) { passed = false; } }
		 */

		// test 3: dcr must be low
		if (af.getRadius() > Thresholds.active.M_ARC_SMALL) {
			if (af.getFeatures().getDCR() > Thresholds.active.M_DCR_TO_BE_POLYLINE) {
				passed = false;
			}
		} else {
			// if (af.getFeatures().getDCR() > 3.65)// 2.75)
			// passed = false;
		}

		if (af.getFeatures().getEndptStrokeLengthRatio() < 0.1) {
			passed = false;
		}

		// NEW TEST: area of ideal arc must be close to the area of the actual
		// stroke
		double arcAreaRatio = 0;
		double area = af.getFeatures().getBounds().getHeight()
				* af.getFeatures().getBounds().getWidth();
		if (af.getArcArea() < area)
			arcAreaRatio = af.getArcArea() / area;
		else
			arcAreaRatio = area / af.getArcArea();
		// TODO: test this before using
		/*
		 * if (arcAreaRatio < M_ARC_AREA_RATIO) passed = false;
		 */

		// test 4: feature area test (results used for error)
		if (af.getRadius() > Thresholds.active.M_ARC_SMALL) {

			// use feature area for large arcs
			if (af.getError() > Thresholds.active.M_ARC_FEATURE_AREA
					&& !af.getFeatures().dirWindowPassed()) {
				passed = false;
			}
		} else {

			// use arc area ratio for small arcs
			if (af.getError() > Thresholds.active.M_ARC_FEATURE_AREA && arcAreaRatio > 0.55) {
				passed = false;
			}
		}

		return passed;
	}

	/**
	 * Determines if ellipse test passed (have to remove some conditions used in
	 * EllipseFit)
	 * 
	 * @param ef
	 *            ellipse fit to test
	 * @return true if passed, else false
	 */
	private boolean ellipseTestPassed(EllipseFit ef) {
		boolean passed = true;

		// test 2: make sure NDDE is high (close to 1.0) or low (close to 0.0);
		// ignore small ellipses
		if (ef.getMajorAxisLength() > Thresholds.active.M_ELLIPSE_SMALL) {
			// if (ef.getFeatures().getNDDE() < 0.63) {
			// passed = false;
			// m_fail = 1;
			// }

			// closed test but with looser thresholds
			if (ef.getFeatures().getEndptStrokeLengthRatio() > 0.3
					&& ef.getFeatures().getNumRevolutions() < 1.0) {
				passed = false;
			}
		} else {
			if (ef.getFeatures().getNDDE() < 0.54) {
				passed = false;
			}

			// closed test but with looser thresholds
			if (ef.getFeatures().getEndptStrokeLengthRatio() > 0.26
					&& ef.getFeatures().getNumRevolutions() < 0.75) {
				passed = false;
			}
		}

		// test 3: feature area test (results used for error)
		if (ef.getMajorAxisLength() > Thresholds.active.M_ELLIPSE_SMALL) {
			if (ef.getError() > Thresholds.active.M_ELLIPSE_FEATURE_AREA) {
				passed = false;
			}
		} else {
			if (ef.getError() > Thresholds.active.M_ELLIPSE_FEATURE_AREA
					&& (ef.getError() > 0.7 || !ef.getFeatures()
							.dirWindowPassed())) {
				passed = false;
			}
		}

		if (ef.getPerimStrokeLengthRatio() < 0.55) {
			passed = false;
		}

		if (ef.getFeatures().getPctDirWindowPassed() <= 0.25
				&& ef.getFeatures().getNumRevolutions() < 0.4) {
			passed = false;
		}

		if (ef.getFeatures().getAvgCornerStrokeDistance() < 0.1) {
			passed = false;
		}

		if (ef.getFeatures().getStrokeLengthPerimRatio() > 1.15) {
			passed = false;
		}

		return passed;
	}

	/**
	 * Get the average stroke length of the strokes in a shape
	 * 
	 * @param shapes
	 *            shape
	 * @param current
	 *            current shape (to be factored too)
	 * @return avg stroke length
	 */
	public double avgStrokeLength(List<Shape> shapes, Shape current) {
		double sum = 0.0;
		double num = 0.0;
		for (Shape s : shapes) {
			for (Stroke str : s.getStrokes()) {
				sum += str.getPathLength();
				num++;
			}
		}
		for (Stroke str : current.getStrokes()) {
			sum += str.getPathLength();
			num++;
		}
		return sum / num;
	}
}
