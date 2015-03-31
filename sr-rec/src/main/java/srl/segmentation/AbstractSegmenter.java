package srl.segmentation;

import java.util.ArrayList;
import java.util.List;

import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;


/**
 * An abstract segmenter containing some common, private functions used in many
 * segmentation techniques.
 * 
 * @author awolin
 */
public abstract class AbstractSegmenter {

	/**
	 * The shortest number of pixels allowed in a stroke
	 */
	protected static final double S_SHORT_STROKE_THRESHOLD = 20.0;

	/**
	 * Default constructor
	 */
	public AbstractSegmenter() {
		// Do nothing
	}

	/**
	 * Cleans a stroke by removing overlapping points. Also updates the time
	 * values to ensure that no two times are similar.
	 * 
	 * @param uncleanedStroke
	 *            Stroke to be cleaned
	 * @return A stroke with no overlapping points
	 */
	protected Stroke cleanStroke(Stroke uncleanedStroke) {

		List<Point> pts = new ArrayList<Point>(uncleanedStroke.getPoints());

		int i = 0;
		while (i + 1 < pts.size() - 1) {
			double x0 = pts.get(i).getX();
			double y0 = pts.get(i).getY();

			double x1 = pts.get(i + 1).getX();
			double y1 = pts.get(i + 1).getY();

			long t0 = pts.get(i).getTime();
			long t1 = pts.get(i + 1).getTime();

			// Remove overlapping points
			if (x0 == x1 && y0 == y1) {
				pts.remove(i + 1);
			}
			// Remove points that occur at the same time
			else if (t0 == t1) {
				pts.remove(i + 1);

				// Probably want to do some average based filtering later
				// pts.get(i+1).setTime(time);
			} else {
				i++;
			}
		}

		// Generate the new stroke
		Stroke newStroke = new Stroke(pts);

		return newStroke;
	}

	/**
	 * Calculate the path lengths at each point, mainly for use in the
	 * {@link #isLine(Stroke, int, int, double[])} test.
	 * 
	 * @param stroke
	 *            Stroke to calculate the path lengths for
	 * @return An array of doubles corresponding to points in the stroke, where
	 *         each double is the path length of the stroke up to the point in
	 *         that array's index
	 */
	protected double[] calcPathLengths(Stroke stroke) {

		double[] pathLengths = new double[stroke.getNumPoints()];
		pathLengths[0] = 0.0;

		for (int i = 1; i < stroke.getNumPoints(); i++) {
			pathLengths[i] = pathLengths[i - 1]
					+ stroke.getPoint(i - 1).distance(stroke.getPoint(i));
		}

		return pathLengths;
	}

	/**
	 * Check to see if the stroke segment between two points is a line
	 * 
	 * @param p1
	 *            Index for point 1
	 * @param p2
	 *            Index for point 2
	 * @param stroke
	 *            Stroke that contains the segment
	 * @param pathLengths
	 *            Path length array storing the total stroke path length at each
	 *            index of the point
	 * @param lineVsArcThreshold
	 *            The threshold for the straight-line distance / path distance
	 * @return True if the segment of the stroke is a line, false otherwise
	 */
	protected boolean isLine(int p1, int p2, Stroke stroke,
			double[] pathLengths, final double lineVsArcThreshold) {

		// Local thresholds that shouldn't need changing.
		// Correspond to the size of the segment
		final double sizeThreshold = 10;
		final double pointThreshold = 5;

		Point pt1 = stroke.getPoint(p1);
		Point pt2 = stroke.getPoint(p2);

		double straightLineDistance = pt1.distance(pt2);
		double pathDistance = pathLengths[p2] - pathLengths[p1];

		double lengthRatio = straightLineDistance / pathDistance;

		// Check whether the straight/path ratio is greater than a threshold
		if (lengthRatio > lineVsArcThreshold || pathDistance < sizeThreshold
				|| p2 - p1 < pointThreshold) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Segment a stroke based on the corner list (stroke indices) given
	 * 
	 * @param stroke
	 *            Stroke to segment
	 * @param corners
	 *            List of indices where the stroke should be split
	 * @param segmenterName
	 *            Name of the segmenter
	 * @param confidence
	 *            How confident the segmenter is
	 * @return The segmentation list for the stroke
	 */
	protected List<Segmentation> segmentStroke(Stroke stroke,
			List<Integer> corners, String segmenterName, double confidence) {

		Segmentation segmentation = new Segmentation();

		// Add the substrokes to the segmentation
		for (int i = 0; i < corners.size() - 1; i++) {

			Stroke substroke = new Stroke();
			substroke.setParent(stroke);

			for (int p = corners.get(i); p <= corners.get(i + 1); p++) {
				substroke.addPoint(stroke.getPoint(p));
			}

			if (substroke.getNumPoints() == 0)
				return null;

			segmentation.addSegmentedStroke(substroke);
		}

		segmentation.setSegmenterName(segmenterName);
		segmentation.confidence = confidence;

		// TODO - we do this through a command
		// stroke.addSegmentation(segmentation);

		// Return the segmentation (currently only support one)
		List<Segmentation> segmentations = new ArrayList<Segmentation>();
		segmentations.add(segmentation);

		return segmentations;
	}

	/**
	 * Map the corners found from an altered (resampled, cleaned, etc.) stroke
	 * to points in the original stroke
	 * 
	 * @param alteredCorners
	 *            Corners from the altered stroke
	 * @param alteredStroke
	 *            Altered stroke
	 * @param origStroke
	 *            Original stroke
	 * @return Corners that are mapped to the original stroke's points
	 */
	protected List<Integer> getOriginalStrokeCorners(
			List<Integer> alteredCorners, Stroke alteredStroke,
			Stroke origStroke) {

		// Initialize the original corners and add the start point
		List<Integer> originalCorners = new ArrayList<Integer>();
		originalCorners.add(0);

		// Get all of the middle corners
		for (int i = 1; i < alteredCorners.size() - 1; i++) {
			int alteredIndex = alteredCorners.get(i);
			int origIndex = findClosestOriginalCorner(alteredIndex,
					alteredStroke, origStroke);
			originalCorners.add(origIndex);
		}

		// Add the end point
		originalCorners.add(origStroke.getNumPoints() - 1);

		return originalCorners;
	}

	/**
	 * Get the corners (point indices) where the stroke should be split
	 * 
	 * @param segmentations
	 *            Segmentations from a segmenter
	 * @return Indices where corners were found (can contain duplicates)
	 */
	protected List<Integer> getCornersFromSegmentations(
			List<Segmentation> segmentations) {

		List<Integer> corners = new ArrayList<Integer>();
		if (segmentations == null)
			return corners;

		// Loop through each segmentation and get the corners
		for (Segmentation seg : segmentations) {

			int c = 0;
			corners.add(c);
			for (Stroke stroke : seg.getSegmentedStrokes()) {
				c += stroke.getNumPoints() - 1;
				corners.add(c);
			}
		}

		return corners;
	}

	/**
	 * Find the closest mapping from an altered (resampled, cleaned, etc.) point
	 * to the original point by performing a binary search across the time
	 * values of the points. The points closest in time are considered to be the
	 * most similar.
	 * 
	 * @param alteredIndex
	 *            Index of the altered point
	 * @param alteredStroke
	 *            Altered stroke
	 * @param origStroke
	 *            Original stroke
	 * @return Closest point index in the original stroke
	 */
	private int findClosestOriginalCorner(int alteredIndex,
			Stroke alteredStroke, Stroke origStroke) {

		// Get the time to search for
		long resampledTime = alteredStroke.getPoint(alteredIndex).getTime();

		// Find the index for the original point with the most similar time
		int closestPointIndex = closestTimeSearch(origStroke.getPoints(),
				resampledTime, 0, origStroke.getNumPoints() - 1);

		return closestPointIndex;
	}

	/**
	 * Find the point closest to the given time value using binary search
	 * 
	 * @param points
	 *            List of points to search in
	 * @param time
	 *            Time to search for
	 * @param low
	 *            Low index in the list of points
	 * @param high
	 *            High index in the list of points
	 * @return The index of the point that has the closest time value to the
	 *         given time
	 */
	private int closestTimeSearch(List<Point> points, long time, int low,
			int high) {

		// Calculate the mid point and find the time at this point
		int mid = (low + high) / 2;
		long midTime = points.get(mid).getTime();

		// Base case, if we are close to the desired time but the actual time is
		// slightly off
		if (low == high || high - low == 1) {
			return mid;
		}
		// Default binary search
		else {
			if (midTime < time) {
				return closestTimeSearch(points, time, mid, high);
			} else if (midTime > time) {
				return closestTimeSearch(points, time, low, mid);
			} else {
				return mid;
			}
		}
	}
}
