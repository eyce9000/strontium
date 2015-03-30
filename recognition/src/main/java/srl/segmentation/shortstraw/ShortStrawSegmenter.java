/**
 * ShortStrawSegmenter.java
 * 
 * Revision History:<br>
 * July 21, 2008 awolin - File created
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
package srl.segmentation.shortstraw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import srl.core.exception.InvalidParametersException;
import srl.core.sketch.ISegmenter;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.segmentation.AbstractSegmenter;


/**
 * A segmentation algorithm that uses a resampled stroke and a basic curvature
 * metric to find a polyline segmentation. Maps resampled corners found to their
 * closest, original-point counterparts.
 * 
 * TODO - somewhere in here the points are being changed in the original stroke
 * 
 * @author awolin
 */
public class ShortStrawSegmenter extends AbstractSegmenter implements
		IShortStrawThresholds, ISegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "ShortStraw";

	/**
	 * Whether or not we are using optimized calculations for ShortStraw
	 */
	private static final boolean S_OPTIMIZED = false;

	/**
	 * Corners possible at this percentage of straw length below the median
	 */
	private double m_medianPercentage = S_MEDIAN_PERCENTAGE;

	/**
	 * Original stroke to segment
	 */
	private Stroke m_origStroke;

	/**
	 * Resampled stroke. We store this as a Stroke rather than an Stroke to have
	 * some greater functionality. Any Stroke can be input in m_origStroke,
	 * though, since we create m_stroke based off the original stroke's points.
	 */
	private Stroke m_stroke;

	/**
	 * Resample spacing
	 */
	private double m_resampledSpacing;

	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<Segmentation> m_threadedSegmentations = null;

	/**
	 * Default constructor.
	 */
	public ShortStrawSegmenter() {
		// Do nothing
	}

	/**
	 * Constructor that takes in a stroke.
	 * 
	 * @param stroke
	 *            Stroke to segment
	 */
	public ShortStrawSegmenter(Stroke stroke) {
		this(stroke, S_MEDIAN_PERCENTAGE);
	}

	/**
	 * Constructor that takes in a stroke and a threshold for the straw length.
	 * 
	 * @param stroke
	 *            Stroke to segment
	 * @param medianPercentage
	 *            At what percentage below the median straw length should a
	 *            corner be possible
	 */
	public ShortStrawSegmenter(Stroke stroke, double medianPercentage) {
		setStroke(stroke);
		m_medianPercentage = medianPercentage;
	}

	/**
	 * Set the stroke to use for the corner finder. Resamples the spacing of the
	 * original stroke.
	 * 
	 * @param stroke
	 *            Stroke to segment in ShortStraw
	 */
	public void setStroke(Stroke stroke) {

		// Store the original stroke
		m_origStroke = stroke;

		// Initial processing (filtering, resampling) on the stroke
		m_stroke = cleanStroke(stroke);

		m_resampledSpacing = determineResampleSpacing(m_stroke.getPoints());

		// The stroke is too short to resample
		if (m_stroke.getPathLength() < S_SHORT_STROKE_THRESHOLD
				|| m_stroke.getNumPoints() < S_MIN_PTS_THRESHOLD
				|| m_resampledSpacing < S_SMALL_RESAMPLE_SPACING_THRESHOLD) {
			// Do nothing...
		}

		// Resample the stroke
		else {
			List<Point> points = resamplePoints(m_stroke, m_resampledSpacing);
			m_stroke = new Stroke(points);
		}
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

		// Check that we have a stroke
		if (m_stroke == null) {
			throw new InvalidParametersException();
		}

		List<Segmentation> segmentations = new ArrayList<Segmentation>();

		// If the stroke is exceptionally short, creates a dummy copy of
		// m_origStroke and uses it for a substroke
		if (m_stroke.getPathLength() < S_SHORT_STROKE_THRESHOLD
				|| m_stroke.getNumPoints() < S_MIN_PTS_THRESHOLD) {

			List<Integer> endPoints = new ArrayList<Integer>();
			endPoints.add(0);
			endPoints.add(m_origStroke.getNumPoints() - 1);

			// TODO - better confidence value
			segmentations = segmentStroke(m_origStroke, endPoints,
					S_SEGMENTER_NAME, 0.95);
		} else {
			List<Integer> corners = getCorners();

			// TODO - better confidence value
			segmentations = segmentStroke(m_origStroke, corners,
					S_SEGMENTER_NAME, 0.8);
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
	 * Resample the points of the stroke, based on the method described by
	 * Wobbrock in $1
	 * 
	 * @param s
	 *            Stroke to resample
	 * @param interspacing
	 *            Interspacing distance between points
	 * @return A new list of points for the stroke
	 */
	private List<Point> resamplePoints(Stroke s, double interspacing) {
		List<Point> points = s.getPoints();

		ArrayList<Point> newPoints = new ArrayList<Point>();
		newPoints.add(points.get(0));

		double D = 0;

		for (int i = 1; i < points.size(); i++) {

			// Get the current distance distance between the two points
			double d = points.get(i - 1).distance(points.get(i));

			if (D + d >= interspacing) {
				double q_x = points.get(i - 1).getX()
						+ (((interspacing - D) / d) * (points.get(i).getX() - points
								.get(i - 1).getX()));

				double q_y = points.get(i - 1).getY()
						+ (((interspacing - D) / d) * (points.get(i).getY() - points
								.get(i - 1).getY()));

				long q_t = points.get(i - 1).getTime()
						+ (long) (((interspacing - D) / d) * (points.get(i)
								.getTime() - points.get(i - 1).getTime()));

				Point q = new Point(q_x, q_y, q_t);

				newPoints.add(q);
				points.add(i, q);

				D = 0;
			} else {
				D = D + d;
			}
		}

		return newPoints;
	}

	/**
	 * Determines the resample spacing for the stroke
	 * 
	 * @param pts
	 *            A list of points to determine the spacing for
	 * @return The distance that each point should be resampled
	 */
	private double determineResampleSpacing(List<Point> pts) {

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < pts.size() - 1; i++) {
			double x = pts.get(i).getX();
			double y = pts.get(i).getY();

			if (x < minX)
				minX = x;
			if (x > maxX)
				maxX = x;
			if (y < minY)
				minY = y;
			if (y > maxY)
				maxY = y;
		}

		double diagonal = Math.sqrt(Math.pow(maxX - minX, 2)
				+ Math.pow(maxY - minY, 2));

		double spacing = diagonal / S_PTS_PER_DIAGONAL;

		return spacing;
	}

	/**
	 * Gets the corners from the resampled points. Works by finding the shortest
	 * local length around a corner
	 * 
	 * @return A list of corner indices for the stroke
	 * @throws InvalidParametersException
	 *             Thrown if the stroke has not been set
	 */
	private List<Integer> getCorners() throws InvalidParametersException {

		// If we do not have a stroke
		if (m_stroke == null) {
			throw new InvalidParametersException(
					"ShortStraw has not received any stroke to segment");
		}

		// Initialize necessary variables
		double[] straws = new double[m_stroke.getNumPoints()];
		double[] sortedStraws = new double[m_stroke.getNumPoints()
				- (S_WINDOW * 2)];

		// Calculate the straws
		for (int i = S_WINDOW; i < m_stroke.getNumPoints() - S_WINDOW; i++) {

			if (S_OPTIMIZED) {
				straws[i] = distanceSq(m_stroke.getPoint(i - S_WINDOW),
						m_stroke.getPoint(i + S_WINDOW));
			} else {
				straws[i] = m_stroke.getPoint(i - S_WINDOW).distance(
						m_stroke.getPoint(i + S_WINDOW));
			}

			// For finding the median
			sortedStraws[i - S_WINDOW] = straws[i];
		}

		// Initialize the corner array, with the start point
		List<Integer> corners = new ArrayList<Integer>();
		corners.add(0);

		// Calculate our local minimum thresholds
		Arrays.sort(sortedStraws);
		double medianDist = sortedStraws[sortedStraws.length / 2];
		double threshold = m_medianPercentage * medianDist;

		// Find the shortest straws
		for (int i = S_WINDOW; i < straws.length - S_WINDOW; i++) {

			// If we are below the median threshold
			if (straws[i] < threshold) {

				double localMinimum = Double.POSITIVE_INFINITY;
				int localMinimumIndex = i;

				// Find only the local minimum
				while (i < straws.length - S_WINDOW && straws[i] < threshold) {
					if (straws[i] < localMinimum) {
						localMinimum = straws[i];
						localMinimumIndex = i;
					}

					i++;
				}

				// Add the index of the local minimum to our list
				corners.add(new Integer(localMinimumIndex));
			}
		}

		// Add the end point
		corners.add(m_stroke.getNumPoints() - 1);

		// Process the corners to check that the initial set contains lines
		List<Integer> processedCorners = postProcessCorners(corners, straws);

		// Map the resampled corners to original points
		List<Integer> originalStrokeCorners = getOriginalStrokeCorners(
				processedCorners, m_stroke, m_origStroke);

		return originalStrokeCorners;
	}

	/**
	 * Process the initial corner set to find missed corners, remove corners
	 * when three are collinear, and remove corners too close to an endpoint
	 * 
	 * @param corners
	 *            A list of indices corresponding to possible corners
	 * @param straws
	 *            Array of straw lengths
	 * @return Processed corners
	 */
	private List<Integer> postProcessCorners(List<Integer> corners,
			double[] straws) {

		List<Integer> filteredCorners = new ArrayList<Integer>(corners);

		// Calculate the path lengths at each point, for use in the isLine test
		double[] pathLengths = null;
		if (S_OPTIMIZED) {
			pathLengths = optimizedPathLengths(m_stroke);
		} else {
			pathLengths = calcPathLengths(m_stroke);
		}

		// Check to see if all of our segments pass a line test
		boolean allLines = false;

		while (!allLines) {
			allLines = true;

			for (int i = 1; i < filteredCorners.size(); i++) {
				int c1 = filteredCorners.get(i - 1);
				int c2 = filteredCorners.get(i);

				if (!isLine(c1, c2, m_stroke, pathLengths, 0.95)) {

					int newCorner = minDistBetweenIndices(c1, c2, straws);
					filteredCorners.add(i, newCorner);

					allLines = false;
				}
			}
		}

		// Collinear test for the lines
		for (int i = 1; i < filteredCorners.size() - 1; i++) {
			int c1 = filteredCorners.get(i - 1);
			int c3 = filteredCorners.get(i + 1);

			if (isLine(c1, c3, m_stroke, pathLengths, 0.95)) {
				filteredCorners.remove(i);
				i--;
			}
		}

		// Adaptive threshold for how many pixels away we allow hooks to be
		double nPixelsAway = Math.min(m_stroke.getBoundingBox()
				.getDiagonalLength() * S_PERC_HOOK_THRESHOLD,
				S_MAX_HOOK_THRESHOLD);

		// Remove corners too close to the start and end points of the stroke
		// (i.e., hooks)
		while (filteredCorners.size() > 1
				&& m_stroke.getFirstPoint().distance(
						m_stroke.getPoint(filteredCorners.get(1))) < nPixelsAway) {
			filteredCorners.remove(1);
		}

		while (filteredCorners.size() > 2
				&& m_stroke.getLastPoint().distance(
						m_stroke.getPoint(filteredCorners.get(filteredCorners
								.size() - 2))) < nPixelsAway) {
			filteredCorners
					.remove(filteredCorners.get(filteredCorners.size() - 2));
		}

		return filteredCorners;
	}

	/**
	 * Relax the constraints on straws and find the minimum between two points,
	 * searching from 1/4 to 3/4 of the distance between the points.
	 * 
	 * @param p1
	 *            Index of the first point
	 * @param p2
	 *            Index of the second point
	 * @param straws
	 *            Array of straw lengths
	 * @return The index with the minimum straw located near the midpoint
	 *         between the two points given
	 */
	private int minDistBetweenIndices(int p1, int p2, double[] straws) {

		int minIndex = Integer.MAX_VALUE;
		double minValue = Double.POSITIVE_INFINITY;
		int toMid = (p2 - p1) / 4;

		// Search for a minimum straw from 1/4 to 3/4 between the two points
		for (int i = p1 + toMid; i < p2 - toMid; i++) {
			if (straws[i] < minValue) {
				minValue = straws[i];
				minIndex = i;
			}
		}

		return minIndex;
	}

	/**
	 * Distance squared algorithm. Euclidean distance squared, so that we do not
	 * have to take the square root.
	 * 
	 * @param p1
	 *            Point 1
	 * @param p2
	 *            Point 2
	 * @return Distance from point 1 to point 2 squared
	 */
	private double distanceSq(Point p1, Point p2) {
		double x2 = (p1.getX() - p2.getX()) * (p1.getX() - p2.getX());
		double y2 = (p1.getY() - p2.getY()) * (p1.getY() - p2.getY());

		return x2 + y2;
	}

	/**
	 * Path length of the stroke, using the fact that the points should be
	 * evenly spaced
	 * 
	 * @param i
	 *            Index of the point
	 * @return Path distance to the point at index i
	 */
	private double optimizedPathLength(int i) {
		return m_resampledSpacing * i;
	}

	/**
	 * Calculates the path lengths between the stroke
	 * 
	 * @param stroke
	 *            Stroke to calculate the path lengths for
	 * @return Approximate path lengths of the stroke
	 */
	private double[] optimizedPathLengths(Stroke stroke) {

		int numPoints = stroke.getNumPoints();

		double[] pathLengths = new double[numPoints];

		for (int i = 0; i < numPoints; i++) {
			pathLengths[i] = optimizedPathLength(i);
		}

		return pathLengths;
	}
}
