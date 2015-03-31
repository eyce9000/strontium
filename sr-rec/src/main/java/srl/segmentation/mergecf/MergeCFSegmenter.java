/**
 * MergeCFSegmenter.java
 * 
 * Revision History:<br>
 * August 27, 2008 awolin - File created
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
package srl.segmentation.mergecf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import srl.core.exception.InvalidParametersException;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.recognition.paleo.ArcFit;
import srl.recognition.paleo.LineFit;
import srl.recognition.paleo.OrigPaleoSketchRecognizer;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;
import srl.segmentation.sezgin.SezginSegmenter;


/**
 * MergeCF segmentation algorithm. This algorithm is built upon many of the
 * features described in Sezgin et al.'s algorithm, and therefore uses many
 * {@link SezginSegmenter} methods. The actual corner detection algorithm uses
 * Paulson's {@link PaleoSketchRecognizer} to computer fit errors, and the
 * fitting of the corners in MergeCF varies heavily from Sezgin's.
 * 
 * @author awolin
 */
public class MergeCFSegmenter extends SezginSegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "MergeCF";

	/**
	 * Configuration file for what primitives we support
	 */
	private PaleoConfig m_paleoConfig;

	/**
	 * Default constructor
	 */
	public MergeCFSegmenter() {
		this(new Stroke());
	}

	/**
	 * Constructor.
	 */
	public MergeCFSegmenter(Stroke stroke) {

		setStroke(stroke);

		// Initialize the Paleo configuration file
		m_paleoConfig = PaleoConfig.allOff();
		m_paleoConfig.setArcTestOn(true);
		m_paleoConfig.setLineTestOn(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getSegmentations()
	 */
	@Override
	public List<Segmentation> getSegmentations()
			throws InvalidParametersException {

		if (m_origStroke == null)
			throw new InvalidParametersException();

		List<Segmentation> segmentations = new ArrayList<Segmentation>();

		// If the stroke is exceptionally short, creates a dummy copy of
		// m_origStroke and uses it for a substroke
		if (m_stroke.getPathLength() < S_SHORT_STROKE_THRESHOLD) {

			List<Integer> endPoints = new ArrayList<Integer>();
			endPoints.add(0);
			endPoints.add(m_origStroke.getNumPoints() - 1);

			// TODO - better confidence value
			segmentations = segmentStroke(m_origStroke, endPoints,
					S_SEGMENTER_NAME, 0.95);
		} else {

			List<Integer> corners = getCorners();

			// TODO - better confidence
			segmentations = segmentStroke(m_origStroke, corners,
					S_SEGMENTER_NAME, 0.80);
		}

		return segmentations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.segmentation.ISegmenter#getName()
	 */
	@Override
	public String getName() {
		return S_SEGMENTER_NAME;
	}

	/**
	 * Finds the corners for a stroke
	 * 
	 * @param s
	 *            Stroke to find the corners for
	 * @return Corners of a stroke
	 */
	private List<Integer> getCorners() {

		double[] pathLengths = calcPathLengths(m_stroke);
		double[] directions = calcDirections(true);
		double[] curvatures = calcCurvatures(pathLengths, directions, true);
		double[] speeds = calcSpeeds(pathLengths, true);

		// Get the corners from curvature and speed
		List<Integer> Fc = generateCornersFromCurvature(curvatures,
				S_AVG_CURVATURE_THRESHOLD);
		List<Integer> Fs = generateCornersFromSpeed(speeds,
				S_AVG_SPEED_THRESHOLD);

		// Calculate an initial fit for the corners
		List<Integer> corners = mergeFit(curvatures, speeds, pathLengths, Fc,
				Fs);

		// Map the corners back to the original points (i.e., non-cleaned)
		List<Integer> origCorners = getOriginalStrokeCorners(corners, m_stroke,
				m_origStroke);

		return origCorners;
	}

	/**
	 * Original merge fit algorithm
	 * 
	 * @param Fc
	 * @param Fs
	 * @return
	 */
	private List<Integer> mergeFit(double[] curvatures, double[] speeds,
			double[] pathLengths, List<Integer> Fc, List<Integer> Fs) {

		// Get all corners
		List<Integer> allCorners = new ArrayList<Integer>();
		allCorners.addAll(Fc);
		allCorners.addAll(Fs);

		Collections.sort(allCorners);

		// Filter out similar corners
		filterCorners(allCorners, curvatures, speeds, pathLengths);

		if (true) {
			// return allCorners;
		}

		List<Integer> corners = new ArrayList<Integer>();
		corners.addAll(allCorners);

		double error0 = brandonFitError(corners.get(0),
				corners.get(corners.size() - 1), pathLengths);
		double thisFitError = error0;
		double prevError = error0;

		double run = 0.0;

		double smallEnoughError = 0.0;

		boolean everythingChecked = false;

		do {
			prevError = thisFitError;
			run++;

			TreeMap<Double, Integer> segmentRatios = getSegmentRatios(corners,
					pathLengths);

			double avgSegmentRatio = 1.0 / (double) corners.size();

			double segmentRatioThreshold = (avgSegmentRatio / 2.0) * run;

			ArrayList<Integer> toRemove = new ArrayList<Integer>();

			double fitWindow = 1.5;

			while (!segmentRatios.isEmpty()
					&& segmentRatios.firstKey() < segmentRatioThreshold) {

				Map.Entry<Double, Integer> smallestSegment = segmentRatios
						.pollFirstEntry();

				int segmentIndex = smallestSegment.getValue();

				int c1 = corners.get(segmentIndex);
				int c2 = corners.get(segmentIndex + 1);

				if (c1 > 0 && c2 < corners.get(corners.size() - 1)) {
					int c0 = corners.get(segmentIndex - 1);
					int c3 = corners.get(segmentIndex + 2);

					double fitLeft = brandonFitError(c0, c1, c2, pathLengths);
					double fitRight = brandonFitError(c1, c2, c3, pathLengths);

					double currFitMiddle = brandonFitError(c1, c2, pathLengths);
					double currFitLeft = brandonFitError(c0, c1, pathLengths)
							+ currFitMiddle;
					double currFitRight = currFitMiddle
							+ brandonFitError(c2, c3, pathLengths);

					if (fitLeft < fitRight
							&& (fitLeft < currFitLeft * fitWindow || fitLeft < smallEnoughError)) {
						toRemove.add(new Integer(c1));
					} else if (fitRight < currFitRight * fitWindow
							|| fitRight < smallEnoughError) {
						toRemove.add(new Integer(c2));
					}
				} else if (c1 == 0 && c2 < corners.get(corners.size() - 1)) {
					int c3 = corners.get(segmentIndex + 2);

					double fitRight = brandonFitError(c1, c2, c3, pathLengths);
					double currFit = brandonFitError(c1, c2, pathLengths)
							+ brandonFitError(c2, c3, pathLengths);

					if (fitRight < currFit * fitWindow
							|| fitRight < smallEnoughError) {
						toRemove.add(new Integer(c2));
					}
				} else if (c1 > 0 && c2 == corners.get(corners.size() - 1)) {
					int c0 = corners.get(segmentIndex - 1);

					double fitLeft = brandonFitError(c0, c1, c2, pathLengths);
					double currFit = brandonFitError(c0, c1, pathLengths)
							+ brandonFitError(c1, c2, pathLengths);

					if (fitLeft < currFit * fitWindow
							|| fitLeft < smallEnoughError) {
						toRemove.add(new Integer(c1));
					}
				}
			}

			// Remove corners to be culled
			while (!toRemove.isEmpty()) {
				Integer removeCorner = toRemove.remove(0);

				if (corners.contains(removeCorner))
					corners.remove(removeCorner);
			}

			thisFitError = brandonFitError(corners.get(0),
					corners.get(corners.size() - 1), pathLengths);

			if (segmentRatios.isEmpty())
				everythingChecked = true;
		} while (!everythingChecked);

		// Check lines to remove them
		int i = 1;
		while (i < corners.size() - 1) {

			int corner0 = corners.get(i - 1);
			int corner1 = corners.get(i);
			int corner2 = corners.get(i + 1);

			if (isLine(corner0, corner1, m_stroke, pathLengths,
					S_LINE_VS_ARC_THRESHOLD)
					&& isLine(corner1, corner2, m_stroke, pathLengths,
							S_LINE_VS_ARC_THRESHOLD)) {

				Point p0 = m_stroke.getPoint(corner0);
				Point p1 = m_stroke.getPoint(corner1);
				Point p2 = m_stroke.getPoint(corner2);

				double dx1 = p1.getX() - p0.getX();
				double dy1 = p1.getY() - p0.getY();

				double angle1 = Math.atan2(dy1, dx1);

				double dx2 = p2.getX() - p1.getX();
				double dy2 = p2.getY() - p1.getY();

				double angle2 = Math.atan2(dy2, dx2);

				if (Math.abs(angle1 - angle2) < Math.PI / 9.0) {
					corners.remove(new Integer(corner1));
				} else {
					i++;
				}
			} else {
				i++;
			}
		}

		return corners;
	}

	/**
	 * Filters corners
	 * 
	 * @param curvatures
	 *            Curvature array of the stroke
	 * @param speeds
	 *            Speed array of the stroke
	 * @param pathLengths
	 *            Arc length array of the stroke
	 * @param corners
	 *            Corners to filter corners
	 */
	private void filterCorners(List<Integer> corners, double[] curvatures,
			double[] speeds, double[] pathLengths) {
		int i = 1;

		// Remove similar corners
		while (i < corners.size()) {
			int corner1 = corners.get(i - 1);
			int corner2 = corners.get(i);

			if (areCornersSimilar(pathLengths, corner1, corner2)) {
				if (corner1 == 0)
					corners.remove(i);
				else if (corner2 == pathLengths.length - 1)
					corners.remove(i - 1);
				else if (curvatures[corner1] < curvatures[corner2])
					corners.remove(i - 1);
				else
					corners.remove(i);
			} else {
				i++;
			}
		}

		// Pixel distance threshold close to the endpoints
		double hookDistThreshold = 10.0;

		// How many indices away from the endpoint we should be
		int endPtThreshold = 3;

		// Calculate the average speed
		double avgSpeed = 0.0;
		for (double s : speeds) {
			avgSpeed += s;
		}
		avgSpeed /= speeds.length;

		// Remove corners too close to the stroke endpoints
		i = 1;
		while (i < corners.size() - 1) {
			if (pathLengths[corners.get(i)] < hookDistThreshold
					|| corners.get(i) < endPtThreshold
					|| corners.get(i) > pathLengths.length - endPtThreshold) {
				corners.remove(i);
			} else {
				i++;
			}
		}

		// Speed filter
		i = 1;
		while (i < corners.size() - 1) {
			if (speeds[corners.get(i)] > avgSpeed
					* S_STRICT_AVG_SPEED_THRESHOLD) {
				corners.remove(i);
			} else {
				i++;
			}
		}
	}

	/**
	 * Checks if two corners are similar to each other through a distance
	 * threshold
	 * 
	 * @param pathLengths
	 *            Array of path length values for points
	 * @param index1
	 *            Index of corner 1
	 * @param index2
	 *            Index of corner 2
	 * @return True if the two corners are similar to one another
	 */
	private boolean areCornersSimilar(double[] pathLengths, int index1,
			int index2) {

		// Pixel threshold to see if corners are too close
		double distThreshold = m_stroke.getBoundingBox().getDiagonalLength()
				* S_CLOSE_POINT_DIST_THRESHOLD;

		// Index threshold to see if the corners are too close
		int pointIndexThreshold = S_CLOSE_POINT_INDICES_THRESHOLD;

		// Are the indices the same or too close?
		if (index1 == index2 || index2 - index1 <= pointIndexThreshold)
			return true;

		// Are the two corners close to each other?
		if (pathLengths[index2] - pathLengths[index1] < distThreshold)
			return true;

		return false;
	}

	/**
	 * Calculate the segment's error using PaleoSketch. TODO - less
	 * computationally intensive
	 * 
	 * @param p1
	 *            Index of the first corner
	 * @param p2
	 *            Index of the second corner
	 * @return An error for the stroke segment
	 */
	private double brandonFitError(int p1, int p2, double[] pathLengths) {

		List<Point> pts = m_stroke.getPoints().subList(p1, p2);
		Stroke segment = new Stroke(pts);

		OrigPaleoSketchRecognizer paleoRecognizer = new OrigPaleoSketchRecognizer(
				segment, m_paleoConfig);

		LineFit lf = paleoRecognizer.getLineFit();
		ArcFit af = paleoRecognizer.getArcFit();

		boolean lineTestPassed = isLine(p1, p2, m_stroke, pathLengths,
				S_LINE_VS_ARC_THRESHOLD);

		if (lf.passed()) {
			return lf.getError();
		}
		if (af.passed() && !lineTestPassed) {
			return af.getError();
		}

		return Double.POSITIVE_INFINITY;

		// paleoRecognizer.recognize();
		// List<Fit> fits = paleoRecognizer.getFits();

		/*
		 * Fit bestFit = null; for (Fit f : fits) { if (bestFit == null ||
		 * bestFit.getError() < f.getError()) bestFit = f; }
		 * 
		 * if (bestFit != null) return bestFit.getError(); else return
		 * Double.POSITIVE_INFINITY;
		 */
	}

	/**
	 * Calculate the segment's error using PaleoSketch. TODO - less
	 * computationally intensive
	 * 
	 * @param p1
	 *            Index of the first corner
	 * @param p2
	 *            Index of the second corner
	 * @return An error for the stroke segment
	 */
	private double brandonFitError(int p1, int p2, int p3, double[] pathLengths) {

		boolean lineTestPassed1 = isLine(p1, p2, m_stroke, pathLengths, 0.88);
		boolean lineTestPassed2 = isLine(p2, p3, m_stroke, pathLengths, 0.88);

		List<Point> mergedPts = m_stroke.getPoints().subList(p1, p3);
		Stroke mergedSegment = new Stroke(mergedPts);

		OrigPaleoSketchRecognizer paleoRecognizer = new OrigPaleoSketchRecognizer(
				mergedSegment, m_paleoConfig);

		if (lineTestPassed1 && lineTestPassed2) {
			LineFit lf = paleoRecognizer.getLineFit();

			if (lf.passed()) {
				return lf.getError();
			}
		} else {
			ArcFit af = paleoRecognizer.getArcFit();

			if (af.passed()) {
				return af.getError();
			}
		}

		return Double.POSITIVE_INFINITY;

		// paleoRecognizer.recognize();
		// List<Fit> fits = paleoRecognizer.getFits();

		/*
		 * Fit bestFit = null; for (Fit f : fits) { if (bestFit == null ||
		 * bestFit.getError() < f.getError()) bestFit = f; }
		 * 
		 * if (bestFit != null) return bestFit.getError(); else return
		 * Double.POSITIVE_INFINITY;
		 */
	}

	/**
	 * Gets the length of each segment as a percentage of the total stroke
	 * length, sorted into a TreeMap.
	 * 
	 * @param corners
	 *            Corners of the stroke
	 * @param pathLengths
	 *            Path lengths of the points
	 * @return Sorted segment ratios
	 */
	private TreeMap<Double, Integer> getSegmentRatios(List<Integer> corners,
			double[] pathLengths) {

		TreeMap<Double, Integer> segmentRatios = new TreeMap<Double, Integer>();

		double totalLength = pathLengths[pathLengths.length - 1];

		for (int i = 0; i < corners.size() - 1; i++) {
			double ratio = (pathLengths[corners.get(i + 1)] - pathLengths[corners
					.get(i)]) / totalLength;
			segmentRatios.put(ratio, i);
		}

		return segmentRatios;
	}

	/*
	 * Get the segment type from Brandon's segment
	 * 
	 * @param p1 @param p2 @return
	 * 
	 * private CornerFinder.SegType getBSegType(int p1, int p2) { BStroke
	 * bSegment = new BStroke(pts.subList(p1, p2)); ArrayList<Fit> fits =
	 * bSegment.recognize(false);
	 * 
	 * Fit bestFit = fits.get(0); String fitName = bestFit.getName();
	 * 
	 * if (fitName == Fit.LINE) return CornerFinder.SegType.Line;
	 * 
	 * else if (fitName == Fit.ARC) return CornerFinder.SegType.Arc;
	 * 
	 * else return CornerFinder.SegType.Other; }
	 */
}
