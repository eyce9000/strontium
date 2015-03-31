/**
 * StrokeFeatures.java
 * 
 * Revision History:<br>
 * Jun 23, 2008 bpaulson - File created
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
import org.openawt.geom.Rectangle2D;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.math.Filtering;
import srl.math.LeastSquares;
import srl.segmentation.paleo.RevolutionSegmenter;
import Jama.Matrix;

/**
 * Miscellaneous calculated features of a stroke
 * 
 * @author bpaulson
 */
public class StrokeFeatures{

	/**
	 * Stroke that these features are calculated from; this should NOT be
	 * modified in this code. It used simply as a reference.
	 */
	protected Stroke m_stroke;

	/**
	 * Points of the stroke (these may be modified to remove duplicates); for
	 * list of original points, use m_stroke.getPoints();
	 */
	protected List<Point> m_points;

	/**
	 * Boolean specifying whether or not the stroke is a single point
	 */
	protected boolean m_isPoint;

	/**
	 * Direction values over time (first derivative)
	 */
	protected double[] m_dir;

	/**
	 * Direction values over time w/o shifting
	 */
	protected double[] m_dirNoShift;

	/**
	 * Maximum direction value
	 */
	protected double m_maxDir;

	/**
	 * Minimum direction value
	 */
	protected double m_minDir;

	/**
	 * Absolute curvature values over time (second derivative)
	 */
	protected double[] m_curvature;

	/**
	 * Curvature values (no absolute value) over time
	 */
	protected double[] m_curvNoAbs;

	/**
	 * Running total of the sum of the curvature values over time
	 */
	protected double[] m_totalCurvature;

	/**
	 * Maximum curvature value for the stroke
	 */
	protected double m_max_curv;

	/**
	 * Index of the point with the maximum curvature value
	 */
	protected int m_max_curv_index;

	/**
	 * Running total of stroke length over time
	 */
	protected double[] m_lengthSoFar;

	/**
	 * Running total of stroke length over time but from the second derivatives
	 * viewpoint
	 */
	protected double[] m_2lengthSoFar;

	/**
	 * List of consecutive lengths of individual line segments
	 */
	protected double[] m_segLength;

	/**
	 * Total length of the stroke (Euclidean distance)
	 */
	protected double m_strokelength;

	/**
	 * Bounding box of the stroke
	 */
	protected BoundingBox m_bounds;

	/**
	 * Normalized distance between direction extremes
	 */
	protected double m_NDDE;

	/**
	 * Direction change ratio
	 */
	protected double m_DCR;

	/**
	 * Total rotation of the stroke
	 */
	protected double m_totalRotation;

	/**
	 * Number of 2PI revolutions that the stroke makes
	 */
	protected double m_numRevolutions;

	/**
	 * Flag denoting whether or not a stroke is overtraced (makes multiple
	 * revolutions)
	 */
	protected boolean m_overtraced;

	/**
	 * Flag denoting whether or not a stroke is close to being complete
	 * (closed); i.e. end points must be near
	 */
	protected boolean m_complete;

	/**
	 * Ratio of the distance (Euclidean) between the end points and the total
	 * stroke length
	 */
	protected double m_endPtStrokeLengthRatio;

	/**
	 * Slope of the best fit line of the direction graph
	 */
	protected double m_slopeDirGraph;

	/**
	 * Best fit line of the direction graph
	 */
	protected Line2D m_bestFitDirGraph;

	/**
	 * Best fit line of the stroke itself
	 */
	protected Line2D m_bestFitLine;

	/**
	 * Flag specifying whether the direction window test passed (see
	 * calcDirWindowPassed() for information about this test)
	 */
	protected boolean m_dirWindowPassed;

	/**
	 * Percentage of the direction window test passed
	 */
	protected double m_pctDirWindowPassed;

	/**
	 * Segmentation which breaks the stroke up at every 2pi interval in the
	 * direction graph
	 */
	protected RevolutionSegmenter m_rev_segments;

	/**
	 * Perform smoothing of the direction graph?
	 */
	protected boolean m_smoothing;

	/**
	 * Ratio of maximum curvature value to average curvature value
	 */
	protected double m_maxCurvToAvgCurvRatio;

	/**
	 * Average curvature of entire stroke
	 */
	protected double m_avgCurvature;

	/**
	 * Major axis of the ellipse fit
	 */
	protected Line2D m_majorAxis;

	/**
	 * Length of the major axis
	 */
	protected double m_majorAxisLength;

	/**
	 * Angle of the major axis (relative to [0,0])
	 */
	protected double m_majorAxisAngle;

	/**
	 * Error of the best fit line to the direction graph
	 */
	protected double m_bestFitDirGraphError;

	/**
	 * Distance between furthest corner and the actual stroke
	 */
	protected double m_cornerStrokeDistance;

	/**
	 * Avg distance between closest point to each corner of the bounding box
	 */
	protected double m_avgCornerStrokeDistance;

	/**
	 * Distance between closest corner and the actual stroke
	 */
	protected double m_minCornerStrokeDistance;

	/**
	 * Std dev between corners and closest points
	 */
	protected double m_stdDevCornerStrokeDistance;

	/**
	 * Ratio between perimeter and stroke length
	 */
	protected double m_perimStrokeLengthRatio;

	/**
	 * Left most point of stroke
	 */
	protected Point m_leftMostPoint;

	/**
	 * Right most point of stroke
	 */
	protected Point m_rightMostPoint;

	/**
	 * Bottom most point of stroke
	 */
	protected Point m_bottomMostPoint;

	/**
	 * Top most point of stroke
	 */
	protected Point m_topMostPoint;

	private double m_maxDC;

	private double m_avgDC;

	/**
	 * Constructor - takes a stroke and will compute miscellaneous features
	 * 
	 * @param stroke
	 *            stroke to compute features for
	 * @param smoothDirGraph
	 *            flag specifying whether or not direction graph smoothing
	 *            (through median filtering) should take place
	 */
	public StrokeFeatures(Stroke stroke, boolean smoothDirGraph) {
		m_stroke = stroke;
		m_smoothing = smoothDirGraph;

		// copy stroke points
		m_points = new ArrayList<Point>();
		for (int i = 0; i < m_stroke.getNumPoints(); i++)
			m_points.add((Point) m_stroke.getPoints().get(i).clone());

		// check for single point instance
		if (m_points.size() <= 1) {
			m_isPoint = true;
		}

		// remove points with consecutive, duplicate x/y values or time values;
		// this is needed in order to avoid divide by zero when calculating
		// derivatives
		removeDuplicates();

		// compute initial derivative values
		computeValues();

		// remove tails/hooks
		removeHooks();

		// compute new derivative values after tails are removed
		computeValues();

		// compute miscellaneous features that a are specific to PaleoSketch
		computePaleoFeatures();

		/*
		 * System.out.println(""); for (int i = 0; i < m_dir.length; i++)
		 * System.out.print(m_dir[i] + " "); System.out.println("");
		 * 
		 * Plot plot = new Plot("Direction"); System.out.println("m_len" +
		 * m_lengthSoFar.length + " m_dir " + m_dir.length);
		 * plot.addLine(m_lengthSoFar, m_dir, Color.blue, 10); plot.plot();
		 */

	}

	/**
	 * Get the points of the stroke
	 * 
	 * @return points of the stroke
	 */
	public List<Point> getPoints() {
		return m_points;
	}

	/**
	 * Gets the first point in the original stroke
	 * 
	 * @return first point in the original stroke
	 */
	public Point getFirstOrigPoint() {
		return m_stroke.getPoints().get(0);
	}

	/**
	 * Gets the last point in the original stroke
	 * 
	 * @return last point in the original stroke
	 */
	public Point getLastOrigPoint() {
		return m_stroke.getPoints().get(m_stroke.getNumPoints() - 1);
	}

	/**
	 * Get the number of original stroke points
	 * 
	 * @return number of original stroke points
	 */
	public int getNumOrigPoints() {
		return m_stroke.getNumPoints();
	}

	/**
	 * Get a list of the original stroke points (before stroke was cleaned)
	 * 
	 * @return list of original stroke points
	 */
	public List<Point> getOrigPoints() {
		return m_stroke.getPoints();
	}

	/**
	 * Get the original stroke object
	 * 
	 * @return original stroke object
	 */
	public Stroke getOrigStroke() {
		return m_stroke;
	}

	/**
	 * Get the number of stroke points (after duplicates have been removed)
	 * 
	 * @return number of stroke points (after duplicates have been removed)
	 */
	public int getNumPoints() {
		return m_points.size();
	}

	/**
	 * Get the total length of the stroke
	 * 
	 * @return total length of the stroke
	 */
	public double getStrokeLength() {
		return m_strokelength;
	}

	/**
	 * Get the normalized distance between direction extremes
	 * 
	 * @return NDDE value
	 */
	public double getNDDE() {
		return m_NDDE;
	}

	/**
	 * Get the direction change ratio
	 * 
	 * @return DCR value
	 */
	public double getDCR() {
		return m_DCR;
	}
	
	/**
	 * Get the maximum direction change between each sequential point
	 * 
	 * @return Maximum direction change
	 */
	public double getMaxDirChange(){
		return m_maxDC;
	}

	/**
	 * Get the average direction change between each sequential point
	 * 
	 * @return Average direction change
	 */
	public double getAvgDirChange(){
		return m_avgDC;
	}
	
	
	
	/**
	 * Flag stating whether or not the stroke is overtraced (makes multiple
	 * revolutions)
	 * 
	 * @return true if overtraced; else false
	 */
	public boolean isOvertraced() {
		return m_overtraced;
	}

	/**
	 * Flag stating whether or not a stroke is closed/complete
	 * 
	 * @return true if stroke is near being closed; else false
	 */
	public boolean isClosed() {
		return m_complete;
	}

	/**
	 * Get the best fit line for this stroke
	 * 
	 * @return best fit line for this stroke
	 */
	public Line2D getBestFitLine() {
		return m_bestFitLine;
	}

	/**
	 * Get the bounding box of the stroke
	 * 
	 * @return bounding box of the stroke
	 */
	public BoundingBox getBounds() {
		return m_bounds;
	}

	/**
	 * Get the number of 2PI (360 degree) revolutions that the stroke makes
	 * 
	 * @return number of revolutions that the stroke makes
	 */
	public double numRevolutions() {
		return m_numRevolutions;
	}

	/**
	 * Get the maximum curvature value for the stroke
	 * 
	 * @return maximum curvature value for the stroke
	 */
	public double getMaxCurv() {
		return m_max_curv;
	}

	/**
	 * Flag denoting whether or not moving direction window test passed
	 * 
	 * @return true if test passed; else false
	 */
	public boolean dirWindowPassed() {
		return m_dirWindowPassed;
	}

	/**
	 * Get the percentage of the direction window passing
	 * 
	 * @return direction window test pass percent
	 */
	public double getPctDirWindowPassed() {
		return m_pctDirWindowPassed;
	}

	/**
	 * Flag denoting whether the stroke is drawn in a clockwise or
	 * counter-clockwise manner
	 * 
	 * @return true if clockwise; false if counter-clockwise
	 */
	public boolean isClockwise() {
		if (m_slopeDirGraph >= 0)
			return true;
		else
			return false;
	}

	/**
	 * Slope of the direction graph
	 * 
	 * @return slope of the direction graph
	 */
	public double getSlopeDirGraph() {
		return m_slopeDirGraph;
	}

	/**
	 * Get the direction values/graph for the stroke
	 * 
	 * @return array of consecutive directional values
	 */
	public double[] getDir() {
		return m_dir;
	}

	/**
	 * Get the unshifted direction values/graph for the stroke
	 * 
	 * @return array of consecutive directional values
	 */
	public double[] getDirNoShift() {
		return m_dirNoShift;
	}

	/**
	 * Print points out to System.out
	 */
	public void printPoints() {
		for (int i = 0; i < m_points.size(); i++)
			System.out.println(m_points.get(i).getX() + ","
					+ m_points.get(i).getY());
	}

	/**
	 * Flag denoting whether or not smoothing is on or off
	 * 
	 * @return true if on, else false
	 */
	public boolean isSmoothingOn() {
		return m_smoothing;
	}

	/**
	 * Get the error of the best fit line of the direction graph
	 * 
	 * @return the error of the best fit line of the direction graph
	 */
	public double getBestFitDirGraphError() {
		return m_bestFitDirGraphError;
	}

	/**
	 * Method used to find the point (or points) where the given line intersects
	 * the stroke
	 * 
	 * @param line
	 *            line to find the intersection with
	 * @return intersection points
	 */
	public ArrayList<Point2D> getIntersection(Line2D.Double line) {
		ArrayList<Point2D> intersectionPts = new ArrayList<Point2D>();
		Point2D intersect = null;
		for (int i = 0; i < getNumPoints() - 1; i++) {
			Point p1 = m_points.get(i);
			Point p2 = m_points.get(i + 1);
			if (line.intersectsLine(p1.getX(), p1.getY(), p2.getX(), p2.getY())) {
				intersect = getIntersectionPt(line, new Line2D.Double(
						p1.getX(), p1.getY(), p2.getX(), p2.getY()));
				intersectionPts.add(intersect);
			}
		}
		if (intersectionPts.size() < 2) {
			Point p1 = m_points.get(0);
			Point p2 = m_points.get(getNumPoints() - 1);
			if (line.intersectsLine(p1.getX(), p1.getY(), p2.getX(), p2.getY())) {
				intersect = getIntersectionPt(line, new Line2D.Double(
						p1.getX(), p1.getY(), p2.getX(), p2.getY()));
				intersectionPts.add(intersect);
			}
		}
		return intersectionPts;
	}

	/**
	 * Method used to find the intersection point between two lines
	 * 
	 * @param l1
	 *            line 1
	 * @param l2
	 *            line 2
	 * @return intersection point between line1 and line2
	 */
	public static Point2D getIntersectionPt(Line2D.Double l1, Line2D.Double l2) {
		Point2D.Double intersect = null;
		double l1slope = (l1.y2 - l1.y1) / (l1.x2 - l1.x1);
		double l2slope = (l2.y2 - l2.y1) / (l2.x2 - l2.x1);
		if (l1slope == l2slope)
			return null;
		double l1intercept = (-l1.x1 * l1slope) + l1.y1;
		double l2intercept = (-l2.x1 * l2slope) + l2.y1;
		if ((l1.x2 - l1.x1) == 0) {
			double x = l1.x2;
			double y = x * l2slope + l2intercept;
			return new Point2D.Double(x, y);
		}
		if ((l2.x2 - l2.x1) == 0) {
			double x = l2.x2;
			double y = x * l1slope + l1intercept;
			return new Point2D.Double(x, y);
		}
		Matrix a = new Matrix(2, 2);
		Matrix b = new Matrix(2, 1);
		a.set(0, 0, -l1slope);
		a.set(0, 1, 1);
		a.set(1, 0, -l2slope);
		a.set(1, 1, 1);
		b.set(0, 0, l1intercept);
		b.set(1, 0, l2intercept);
		Matrix result = a.solve(b);
		intersect = new Point2D.Double(result.get(0, 0), result.get(1, 0));
		return intersect;
	}

	/**
	 * Get the x values in array form from the stroke
	 * 
	 * @param stroke
	 *            stroke to get the x values from
	 * @return array of x values for the stroke
	 */
	protected double[] getXVals(Stroke stroke) {
		double[] x_vals = new double[stroke.getNumPoints()];
		for (int i = 0; i < stroke.getNumPoints(); i++)
			x_vals[i] = stroke.getPoints().get(i).getX();
		return x_vals;
	}

	/**
	 * Get the y values in array form from the stroke
	 * 
	 * @param stroke
	 *            stroke to get the y values from
	 * @return array of y values for the stroke
	 */
	protected double[] getYVals(Stroke stroke) {
		double[] y_vals = new double[stroke.getNumPoints()];
		for (int i = 0; i < stroke.getNumPoints(); i++)
			y_vals[i] = stroke.getPoints().get(i).getY();
		return y_vals;
	}

	/**
	 * Removes immediately subsequent points in the same location. (i.e., if the
	 * mouse hasn't moved, it doesn't create a new point) If two points have the
	 * same time stamp, the time on the first is changed to be interpolated
	 * between the two surrounding points.
	 */
	protected void removeDuplicates() {
		List<Point> newPoints = new ArrayList<Point>();
		newPoints.add(m_points.get(0));

		// add non-duplicates to new point list
		for (int i = 1; i < getNumPoints(); i++) {

			// same x and y value so the point is not added to the new list
			if (m_points.get(i - 1).getX() == m_points.get(i).getX()
					&& m_points.get(i - 1).getY() == m_points.get(i).getY()) {
				// do nothing
			} else {
				// add point to new list
				newPoints.add(m_points.get(i));

				// check for same time value
				if (newPoints.size() > 1
						&& newPoints.get(newPoints.size() - 1).getTime() == newPoints
								.get(newPoints.size() - 2).getTime()) {
					if (newPoints.size() == 2) {
						newPoints.set(newPoints.size() - 1, new Point(newPoints
								.get(newPoints.size() - 1).getX(), newPoints
								.get(newPoints.size() - 1).getY(), newPoints
								.get(newPoints.size() - 1).getTime() + 1));
					} else {
						newPoints.set(newPoints.size() - 2, new Point(newPoints
								.get(newPoints.size() - 2).getX(), newPoints
								.get(newPoints.size() - 2).getY(), newPoints
								.get(newPoints.size() - 3).getTime()
								+ newPoints.get(newPoints.size() - 1).getTime()
								/ 2));
					}
				}
			}
		}
		m_points = newPoints;
	}

	/**
	 * Computes various derivatives
	 */
	protected void computeValues() {
		m_dir = new double[getNumPoints() - 1];
		m_dirNoShift = new double[getNumPoints() - 1];
		m_segLength = new double[getNumPoints() - 1];
		m_lengthSoFar = new double[getNumPoints() - 1];
		if (getNumPoints() > 1) {
			m_2lengthSoFar = new double[getNumPoints() - 2];
			m_curvature = new double[getNumPoints() - 2];
			m_curvNoAbs = new double[getNumPoints() - 2];
			m_totalCurvature = new double[getNumPoints() - 2];
		} else {
			m_2lengthSoFar = new double[getNumPoints() - 1];
			m_curvature = new double[getNumPoints() - 1];
			m_curvNoAbs = new double[getNumPoints() - 1];
			m_totalCurvature = new double[getNumPoints() - 1];
		}
		m_max_curv = 0.0;
		m_max_curv_index = 0;

		// compute direction graph
		for (int i = 0; i < getNumPoints() - 1; i++) {
			m_dir[i] = Math.atan2(m_points.get(i + 1).getY()
					- m_points.get(i).getY(), m_points.get(i + 1).getX()
					- m_points.get(i).getX());
			m_dirNoShift[i] = m_dir[i];
			while ((i > 0) && (m_dir[i] - m_dir[i - 1] > Math.PI))
				m_dir[i] = m_dir[i] - 2 * Math.PI;
			while ((i > 0) && (m_dir[i - 1] - m_dir[i] > Math.PI))
				m_dir[i] = m_dir[i] + 2 * Math.PI;
			m_segLength[i] = Math.sqrt((m_points.get(i + 1).getY() - m_points
					.get(i).getY())
					* (m_points.get(i + 1).getY() - m_points.get(i).getY())
					+ (m_points.get(i + 1).getX() - m_points.get(i).getX())
					* (m_points.get(i + 1).getX() - m_points.get(i).getX()));
			if (i == 0)
				m_lengthSoFar[i] = m_segLength[i];
			else
				m_lengthSoFar[i] = m_lengthSoFar[i - 1] + m_segLength[i];
		}
		if (m_lengthSoFar.length > 0)
			m_strokelength = m_lengthSoFar[m_lengthSoFar.length - 1];
		else
			m_strokelength = 1.0;

		// perform smoothing if desired
		if (m_smoothing) {
			m_dir = Filtering.medianFilter(m_dir);
			m_dirNoShift = Filtering.medianFilter(m_dirNoShift);
		}

		// compute curvature graph
		for (int i = 0; i < getNumPoints() - 2; i++) {
			m_2lengthSoFar[i] = m_lengthSoFar[i + 1];
			m_curvature[i] = Math.abs(m_dir[i + 1] - m_dir[i])
					/ (m_segLength[i] + m_segLength[i + 1]);
			m_curvNoAbs[i] = (m_dir[i + 1] - m_dir[i])
					/ (m_segLength[i] + m_segLength[i + 1]);
			if (Math.abs(m_curvature[i]) > m_max_curv && i != 0) {
				m_max_curv = Math.abs(m_curvature[i]);
				m_max_curv_index = i;
			}
			if (i == 0)
				m_totalCurvature[i] = m_curvature[i];
			else
				m_totalCurvature[i] = m_totalCurvature[i - 1] + m_curvature[i];
		}
		if (m_totalCurvature.length > 0) {
			m_avgCurvature = m_totalCurvature[m_totalCurvature.length - 1]
					/ m_totalCurvature.length;
			m_maxCurvToAvgCurvRatio = m_max_curv / m_avgCurvature;
		}
	}

	/**
	 * Tail/hook removal code
	 */
	protected void removeHooks() {
		// conditions for not removing tails (basically if stroke is too small)
		if (getNumPoints() < Thresholds.active.M_HOOK_MINPOINTS || m_totalCurvature.length == 0
				|| m_lengthSoFar[getNumPoints() - 2] < Thresholds.active.M_HOOK_MINSTROKELENGTH
				|| m_strokelength < Thresholds.active.M_HOOK_MINSTROKELENGTH)
			return;

		double hookcurvature = 0;
		int startindex = 0;

		// check for hooks at the beginning of the stroke
		for (int i = 1; i < getNumPoints() - 1; i++) {

			// only check for tails near endpoints; if we have gone too far into
			// the stroke then we are no longer checking for hooks
			if (m_lengthSoFar[i] > Thresholds.active.M_HOOK_MAXHOOKLENGTH
					|| m_lengthSoFar[i] / m_strokelength > Thresholds.active.M_HOOK_MAXHOOKPERCENT)
				break;

			// finding the maximum curvature value at the beginning of the
			// stroke
			if (Math.abs(m_totalCurvature[i]) > hookcurvature) {
				hookcurvature = Math.abs(m_totalCurvature[i]);
				startindex = i + 1;
			}
		}

		// max curvature near start point is too small to denote a tail
		if (hookcurvature < Thresholds.active.M_HOOK_MINHOOKCURVATURE)
			startindex = 0;

		// check for hooks at the end of the stroke
		hookcurvature = 0;
		int endindex = getNumPoints();
		for (int i = 1; i < getNumPoints() - 1; i++) {

			int startIndex = m_totalCurvature.length - 1 - i;
			if (startIndex < 0)
				startIndex = 0;

			double c = m_totalCurvature[m_totalCurvature.length - 1]
					- m_totalCurvature[startIndex];
			double l = m_lengthSoFar[m_lengthSoFar.length - 1]
					- m_lengthSoFar[m_lengthSoFar.length - 1 - i];

			// we have gone too far into the stroke so we stop
			if (l > Thresholds.active.M_HOOK_MAXHOOKLENGTH
					|| l / m_strokelength > Thresholds.active.M_HOOK_MAXHOOKPERCENT)
				break;

			// finding max curvature value near end of the stroke
			if (Math.abs(c) > hookcurvature) {
				hookcurvature = Math.abs(c);
				endindex = getNumPoints() - i;
			}
		}

		// max curvature near end point is too small to denote a tail
		if (Math.abs(hookcurvature) < Thresholds.active.M_HOOK_MINHOOKCURVATURE)
			endindex = getNumPoints();

		// update size, x, y, and time values with tails removed
		List<Point> newPoints = new ArrayList<Point>();
		for (int i = startindex; i < endindex; i++)
			newPoints.add(m_points.get(i));
		m_points = newPoints;
	}

	/**
	 * Computes miscellaneous PaleoSketch features
	 */
	protected void computePaleoFeatures() {
		calcBounds();
		calcTotalRotation();
		calcNDDE();
		calcDCR();
		calcBestFitLineForDirGraph();
		calcBestFitLine();
		calcDirWindowPassed();
		calcMajorAxis();
		calcDistanceBetweenFarthestCornerAndStroke();
	}

	/**
	 * Calculates the bounding box of the stroke
	 */
	protected void calcBounds() {
		if (m_points.size() == 0)
			return;
		double maxX = m_points.get(0).getX();
		double minX = m_points.get(0).getX();
		double maxY = m_points.get(0).getY();
		double minY = m_points.get(0).getY();
		m_leftMostPoint = m_points.get(0);
		m_rightMostPoint = m_points.get(0);
		m_bottomMostPoint = m_points.get(0);
		m_topMostPoint = m_points.get(0);
		for (int i = 1; i < m_points.size(); i++) {
			if (m_points.get(i).getX() > maxX) {
				maxX = m_points.get(i).getX();
				m_rightMostPoint = m_points.get(i);
			}
			if (m_points.get(i).getX() < minX) {
				minX = m_points.get(i).getX();
				m_leftMostPoint = m_points.get(i);
			}
			if (m_points.get(i).getY() > maxY) {
				maxY = m_points.get(i).getY();
				m_topMostPoint = m_points.get(i);
			}
			if (m_points.get(i).getY() < minY) {
				minY = m_points.get(i).getY();
				m_bottomMostPoint = m_points.get(i);
			}
		}
		m_bounds = new BoundingBox(minX, minY, maxX, maxY);
		m_perimStrokeLengthRatio = m_strokelength / m_bounds.getPerimeter();
	}

	/**
	 * Calculate total rotation and other rotation-related features (
	 */
	protected void calcTotalRotation() {
		double sum = 0;
		double deltaX, deltaY, deltaX1, deltaY1;
		for (int i = 1; i < m_points.size() - 1; i++) {
			deltaX = m_points.get(i + 1).getX() - m_points.get(i).getX();
			deltaY = m_points.get(i + 1).getY() - m_points.get(i).getY();
			deltaX1 = m_points.get(i).getX() - m_points.get(i - 1).getX();
			deltaY1 = m_points.get(i).getY() - m_points.get(i - 1).getY();

			// check for divide by zero; add or subtract PI/2 accordingly (this
			// is the limit of atan as it approaches infinity)
			if (deltaX * deltaX1 + deltaY * deltaY1 == 0) {
				if (deltaX * deltaY1 - deltaX1 * deltaY < 0) {
					sum += Math.PI / -2.0;
				} else if (deltaX * deltaY1 - deltaX1 * deltaY > 0) {
					sum += Math.PI / 2.0;
				}
			}

			// otherwise sum the rotation
			else
				sum += Math.atan2((deltaX * deltaY1 - deltaX1 * deltaY),
						(deltaX * deltaX1 + deltaY * deltaY1));
		}
		m_totalRotation = sum;

		// num revolutions = total rotation divided by 2PI
		m_numRevolutions = Math.abs(m_totalRotation) / (Math.PI * 2.0);

		// this just accounts for numerical instabilities
		if (m_numRevolutions < .0000001)
			m_numRevolutions = 0.0;

		// overtraced check
		if (m_numRevolutions >= Thresholds.active.M_REVS_TO_BE_OVERTRACED)
			m_overtraced = true;
		else
			m_overtraced = false;

		// compute distance between end points divided by total stroke length
		m_endPtStrokeLengthRatio = getFirstOrigPoint().distance(
				getLastOrigPoint())
				/ m_strokelength;

		// closed shape test
		if (m_endPtStrokeLengthRatio <= Thresholds.active.M_PERCENT_DISTANCE_TO_BE_COMPLETE
				&& m_numRevolutions >= Thresholds.active.M_NUM_REVS_TO_BE_COMPLETE)
			m_complete = true;
		else
			m_complete = false;
	}

	/**
	 * Calculate the normalized distance between direction extremes (NDDE)
	 */
	protected void calcNDDE() {
		int maxIndex = 0, minIndex = 0;
		if (m_dir.length == 0)
			return;
		m_maxDir = m_dir[0];
		m_minDir = m_dir[0];

		// find minimum and maximum direction values
		for (int i = 1; i < m_dir.length; i++) {
			if (m_dir[i] > m_maxDir) {
				m_maxDir = m_dir[i];
				maxIndex = i;
			}
			if (m_dir[i] < m_minDir) {
				m_minDir = m_dir[i];
				minIndex = i;
			}
		}

		// NDDE = difference in stroke lengths at the indices of max and min
		// direction value divided by total stroke length
		if (m_lengthSoFar[maxIndex] > m_lengthSoFar[minIndex])
			m_NDDE = (m_lengthSoFar[maxIndex] - m_lengthSoFar[minIndex])
					/ m_strokelength;
		else
			m_NDDE = (m_lengthSoFar[minIndex] - m_lengthSoFar[maxIndex])
					/ m_strokelength;
	}

	/**
	 * Compute the direction change ratio (max direction change divided by
	 * average direction change)
	 */
	protected void calcDCR() {
		m_maxDC = Double.MIN_VALUE;
		double sum = 0.0;

		// ignore 5% at ends (to avoid tails)
		int start = (int) (m_dir.length * .05);
		int end = m_dir.length - start;
		int i;

		// compute average change in direction and find max change in direction
		for (i = start; i < end - 1; i++) {
			double dc = Math.abs(m_dir[i] - m_dir[i + 1]);
			if (dc >= m_maxDC)
				m_maxDC = dc;
			sum += dc;
		}
		m_avgDC = sum / i;

		// DCR = max change in direction / avg change in direction
		if (m_avgDC == 0.0 || Double.isNaN(m_avgDC))
			m_DCR = 0.0;
		else
			m_DCR = m_maxDC / m_avgDC;
	}

	/**
	 * Compute the line that best fits the direction graph
	 */
	protected void calcBestFitLineForDirGraph() {
		double sx = 0, sx2 = 0, sy = 0, sxy = 0;
		double[] x = new double[m_dir.length];

		// calculate sum of the x values (in this case the indexes), y values
		// (in this case the direction values), x^2 values, and x*y values
		// (those needed to compute least squares line)
		for (int i = 0; i < m_dir.length; i++) {
			sx += i;
			sx2 += (i * i);
			sy += m_dir[i];
			sxy += (i * m_dir[i]);
			x[i] = i;
		}

		// calculate bounds of the direction graph
		Rectangle2D bounds = new Rectangle2D.Double(0, m_minDir, m_dir.length,
				m_maxDir);
		try {
			// calculate best fit line of the direction graph and the slope of
			// that line
			m_bestFitDirGraph = LeastSquares.bestFitLine(sx, sx2, sy, sxy,
					m_dir.length, bounds);
			m_slopeDirGraph = (m_bestFitDirGraph.getY2() - m_bestFitDirGraph
					.getY1())
					/ (m_bestFitDirGraph.getX2() - m_bestFitDirGraph.getX1());
			m_bestFitDirGraphError = LeastSquares.error(x, getDir(),
					m_bestFitDirGraph) / m_dir.length;
		} catch (Exception e) {
		}
	}

	public static double mean(double[] arr) {
		double total = 0.0;
		for (double d : arr)
			total += d;
		return total / arr.length;
	}

	public static double stdev(double[] arr) {
		return stdev(arr, mean(arr));
	}

	public static double stdev(double[] arr, double mean) {
		double total = 0.0;
		for (double d : arr) {
			double temp = d - mean;
			total += temp * temp;
		}
		return Math.sqrt(total / arr.length);
	}

	/**
	 * Compute the best fit line of the stroke itself
	 */
	protected void calcBestFitLine() {
		double sx = 0, sx2 = 0, sy = 0, sy2 = 0, sxy = 0;

		// calculate sum of the x values, y values, x^2 values, y^2 values and
		// x*y values (those needed to compute least squares line)
		for (int i = 0; i < m_points.size(); i++) {
			sx += m_points.get(i).getX();
			sx2 += Math.pow(m_points.get(i).getX(), 2);
			sy += m_points.get(i).getY();
			sy2 += Math.pow(m_points.get(i).getY(), 2);
			sxy += m_points.get(i).getX() * m_points.get(i).getY();
		}
		Line2D l1 = new Line2D.Double();
		Line2D l2 = new Line2D.Double();
		double err1 = Double.MAX_VALUE;
		double err2 = Double.MAX_VALUE;
		try {
			// compute least squares line and error in the x direction
			l1 = LeastSquares.bestFitLine(sx, sx2, sy, sxy, m_points.size(),
					m_bounds);
			err1 = LeastSquares.error(m_points, l1);
		} catch (Exception e) {
		}
		try {
			// compute least squares line and error in the y direction
			l2 = LeastSquares.bestFitLine(sy, sy2, sx, sxy, m_points.size(),
					m_bounds);
			err2 = LeastSquares.error(m_points, l2);
		} catch (Exception e) {
		}

		// choose the line (either in x or y direction) that had the least error
		if (err1 < err2)
			m_bestFitLine = l1;
		else
			m_bestFitLine = l2;
	}

	/**
	 * Test used to determine if the direction graph is continuously increasing
	 * or decreasing. The idea is to split the graph into windows and see if
	 * each window get consecutively smaller or larger over time.
	 */
	protected void calcDirWindowPassed() {

		// each window contains 5 direction values
		double windowSize = Thresholds.active.M_DIR_WINDOW_SIZE;
		boolean result = true;
		m_pctDirWindowPassed = 1.0;
		ArrayList<Double> dirWindows = new ArrayList<Double>();

		// ignore 6% at ends (to avoid tails)
		int start = (int) (m_dir.length * .06);
		int end = m_dir.length - start;
		int j = 0;
		double mean = 0;

		// cluster direction graph into windows
		for (int i = start; i < end; i++) {
			if (j >= windowSize) {
				dirWindows.add(mean / windowSize);
				j = 0;
				mean = 0;
			} else {
				mean += m_dir[i];
				j++;
			}
		}

		// see if average values in windows are consecutively increasing or
		// decreasing
		if (dirWindows.size() > 3) {
			boolean increasing = true;
			if (dirWindows.get(1) < dirWindows.get(0))
				increasing = false;
			double numPassed = 0.0;
			double numChecked = 0.0;
			for (int i = 0; i < dirWindows.size() - 1; i++) {
				if (increasing && dirWindows.get(i + 1) < dirWindows.get(i)
						|| !increasing
						&& dirWindows.get(i + 1) > dirWindows.get(i)) {
					result = false;
				} else {
					numPassed++;
				}
				numChecked++;
			}
			m_pctDirWindowPassed = numPassed / numChecked;
		}
		m_dirWindowPassed = result;
	}

	/**
	 * Get the running total array of stroke length
	 * 
	 * @return array containing a running total of stroke length
	 */
	public double[] getLengthSoFar() {
		return m_lengthSoFar;
	}

	/**
	 * Get the number of revolutions (based on direction graph) that the stroke
	 * makes
	 * 
	 * @return number of revolutions
	 */
	public double getNumRevolutions() {
		return m_numRevolutions;
	}

	/**
	 * Static method used to compute the ratio of the distance between the end
	 * points and the stroke length
	 * 
	 * @param stroke
	 *            stroke to compute ratio for
	 * @return ratio
	 */
	public static double getEndptStrokeLengthRatio(Stroke stroke) {
		double strokelength = 0;
		for (int i = 1; i < stroke.getNumPoints(); i++)
			strokelength += stroke.getPoints().get(i - 1)
					.distance(stroke.getPoints().get(i));
		return (stroke.getPoints().get(0).distance(stroke.getPoints().get(
				stroke.getNumPoints() - 1)))
				/ strokelength;
	}

	/**
	 * Static method used to compute the stroke length of a given stroke
	 * 
	 * @param stroke
	 *            stroke to compute length for
	 * @return length of stroke
	 */
	public static double getStrokeLength(Stroke stroke) {
		double strokelength = 0;
		for (int i = 1; i < stroke.getNumPoints(); i++)
			strokelength += stroke.getPoints().get(i - 1)
					.distance(stroke.getPoints().get(i));
		return strokelength;
	}

	/**
	 * Method used to find the point (or points) where the given line intersects
	 * the stroke
	 * 
	 * @param stroke
	 *            stroke to find intersection for
	 * @param line
	 *            line to find the intersection with
	 * @return intersection points
	 */
	public static ArrayList<Point2D> getIntersection(Stroke stroke,
			Line2D.Double line) {
		ArrayList<Point2D> intersectionPts = new ArrayList<Point2D>();
		Point2D intersect = null;
		for (int i = 0; i < stroke.getNumPoints() - 1; i++) {
			Point p1 = stroke.getPoints().get(i);
			Point p2 = stroke.getPoints().get(i + 1);
			if (line.intersectsLine(p1.getX(), p1.getY(), p2.getX(), p2.getY())) {
				intersect = getIntersectionPt(line, new Line2D.Double(
						p1.getX(), p1.getY(), p2.getX(), p2.getY()));
				intersectionPts.add(intersect);
			}
		}
		if (intersectionPts.size() < 2) {
			Point p1 = stroke.getPoints().get(0);
			Point p2 = stroke.getPoints().get(stroke.getNumPoints() - 1);
			if (line.intersectsLine(p1.getX(), p1.getY(), p2.getX(), p2.getY())) {
				intersect = getIntersectionPt(line, new Line2D.Double(
						p1.getX(), p1.getY(), p2.getX(), p2.getY()));
				intersectionPts.add(intersect);
			}
		}
		return intersectionPts;
	}

	/**
	 * Given a list of indexes for corners, return a list of sub strokes
	 * 
	 * @param corners
	 *            list of indexes of corners
	 * @return list of sub strokes
	 */
	public ArrayList<Stroke> getSubStrokes(List<Integer> corners) {
		ArrayList<Stroke> ss = new ArrayList<Stroke>();
		if (corners.size() <= 0)
			return ss;
		int start = corners.get(0);
		int end;
		for (int i = 1; i < corners.size(); i++) {
			end = corners.get(i);
			Stroke s = getSubStroke(start, end);
			ss.add(s);
			start = end;
		}
		return ss;
	}

	/**
	 * Given a range of indexes, produces a sub-stroke of the stroke
	 * 
	 * @param start
	 *            start index
	 * @param end
	 *            end index
	 * @return substroke from start index to end index
	 */
	public Stroke getSubStroke(int start, int end) {
		Stroke s = new Stroke();
		if (start > end) {
			s.addPoint(m_points.get(start));
			s.addPoint(m_points.get(end));
		} else {
			for (int j = 0; j < m_points.size(); j++) {
				if (j >= start && j <= end)
					s.addPoint(m_points.get(j));
			}
		}
		return s;
	}

	/**
	 * Get the segmenter which breaks the stroke up at every 2pi interval in the
	 * direction graph
	 * 
	 * @return segmenter which breaks the stroke up at every 2pi interval in the
	 *         direction graph
	 */
	public RevolutionSegmenter getRevSegmenter() {
		if (m_rev_segments == null)
			m_rev_segments = new RevolutionSegmenter(this);
		return m_rev_segments;
	}

	/**
	 * Get the curvature graph for the stroke
	 * 
	 * @return curvature graph for the stroke
	 */
	public double[] getCurvature() {
		return m_curvature;
	}

	/**
	 * Get the endpoint to stroke length ratio of the stroke
	 * 
	 * @return endpoint to stroke length ratio
	 */
	public double getEndptStrokeLengthRatio() {
		return m_endPtStrokeLengthRatio;
	}

	/**
	 * Get the index of the maximum curvature value
	 * 
	 * @return index of the max curvature value
	 */
	public int getMaxCurvIndex() {
		return m_max_curv_index;
	}

	/**
	 * Return array of lengths over time according to second derivative
	 * 
	 * @return running total of stroke lengths over time of the stroke (2nd
	 *         deriv)
	 */
	public double[] getLengthSoFar2nd() {
		return m_2lengthSoFar;
	}

	/**
	 * Get the average curvature of the stroke
	 * 
	 * @return average curvature
	 */
	public double getAvgCurvature() {
		return m_avgCurvature;
	}

	/**
	 * Get the maximum curvature to average curvature value
	 * 
	 * @return max curvature to average curvature value
	 */
	public double getMaxCurvToAvgCurvRatio() {
		return m_maxCurvToAvgCurvRatio;
	}

	/**
	 * Get the angle of the major axis relative to (0,0)
	 * 
	 * @return angle of the major axis
	 */
	public double getMajorAxisAngle() {
		return m_majorAxisAngle;
	}

	/**
	 * Get the major axis of the stroke
	 * 
	 * @return major axis of the stroke
	 */
	public Line2D getMajorAxis() {
		return m_majorAxis;
	}

	/**
	 * Get the length of the major axis for this ellipse estimation
	 * 
	 * @return length of the major axis
	 */
	public double getMajorAxisLength() {
		return m_majorAxisLength;
	}

	/**
	 * Estimate the major axis of the stroke(this is done by finding the two
	 * points that are farthest apart and joining them with a line)
	 */
	protected void calcMajorAxis() {
		double maxDistance = Double.MIN_VALUE;
		int max1 = 0, max2 = 0;
		int numPoints = getNumPoints();
		for (int i = 0; i < numPoints; i++) {
			for (int j = 0; j < numPoints; j++) {
				if (i != j) {
					double d = getPoints().get(i).distanceSquared(getPoints().get(j));
					if (d > maxDistance) {
						maxDistance = d;
						max1 = i;
						max2 = j;
					}
				}
			}
		}
		m_majorAxis = new Line2D.Double(getPoints().get(max1).getX(),
				getPoints().get(max1).getY(), getPoints().get(max2).getX(),
				getPoints().get(max2).getY());
		m_majorAxisLength = m_majorAxis.getP1().distance(m_majorAxis.getP2());
		m_majorAxisAngle = Math.atan2(
				m_majorAxis.getY2() - m_majorAxis.getY1(), m_majorAxis.getX2()
						- m_majorAxis.getX1());
	}

	/**
	 * Get a list of the physical corners based on a given segmentation
	 * 
	 * @param segmentation
	 *            segmentation to use for corner finding
	 * @return list of the actual corners (points) for the stroke
	 */
	public List<Point> getCorners(Segmentation segmentation) {
		List<Point> pts = new ArrayList<Point>();
		pts.add(getFirstOrigPoint());
		for (int i = 0; i < segmentation.getSegmentedStrokes().size(); i++) {
			pts.add(segmentation.getSegmentedStrokes().get(i).getLastPoint());
		}
		return pts;
	}

	/**
	 * Method used to rotate the points of a stroke around a certain point
	 * 
	 * @param s
	 *            stroke to rotate
	 * @param angle
	 *            angle to rotate by (in radians)
	 * @param c
	 *            the point the rotate around (center point of rotation)
	 * @return new stroke of rotated points
	 */
	public static Stroke rotatePoints(Stroke s, double angle, Point c) {
		double newX, newY;
		Stroke newS = new Stroke();
		Point p;
		for (int i = 0; i < s.getNumPoints(); i++) {
			Point sp = s.getPoint(i);
			newX = c.getX() + Math.cos(angle) * (sp.getX() - c.getX())
					- Math.sin(angle) * (sp.getY() - c.getY());
			newY = c.getY() + Math.cos(angle) * (sp.getY() - c.getY())
					+ Math.sin(angle) * (sp.getX() - c.getX());
			p = new Point(newX, newY, sp.getTime());
			newS.addPoint(p);
		}
		return newS;
	}

	/**
	 * Compute the distance between the farthest corner of the bounding box and
	 * the stroke
	 * 
	 * @return distance between farthest corner and the stroke
	 */
	protected void calcDistanceBetweenFarthestCornerAndStroke() {
		m_cornerStrokeDistance = 0.0;
		m_minCornerStrokeDistance = Double.MAX_VALUE;
		m_stdDevCornerStrokeDistance = 0.0;
		double dis = 0.0;
		double[] dist = new double[4];
		Point[] corners = new Point[4];
		// TODO: Marty: fix
		corners[0] = new Point(getBounds().getBottomLeftPoint());
		corners[1] = new Point(getBounds().getBottomRightPoint());
		corners[2] = new Point(getBounds().getTopLeftPoint());
		corners[3] = new Point(getBounds().getTopRightPoint());
		for (int i = 0; i < 4; i++)
			dist[i] = Double.MAX_VALUE;
		for (Point p : getOrigPoints()) {
			for (int i = 0; i < 4; i++) {
				dis = corners[i].distance(p);
				if (dis > m_cornerStrokeDistance)
					m_cornerStrokeDistance = dis;
				if (dis < m_minCornerStrokeDistance)
					m_minCornerStrokeDistance = dis;
				if (dis < dist[i])
					dist[i] = dis;
			}
		}
		m_cornerStrokeDistance /= ((getBounds().height + getBounds().width) / 2.0);
		m_minCornerStrokeDistance /= ((getBounds().height + getBounds().width) / 2.0);
		double sum = 0.0;
		for (int i = 0; i < 4; i++)
			sum += dist[i];
		sum /= 4.0;
		m_avgCornerStrokeDistance = sum
				/ ((getBounds().height + getBounds().width) / 2.0);
		for (int i = 0; i < 4; i++) {
			m_stdDevCornerStrokeDistance += (m_avgCornerStrokeDistance - dist[i])
					* (m_avgCornerStrokeDistance - dist[i]);
		}
		m_stdDevCornerStrokeDistance = Math
				.sqrt(m_stdDevCornerStrokeDistance / 4.0);
	}

	/**
	 * Get the distance (normalized by bounding box size) between the furthest
	 * corner and the stroke
	 * 
	 * @return distance
	 */
	public double getDistanceBetweenFarthestCornerAndStroke() {
		return m_cornerStrokeDistance;
	}

	/**
	 * Avg distance between closest point to each corner of the bounding box
	 * 
	 * @return average distance
	 */
	public double getAvgCornerStrokeDistance() {
		return m_avgCornerStrokeDistance;
	}

	/**
	 * Max distance between closest point and each corner of BB
	 * 
	 * @return max distance
	 */
	public double getMaxCornerStrokeDistance() {
		return m_cornerStrokeDistance;
	}

	/**
	 * Min distance between closest point and each corner of BB
	 * 
	 * @return min distance
	 */
	public double getMinCornerStrokeDistance() {
		return m_minCornerStrokeDistance;
	}

	/**
	 * Standard deviation between closest point and each corner of BB
	 * 
	 * @return std dev distance
	 */
	public double getStdDevCornerStrokeDistance() {
		return m_stdDevCornerStrokeDistance;
	}

	/**
	 * Stroke length to perimeter (of bounding box) ratio
	 * 
	 * @return ratio
	 */
	public double getStrokeLengthPerimRatio() {
		return m_perimStrokeLengthRatio;
	}

	/**
	 * Left most point of stroke
	 * 
	 * @return left most point
	 */
	public Point getLeftMostPoint() {
		return m_leftMostPoint;
	}

	/**
	 * Right most point of stroke
	 * 
	 * @return right most point
	 */
	public Point getRightMostPoint() {
		return m_rightMostPoint;
	}

	/**
	 * Top most point of stroke
	 * 
	 * @return top most point
	 */
	public Point getTopMostPoint() {
		return m_topMostPoint;
	}

	/**
	 * Bottom most point of stroke
	 * 
	 * @return bottom most point
	 */
	public Point getBottomMostPoint() {
		return m_bottomMostPoint;
	}
}
