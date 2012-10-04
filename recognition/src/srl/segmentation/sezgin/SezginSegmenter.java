/**
 * SezginSegmenter.java
 * 
 * Revision History:<br>
 * August 26, 2008 awolin - File created
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
package srl.segmentation.sezgin;

import org.openawt.geom.Line2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import srl.core.exception.InvalidParametersException;
import srl.core.sketch.ISegmenter;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.segmentation.AbstractSegmenter;
import srl.segmentation.combination.objectiveFunctions.MSEObjectiveFunction;


/**
 * A segmentation algorithm based mainly on Sezgin's work with speed, curvature,
 * and hybrid fits. Heavily augmented with tweaks in order to boost the
 * percentage.
 * 
 * @author awolin
 */
public class SezginSegmenter extends AbstractSegmenter implements
		ISezginThresholds, ISegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "Sezgin";

	/**
	 * For outputting some debug values
	 */
	private static final boolean S_DEBUG = false;

	/**
	 * Original stroke
	 */
	protected Stroke m_origStroke;

	/**
	 * Stroke to segment
	 */
	protected Stroke m_stroke;

	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<Segmentation> m_threadedSegmentations = null;

	/**
	 * Default constructor
	 */
	public SezginSegmenter() {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.segmentation.ISegmenter#setStroke(edu.tamu.core.sketch.Stroke )
	 */
	public void setStroke(Stroke stroke) {
		m_origStroke = stroke;
		m_stroke = cleanStroke(m_origStroke);
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
	 * Finds the corners for a stroke
	 * 
	 * @return Corners of a stroke
	 */
	private List<Integer> getCorners() {

		// Get the arc length at each point
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
		List<Integer> corners = initialFit(Fc, Fs);

		// Find the best corners from a hybrid fit
		corners = hybridFit(curvatures, speeds, pathLengths, corners, Fc, Fs);

		// Graph output (for debugging)
		if (S_DEBUG) {
			outputDirectionGraph(directions, pathLengths);
			outputSpeedGraph(speeds, pathLengths);
			outputCurvatureGraph(curvatures, pathLengths);
			outputCornerGraph(curvatures, speeds, pathLengths, corners);
		}

		// Map the corners back to the original points (i.e., non-cleaned)
		List<Integer> origCorners = getOriginalStrokeCorners(corners, m_stroke,
				m_origStroke);

		return origCorners;
	}

	/*
	 * Feature functions
	 */

	/**
	 * Calculates the speed at each point
	 * 
	 * @param pts
	 *            Points of the stroke
	 * @param arcLength
	 *            Arc length at each point
	 * @param smooth
	 *            Should an average filter be applied?
	 * @return Speed values at each point
	 */
	protected double[] calcSpeeds(double[] pathLengths, boolean smooth) {

		double[] speeds = new double[m_stroke.getNumPoints()];

		// First pt's speed
		speeds[0] = 0.0;

		// Get speed the for each point in the stroke
		for (int i = 1; i < m_stroke.getNumPoints() - 1; i++) {
			double timeDiff = m_stroke.getPoint(i + 1).getTime()
					- m_stroke.getPoint(i - 1).getTime();

			if (timeDiff != 0.0)
				speeds[i] = (pathLengths[i + 1] - pathLengths[i - 1])
						/ timeDiff;
			else
				speeds[i] = speeds[i - 1];
		}

		// Last pt's speed
		speeds[m_stroke.getNumPoints() - 1] = 0.0;

		// Smooth the speed values
		if (smooth) {
			double[] smoothSpeed = new double[m_stroke.getNumPoints()];

			for (int i = 1; i < m_stroke.getNumPoints() - 1; i++) {
				smoothSpeed[i] = (speeds[i - 1] + speeds[i] + speeds[i + 1]) / 3.0;
			}

			speeds = smoothSpeed;
		}

		return speeds;
	}

	/**
	 * Calculates the direction at each point
	 * 
	 * @param smooth
	 *            Should an average filter be applied?
	 * @return Direction values at each point
	 */
	protected double[] calcDirections(boolean smooth) {

		double[] directions = new double[m_stroke.getNumPoints()];

		// Set the initial direction point
		directions[0] = 0.0;

		// Calculate the direction value for each point
		for (int i = 1; i < m_stroke.getNumPoints(); i++) {
			double d = direction(i);

			// Make sure there are no large jumps in direction - ensures graph
			// continuity
			while (d - directions[i - 1] > Math.PI) {
				d -= (Math.PI * 2);
			}
			while (directions[i - 1] - d > Math.PI) {
				d += (Math.PI * 2);
			}

			directions[i] = d;
		}

		// Average filtering
		if (smooth) {
			double[] smoothDirection = new double[m_stroke.getNumPoints()];

			for (int i = 1; i < m_stroke.getNumPoints() - 1; i++) {
				smoothDirection[i] = (directions[i - 1] + directions[i] + directions[i + 1]) / 3.0;
			}

			directions = smoothDirection;
		}

		return directions;
	}

	/**
	 * Calculates the direction at a point
	 * 
	 * @param pts
	 *            Points of the stroke
	 * @param index
	 *            Index of the point to check
	 * @return The direction at the index point
	 */
	protected double direction(int index) {

		if (index - 1 >= 0) {
			double dy = m_stroke.getPoint(index).getY()
					- m_stroke.getPoint(index - 1).getY();
			double dx = m_stroke.getPoint(index).getX()
					- m_stroke.getPoint(index - 1).getX();

			return Math.atan2(dy, dx);
		} else
			return 0.0;
	}

	/**
	 * Calculates the curvature values at each point
	 * 
	 * @param pathLengths
	 *            Path lengths of the points
	 * @param directions
	 *            Direction (angles) of the points
	 * @param smooth
	 *            Should an average filter be applied?
	 * @return The curvature value at each point
	 */
	protected double[] calcCurvatures(double[] pathLengths,
			double[] directions, boolean smooth) {

		double[] curvature = new double[m_stroke.getNumPoints()];

		curvature[0] = 0.0;

		// Calculate the curvature value for each point
		for (int i = 1; i < m_stroke.getNumPoints() - 1; i++) {
			double curv = curvature(pathLengths, directions, i, 3);

			// Hack to check if we have a divide-by-0 error
			if (curv != -1.0)
				curvature[i] = curv;
			else
				curvature[i] = curvature[i - 1];
		}

		// Average filtering
		if (smooth) {
			double[] smoothCurvature = new double[m_stroke.getNumPoints()];

			for (int i = 1; i < m_stroke.getNumPoints() - 1; i++) {
				smoothCurvature[i] = (curvature[i - 1] + curvature[i] + curvature[i + 1]) / 3.0;
			}

			curvature = smoothCurvature;
		}

		return curvature;
	}

	/**
	 * Finds the curvature at a given point index, given a point window, the arc
	 * lengths of the points, and the directions of the points
	 * 
	 * @param pathLengths
	 *            Path lengths of the points
	 * @param directions
	 *            Direction (angles) of the points
	 * @param index
	 *            Corner index
	 * @param window
	 *            Neighborhood window of points
	 * @return The curvature value for a point at the index
	 */
	protected double curvature(double[] pathLengths, double[] directions,
			int index, int window) {

		int start = index - window;
		if (index - window < 0)
			start = 0;

		int end = index + window;
		if (end + window > pathLengths.length)
			end = pathLengths.length - 1;

		double segmentLength = pathLengths[end] - pathLengths[start];

		if (segmentLength > 0.0) {
			double dirChanges = 0.0;

			for (int i = start + 1; i <= end; i++) {
				dirChanges += (directions[i] - directions[i - 1]);
			}

			return Math.abs(dirChanges) / segmentLength;
		} else {
			return 0.0;
		}
	}

	/*
	 * Generate corner fits
	 */

	/**
	 * Finds possible corners from the curvature values of points. Uses the
	 * average curvature for a cutoff threshold.
	 * 
	 * @param curvatures
	 *            Curvature values for each point
	 * @return An ArrayList of indices indicating corners
	 */
	protected List<Integer> generateCornersFromCurvature(double[] curvatures,
			final double AVG_CURVATURE_THRESHOLD) {

		List<Integer> corners = new ArrayList<Integer>();

		double minCurvature = Double.POSITIVE_INFINITY;
		double maxCurvature = Double.NEGATIVE_INFINITY;
		double avgCurvature = 0.0;

		// Calculate the average, minimum, and maximum curvatures
		for (int i = 0; i < curvatures.length; i++) {
			avgCurvature += curvatures[i];

			if (curvatures[i] < minCurvature)
				minCurvature = curvatures[i];

			if (curvatures[i] > maxCurvature)
				maxCurvature = curvatures[i];
		}

		avgCurvature /= (double) curvatures.length;

		// Curvature values above this threshold will be considered for corners
		double threshold = avgCurvature * AVG_CURVATURE_THRESHOLD;

		// Find corners where our curvature is over the average curvature
		// threshold
		for (int i = 0; i < curvatures.length; i++) {

			// Find only the local maximum
			if (curvatures[i] > threshold) {

				double localMaximum = Double.NEGATIVE_INFINITY;
				int localMaximumIndex = i;

				while (i < curvatures.length && curvatures[i] > threshold) {

					if (curvatures[i] > localMaximum) {
						localMaximum = curvatures[i];
						localMaximumIndex = i;
					}

					i++;
				}

				corners.add(new Integer(localMaximumIndex));
			}
		}

		// Add the endpoints
		if (!corners.contains(0))
			corners.add(0);
		if (!corners.contains(curvatures.length - 1))
			corners.add(curvatures.length - 1);

		// Sort the corners
		Collections.sort(corners);

		// Return the list of corners (indices)
		return corners;
	}

	/**
	 * Finds possible corners from the speed values of points.
	 * 
	 * @param speeds
	 *            Speed values for each point
	 * @return An ArrayList of indices indicating corners
	 */
	protected List<Integer> generateCornersFromSpeed(double[] speeds,
			final double AVG_SPEED_THRESHOLD) {

		List<Integer> corners = new ArrayList<Integer>();

		double minSpeed = Double.POSITIVE_INFINITY;
		double maxSpeed = Double.NEGATIVE_INFINITY;
		double avgSpeed = 0.0;

		// Calculate the average, minimum, and maximum speed
		for (int i = 0; i < speeds.length; i++) {
			avgSpeed += speeds[i];

			if (speeds[i] < minSpeed)
				minSpeed = speeds[i];

			if (speeds[i] > maxSpeed)
				maxSpeed = speeds[i];
		}

		avgSpeed /= (double) speeds.length;

		// Speed values below this threshold will be considered for corners
		double threshold = avgSpeed * AVG_SPEED_THRESHOLD;

		// Find corners where our speed is under the average curvature threshold
		for (int i = 0; i < speeds.length; i++) {

			// Find only the local minimum
			if (speeds[i] < threshold) {

				double localMinimum = Double.POSITIVE_INFINITY;
				int localMinimumIndex = i;

				while (i < speeds.length && speeds[i] < threshold) {

					if (speeds[i] < localMinimum) {
						localMinimum = speeds[i];
						localMinimumIndex = i;
					}

					i++;
				}

				corners.add(new Integer(localMinimumIndex));
			}
		}

		// Add the endpoints
		if (!corners.contains(0))
			corners.add(0);
		if (!corners.contains(speeds.length - 1))
			corners.add(speeds.length - 1);

		// Sort the corners
		Collections.sort(corners);

		// Return the list of corners (indices)
		return corners;
	}

	/**
	 * Finds the initial corner fit by taking the intersection of the curvature
	 * and speed corners
	 * 
	 * @param Fc
	 *            Curvature corners
	 * @param Fs
	 *            Speed corners
	 * @return The intersection of Fc and Fs
	 */
	private List<Integer> initialFit(List<Integer> Fc, List<Integer> Fs) {

		List<Integer> corners = new ArrayList<Integer>();

		// Initial fit is comprised of the intersection of
		// curvature and speed corners
		int i = 0;

		while (i < Fc.size()) {
			int value = Fc.get(i).intValue();

			if (Fs.contains(value)) {
				corners.add(value);

				Integer corner = Fc.get(i);

				Fs.remove(corner);
				Fc.remove(corner);
			} else {
				i++;
			}
		}

		// Sort the collections, just to be safe
		Collections.sort(corners);
		Collections.sort(Fc);
		Collections.sort(Fs);

		return corners;
	}

	/**
	 * Generate hybrid fits from the corners, and choose the "best" fit based on
	 * the fit that is below a threshold with the least number of corners
	 * 
	 * @param curvatures
	 *            Curvature values in an array
	 * @param speeds
	 *            Speed values in an array
	 * @param pathLengths
	 *            Arc length values in an array
	 * @param corners
	 *            Initial fit
	 * @param Fc
	 *            Remaining curvature corners
	 * @param Fs
	 *            Remaining speed corners
	 * @return Corners representing the best hybrid fit for the stroke
	 */
	private List<Integer> hybridFit(double[] curvatures, double[] speeds,
			double[] pathLengths, List<Integer> corners, List<Integer> Fc,
			List<Integer> Fs) {

		// Filter out similar corners
		filterCorners(curvatures, speeds, pathLengths, Fc, Fs);

		// Calculate the curvature and speed metrics
		TreeMap<Double, Integer> curvatureMetrics = calcCurvatureMetrics(
				curvatures, pathLengths, Fc);
		TreeMap<Double, Integer> speedMetrics = calcSpeedMetrics(speeds, Fs);

		// Get a fit for all of the corners
		List<Integer> allCorners = new ArrayList<Integer>();
		allCorners.addAll(corners);
		allCorners.addAll(Fc);
		allCorners.addAll(Fs);
		Collections.sort(allCorners);

		// Get the initial fit
		double error0 = calculateFitError(corners, pathLengths);

		// Get the fit for all of the corners
		double errorAll = calculateFitError(allCorners, pathLengths);

		// Create an error threshold
		double errorThreshold = (0.1 * (error0 - errorAll)) + errorAll;

		// Check to see if the two endpoints are a decent enough approximation
		// double singleShapeRatio = S_SINGLE_SHAPE_APPROXIMATION_THRESHOLD;
		// if (errorAll / errorThreshold > singleShapeRatio || error0 < 100.0)
		// return corners;

		// Storage for the hybrid fits
		TreeMap<Double, List<Integer>> hybridFits = new TreeMap<Double, List<Integer>>();

		// Generate the Hybrid fits
		List<Integer> Hi = new ArrayList<Integer>();
		Hi.addAll(corners);

		// Create hybrid fits
		while (!curvatureMetrics.isEmpty() || !speedMetrics.isEmpty()) {

			// Create a hybrid fit for curvature
			List<Integer> HiP = new ArrayList<Integer>();
			HiP.addAll(Hi);

			double HiPFit = Double.POSITIVE_INFINITY;
			Double HiPKey = 0.0;

			if (!curvatureMetrics.isEmpty()) {
				HiP.add(curvatureMetrics.lastEntry().getValue());
				HiPKey = curvatureMetrics.lastEntry().getKey();
				Collections.sort(HiP);

				HiPFit = calculateFitError(HiP, pathLengths);
			}

			// Create a hybrid fit for speed
			List<Integer> HiPP = new ArrayList<Integer>();
			HiPP.addAll(Hi);

			double HiPPFit = Double.POSITIVE_INFINITY;
			Double HiPPKey = 0.0;

			if (!speedMetrics.isEmpty()) {
				HiPP.add(speedMetrics.lastEntry().getValue());
				HiPPKey = speedMetrics.lastEntry().getKey();
				Collections.sort(HiPP);

				HiPPFit = calculateFitError(HiPP, pathLengths);
			}

			// Set the next fit in the series
			if (HiPFit <= HiPPFit) {
				hybridFits.put(HiPFit, HiP);
				Hi = HiP;

				curvatureMetrics.remove(HiPKey);
			} else {
				hybridFits.put(HiPPFit, HiPP);
				Hi = HiPP;

				speedMetrics.remove(HiPPKey);
			}
		}

		// Find the best fit by corners
		List<Integer> bestFit = corners;
		int fewestCorners = Integer.MAX_VALUE;

		// while (hybridFits.size() > 1) {
		// hybridFits.pollFirstEntry();
		// }
		//
		// if (true)
		// return hybridFits.firstEntry().getValue();

		while (!hybridFits.isEmpty()) {
			Map.Entry<Double, List<Integer>> entry = hybridFits
					.pollFirstEntry();

			double fitError = entry.getKey();
			int numCorners = entry.getValue().size();

			if (numCorners < fewestCorners && fitError < errorThreshold) {
				fewestCorners = numCorners;
				bestFit = entry.getValue();
			}
		}

		bestFit = curveCheck(bestFit, pathLengths);

		return bestFit;
	}

	private List<Integer> curveCheck(List<Integer> corners, double[] pathLengths) {

		List<Integer> newCorners = new ArrayList<Integer>(corners);

		MSEObjectiveFunction cv = new MSEObjectiveFunction();

		for (int i = 1; i < corners.size(); i++) {
			int p1 = corners.get(i - 1);
			int p2 = corners.get(i);

			if (!isLine(p1, p2, m_stroke, pathLengths, S_LINE_VS_ARC_THRESHOLD)) {

				Stroke cvStroke = new Stroke(m_stroke.getPoints().subList(p1,
						p2));

				double err = cv.solveCurve(cvStroke);

				if (err > 20.0) {
					corners.add((p1 + p2) / 2);
					Collections.sort(corners);
					i--;
				}

				// System.out.println("Cv err = " + err);
			}
		}

		return corners;
	}

	/*
	 * Metrics for curvature and speed
	 */

	/**
	 * Calculates the CCM metric for all corners given
	 * 
	 * @param curvatures
	 *            Curvature values in an array
	 * @param pathLengths
	 *            Path length values in an array
	 * @param corners
	 *            Corners to calculate the metric for
	 * @return A sorted (ascending) TreeMap mapping the metric value with the
	 *         corner
	 */
	private TreeMap<Double, Integer> calcCurvatureMetrics(double[] curvatures,
			double[] pathLengths, List<Integer> corners) {

		TreeMap<Double, Integer> curvatureMetrics = new TreeMap<Double, Integer>();

		for (Integer c : corners) {
			curvatureMetrics.put(CCM(curvatures, pathLengths, c), c);
		}

		return curvatureMetrics;
	}

	/**
	 * Calculates the SCM metric for all corners given
	 * 
	 * @param speeds
	 *            Speed values in an array
	 * @param corners
	 *            Corners to calculate the metric for
	 * @return A sorted (ascending) TreeMap mapping the metric value with the
	 *         corner
	 */
	private TreeMap<Double, Integer> calcSpeedMetrics(double[] speeds,
			List<Integer> corners) {

		TreeMap<Double, Integer> speedMetrics = new TreeMap<Double, Integer>();

		// Calculate the maximum speed
		double maxSpeed = Double.MIN_VALUE;
		for (double s : speeds) {
			if (s > maxSpeed) {
				maxSpeed = s;
			}
		}

		for (Integer c : corners) {
			speedMetrics.put(SCM(speeds, maxSpeed, c), c);
		}

		return speedMetrics;
	}

	/**
	 * CCM metric from Sezgin's paper
	 * 
	 * @param curvatures
	 *            Curvature values in an array
	 * @param pathLengths
	 *            Path length values in an array
	 * @param index
	 *            Index of the point (corner)
	 * @return The computed metric
	 */
	private double CCM(double[] curvatures, double[] pathLengths, int index) {

		// Window
		int k = 3;

		int start = index - k;
		if (start < 0)
			start = 0;

		int end = index + k;
		if (end > curvatures.length - 1)
			end = curvatures.length - 1;

		double segmentLength = pathLengths[end] - pathLengths[start];

		if (segmentLength > 0.0)
			return Math.abs(curvatures[start] - curvatures[end])
					/ segmentLength;
		else
			return 0.0;
	}

	/**
	 * SCM metric from Sezgin's paper
	 * 
	 * @param speeds
	 *            Curvature values in an array
	 * @param maxSpeed
	 *            Maximum speed value of the stroke
	 * @param index
	 *            Index of the point (corner)
	 * @return The computed metric
	 */
	private double SCM(double[] speeds, double maxSpeed, int index) {

		if (maxSpeed > 0.0)
			return 1.0 - (speeds[index] / maxSpeed);
		else
			return 1.0;
	}

	/**
	 * Removes corners based on similarity. This is my work, not Sezgin's
	 * 
	 * @param curvatures
	 *            Curvature values for points
	 * @param speeds
	 *            Speed values for points
	 * @param pathLengths
	 *            Arc length values for points
	 * @param Fc
	 *            Curvature corners found
	 * @param Fs
	 *            Speed corners
	 * @return A List of filtered corners, removing some extraneous ones from
	 *         the set
	 */
	private void filterCorners(double[] curvatures, double[] speeds,
			double[] pathLengths, List<Integer> Fc, List<Integer> Fs) {

		List<Integer> corners = new ArrayList<Integer>();
		corners.addAll(Fc);
		corners.addAll(Fs);
		Collections.sort(corners);

		int i = 1;

		// Remove similar corners
		while (i < corners.size()) {
			int corner1 = corners.get(i - 1);
			int corner2 = corners.get(i);

			if (areCornersSimilar(pathLengths, corner1, corner2)) {
				if (CCM(curvatures, pathLengths, corner1) < CCM(curvatures,
						pathLengths, corner2))
					corners.remove(i - 1);
				else
					corners.remove(i);
			} else {
				i++;
			}
		}

		// Pixel distance threshold close to the endpoints
		double hookDistThreshold = Math.min(m_stroke.getBoundingBox()
				.getDiagonalLength() * S_PERC_HOOK_THRESHOLD,
				S_MAX_HOOK_THRESHOLD);

		// How many indices away from the endpoint we should be
		int endPtThreshold = 3;

		// Calculate the average speed
		double avgSpeed = 0.0;
		for (double s : speeds) {
			avgSpeed += s;
		}
		avgSpeed /= speeds.length;

		// Remove corners too close to the stroke endpoints
		i = 0;
		while (i < corners.size()) {
			if (pathLengths[corners.get(i)] < hookDistThreshold
					|| pathLengths[pathLengths.length - 1]
							- pathLengths[corners.get(i)] < hookDistThreshold
					|| corners.get(i) < endPtThreshold
					|| corners.get(i) > pathLengths.length - endPtThreshold) {
				corners.remove(i);
			} else {
				i++;
			}
		}

		i = 0;
		while (i < corners.size() - 1) {
			if (speeds[corners.get(i)] > avgSpeed
					* S_STRICT_AVG_SPEED_THRESHOLD) {
				corners.remove(i);
			} else {
				i++;
			}
		}

		// Resort the corners into their respective Fc and Fs holders
		i = 0;
		while (i < Fc.size()) {
			if (!corners.contains(Fc.get(i))) {
				Fc.remove(i);
			} else {
				i++;
			}
		}

		i = 0;
		while (i < Fs.size()) {
			if (!corners.contains(Fs.get(i))) {
				Fs.remove(i);
			} else {
				i++;
			}
		}

		return;
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
	 * Calculates the fit error for the stroke, given the corners
	 * 
	 * @param corners
	 *            Corner fit to calculate the error for
	 * @param totalLength
	 *            Total length of the stroke
	 * @return The orthogonal distance squared error for the entire stroke
	 */
	private double calculateFitError(List<Integer> corners, double[] pathLengths) {

		int numCorners = corners.size();

		double totalError = 0.0;
		double totalLength = pathLengths[pathLengths.length - 1];

		for (int i = 1; i < numCorners; i++) {
			int p1 = corners.get(i - 1);
			int p2 = corners.get(i);

			double error = segmentError(p1, p2, pathLengths);

			if (!Double.isNaN(error))
				totalError += error;
		}

		if (totalLength > 0.0)
			return totalError / totalLength;
		else
			return totalError;
	}

	/**
	 * Segmentation error for a stroke
	 * 
	 * @param p1
	 *            Starting index point
	 * @param p2
	 *            Ending index point
	 * @param arcLength
	 *            Arc length array of the stroke
	 * @return Segmentation error
	 */
	private double segmentError(int p1, int p2, double[] pathLengths) {

		double error = 0.0;

		// if (isLine(p1, p2, m_stroke, pathLengths, S_LINE_VS_ARC_THRESHOLD)) {
		error = orthogonalDistanceSquared(m_stroke.getPoints(), p1, p2);
		// }
		// else {
		// error = arcOrthogonalDistanceSquared(m_stroke.getPoints(), p1, p2);
		// }

		return error;
	}

	/**
	 * Compute the ODSQ using the original stroke and an optimal line between
	 * two points
	 * 
	 * @param pts
	 *            Points of the stroke
	 * @param p1
	 *            End point one of the line (corner 1)
	 * @param p2
	 *            End point two of the line (corner 2)
	 * @return The ODSQ value for the segment
	 */
	protected double orthogonalDistanceSquared(List<Point> pts, int p1, int p2) {
		double error = 0.0;

		// Set the line between the corners
		Line2D.Double line = new Line2D.Double();
		line.setLine(pts.get(p1).getX(), pts.get(p1).getY(),
				pts.get(p2).getX(), pts.get(p2).getY());

		// Checking for weird times when the point at p1 and p2 are equal
		if (pts.get(p1).getX() == pts.get(p2).getX()
				&& pts.get(p1).getY() == pts.get(p2).getY()) {
			for (int i = p1 + 1; i < p2; i++) {
				double dist = pts.get(p1).distance(pts.get(i));

				error += (dist * dist);
			}
		}

		// Get the orthogonal distance between each point in the stroke and the
		// line
		for (int i = p1 + 1; i < p2; i++) {

			// Running into an odd issue when a point is on the line, yet not
			// part of the line segment:
			// p1------p2 * <- pt we're looking at is here
			try {
				double distSq = line.ptLineDistSq(pts.get(i).getX(), pts.get(i)
						.getY());
				error += distSq;
			} catch (Exception e) {
				// Do nothing then for this point
			}
		}

		return error;
	}

	/**
	 * Compute the ODSQ of an arc using the original stroke and an optimal line
	 * between two points
	 * 
	 * @param pts
	 *            Points of the stroke
	 * @param p1
	 *            End point one of the line (corner 1)
	 * @param p2
	 *            End point two of the line (corner 2)
	 * @return The ODSQ value for the segment
	 */
	protected double arcOrthogonalDistanceSquared(List<Point> pts, int p1,
			int p2) {

		double error = 0.0;

		Point pt1 = pts.get(p1);
		Point pt2 = pts.get(p2);

		// Set the line between the corners
		Line2D.Double line = new Line2D.Double();
		line.setLine(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());

		double lineLength = pt1.distance(pt2);
		double slope = Math.tan(line.getBounds2D().getHeight()
				/ line.getBounds2D().getWidth());
		double midX = Math.cos(slope) * (lineLength / 2.0);
		double midY = Math.sin(slope) * (lineLength / 2.0);

		Point midPoint = new Point(midX, midY);

		// Stolen from the old TLine
		double perpSlope = Math.atan2(line.getY2() - line.getY1(), line.getX2()
				- line.getX1())
				+ Math.PI / 2;
		double perpLine[] = { midX, midY,
				midX + Math.cos(perpSlope) * lineLength,
				midY + Math.sin(perpSlope) * lineLength };

		Line2D.Double perpBisector = new Line2D.Double();
		perpBisector
				.setLine(perpLine[0], perpLine[1], perpLine[2], perpLine[3]);

		// Also stolen from TLine
		double yInt = perpBisector.getP1().getY() - perpSlope
				* perpBisector.getP1().getX();

		Point pbPt1;
		Point pbPt2;

		// Convoluted way to ensure that our perpendicular bisector line will
		// definitely go through our stroke
		// It's long because there are many checks
		// TODO: Get rid of magic numbers
		if (Double.isInfinite(perpSlope)) {
			pbPt1 = new Point(midPoint.getX(), 0.0);
			pbPt2 = new Point(midPoint.getX(), 10000.0);
		} else if (perpSlope == 0.0) {
			pbPt1 = new Point(0, yInt);
			pbPt2 = new Point(10000.0, yInt);
		} else {
			if (yInt < 0.0) {
				pbPt1 = new Point((-yInt / perpSlope), 0.0);
				pbPt2 = new Point((10000.0 - yInt) / perpSlope, 10000.0);
			} else {
				double xInt = -yInt / perpSlope;

				if (xInt < 0.0) {
					pbPt2 = new Point(0, yInt);
					pbPt1 = new Point(10000.0, (10000.0 * perpSlope) + yInt);
				} else {
					pbPt1 = new Point(xInt, 0.0);
					pbPt2 = new Point(0, yInt);
				}
			}
		}

		Point p3 = midPoint;

		// Get a third point that intersects the _stroke_ around its midpoint
		for (int i = p1; i < p2 - 1; i += 2) {
			if (isIntersection(pts.get(i), pts.get(i + 2), pbPt1, pbPt2)) {
				double newX = (pts.get(i + 2).getX() + pts.get(i).getX()) / 2.0;
				double newY = (pts.get(i + 2).getY() + pts.get(i).getY()) / 2.0;
				p3 = new Point(newX, newY);

				i = p2;
			}
		}

		// http://mcraefamily.com/MathHelp/
		// GeometryConicSectionCircleEquationGivenThreePoints.htm
		double a = pts.get(p1).getX();
		double b = pts.get(p1).getY();
		double c = p3.getX();
		double d = p3.getY();
		double e = pts.get(p2).getX();
		double f = pts.get(p2).getY();

		double k = (0.5 * ((((a * a) + (b * b)) * (e - c))
				+ (((c * c) + (d * d)) * (a - e)) + (((e * e) + (f * f)) * (c - a))))
				/ ((b * (e - c)) + (d * (a - e)) + (f * (c - a)));

		double h = (0.5 * ((((a * a) + (b * b)) * (f - d))
				+ (((c * c) + (d * d)) * (b - f)) + (((e * e) + (f * f)) * (d - b))))
				/ ((a * (f - d)) + (c * (b - f)) + (e * (d - b)));

		// If we're actually a line
		if (Double.isInfinite(k) || Double.isInfinite(h) || Double.isNaN(k)
				|| Double.isNaN(h))
			return orthogonalDistanceSquared(pts, p1, p2);

		// Set the circle's center and radius
		Point center = new Point(h, k);
		double radius = Math.sqrt(((a - h) * (a - h)) + ((b - k) * (b - k)));

		// Get the orthogonal distance between each point in the stroke and the
		// line
		for (int i = p1; i <= p2; i++) {
			double euc = pts.get(i).distance(center);
			double dist = radius - euc;

			error += (dist * dist);
		}

		return error;
	}

	/**
	 * http://www.mema.ucl.ac.be/~wu/FSA2716/Exercise1.htm
	 * 
	 * @param pt1
	 *            Endpoint 1 for line 1
	 * @param pt2
	 *            Endpoint 2 for line 1
	 * @param pt3
	 *            Endpoint 1 for line 2
	 * @param pt4
	 *            Endpoint 2 for line 2
	 * @return Whether the two lines intersect
	 */
	protected boolean isIntersection(Point pt1, Point pt2, Point pt3, Point pt4) {

		// A
		double Ax = pt1.getX();
		double Ay = pt1.getY();

		// B
		double Bx = pt2.getX();
		double By = pt2.getY();

		// C
		double Cx = pt3.getX();
		double Cy = pt3.getY();

		// D
		double Dx = pt4.getX();
		double Dy = pt4.getY();

		double denom = (((Bx - Ax) * (Dy - Cy)) - ((By - Ay) * (Dx - Cx)));

		// AB and CD are parallel
		if (denom == 0.0)
			return false;

		double numR = (((Ay - Cy) * (Dx - Cx)) - ((Ax - Cx) * (Dy - Cy)));
		double r = numR / denom;

		double numS = (((Ay - Cy) * (Bx - Ax)) - ((Ax - Cx) * (By - Ay)));
		double s = numS / denom;

		// An intersection exists
		if (r >= 0.0 && r <= 1.0 && s >= 0.0 && s <= 1.0)
			return true;

		return false;
	}

	/*
	 * Output information to create graphs in Excel
	 */

	/**
	 * Outputs a direction graph in a .txt file that can be imported to Excel
	 * 
	 * @param directions
	 *            Directions of a stroke
	 * @param pathLengths
	 *            Path length values of a stroke
	 */
	private static void outputDirectionGraph(double[] directions,
			double[] pathLengths) {

		try {
			FileOutputStream fout = new FileOutputStream("dirGraph.txt");
			PrintStream p = new PrintStream(fout);

			for (int i = 0; i < directions.length; i++) {
				p.print(directions[i] + "\t");
				p.print(pathLengths[i] + "\n");
			}

			fout.close();
		} catch (IOException e) {
			System.err.println("Error: could not print to file");
			System.exit(-1);
		}
	}

	/**
	 * Outputs a curvature graph in a .txt file that can be imported to Excel
	 * 
	 * @param curvatures
	 *            Curvatures of a stroke
	 * @param pathLengths
	 *            Path length values of a stroke
	 */
	private static void outputCurvatureGraph(double[] curvatures,
			double[] pathLengths) {

		try {
			FileOutputStream fout = new FileOutputStream("curvGraph.txt");
			PrintStream p = new PrintStream(fout);

			for (int i = 0; i < curvatures.length; i++) {
				p.print(curvatures[i] + "\t");
				p.print(pathLengths[i] + "\n");
			}

			fout.close();
		} catch (IOException e) {
			System.err.println("Error: could not print to file");
			System.exit(-1);
		}
	}

	/**
	 * Outputs a speed graph in a .txt file that can be imported to Excel
	 * 
	 * @param speeds
	 *            Speeds of a stroke
	 * @param pathLengths
	 *            Path length values of a stroke
	 */
	private static void outputSpeedGraph(double[] speeds, double[] pathLengths) {

		try {
			FileOutputStream fout = new FileOutputStream("speedGraph.txt");
			PrintStream p = new PrintStream(fout);

			for (int i = 0; i < speeds.length; i++) {
				p.print(speeds[i] + "\t");
				p.print(pathLengths[i] + "\n");
			}

			fout.close();
		} catch (IOException e) {
			System.err.println("Error: could not print to file");
			System.exit(-1);
		}
	}

	/**
	 * Outputs a corner graph in a .txt file that can be imported to Excel
	 * 
	 * @param curvatures
	 *            Curvatures of a stroke
	 * @param speeds
	 *            Speeds of a stroke
	 * @param pathLengths
	 *            Path length values of a stroke
	 * @param corners
	 *            Corners of a stroke
	 */
	private static void outputCornerGraph(double[] curvatures, double[] speeds,
			double[] pathLengths, List<Integer> corners) {

		try {
			FileOutputStream fout = new FileOutputStream("cornerGraph.txt");
			PrintStream p = new PrintStream(fout);

			for (int i = 0; i < speeds.length; i++) {
				p.print(curvatures[i] + "\t");
				p.print(speeds[i] + "\t");
				p.print(pathLengths[i] + "\n");
			}

			p.println();
			for (Integer corner : corners) {
				p.println(corner);
			}

			fout.close();
		} catch (IOException e) {
			System.err.println("Error: could not print to file");
			System.exit(-1);
		}
	}
}
