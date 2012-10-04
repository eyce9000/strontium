/**
 * IStroke.java
 * 
 * Revision History: <br>
 * (5/23/08) awolin - Created the interface <br>
 * (5/23/08) awolin - Added a getTime() and a getNumPoints() to the interface,
 * since they are heavily used <br>
 * (5/23/08) awolin - Changed P to Pt in the generics definition <br>
 * (5/26/08) bpaulson - Added an addPoint() function <br>
 * 2 July 2008 : jbjohns : bounding box caching stuff <br>
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

package srl.core.sketch;

import org.openawt.Color;
import java.util.List;
import java.util.UUID;


/**
 * IStroke interface for a stroke containing IPoints.
 * 
 * @author awolin
 */
public interface IStroke extends Comparable<IStroke>, Cloneable {

	/**
	 * Gets the list of IPoints of the IStroke. If you use this method to update
	 * points directly, you should also call {@link #flagExternalUpdate()} so
	 * the object knows it needs to update cached values. If you do not call
	 * {@link #flagExternalUpdate()}, the stroke may NOT assume the point list
	 * has changed and any cached values (e.g. bounding box) you get MAY BE
	 * INACCURATE.
	 * 
	 * @return a List of IPoints.
	 */
	public List<IPoint> getPoints();

	/**
	 * Get the minimum inter-point distance (for 0 <= i < points.size()-1,
	 * min(points(i).distance(points(i+1)))) for this stroke.
	 * 
	 * @return the minimum inter-point distance for the stroke.
	 */
	public double getMinInterPointDistance();

	/**
	 * Get the point at the specified index. This is a pass-through call to
	 * {@link List#get(int)}.
	 * 
	 * @param index
	 *            the index of point to get.
	 * @return the point at the specified index.
	 */
	public IPoint getPoint(int index);

	/**
	 * Returns a list of segmentations (corner finding interpretations) for the
	 * stroke.
	 * 
	 * @return a list of possible segmentations.
	 */
	public List<ISegmentation> getSegmentations();

	/**
	 * Get the ID of the IStroke.
	 * 
	 * @return UUID of the IStroke.
	 */
	public UUID getID();

	/**
	 * Get the parent stroke (if this stroke is a sub-stroke). If not a
	 * sub-stroke then parent will be null.
	 * 
	 * @return parent stroke (if it exists).
	 */
	public IStroke getParent();

	/**
	 * Get the first point in this stroke. This is a pass-through call to
	 * {@link List#get(int)} with index of 0. Make sure there is a first point
	 * first, or you will probably get an exception.
	 * 
	 * @return the first point, if it exists, or exception.
	 */
	public IPoint getFirstPoint();

	/**
	 * Get the last point in this stroke. This is a pass-through call to
	 * {@link List#get(int)} with index of {@link #getNumPoints()}-1. Make sure
	 * there is a last point (non-empty stroke), or you will probably get an
	 * exception.
	 * 
	 * @return the last point, if it exists, or exception.
	 */
	public IPoint getLastPoint();

	/**
	 * Gets the creation time of the stroke. This creation time is defined as
	 * the time of the last IPoint in the stroke. UPDATING THE STROKE THROUGH
	 * NON-APPEND METHODS CAN BREAK THIS FUNCTIONALITY.
	 * 
	 * @return the creation time of the stroke.
	 */
	public long getTime();

	/**
	 * Get the number of points in the stroke.
	 * 
	 * @return the number of points within the stroke.
	 */
	public int getNumPoints();

	/**
	 * Gets the length of the path, that is, the sum of the Euclidean distances
	 * between all pairs of consecutive points. This value is cached and updated
	 * iteratively in O(1) time with each call to {@link #addPoint(IPoint)}.
	 * However, if you {@link #getPoints()} or {@link #getPoint(int)} and modify
	 * anything, you will need to {@link #flagExternalUpdate()} so we know the
	 * value needs to be recomputed at a cost of O(n).
	 * 
	 * @return the length of the stroke&#39;s path.
	 */
	public double getPathLength();

	/**
	 * Get the bounding box around this stroke. If you use the
	 * {@link #addPoint(IPoint)} method, the bounding box is updated iteratively
	 * in O(1) time. If you use something like {@link #getPoints()} and modify
	 * points directly, you will need to {@link #flagExternalUpdate()} so we
	 * know we need to recompute the bounding box, at a cost of O(1).
	 * 
	 * @return the bounding box around this stroke.
	 */
	public BoundingBox getBoundingBox();

	/**
	 * Sets the list of IPoints of the IStroke. You should sort the list of
	 * points, or ensure they are in ascending temporal order, BEFORE you use
	 * this method. The functionality and definition of time-related methods
	 * depends on the list of points in a stroke being in ascending temporal
	 * order.
	 * 
	 * @param points
	 *            the list of points to set for the IStroke.
	 * 
	 * @throws NullPointerException
	 *             if the points argument is null.
	 */
	public void setPoints(List<IPoint> points) throws NullPointerException;

	/**
	 * Sets the list of segmentations (corner finding interpretations) for the
	 * stroke.
	 * 
	 * @param segmentations
	 *            list of possible segmentations.
	 * 
	 * @throws NullPointerException
	 *             if the segmentation argument is null.
	 */
	public void setSegmentations(List<ISegmentation> segmentations)
			throws NullPointerException;

	/**
	 * Set the parent stroke for this stroke.
	 * 
	 * @param parent
	 *            parent stroke.
	 */
	public void setParent(IStroke parent);

	/**
	 * Add a point to the list of point. This method will update the cached
	 * values, if needed, with a O(1) iterative update. This method DOES NOT
	 * ensure the points are put in temporal order. It is assumed you would only
	 * add points in ascending temporal order. If you are not, you need to do
	 * your own book keeping.
	 * 
	 * @param point
	 *            point to add.
	 * 
	 * @throws NullPointerException
	 *             if the point argument is null.
	 */
	public void addPoint(IPoint point) throws NullPointerException;

	/**
	 * Add a segmentation to the list of possible segmentations. Currently,
	 * segmentations are only allowed for strokes that do not have a parent.
	 * This prevents recursive segmentations.
	 * 
	 * TODO - Throw a SegmentationNotAddedException TODO - Create this
	 * exception...
	 * 
	 * @param segmentation
	 *            segmentation (corner finding) interpretation to add.
	 */
	public void addSegmentation(ISegmentation segmentation)
			throws NullPointerException;

	/**
	 * Remove a segmentation from the list of possible segmentations
	 * 
	 * @param segmentation
	 *            segmentation to remove
	 * @return true if the segmentation was removed, false otherwise.
	 * 
	 * @throws NullPointerException
	 *             if the segmentation argument is null.
	 */
	public boolean removeSegmentation(ISegmentation segmentation)
			throws NullPointerException;

	/**
	 * Let the class know that it needs to recompute any cached values. Use this
	 * method if you {@link #getPoints()} and modify the list directly. The
	 * method {@link #setPoints(List)} should automatically call this method so
	 * any cached values are forced to update next time it is requested. The
	 * method {@link #addPoint(IPoint)} should update cached values with each
	 * point addition, operating in O(1) time.
	 */
	public void flagExternalUpdate();

	/**
	 * Clone an IStroke.
	 * 
	 * @return a new, deep copied IStroke.
	 */
	public Object clone();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IDrawingAttributes#setColor(java.awt.Color)
	 */
	public void setColor(Color color);

	public Color getColor();

	/**
	 * Get the label that is assigned to this stroke, may be null if no label
	 * has been set.
	 * 
	 * @return the label assigned to this stroke.
	 */
	public String getLabel();

	/**
	 * Get the probability associated with the stroke's label.
	 * 
	 * @return the probability associated with the stroke's label.
	 */
	public double getLabelProbability();

	/**
	 * Set the label for this stroke. May set to null if you do not want a
	 * label.
	 * 
	 * @param label
	 *            the label for this stroke.
	 */
	public void setLabel(String label);

	/**
	 * Sets the probability associated with the stroke's label.
	 * 
	 * @param probability
	 *            the probability associated with the stroke's label.
	 */
	public void setLabelProbability(double probability);

	public void translate(double x, double y);

	public void scale(double x, double y);
}
