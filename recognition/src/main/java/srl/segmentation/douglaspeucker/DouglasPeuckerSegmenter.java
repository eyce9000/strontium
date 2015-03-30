/**
 * DouglasPeuckerSegmenter.java
 * 
 * Revision History:<br>
 * August 25, 2008 awolin - File created
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
package srl.segmentation.douglaspeucker;

import org.openawt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;


import srl.core.exception.InvalidParametersException;
import srl.core.sketch.ISegmenter;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.segmentation.AbstractSegmenter;


/**
 * Segmenter based off the Douglas-Peucker algorithm for segmenting polylines.
 * Looks at segments of strokes and finds the farthest points from the
 * straight-line segments connecting the endpoints of a stroke segment,
 * considering these points to be a corners. We remove points when groups are
 * collinear.
 * 
 * @author awolin
 */
public class DouglasPeuckerSegmenter extends AbstractSegmenter implements
		ISegmenter, IDouglasPeuckerThresholds {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "Douglas-Peucker";

	/**
	 * Stroke to segment
	 */
	private Stroke m_stroke;

	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<Segmentation> m_threadedSegmentations = null;

	/**
	 * Default constructor
	 */
	public DouglasPeuckerSegmenter() {
		// Do nothing
	}

	/**
	 * Constructor that takes in a stroke
	 * 
	 * @param stroke
	 *            Stroke to segment
	 */
	public DouglasPeuckerSegmenter(Stroke stroke) {
		setStroke(stroke);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.segmentation.ISegmenter#setStroke(edu.tamu.core.sketch.Stroke )
	 */
	public void setStroke(Stroke stroke) {
		m_stroke = stroke;
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

		if (m_stroke == null)
			throw new InvalidParametersException();

		List<Integer> corners = getCorners();

		// TODO - better confidence
		List<Segmentation> segs = segmentStroke(m_stroke, corners,
				S_SEGMENTER_NAME, 0.80);

		return segs;
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
	 * Segment the stroke using the Douglas-Peucker algorithm
	 * 
	 * @return The stroke's corners
	 */
	private List<Integer> getCorners() {

		// Initialize the corner list and add the start point
		List<Integer> corners = new ArrayList<Integer>();
		corners.add(0);

		Stack<Integer> floaters = new Stack<Integer>();

		int anchorIndex = 0;
		int floaterIndex = m_stroke.getNumPoints() - 1;
		int lastPointIndex = floaterIndex;

		floaters.push(lastPointIndex);

		// Main algorithm loop
		while (anchorIndex != lastPointIndex) {

			// Initialize the anchor and floater points
			Point anchor = m_stroke.getPoint(anchorIndex);
			Point floater = m_stroke.getPoint(floaterIndex);

			// Initialize the maximum distance values
			double maxDist = Double.MIN_VALUE;
			int maxDistIndex = anchorIndex;

			// Find the point farthest away from the straight line segment
			for (int i = anchorIndex + 1; i < floaterIndex; i++) {

				Point currPoint = m_stroke.getPoint(i);

				double dist = Line2D.ptLineDist(anchor.getX(), anchor.getY(),
						floater.getX(), floater.getY(), currPoint.getX(),
						currPoint.getY());

				if (dist > maxDist) {
					maxDist = dist;
					maxDistIndex = i;
				}
			}

			if (maxDist > S_DIST_OFFSET_THRESHOLD * anchor.distance(floater)
					&& anchor.distance(floater) > 0.0) {

				if (!corners.contains(maxDistIndex)) {
					corners.add(maxDistIndex);
				}

				// System.out.println(maxDistIndex + ": " + maxDist + " between
				// "
				// + anchorIndex + "," + floaterIndex);

				floaterIndex = maxDistIndex;
				floaters.push(maxDistIndex);
			} else {
				// anchorIndex = floaterIndex;
				// floaterIndex = lastPointIndex;

				anchorIndex = floaters.pop();

				if (!floaters.empty()) {
					floaterIndex = floaters.lastElement();
				} else {
					break;
				}
			}
		}

		// Add the endpoint
		corners.add(lastPointIndex);

		// Sort the corners
		Collections.sort(corners);

		// System.out.println("-------------");

		// Remove extraneous corners
		List<Integer> filteredCorners = postProcessCorners(corners);

		// return filteredCorners;

		return corners;
	}

	/**
	 * Removes the middle points in clusters of collinear points, ensuring that
	 * each segment remaining is considered to be a line.
	 * 
	 * @param corners
	 *            Segmentation points to process
	 * @return A list of processed corners
	 */
	private List<Integer> postProcessCorners(List<Integer> corners) {

		// Post-processed corners
		List<Integer> filteredCorners = new ArrayList<Integer>(corners);

		// Calculate the path lengths at each point, for use in the isLine test
		double[] pathLengths = new double[m_stroke.getNumPoints()];
		pathLengths[0] = 0.0;

		for (int i = 1; i < m_stroke.getNumPoints(); i++) {
			pathLengths[i] = pathLengths[i - 1]
					+ m_stroke.getPoint(i - 1).distance(m_stroke.getPoint(i));
		}

		// Collinear test for the lines
		for (int i = 1; i < filteredCorners.size() - 1; i++) {
			int c1 = filteredCorners.get(i - 1);
			int c3 = filteredCorners.get(i + 1);

			if (isLine(c1, c3, m_stroke, pathLengths, S_LINE_VS_ARC_THRESHOLD)) {
				filteredCorners.remove(i);
				i--;
			}
		}

		return filteredCorners;
	}
}
