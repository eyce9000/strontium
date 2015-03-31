/**
 * PaleoSketchRecognizer.java
 * 
 * Revision History:<br>
 * Jun 23, 2008 bpaulson - File created <br>
 * Oct 04, 2008 jbjohns - Recognize() returns List<RecognitionResult> <br>
 * Oct 09, 2008 jbjohns - recognize() sets shape recognition time <br>
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import srl.core.exception.InvalidParametersException;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.recognition.IRecognitionResult;
import srl.recognition.RecognitionResult;
import srl.recognition.recognizer.IRecognizer;
import srl.segmentation.combination.PolylineCombinationSegmenter;
import srl.segmentation.paleo.PaleoSegmenter;
import srl.segmentation.paleo.VSegmenter;
import srl.segmentation.paleo.WaveSegmenter;


/**
 * Paleosketch recognizer - recognizes basic primitive shapes: line, arc, curve,
 * circle, ellipse, spiral, helix, polyline, complex. This is a single stroke
 * recognizer. The list of strokes passed into this recognizer should contain
 * only one stroke, otherwise an exception is thrown.
 * 
 * @author bpaulson
 */
public class OrigPaleoSketchRecognizer implements
		IRecognizer<Stroke, IRecognitionResult> {

	// scores used for complex fitting
	protected static final int LINE_SCORE = 1;

	protected static final int ARC_SCORE = 2;

	protected static final int CURVE_SCORE = 4;

	protected static final int ELLIPSE_SCORE = 3;

	protected static final int CIRCLE_SCORE = 3;

	protected static final int SPIRAL_SCORE = 5;

	protected static final int HELIX_SCORE = 5;

	/**
	 * Feature values of the stroke we are recognizing
	 */
	protected StrokeFeatures m_features;

	/**
	 * Line fit of the stroke we are recognizing
	 */
	protected Fit m_lineFit;

	/**
	 * Curve fit of the stroke we are recognizing
	 */
	protected Fit m_curveFit;

	/**
	 * Arc fit of the stroke we are recognizing
	 */
	protected Fit m_arcFit;

	/**
	 * Circle fit of the stroke we are recognizing
	 */
	protected Fit m_circleFit;

	/**
	 * Ellipse fit of the stroke we are recognizing
	 */
	protected Fit m_ellipseFit;

	/**
	 * Helix fit of the stroke we are recognizing
	 */
	protected Fit m_helixFit;

	/**
	 * Spiral fit of the stroke we are recognizing
	 */
	protected Fit m_spiralFit;

	/**
	 * Polyline fit of the stroke we are recognizing
	 */
	protected Fit m_polylineFit;

	/**
	 * Complex fit of the stroke we are recognizing
	 */
	protected Fit m_complexFit;

	/**
	 * Polygon fit of the stroke we are recognizing
	 */
	protected Fit m_polygonFit;

	/**
	 * Arrow fit of the stroke we are recognizing
	 */
	protected Fit m_arrowFit;

	/**
	 * Rectangle fit of the stroke we are recognizing
	 */
	protected Fit m_rectangleFit;

	/**
	 * Square fit of the stroke we are recognizing
	 */
	protected Fit m_squareFit;

	/**
	 * Diamond fit of the stroke we are recognizing
	 */
	protected Fit m_diamondFit;

	/**
	 * Dot fit of the stroke we are recognizing
	 */
	protected Fit m_dotFit;

	/**
	 * Wave fit of the stroke we are recognizing
	 */
	protected Fit m_waveFit;

	/**
	 * Gull fit of the stroke we are recognizing
	 */
	protected Fit m_gullFit;

	/**
	 * Blob fit of the stroke we are recognizing
	 */
	protected Fit m_blobFit;

	/**
	 * Infinity fit of the stroke we are recognizing
	 */
	protected Fit m_infinityFit;

	/**
	 * NBC fit of the stroke we are recognizing
	 */
	protected Fit m_nbcFit;

	/**
	 * List of possible fits
	 */
	protected FitList m_fits;

	/**
	 * Complex score (used when ranking interpretations)
	 */
	protected double m_complexScore;

	/**
	 * Configuration file for the recognizer
	 */
	protected PaleoConfig m_config;

	/**
	 * Main corner finding interpretation
	 */
	protected Segmentation m_segmentation;

	/**
	 * Wave segmentation of stroke
	 */
	protected Segmentation m_waveSegmentation;

	/**
	 * Map of values to monitor when performing recognition
	 */
	private Map<String, Double> m_monitoredValues = new HashMap<String, Double>();

	/**
	 * Default constructor. Every fit is on, and no stroke is loaded.
	 */
	public OrigPaleoSketchRecognizer() {
		m_config = PaleoConfig.allOn();
		m_features = null;
	}

	/**
	 * Constructor
	 * 
	 * @param config
	 *            Configuration file for the fits to return
	 */
	public OrigPaleoSketchRecognizer(PaleoConfig config) {
		m_config = config;
		m_features = null;
	}

	/**
	 * Constructor
	 * 
	 * @param stroke
	 *            stroke to recognize
	 * @param config
	 *            Configuration file for the fits to return
	 */
	public OrigPaleoSketchRecognizer(Stroke stroke, PaleoConfig config) {
		m_config = config;
		setStroke(stroke);
	}

	/**
	 * Set the stroke to use in the primitive recognizer
	 * 
	 * @param stroke
	 *            Stroke to recognize
	 */
	public void setStroke(Stroke stroke) {
		if (m_features != null && m_features.getOrigStroke() != null
				&& stroke.equals(m_features.getOrigStroke()))
			return;
		if (m_config.getHeuristics().FILTER_DIR_GRAPH)
			m_features = new StrokeFeatures(stroke, true);
		else
			m_features = new StrokeFeatures(stroke, false);
		m_segmentation = null;
		m_waveSegmentation = null;
		if (m_fits != null)
			m_fits.clear();
		m_lineFit = new NullFit();
		m_curveFit = new NullFit();
		m_arcFit = new NullFit();
		m_circleFit = new NullFit();
		m_ellipseFit = new NullFit();
		m_helixFit = new NullFit();
		m_spiralFit = new NullFit();
		m_polylineFit = new NullFit();
		m_complexFit = new NullFit();
		m_polygonFit = new NullFit();
		m_arrowFit = new NullFit();
		m_rectangleFit = new NullFit();
		m_squareFit = new NullFit();
		m_diamondFit = new NullFit();
		m_dotFit = new NullFit();
		m_waveFit = new NullFit();
		m_gullFit = new NullFit();
		m_blobFit = new NullFit();
		m_infinityFit = new NullFit();
		m_nbcFit = new NullFit();
	}

	/**
	 * Set the stroke while also forcing a recomputation
	 * 
	 * @param stroke
	 *            stroke
	 */
	public void setStrokeRecalc(Stroke stroke) {
		if (m_config.getHeuristics().FILTER_DIR_GRAPH)
			m_features = new StrokeFeatures(stroke, true);
		else
			m_features = new StrokeFeatures(stroke, false);
		m_segmentation = null;
		m_waveSegmentation = null;
		if (m_fits != null)
			m_fits.clear();
		m_lineFit = new NullFit();
		m_curveFit = new NullFit();
		m_arcFit = new NullFit();
		m_circleFit = new NullFit();
		m_ellipseFit = new NullFit();
		m_helixFit = new NullFit();
		m_spiralFit = new NullFit();
		m_polylineFit = new NullFit();
		m_complexFit = new NullFit();
		m_polygonFit = new NullFit();
		m_arrowFit = new NullFit();
		m_rectangleFit = new NullFit();
		m_squareFit = new NullFit();
		m_diamondFit = new NullFit();
		m_dotFit = new NullFit();
		m_waveFit = new NullFit();
		m_gullFit = new NullFit();
		m_blobFit = new NullFit();
		m_infinityFit = new NullFit();
		m_nbcFit = new NullFit();
	}

	/**
	 * Get the stroke being recognized
	 * 
	 * @return stroke
	 */
	public Stroke getStroke() {
		return m_features.getOrigStroke();
	}

	/**
	 * Recognize the active stroke and return a list of valid shape
	 * interpretations (avoids having to go through IRecognitionResult to get
	 * results)
	 * 
	 * @return list of sorted shape interpretations
	 */
	public List<Shape> recognizeShape() {
		IRecognitionResult top = recognize();
		return top.getNBestList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognizer#recognize()
	 */
	public IRecognitionResult recognize() {
		if (m_features == null) {
			System.err.println("No stroke loaded into the recognizer");
			return null;
		}

		if (m_features.getNumPoints() < 2) {
			calcDotFit();
			RecognitionResult recResult = new RecognitionResult();
			recResult.addShapeToNBestList(m_dotFit.getShape());
			return recResult;
		}

		m_fits = new FitList();

		// calculate all fits
		calculateAllFits();

		// order fits
		orderFits();

		// remove fits not present in the config file
		removeUnwantedFits();

		// create list of shapes to return (in the form of recognition results)
		double n = 0;
		RecognitionResult paleoResults = new RecognitionResult();
		for (Fit fit : m_fits) {
			// track the time we're considering these shapes as recognized. This
			// is close enough to the calculateAllFits() call.
			Shape fitShape = fit.getShape();
			// fitShape.setRecognitionTime(new
			// Long(System.currentTimeMillis()));

			// hack for confidence. The first thing will have the highest value
			// of confidence (highest value of n).
			fitShape.getInterpretation().confidence = (1.0 - (n / (double) m_fits
					.size()));
			n++;

			paleoResults.addShapeToNBestList(fitShape);
		}

		// add monitored values (optional)
		/*
		 * m_monitoredValues.put("perim:sl ratio", ((RectangleFit)
		 * m_rectangleFit) .getPerimeterStrokeLengthRatio());
		 * m_monitoredValues.put("major:bb ratio", ((RectangleFit)
		 * m_rectangleFit) .getMajorAxisBBDiagRatio());
		 * m_monitoredValues.put("rectangle error", m_rectangleFit.getError());
		 * m_monitoredValues.put("corner avg distance", m_features
		 * .getAvgCornerStrokeDistance());
		 * m_monitoredValues.put("ellipse error", m_ellipseFit.getError());
		 * m_monitoredValues.put("num revs", m_features.numRevolutions());
		 * m_monitoredValues.put("pct dir passed", m_features
		 * .getPctDirWindowPassed()); m_monitoredValues.put("best fit dir",
		 * m_features .getBestFitDirGraphError());
		 * m_monitoredValues.put("avg ang dir", ((PolylineFit) m_polylineFit)
		 * .getAvgAngularDirection()); m_monitoredValues.put("poly error",
		 * m_polylineFit.getError());
		 * m_monitoredValues.put("corner max distance", m_features
		 * .getMaxCornerStrokeDistance());
		 * m_monitoredValues.put("min corn dist", m_features
		 * .getMinCornerStrokeDistance());
		 * m_monitoredValues.put("std dev corn dist", m_features
		 * .getStdDevCornerStrokeDistance());
		 */

		return paleoResults;
	}

	/**
	 * Get the list of fits used by the recognizer
	 * 
	 * @return list of fits
	 */
	public FitList getFits() {
		return m_fits;
	}

	/**
	 * Calculates all of the various shape fits
	 */
	protected void calculateAllFits() {
		if (m_config.isLineTestOn())
			calcLineFit();
		if (m_config.isArcTestOn())
			calcArcFit();
		if (m_config.isCurveTestOn())
			calcCurveFit();
		if (m_config.isArrowTestOn())
			calcArrowFit();
		if (m_config.isPolylineTestOn())
			calcPolylineFit();
		if (m_config.isEllipseTestOn())
			calcEllipseFit();
		if (m_config.isCircleTestOn())
			calcCircleFit();
		if (m_config.isSpiralTestOn())
			calcSpiralFit();
		if (m_config.isHelixTestOn())
			calcHelixFit();
		if (m_config.isPolygonTestOn())
			calcPolygonFit();
		if (m_config.isRectangleTestOn())
			calcRectangleFit();
		if (m_config.isSquareTestOn())
			calcSquareFit();
		if (m_config.isDiamondTestOn())
			calcDiamondFit();
		if (m_config.isDotTestOn())
			calcDotFit();
		if (m_config.isWaveTestOn())
			calcWaveFit();
		if (m_config.isGullTestOn())
			calcGullFit();
		if (m_config.isBlobTestOn())
			calcBlobFit();
		if (m_config.isInfinityTestOn())
			calcInfinityFit();
		if (m_config.isNBCTestOn())
			calcNBCFit();
	}

	/**
	 * Get a fit by its string name
	 * 
	 * @param fitName
	 *            name of fit to get
	 * @return shape fit
	 */
	public Fit getFit(String fitName) {
		if (fitName.compareToIgnoreCase(Fit.ARC) == 0)
			return getArcFit();
		else if (fitName.compareToIgnoreCase(Fit.ARROW) == 0)
			return getArrowFit();
		else if (fitName.compareToIgnoreCase(Fit.BLOB) == 0)
			return getBlobFit();
		else if (fitName.compareToIgnoreCase(Fit.CIRCLE) == 0)
			return getCircleFit();
		else if (fitName.startsWith(Fit.COMPLEX))
			return getComplexFit();
		else if (fitName.compareToIgnoreCase(Fit.CURVE) == 0)
			return getCurveFit();
		else if (fitName.compareToIgnoreCase(Fit.DIAMOND) == 0)
			return getDiamondFit();
		else if (fitName.compareToIgnoreCase(Fit.DOT) == 0)
			return getDotFit();
		else if (fitName.compareToIgnoreCase(Fit.ELLIPSE) == 0)
			return getEllipseFit();
		else if (fitName.compareToIgnoreCase(Fit.GULL) == 0)
			return getGullFit();
		else if (fitName.compareToIgnoreCase(Fit.HELIX) == 0)
			return getHelixFit();
		else if (fitName.compareToIgnoreCase(Fit.INFINITY) == 0)
			return getInfinityFit();
		else if (fitName.compareToIgnoreCase(Fit.LINE) == 0)
			return getLineFit();
		else if (fitName.compareToIgnoreCase(Fit.NBC) == 0)
			return getNBCFit();
		else if (fitName.startsWith(Fit.POLYGON))
			return getPolygonFit();
		else if (fitName.startsWith(Fit.POLYLINE))
			return getPolylineFit();
		else if (fitName.compareToIgnoreCase(Fit.RECTANGLE) == 0)
			return getRectangleFit();
		else if (fitName.compareToIgnoreCase(Fit.SPIRAL) == 0)
			return getSpiralFit();
		else if (fitName.compareToIgnoreCase(Fit.SQUARE) == 0)
			return getSquareFit();
		else if (fitName.compareToIgnoreCase(Fit.WAVE) == 0)
			return getWaveFit();
		else
			return new NullFit();
	}

	/**
	 * Uses hierarchy to order fits
	 */
	protected void orderFits() {
		boolean allLines = true;

		// add all lines
		if (m_lineFit.passed() && !m_gullFit.passed() && !m_waveFit.passed()) {
			m_fits.add(m_lineFit, 0);
		}

		// add all dots
		if (m_dotFit.passed()) {
			if (!m_ellipseFit.passed()
					|| ((DotFit) m_dotFit).getDensity()
							* m_features.getNumRevolutions() > 0.91)
				m_fits.add(m_dotFit, 1);
		}

		// add all NBCs that didnt pass wave test
		if (m_nbcFit.passed() && !m_waveFit.passed()) {
			m_fits.add(m_nbcFit, 2);
		}

		// add infinity fits that arent ellipses
		if (m_infinityFit.passed() && !m_ellipseFit.passed()
				&& !m_diamondFit.passed()) {
			// see if polyline fit is better
			if (m_polylineFit.passed() && m_polylineFit.getError() < 0.04)
				m_fits.add(m_polylineFit, 3);
			m_fits.add(m_infinityFit, 4);
		}

		// add blob fits
		if (m_blobFit.passed()) {

			// see if polyline better
			if (m_polylineFit.passed() && m_polylineFit.getError() < 0.025) {
				m_fits.add(m_polylineFit, 41);
			}
			if (m_spiralFit.passed()) {
				m_fits.add(m_spiralFit, 412);
			}
			m_fits.add(m_blobFit, 5);
		}

		// add all square that are not ambiguous with ellipses
		if (m_squareFit.passed() && !m_ellipseFit.passed()) {
			m_fits.add(m_squareFit, 6);
		}

		// add all rectangles that are not ambiguous with ellipses
		if (m_rectangleFit.passed() && !m_ellipseFit.passed()) {
			m_fits.add(m_rectangleFit, 7);
			m_fits.add(m_squareFit, 8);
		}

		// add all diamonds that are not ambiguous with ellipses
		if (m_diamondFit.passed() && !m_ellipseFit.passed()) {
			m_fits.add(m_diamondFit, 9);
		}

		// add all arrows only if spiral test did not pass
		if (m_arrowFit.passed() && !m_spiralFit.passed()) {
			m_fits.add(m_arrowFit, 10);
		}

		// add squares with small errors (or error less than ellipse fit) and
		// makes less than a full rotation
		if (m_squareFit.passed()
				&& (m_squareFit.getError() < m_ellipseFit.getError() || m_squareFit
						.getError() < Thresholds.active.M_RECTANGLE_LOW_ERROR)
				&& m_features.numRevolutions() < Thresholds.active.M_LOW_ROTATION) {
			m_fits.add(m_squareFit, 11);
		}

		// add rectangles with small errors (or error less than ellipse fit) and
		// makes less than a full rotation
		// ADDED: check for condition when the continuous direction test fails
		// which is an indicator of rectangle rather than ellipse
		if (m_rectangleFit.passed()
				&& (m_rectangleFit.getError() < m_ellipseFit.getError()
						|| m_rectangleFit.getError() < Thresholds.active.M_RECTANGLE_LOW_ERROR || !m_features
						.dirWindowPassed())
				&& (m_features.numRevolutions() < Thresholds.active.M_LOW_ROTATION || (m_rectangleFit instanceof RectangleFit && ((RectangleFit) m_rectangleFit)
						.getNumCorners() == 4))) {

			// HQ hack
			if (m_polylineFit.passed()
					&& m_polylineFit.getError() < 0.025
					&& m_features.getStdDevCornerStrokeDistance() > 38.0
					&& ((PolylineFit) m_polylineFit).getSubStrokes().size() != 4) {
				m_fits.add(m_polylineFit, 112);
			}

			m_fits.add(m_rectangleFit, 12);
			m_fits.add(m_squareFit, 13);
		}

		// add diamonds with small errors (or error less than ellipse fit) and
		// makes less than a full rotation
		// ADDED: check for condition when the continuous direction test fails
		// which is an indicator of diamond rather than ellipse
		if (m_diamondFit.passed()
				&& !m_features.dirWindowPassed()
				&& (m_diamondFit.getError() < m_ellipseFit.getError()
						|| m_diamondFit.getError() < Thresholds.active.M_RECTANGLE_LOW_ERROR || (!m_features
						.dirWindowPassed()
						&& m_diamondFit instanceof DiamondFit && ((DiamondFit) m_diamondFit)
						.getPerimeterStrokeLengthRatio() > 0.8))
				&& m_ellipseFit.getError() > 0.03) {// &&
			// m_features.numRevolutions() <
			// M_LOW_ROTATION) {
			m_fits.add(m_diamondFit, 14);
		}

		// add gulls if helix did not pass
		if (m_gullFit.passed() && !m_helixFit.passed()) {
			// check polyline first & make sure its not a better fit

			if (m_polylineFit.passed()
					&& (((PolylineFit) m_polylineFit).getSubStrokes().size() == 4)
					&& m_config.getHeuristics().M_VS_GULL_CHECK) {
				if ((m_polylineFit.getError() < 0.042 && ((PolylineFit) m_polylineFit)
						.getLSQE() < 0.3)
						|| ((PolylineFit) m_polylineFit)
								.getAvgAngularDirection() > 66.0) {
					m_fits.add(m_polylineFit, 15);
				}
			}
			m_fits.add(m_gullFit, 16);
		}

		// add waves if helix did not pass
		if (m_waveFit.passed() && !m_helixFit.passed()) {

			// check polyline first & make sure its not a better fit
			if (m_polylineFit.passed()
					&& ((PolylineFit) m_polylineFit).getSubStrokes().size() < Thresholds.active.M_POLYLINE_SUBSTROKES_LOW
					&& m_polylineFit.getError() < 0.02)
				m_fits.add(m_polylineFit, 17);
			m_fits.add(m_waveFit, 18);
		}

		// add all lines
		if (m_lineFit.passed()) {
			m_fits.add(m_lineFit, 181);
		}

		// add arcs with feature area errors less than the polyline
		// interpretation
		if (m_arcFit.getError() < m_polylineFit.getError() && m_arcFit.passed()) {
			m_fits.add(m_arcFit, 19);
		}

		// add non-overtraced circles with errors less than the polyline
		// interpretation
		if (m_circleFit.getError() < m_polylineFit.getError()
				&& m_circleFit.passed() && !m_features.isOvertraced()) {

			// add polyline first if its score is better than the circle's score
			if (m_polylineFit instanceof PolylineFit
					&& m_circleFit instanceof CircleFit) {
				if (m_polylineFit.passed()
						&& ((PolylineFit) m_polylineFit).getNumSubStrokes() < CIRCLE_SCORE
						&& ((CircleFit) m_circleFit).getRadius() > Thresholds.active.M_CIRCLE_SMALL) {

					m_fits.add(m_polylineFit, 20);
				}
			}
			m_fits.add(m_circleFit, 21);
		}

		// add non-overtraced ellipses with errors less than the polyline
		// interpretation
		if (m_ellipseFit.getError() < m_polylineFit.getError()
				&& m_ellipseFit.passed() && !m_circleFit.passed()
				&& !m_features.isOvertraced()) {

			// add polyline first if its score is better than the ellipse's
			// score
			if (m_polylineFit instanceof PolylineFit
					&& m_ellipseFit instanceof EllipseFit) {
				if (m_polylineFit.passed()
						&& ((PolylineFit) m_polylineFit).getNumSubStrokes() < ELLIPSE_SCORE
						&& ((PolylineFit) m_polylineFit).getNumSubStrokes() > 1
						&& ((EllipseFit) m_ellipseFit).getMajorAxisLength() > Thresholds.active.M_ELLIPSE_SMALL) {
					m_fits.add(m_polylineFit, 22);
				}
				// HQ hack
				if (m_polylineFit.passed()
						&& ((PolylineFit) m_polylineFit).allLinesPassed()
						&& m_polylineFit.getError() < 0.025
						&& m_features.getStdDevCornerStrokeDistance() > 38.0) {
					m_fits.add(m_polylineFit, 222);
				}
			}
			m_fits.add(m_ellipseFit, 23);

			// always add circle as alternative to ellipse
			m_fits.add(m_circleFit, 24);
		}

		// add polylines with high dcr and low number of sub-strokes (threshold
		// is less strict in cases where all sub-strokes passed a line test)
		if (m_polylineFit instanceof PolylineFit) {
			if (m_polylineFit.passed()
					&& ((m_features.getDCR() > Thresholds.active.M_DCR_TO_BE_POLYLINE_STRICT && ((PolylineFit) m_polylineFit)
							.getNumSubStrokes() < Thresholds.active.M_POLYLINE_SUBSTROKES_LOW) || (((PolylineFit) m_polylineFit)
							.allLinesPassed()
							&& m_features.getDCR() > Thresholds.active.M_DCR_TO_BE_POLYLINE && ((PolylineFit) m_polylineFit)
							.getNumSubStrokes() < Thresholds.active.M_POLYLINE_SUBSTROKES_LOW))) {
				m_fits.add(m_polylineFit, 25);
			}
		}

		// add any other arcs that haven't been added already
		if (m_arcFit.passed()) {
			if (m_polylineFit.passed()
					&& ((PolylineFit) m_polylineFit).allLinesPassed()
					&& m_polylineFit.getError() * 7.0 < m_arcFit.getError()
					&& m_features.getMaxCurvToAvgCurvRatio() > 5.0
					&& ((PolylineFit) m_polylineFit).getSubStrokes().size() < 4) {
				m_fits.add(m_polylineFit, 26);
			}
			if (!m_features.dirWindowPassed()
					&& m_polylineFit.getError() < 0.03
					&& ((PolylineFit) m_polylineFit).getNumSubStrokes() <= 3)
				m_fits.add(m_polylineFit, 27);
			m_fits.add(m_arcFit, 28);
		}

		// add any circles that haven't already been added
		if (m_circleFit.passed()) {

			// if overtraced, make sure spiral test didn't pass
			if (m_features.isOvertraced() && m_spiralFit.passed()) {
				m_fits.add(m_spiralFit, 29);
			}

			// add polyline first if its score is better than the circle's score
			if (m_polylineFit instanceof PolylineFit
					&& m_circleFit instanceof CircleFit) {
				if (m_polylineFit.passed()
						&& ((PolylineFit) m_polylineFit).getNumSubStrokes() < CIRCLE_SCORE
						&& ((CircleFit) m_circleFit).getRadius() > Thresholds.active.M_CIRCLE_SMALL) {

					m_fits.add(m_polylineFit, 30);
				}
			}
			m_fits.add(m_circleFit, 31);
		}

		// add any ellipses that haven't already been added
		if (m_ellipseFit.passed()) {

			// if overtraced, make sure spiral test didn't pass
			if (m_features.isOvertraced() && m_spiralFit.passed()) {
				m_fits.add(m_spiralFit, 32);
			}

			if (m_features.isOvertraced() && m_helixFit.passed()) {
				m_fits.add(m_helixFit, 322);
			}

			// add diamonds if fit is better
			if (m_diamondFit.passed()
					&& !m_rectangleFit.passed()
					&& m_diamondFit.getError() < m_ellipseFit.getError()
					&& ((DiamondFit) m_diamondFit)
							.getPerimeterStrokeLengthRatio() > 0.92
					&& ((DiamondFit) m_diamondFit).getNumSubStrokes() == 4) {
				m_fits.add(m_diamondFit, 33);
			}

			// add polyline first if its score is better than the ellipse's
			// score
			if (m_polylineFit instanceof PolylineFit
					&& m_ellipseFit instanceof EllipseFit) {
				if (m_polylineFit.passed()
						&& ((PolylineFit) m_polylineFit).getNumSubStrokes() < ELLIPSE_SCORE
						&& ((EllipseFit) m_ellipseFit).getMajorAxisLength() > Thresholds.active.M_ELLIPSE_SMALL) {
					m_fits.add(m_polylineFit, 34);
				}
				// HQ hack
				if (m_polylineFit.passed() && m_polylineFit.getError() < 0.025
						&& m_features.getStdDevCornerStrokeDistance() > 38.0) {
					m_fits.add(m_polylineFit, 342);
				}
			}

			// add rectangle if fit is better
			if (m_rectangleFit.passed()
					&& m_features.getAvgCornerStrokeDistance() < 0.125) {
				m_fits.add(m_rectangleFit, 35);
			}

			// add diamonds if fit is better
			if (m_diamondFit.passed()
					&& m_features.getAvgCornerStrokeDistance() > 0.3)
				m_fits.add(m_diamondFit, 351);

			if (m_polylineFit.passed() && m_polylineFit.getError() < 0.03
					&& m_features.getBestFitDirGraphError() > 0.6
					&& m_features.getDCR() > 5.0)
				m_fits.add(m_polylineFit, 352);

			if (m_polylineFit.passed()
					&& ((PolylineFit) m_polylineFit).getNumSubStrokes() <= ELLIPSE_SCORE
					&& m_ellipseFit.getError() > 0.17
					&& m_features.getNumRevolutions() < 0.88
					&& m_features.getDCR() > 3.5
					&& ((EllipseFit) m_ellipseFit).getMajorAxisLength() > Thresholds.active.M_ELLIPSE_SMALL)
				m_fits.add(m_polylineFit, 353);

			m_fits.add(m_ellipseFit, 36);

			// always add circle as alternative to ellipse
			m_fits.add(m_circleFit, 37);
		}

		// add all dots
		if (m_dotFit.passed()) {
			m_fits.add(m_dotFit, 371);
		}

		// add all helixes
		if (m_helixFit.passed()) {

			// check to see if complex is better fit first
			if (m_config.isComplexTestOn()) {
				calcComplexFit();
				if (((ComplexFit) m_complexFit).getSubFits().size() > 1) {
					calcComplexScore();
					if (m_complexScore < HELIX_SCORE)
						m_fits.add(m_complexFit, 38);
				}
			}

			m_fits.add(m_helixFit, 39);
		}

		// add all other infinity fits
		if (m_infinityFit.passed()) {
			// see if polyline fit is better
			if (m_polylineFit.passed() && m_polylineFit.getError() < 0.04)
				m_fits.add(m_polylineFit, 40);
			m_fits.add(m_infinityFit, 41);
		}

		// add all other squares
		if (m_squareFit.passed()) {
			m_fits.add(m_squareFit, 42);
		}

		// add all other rectangles
		if (m_rectangleFit.passed()) {
			m_fits.add(m_rectangleFit, 43);
			m_fits.add(m_squareFit, 44);
		}

		// add all other diamonds
		if (m_diamondFit.passed()) {
			// if top interpretation is polyline and diamond passed then add it
			// instead
			if (m_fits.size() > 0 && m_fits.get(0).getName() == Fit.POLYLINE)
				m_fits.add(0, m_diamondFit, 45);
			else
				m_fits.add(m_diamondFit, 46);
		}

		// add all spirals
		if (m_spiralFit.passed()) {
			m_fits.add(m_spiralFit, 47);
		}

		// add all curves
		if (m_curveFit.passed()) {
			if (m_features.getMaxCurv() > 0.33 && m_polylineFit.passed())
				m_fits.add(m_polylineFit, 48);
			m_fits.add(m_curveFit, 49);
		}

		// add all other gulls
		if (m_gullFit.passed()) {
			m_fits.add(m_gullFit, 50);
		}

		// add all other waves
		if (m_waveFit.passed())
			m_fits.add(m_waveFit, 51);

		// add all other NBCs
		if (m_nbcFit.passed()) {
			m_fits.add(m_nbcFit, 52);
		}

		// add all polylines that haven't already been added
		if (m_polylineFit.passed()) {
			m_fits.add(m_polylineFit, 53);
		}

		// if best fit is a polyline or curve, see if a complex interpretation
		// is better than this best interpretation
		if (m_fits.size() == 0 || m_fits.get(0) instanceof CurveFit
				|| m_fits.get(0) instanceof PolylineFit) {

			// check to see if complex is better fit first
			if (m_config.isComplexTestOn()) {
				calcComplexFit();
				if (((ComplexFit) m_complexFit).getSubFits().size() > 1) {

					// if complex fit consists of all lines then make it a
					// polyline fit
					for (int i = 0; i < ((ComplexFit) m_complexFit)
							.getSubFits().size() && allLines; i++) {
						if (!(((ComplexFit) m_complexFit).getSubFits().get(i) instanceof LineFit)
								&& !(((ComplexFit) m_complexFit).getSubFits()
										.get(i) instanceof PolylineFit))
							allLines = false;
					}
					if (allLines) {
						m_fits.add(m_polylineFit, 54);
					}

					m_fits.add(m_complexFit, 55);
					calcComplexScore();

					// if polyline score less than complex score then put
					// polyline fit before complex fit
					if (!allLines
							&& m_polylineFit instanceof PolylineFit
							&& m_complexScore < ((PolylineFit) m_polylineFit)
									.getSubStrokes().size()) {
						m_fits.remove(m_complexFit);
						int index = m_fits.indexOf(m_polylineFit);
						if (index >= 0)
							m_fits.add(index, m_complexFit, 56);
						else
							m_fits.add(m_complexFit, 57);
					}

					// if complex has better score than curve then put it before
					// curve in the list
					if ((m_fits.get(0) instanceof CurveFit)
							&& m_complexScore < CURVE_SCORE) {
						m_fits.remove(m_complexFit);
						m_fits.add(m_fits.indexOf(m_curveFit), m_complexFit, 58);
					}
					if (allLines
							|| ((ComplexFit) m_complexFit).getSubFits().size() == 1)
						m_fits.remove(m_complexFit);

					// check for complex tails (meaningless substrokes at the
					// ends)
					//
					// removing this because it is rarely used and when it does
					// occur, it causes paint problems (wont remove original
					// stroke for some reason)
					if (m_fits.get(0) == m_complexFit) {
						List<Stroke> s = ((ComplexFit) m_complexFit)
								.getSubStrokes();
						double first = StrokeFeatures.getStrokeLength(s.get(0));
						double last = StrokeFeatures.getStrokeLength(s.get(s
								.size() - 1));
						double ratio = 0;
						if (first < last) {
							for (int i = 1; i < s.size(); i++)
								ratio += StrokeFeatures.getStrokeLength(s
										.get(i));
							ratio = first / ratio;
						} else {
							for (int i = 0; i < s.size() - 1; i++)
								ratio += StrokeFeatures.getStrokeLength(s
										.get(i));
							ratio = last / ratio;
						}

						// if ratio is too small then tail is removed
						if (ratio < Thresholds.active.M_RATIO_TO_REMOVE_TAIL) {
							if (((ComplexFit) m_complexFit).getSubFits().size() == 2) {
								if (first < last)
									m_fits.add(0, ((ComplexFit) m_complexFit)
											.getSubFits().get(1), 59);
								else
									m_fits.add(0, ((ComplexFit) m_complexFit)
											.getSubFits().get(0), 60);
							} else {
								if (first < last)
									((ComplexFit) m_complexFit).getSubFits()
											.remove(0);
								else
									((ComplexFit) m_complexFit).getSubFits()
											.remove(((ComplexFit) m_complexFit)
													.getSubFits().size() - 1);
							}
						}
					}

				}
			}

		}

		// add default polyline or line interpretation
		if (m_polylineFit instanceof PolylineFit) {
			if (((PolylineFit) m_polylineFit).getSubShapes().size() <= 1) {
				if (!m_lineFit.passed() && m_config.getHeuristics().SMALL_V)
					m_fits.add(checkForSmallV(), 61); // this is a blatant hack
				else
					m_fits.add(m_lineFit, 62);
			} else
				m_fits.add(m_polylineFit, 63);
		}

		// check for polygon fit; will add polygon fit in front of polyline fit
		if (m_polygonFit.passed()) {
			boolean stop = false;
			for (int i = 0; i < m_fits.size() && !stop; i++) {
				if (m_fits.get(i).equals(m_polylineFit)) {
					stop = true;
					m_fits.add(i, m_polygonFit, 64);
				}
			}
		}

		// diamonds and rectangles should never occur together (this is typical
		// in cases of circles); if they do then move them to the end
		if (m_fits.contains(m_rectangleFit) && m_fits.contains(m_diamondFit)
				&& m_fits.size() > 2) {
			m_fits.remove(m_rectangleFit);
			m_fits.remove(m_diamondFit);
			if (m_fits.contains(m_squareFit)) {
				m_fits.remove(m_squareFit);
				m_fits.add(m_squareFit, 65);
			}
			m_fits.add(m_rectangleFit, 66);
			m_fits.add(m_diamondFit, 67);
		}

		// check for small arc/"v" confusion
		if (m_fits.size() == 2 && m_fits.get(0) == m_arcFit
				&& m_fits.get(1) == m_polylineFit && m_arcFit instanceof ArcFit
				&& m_polylineFit instanceof PolylineFit) {
			if (((PolylineFit) m_polylineFit).getSubStrokes().size() == 2
					&& ((PolylineFit) m_polylineFit).allLinesPassed()
					&& m_polylineFit.getError() <= 0.08// 0.1 // 0.04
					&& ((ArcFit) m_arcFit).getRadius() < 25.0) {
				m_fits.remove(m_arcFit);
				m_fits.add(m_arcFit, 68);
			}
		}

		// check to see if top result is polyline (4) or polygon (4) and
		// rectangle or square present; if so then replace polyline 4 w/
		// rectangle or square
		if ((m_polylineFit instanceof PolylineFit
				&& m_fits.get(0) == m_polylineFit
				&& ((PolylineFit) m_polylineFit).getSubShapes().size() == 4 && m_fits
				.contains(m_squareFit))
				|| (m_polygonFit instanceof PolygonFit
						&& m_fits.get(0) == m_polygonFit
						&& ((PolygonFit) m_polygonFit).getSubStrokes().size() == 4 && m_fits
						.contains(m_squareFit))) {
			m_fits.remove(m_squareFit);
			m_fits.add(0, m_squareFit, 69);
			if (m_fits.contains(m_rectangleFit)) {
				m_fits.remove(m_rectangleFit);
				m_fits.add(1, m_rectangleFit, 70);
			}
		}
		if ((m_polylineFit instanceof PolylineFit
				&& m_fits.get(0) == m_polylineFit
				&& ((PolylineFit) m_polylineFit).getSubShapes().size() == 4 && m_fits
				.contains(m_rectangleFit))
				|| (m_polygonFit instanceof PolygonFit
						&& m_fits.get(0) == m_polygonFit
						&& ((PolygonFit) m_polygonFit).getSubStrokes().size() == 4 && m_fits
						.contains(m_rectangleFit))) {
			m_fits.remove(m_rectangleFit);
			m_fits.add(0, m_rectangleFit, 71);
		}

		// check to see if top result is polyline (4) or polygon (4) and
		// diamond present; if so then replace polyline 4 w/
		// diamond
		if ((m_polylineFit instanceof PolylineFit
				&& m_fits.get(0) == m_polylineFit
				&& ((PolylineFit) m_polylineFit).getSubShapes().size() == 4 && m_fits
				.contains(m_diamondFit))
				|| (m_polygonFit instanceof PolygonFit
						&& m_fits.get(0) == m_polygonFit
						&& ((PolygonFit) m_polygonFit).getSubStrokes().size() == 4 && m_fits
						.contains(m_diamondFit))) {
			m_fits.remove(m_diamondFit);
			m_fits.add(0, m_diamondFit, 72);
		}

		// check for single line polylines
		if (m_polylineFit instanceof PolylineFit
				&& ((PolylineFit) m_polylineFit).getSubShapes().size() == 1
				&& m_fits.contains(m_polylineFit)) {
			if (m_fits.contains(m_lineFit))
				m_fits.remove(m_polylineFit);
			else {
				m_lineFit = new LineFit(m_features, false);
				m_fits.add(m_fits.indexOf(m_polylineFit), m_lineFit, 73);
				m_fits.remove(m_polylineFit);
			}
		}

		// add blob as secondary to ellipse
		if (m_fits.contains(m_ellipseFit) && !m_fits.contains(m_blobFit)) {
			m_fits.add(m_fits.indexOf(m_ellipseFit) + 1, m_blobFit, 74);
		}
	}

	/**
	 * Check for small V-shaped strokes. This is a hack needed because corner
	 * finders break down on small strokes
	 * 
	 * @return "V" shape interpretation of stroke
	 */
	protected Fit checkForSmallV() {
		// System.out.println("Check V");
		if (m_features.getNumPoints() < 25)
			return new PolylineFit(new VSegmenter(m_features));
		return m_lineFit;
	}

	/**
	 * Removes the unwanted fits (according to the config file) from the list of
	 * interpretations
	 */
	protected void removeUnwantedFits() {
		if (!m_config.isArcTestOn() && m_fits.contains(m_arcFit))
			m_fits.remove(m_arcFit);
		if (!m_config.isArrowTestOn() && m_fits.contains(m_arrowFit))
			m_fits.remove(m_arrowFit);
		if (!m_config.isCircleTestOn() && m_fits.contains(m_circleFit))
			m_fits.remove(m_circleFit);
		if (!m_config.isComplexTestOn() && m_fits.contains(m_complexFit))
			m_fits.remove(m_complexFit);
		if (!m_config.isCurveTestOn() && m_fits.contains(m_curveFit))
			m_fits.remove(m_curveFit);
		if (!m_config.isDiamondTestOn() && m_fits.contains(m_diamondFit))
			m_fits.remove(m_diamondFit);
		if (!m_config.isDotTestOn() && m_fits.contains(m_dotFit))
			m_fits.remove(m_dotFit);
		if (!m_config.isEllipseTestOn() && m_fits.contains(m_ellipseFit))
			m_fits.remove(m_ellipseFit);
		if (!m_config.isHelixTestOn() && m_fits.contains(m_helixFit))
			m_fits.remove(m_helixFit);
		if (!m_config.isLineTestOn() && m_fits.contains(m_lineFit))
			m_fits.remove(m_lineFit);
		if (!m_config.isPolygonTestOn() && m_fits.contains(m_polygonFit))
			m_fits.remove(m_polygonFit);
		if (!m_config.isPolylineTestOn() && m_fits.contains(m_polylineFit))
			m_fits.remove(m_polylineFit);
		if (!m_config.isRectangleTestOn() && m_fits.contains(m_rectangleFit))
			m_fits.remove(m_rectangleFit);
		if (!m_config.isSpiralTestOn() && m_fits.contains(m_spiralFit))
			m_fits.remove(m_spiralFit);
		if (!m_config.isSquareTestOn() && m_fits.contains(m_squareFit))
			m_fits.remove(m_squareFit);
		if (!m_config.isWaveTestOn() && m_fits.contains(m_waveFit))
			m_fits.remove(m_waveFit);
		if (!m_config.isGullTestOn() && m_fits.contains(m_gullFit))
			m_fits.remove(m_gullFit);
		if (!m_config.isBlobTestOn() && m_fits.contains(m_blobFit))
			m_fits.remove(m_blobFit);
		if (!m_config.isInfinityTestOn() && m_fits.contains(m_infinityFit))
			m_fits.remove(m_infinityFit);
		if (!m_config.isNBCTestOn() && m_fits.contains(m_nbcFit))
			m_fits.remove(m_nbcFit);
	}

	/**
	 * Calculates the complex interpretation score
	 */
	protected void calcComplexScore() {
		m_complexScore = 0;
		for (int i = 0; i < ((ComplexFit) m_complexFit).getSubFits().size(); i++) {
			Fit f = ((ComplexFit) m_complexFit).getSubFits().get(i);
			if (f instanceof LineFit)
				m_complexScore += LINE_SCORE;
			else if (f instanceof ArcFit)
				m_complexScore += ARC_SCORE;
			else if (f instanceof CurveFit)
				m_complexScore += CURVE_SCORE;
			else if (f instanceof SpiralFit)
				m_complexScore += SPIRAL_SCORE;
			else if (f instanceof HelixFit)
				m_complexScore += HELIX_SCORE;
			else if (f instanceof EllipseFit)
				m_complexScore += ELLIPSE_SCORE;
			else if (f instanceof CircleFit)
				m_complexScore += CIRCLE_SCORE;
			else if (f instanceof PolylineFit)
				m_complexScore += ((PolylineFit) f).getSubStrokes().size();
			else if (f instanceof PolygonFit)
				m_complexScore += ((PolygonFit) f).getSubStrokes().size();
			else if (f instanceof RectangleFit || f instanceof SquareFit
					|| f instanceof DiamondFit)
				m_complexScore += 4;
			else if (f instanceof DotFit)
				m_complexScore += 5;
		}
	}

	/**
	 * Calculate line fit
	 */
	protected void calcLineFit() {
		m_lineFit = new LineFit(m_features, true);
	}

	/**
	 * Calculate arc fit
	 */
	protected void calcArcFit() {
		m_arcFit = new ArcFit(m_features, m_config);
	}

	/**
	 * Calculate curve fit
	 */
	protected void calcCurveFit() {
		m_curveFit = new CurveFit(m_features);
	}

	/**
	 * Calculate polyline fit
	 */
	protected void calcPolylineFit() {
		if (m_segmentation == null) {
			if (m_config.getHeuristics().MULTI_CF) {
				try {
					PolylineCombinationSegmenter seg = new PolylineCombinationSegmenter(
							m_config.getHeuristics().FILTER_DIR_GRAPH);
					seg.setStroke(m_features.getOrigStroke());
					m_segmentation = seg.getSegmentations().get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				m_segmentation = new PaleoSegmenter(m_features)
						.getSegmentations().get(0);
			}
		}
		m_polylineFit = new PolylineFit(m_features, m_segmentation, m_config);
	}

	/**
	 * Calculate ellipse fit
	 */
	protected void calcEllipseFit() {
		m_ellipseFit = new EllipseFit(m_features);
	}

	/**
	 * Calculate circle fit
	 */
	protected void calcCircleFit() {
		if (m_ellipseFit instanceof NullFit)
			calcEllipseFit();
		m_circleFit = new CircleFit(m_features, (EllipseFit) m_ellipseFit);
	}

	/**
	 * Calculate spiral fit
	 */
	protected void calcSpiralFit() {
		if (m_circleFit instanceof NullFit)
			calcCircleFit();
		m_spiralFit = new SpiralFit(m_features, (CircleFit) m_circleFit);
	}

	/**
	 * Calculate helix fit
	 */
	protected void calcHelixFit() {
		if (m_spiralFit instanceof NullFit)
			calcSpiralFit();
		m_helixFit = new HelixFit(m_features, (SpiralFit) m_spiralFit);
	}

	/**
	 * Calculate polygon fit
	 */
	protected void calcPolygonFit() {
		if (m_polylineFit instanceof NullFit)
			calcPolylineFit();
		m_polygonFit = new PolygonFit(m_features, (PolylineFit) m_polylineFit);
	}

	/**
	 * Calculate arrow fit
	 */
	protected void calcArrowFit() {
		if (m_segmentation == null) {
			if (m_config.getHeuristics().MULTI_CF) {
				try {
					PolylineCombinationSegmenter seg = new PolylineCombinationSegmenter(
							m_config.getHeuristics().FILTER_DIR_GRAPH);
					seg.setStroke(m_features.getOrigStroke());
					m_segmentation = seg.getSegmentations().get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				m_segmentation = new PaleoSegmenter(m_features)
						.getSegmentations().get(0);
			}
		}
		m_arrowFit = new ArrowFit(m_features, m_segmentation);
		if (m_features.getOrigStroke().getSegmentations().size() > 0) {
			Segmentation seg = m_features.getOrigStroke().getSegmentations()
					.get(0);
			for (Stroke s : seg.getSegmentedStrokes()) {
				m_arrowFit.getShape().getShapes().add(LineFit.getLineFit(s));
			}
		}
	}

	/**
	 * Calculate rectangle fit
	 */
	protected void calcRectangleFit() {
		if (m_ellipseFit instanceof NullFit)
			calcEllipseFit();
		if (m_segmentation == null) {
			if (m_config.getHeuristics().MULTI_CF) {
				try {
					PolylineCombinationSegmenter seg = new PolylineCombinationSegmenter(
							m_config.getHeuristics().FILTER_DIR_GRAPH);
					seg.setStroke(m_features.getOrigStroke());
					m_segmentation = seg.getSegmentations().get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				m_segmentation = new PaleoSegmenter(m_features)
						.getSegmentations().get(0);
			}
		}
		m_rectangleFit = new RectangleFit(m_features,
				(EllipseFit) m_ellipseFit, m_segmentation);
	}

	/**
	 * Calculate square fit
	 */
	protected void calcSquareFit() {
		if (m_rectangleFit instanceof NullFit)
			calcRectangleFit();
		m_squareFit = new SquareFit(m_features, (RectangleFit) m_rectangleFit);
	}

	/**
	 * Calculate diamond fit
	 */
	protected void calcDiamondFit() {
		if (m_segmentation == null) {
			if (m_config.getHeuristics().MULTI_CF) {
				try {
					PolylineCombinationSegmenter seg = new PolylineCombinationSegmenter(
							m_config.getHeuristics().FILTER_DIR_GRAPH);
					seg.setStroke(m_features.getOrigStroke());
					m_segmentation = seg.getSegmentations().get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				m_segmentation = new PaleoSegmenter(m_features)
						.getSegmentations().get(0);
			}
		}
		m_diamondFit = new DiamondFit(m_features, m_config, m_segmentation);
	}

	/**
	 * Calculate dot fit
	 */
	protected void calcDotFit() {
		m_dotFit = new DotFit(m_features);
	}

	/**
	 * Calculate wave fit
	 */
	protected void calcWaveFit() {
		if (m_waveSegmentation == null) {
			WaveSegmenter waveSeg = new WaveSegmenter(m_features);
			try {
				m_waveSegmentation = waveSeg.getSegmentations().get(0);
			} catch (InvalidParametersException e) {
			}
		}
		m_waveFit = new WaveFit(m_features, m_waveSegmentation);
	}

	/**
	 * Calculate gull fit
	 */
	protected void calcGullFit() {
		if (m_waveSegmentation == null) {
			WaveSegmenter waveSeg = new WaveSegmenter(m_features);
			try {
				m_waveSegmentation = waveSeg.getSegmentations().get(0);
			} catch (InvalidParametersException e) {
			}
		}
		if (m_polylineFit instanceof NullFit)
			calcPolylineFit();
		m_gullFit = new GullFit(m_features, m_waveSegmentation,
				(PolylineFit) m_polylineFit, m_config);
	}

	/**
	 * Calculate blob fit
	 */
	protected void calcBlobFit() {
		m_blobFit = new BlobFit(m_features);
	}

	/**
	 * Calculate infinity fit
	 */
	protected void calcInfinityFit() {
		m_infinityFit = new InfinityFit(m_features);
	}

	/**
	 * Calculate NBC fit
	 */
	protected void calcNBCFit() {
		if (m_waveSegmentation == null) {
			WaveSegmenter waveSeg = new WaveSegmenter(m_features);
			try {
				m_waveSegmentation = waveSeg.getSegmentations().get(0);
			} catch (InvalidParametersException e) {
			}
		}
		m_nbcFit = new NBCFit(m_features, m_waveSegmentation);
	}

	/**
	 * Calculate complex fit
	 */
	protected void calcComplexFit() {
		m_complexFit = new ComplexFit(m_features, m_config);
	}

	/**
	 * Corner finding segmentation
	 * 
	 * @return segmentation
	 */
	public Segmentation getSegmentation() {
		return m_segmentation;
	}

	/**
	 * Wave segmentation
	 * 
	 * @return segmentation
	 */
	public Segmentation getWaveSegmentation() {
		return m_waveSegmentation;
	}

	/**
	 * Get a line fit for the stroke
	 * 
	 * @return line fit for the stroke
	 */
	public Fit getLineFitNoCalc() {
		return m_lineFit;
	}

	/**
	 * Get a arc fit for the stroke
	 * 
	 * @return arc fit for the stroke
	 */
	public Fit getArcFitNoCalc() {
		return m_arcFit;
	}

	/**
	 * Get a curve fit for the stroke
	 * 
	 * @return curve fit for the stroke
	 */
	public Fit getCurveFitNoCalc() {
		return m_curveFit;
	}

	/**
	 * Get a complex fit for the stroke
	 * 
	 * @return complex fit for the stroke
	 */
	public Fit getComplexFitNoCalc() {
		return m_complexFit;
	}

	/**
	 * Get an ellipse fit for the stroke
	 * 
	 * @return ellipse fit for the stroke
	 */
	public Fit getEllipseFitNoCalc() {
		return m_ellipseFit;
	}

	/**
	 * Get a circle fit for the stroke
	 * 
	 * @return circle fit for the stroke
	 */
	public Fit getCircleFitNoCalc() {
		return m_circleFit;
	}

	/**
	 * Get a spiral fit for the stroke
	 * 
	 * @return spiral fit for the stroke
	 */
	public Fit getSpiralFitNoCalc() {
		return m_spiralFit;
	}

	/**
	 * Get a helix fit for the stroke
	 * 
	 * @return helix fit for the stroke
	 */
	public Fit getHelixFitNoCalc() {
		return m_helixFit;
	}

	/**
	 * Get a polyline fit for the stroke
	 * 
	 * @return polyline fit for the stroke
	 */
	public Fit getPolylineFitNoCalc() {
		return m_polylineFit;
	}

	/**
	 * Get a polygon fit for the stroke
	 * 
	 * @return polygon fit for the stroke
	 */
	public Fit getPolygonFitNoCalc() {
		return m_polygonFit;
	}

	/**
	 * Get a rectangle fit for the stroke
	 * 
	 * @return rectangle fit for the stroke
	 */
	public Fit getRectangleFitNoCalc() {
		return m_rectangleFit;
	}

	/**
	 * Get a square fit for the stroke
	 * 
	 * @return square fit for the stroke
	 */
	public Fit getSquareFitNoCalc() {
		return m_squareFit;
	}

	/**
	 * Get a arrow fit for the stroke
	 * 
	 * @return arrow fit for the stroke
	 */
	public Fit getArrowFitNoCalc() {
		return m_arrowFit;
	}

	/**
	 * Get a diamond fit for the stroke
	 * 
	 * @return diamond fit for the stroke
	 */
	public Fit getDiamondFitNoCalc() {
		return m_diamondFit;
	}

	/**
	 * Get a dot fit for the stroke
	 * 
	 * @return dot fit for the stroke
	 */
	public Fit getDotFitNoCalc() {
		return m_dotFit;
	}

	/**
	 * Get a wave fit for the stroke
	 * 
	 * @return wave fit for the stroke
	 */
	public Fit getWaveFitNoCalc() {
		return m_waveFit;
	}

	/**
	 * Get a gull fit for the stroke
	 * 
	 * @return gull fit for the stroke
	 */
	public Fit getGullFitNoCalc() {
		return m_gullFit;
	}

	/**
	 * Get a blob fit for the stroke
	 * 
	 * @return blob fit for the stroke
	 */
	public Fit getBlobFitNoCalc() {
		return m_blobFit;
	}

	/**
	 * Get a infinity fit for the stroke
	 * 
	 * @return infinity fit for the stroke
	 */
	public Fit getInfinityFitNoCalc() {
		return m_infinityFit;
	}

	/**
	 * Get a NBC fit for the stroke
	 * 
	 * @return NBC fit for the stroke
	 */
	public Fit getNBCFitNoCalc() {
		return m_nbcFit;
	}

	/**
	 * Get a line fit for the stroke
	 * 
	 * @return line fit for the stroke
	 */
	public LineFit getLineFit() {
		if (m_lineFit instanceof NullFit)
			calcLineFit();
		return (LineFit) m_lineFit;
	}

	/**
	 * Get a arc fit for the stroke
	 * 
	 * @return arc fit for the stroke
	 */
	public ArcFit getArcFit() {
		if (m_arcFit instanceof NullFit)
			calcArcFit();
		return (ArcFit) m_arcFit;
	}

	/**
	 * Get a curve fit for the stroke
	 * 
	 * @return curve fit for the stroke
	 */
	public CurveFit getCurveFit() {
		if (m_curveFit instanceof NullFit)
			calcCurveFit();
		return (CurveFit) m_curveFit;
	}

	/**
	 * Get a complex fit for the stroke
	 * 
	 * @return complex fit for the stroke
	 */
	public ComplexFit getComplexFit() {
		if (m_complexFit instanceof NullFit)
			calcComplexFit();
		return (ComplexFit) m_complexFit;
	}

	/**
	 * Get an ellipse fit for the stroke
	 * 
	 * @return ellipse fit for the stroke
	 */
	public EllipseFit getEllipseFit() {
		if (m_ellipseFit instanceof NullFit)
			calcEllipseFit();
		return (EllipseFit) m_ellipseFit;
	}

	/**
	 * Get a circle fit for the stroke
	 * 
	 * @return circle fit for the stroke
	 */
	public CircleFit getCircleFit() {
		if (m_circleFit instanceof NullFit)
			calcCircleFit();
		return (CircleFit) m_circleFit;
	}

	/**
	 * Get a spiral fit for the stroke
	 * 
	 * @return spiral fit for the stroke
	 */
	public SpiralFit getSpiralFit() {
		if (m_spiralFit instanceof NullFit)
			calcSpiralFit();
		return (SpiralFit) m_spiralFit;
	}

	/**
	 * Get a helix fit for the stroke
	 * 
	 * @return helix fit for the stroke
	 */
	public HelixFit getHelixFit() {
		if (m_helixFit instanceof NullFit)
			calcHelixFit();
		return (HelixFit) m_helixFit;
	}

	/**
	 * Get a polyline fit for the stroke
	 * 
	 * @return polyline fit for the stroke
	 */
	public PolylineFit getPolylineFit() {
		if (m_polylineFit instanceof NullFit)
			calcPolylineFit();
		return (PolylineFit) m_polylineFit;
	}

	/**
	 * Get a polygon fit for the stroke
	 * 
	 * @return polygon fit for the stroke
	 */
	public PolygonFit getPolygonFit() {
		if (m_polygonFit instanceof NullFit)
			calcPolygonFit();
		return (PolygonFit) m_polygonFit;
	}

	/**
	 * Get a rectangle fit for the stroke
	 * 
	 * @return rectangle fit for the stroke
	 */
	public RectangleFit getRectangleFit() {
		if (m_rectangleFit instanceof NullFit)
			calcRectangleFit();
		return (RectangleFit) m_rectangleFit;
	}

	/**
	 * Get a square fit for the stroke
	 * 
	 * @return square fit for the stroke
	 */
	public SquareFit getSquareFit() {
		if (m_squareFit instanceof NullFit)
			calcSquareFit();
		return (SquareFit) m_squareFit;
	}

	/**
	 * Get a arrow fit for the stroke
	 * 
	 * @return arrow fit for the stroke
	 */
	public ArrowFit getArrowFit() {
		if (m_arrowFit instanceof NullFit)
			calcArrowFit();
		return (ArrowFit) m_arrowFit;
	}

	/**
	 * Get a diamond fit for the stroke
	 * 
	 * @return diamond fit for the stroke
	 */
	public DiamondFit getDiamondFit() {
		if (m_diamondFit instanceof NullFit)
			calcDiamondFit();
		return (DiamondFit) m_diamondFit;
	}

	/**
	 * Get a dot fit for the stroke
	 * 
	 * @return dot fit for the stroke
	 */
	public DotFit getDotFit() {
		if (m_dotFit instanceof NullFit)
			calcDotFit();
		return (DotFit) m_dotFit;
	}

	/**
	 * Get a wave fit for the stroke
	 * 
	 * @return wave fit for the stroke
	 */
	public WaveFit getWaveFit() {
		if (m_waveFit instanceof NullFit)
			calcWaveFit();
		return (WaveFit) m_waveFit;
	}

	/**
	 * Get a gull fit for the stroke
	 * 
	 * @return gull fit for the stroke
	 */
	public GullFit getGullFit() {
		if (m_gullFit instanceof NullFit)
			calcGullFit();
		return (GullFit) m_gullFit;
	}

	/**
	 * Get a blob fit for the stroke
	 * 
	 * @return blob fit for the stroke
	 */
	public BlobFit getBlobFit() {
		if (m_blobFit instanceof NullFit)
			calcBlobFit();
		return (BlobFit) m_blobFit;
	}

	/**
	 * Get a infinity fit for the stroke
	 * 
	 * @return infinity fit for the stroke
	 */
	public InfinityFit getInfinityFit() {
		if (m_infinityFit instanceof NullFit)
			calcInfinityFit();
		return (InfinityFit) m_infinityFit;
	}

	/**
	 * Get a NBC fit for the stroke
	 * 
	 * @return NBC fit for the stroke
	 */
	public NBCFit getNBCFit() {
		if (m_nbcFit instanceof NullFit)
			calcNBCFit();
		return (NBCFit) m_nbcFit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.IRecognizer#submitForRecognition(java
	 * .lang.Object)
	 */
	@Override
	public void submitForRecognition(Stroke stroke) {
		setStroke(stroke);
	}

	/**
	 * Returns the map of currently monitored threshold values
	 * 
	 * @return map of monitored threshold values
	 */
	public Map<String, Double> getMonitoredValues() {
		return m_monitoredValues;
	}

	/**
	 * Features of stroke
	 * 
	 * @return features of stroke
	 */
	public StrokeFeatures getFeatures() {
		return m_features;
	}

	/**
	 * Get the config class
	 * 
	 * @return config class
	 */
	public PaleoConfig getConfig() {
		return m_config;
	}
}
