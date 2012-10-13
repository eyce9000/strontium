package srl.core.sketch;
/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2011 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&M University 
 *        nor the names of its contributors may be used to endorse or promote 
 *        products derived from this software without specific prior written 
 *        permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *  
 *******************************************************************************/
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.openawt.BasicStroke;
import org.openawt.Color;
import org.openawt.geom.AffineTransform;
import org.openawt.geom.GeneralPath;
import org.openawt.geom.Path2D;
import org.openawt.geom.PathIterator;
import org.openawt.svg.SVGPath;
import org.openawt.svg.SVGShape;
import org.openawt.svg.Style;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

import srl.core.sketch.comparators.TimePeriodComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Root(name="stroke",strict=false)
public class Stroke extends SComponent implements IClassifiable,
Iterable<Point>,TimePeriod, Comparable<TimePeriod> {

	@ElementList(inline=true,entry="point")
	protected List<Point> points;
	@ElementList(inline=true,entry="interpretation", required = false)
	protected List<Interpretation> interpretations;
	@ElementList(inline=true,entry="segmentation", required = false)
	protected List<Segmentation> segmentations;

	@Element(required = false)
	protected Stroke parent;
	
	private transient long timeStart = -1L;
	private transient long timeEnd = -1L;

	public Stroke() {
		points = new ArrayList<Point>();
		interpretations = new ArrayList<Interpretation>();
		segmentations = new ArrayList<Segmentation>();
	}

	public Stroke(List<Point> points) {
		this();
		this.points = points;
	}

	public Stroke(Stroke copyFrom) {
		super(copyFrom);

		points = new ArrayList<Point>();
		interpretations = new ArrayList<Interpretation>();
		segmentations = new ArrayList<Segmentation>();

		for (Point p : copyFrom.points)
			points.add(p.clone());
		for (Interpretation i : copyFrom.interpretations)
			interpretations.add(i.clone());
		for (Segmentation s : copyFrom.segmentations)
			segmentations.add(s.clone());
	}

	public Stroke(IStroke stroke) {
		this();

		for (IPoint point : stroke.getPoints())
			points.add(new Point(point));

		id = stroke.getID();

		for (ISegmentation seg : stroke.getSegmentations())
			segmentations.add(new Segmentation(seg));

		if(stroke.getParent()!=null)
			parent = new Stroke(stroke.getParent());

		if (stroke.getLabel() != null && stroke.getLabel() != "")
			interpretations.add(new Interpretation(stroke.getLabel(), stroke
					.getLabelProbability()));

	}
	
	@Override
	public void applyTransform(AffineTransform xform, Set<Transformable> xformed) {
		if (xformed.contains(this))
			return;
		xformed.add(this);

		for (Point p : points)
			p.applyTransform(xform, xformed);

		flagExternalUpdate();
	}

	@Override
	public Interpretation getInterpretation() {
		return (interpretations.size() > 0) ? interpretations
				.get(interpretations.size() - 1) : null;
	}

	@Override
	public List<Interpretation> getNBestList() {
		return interpretations;
	}

	@Override
	public void addInterpretation(Interpretation i) {
		int position = Collections.binarySearch(interpretations, i);

		if (position >= 0)
			return;

		interpretations.add(-(position + 1), i);
	}

	@Override
	public void setNBestList(List<Interpretation> list) {
		interpretations = new ArrayList<Interpretation>(list);
	}

	/**
	 * Append a point onto this stroke.
	 * 
	 * @param p
	 */
	public void addPoint(Point p) {
		points.add(p);
		flagExternalUpdate();
	}

	/**
	 * Append a list of points onto this stroke
	 * @param points
	 */
	public void addPoints(List<Point> points){
		this.points.addAll(points);
	}
	
	/**
	 * Get a pointer to this stroke's points. Modify at your own risk.
	 * 
	 * @return
	 */
	public List<Point> getPoints() {
		return points;
	}

	/**
	 * Replace this stroke's list of points with the given list.
	 * 
	 * @param points
	 */
	public void setPoints(List<Point> points) {
		this.points = points;
		flagExternalUpdate();
	}

	/**
	 * Get the distance between endpoints
	 * @return
	 */
	public double getLength()
	{
		if (points.size() == 0) return 0;
		return getFirstPoint().distance(getLastPoint());
	}
	
	/**
	 * Get the path length of this stroke
	 * 
	 * @return
	 */
	public double getPathLength() {
		double res = 0.0;

		for (int i = 1; i < points.size(); ++i)
			res += points.get(i - 1).distance(points.get(i));

		return res;
	}

	/**
	 * Get the number of points in this stroke
	 * 
	 * @return
	 */
	public int getNumPoints() {
		return points.size();
	}

	/**
	 * Get the ith point of this stroke.
	 * 
	 * @param i
	 * @return
	 */
	public Point getPoint(int i) {
		return points.get(i);
	}

	/**
	 * Get the first point in this stroke.
	 * 
	 * @return
	 */
	public Point getFirstPoint() {
		if (points.size() == 0)
			return null;
		return points.get(0);
	}

	/**
	 * Get the last point in this stroke.
	 * 
	 * @return
	 */
	public Point getLastPoint() {
		if (points.size() == 0)
			return null;
		return points.get(points.size() - 1);
	}

	@Override
	public void setInterpretation(Interpretation i) {
		if (interpretations.size() > 1) {
			System.err
			.println("warning: clearning interpretations with setInterpretation");
		}
		interpretations.clear();
		interpretations.add(i);
	}

	@Override
	public Stroke clone() {
		return new Stroke(this);
	}

	/**
	 * Returns this stroke's list of segmentations. Modify at your own risk.
	 * 
	 * @return
	 */
	public List<Segmentation> getSegmentations() {
		return segmentations;
	}

	/**
	 * Add segmentation
	 * @param segmentation
	 */
	public void addSegmentation(Segmentation seg) {
		segmentations.add(seg);
	}
	
	/**
	 * Add segmentations
	 * @param segmentations
	 */
	public void addSegmentations(List<Segmentation> segs){
		segmentations.addAll(segs);
	}
	
	public void setParent(Stroke parent) {
		this.parent = parent;
	}

	public Stroke getParent() {
		return parent;
	}

	@Override
	public void setInterpretation(String label, double confidence) {
		setInterpretation(new Interpretation(label, confidence));
	}

	/**
	 * Set the label of this shape.
	 * 
	 * Override all current interpretations.
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		if (interpretations.size() > 1) {
			System.err
			.println("warning: clearning interpretations with setLabel");
		}
		interpretations.clear();
		interpretations.add(new Interpretation(label, 1.0));
	}

	/**
	 * Adds a point to this stroke where the two segments intersect - if they
	 * intersect
	 * 
	 * @return the index of the newly created point in the getPoints() array.
	 *         Returns -1 if added nothing This method automatically calls
	 *         {@link #flagExternalUpdate()}.
	 */
	public int addPointAtIntersection(Stroke stroke)
	throws NullPointerException {
		List<Point> otherPoints = stroke.getPoints();

		int closestIndexThis = -1;
		int closestIndexOther = -1;

		int returnIndex = -1;
		double[] answer = strokeIntersection(stroke);

		if (answer[0] >= .00001 && answer[0] <= .9999 && answer[1] >= 0
				&& answer[1] <= 1) {

			for (int j = 1; j < otherPoints.size(); j++) {
				Point otherEnd1 = otherPoints.get(j - 1);
				Point otherEnd2 = otherPoints.get(j);
				if (otherEnd1.getX() > otherEnd2.getX()) {
					Point temp = otherEnd1;
					otherEnd1 = otherEnd2;
					otherEnd2 = temp;
				}

				for (int i = 1; i < getPoints().size(); i++) {
					Point this1 = points.get(i - 1);
					Point this2 = points.get(i);
					if (this1.getX() > this2.getX()) {
						Point temp = this1;
						this1 = this2;
						this2 = temp;
					}

					answer = segmentIntersection(this1, this2, otherEnd1,
							otherEnd2);
					if (answer[0] >= 0 && answer[0] <= 1 && answer[1] >= 0
							&& answer[1] <= 1) {
						double x = this2.getX();
						x = x - this1.getX();
						x = x * answer[0];
						x = x + this1.getX();

						// y = ((y1-y2) * distance along line) + y2
						double y = this2.getY();
						y = y - this1.getY();
						y = y * answer[0];
						y = y + this1.getY();

						Point newPoint = new Point(x, y);

						if (newPoint.distance(this1) < 1)
							return points.indexOf(this1);
						if (newPoint.distance(this2) < 1)
							return points.indexOf(this2);

						newPoint.setTime(this1.getTime()
								+ (this2.getTime() - this1.getTime()) / 2);// simple
						// average
						points.add(i, newPoint);

						returnIndex = i;

						flagExternalUpdate();

						break;
					}
				}
			}
		} else if (answer[0] >= .00001 && answer[0] <= .9999 && answer[1] >= -1
				&& answer[1] <= 2) {
			Point otherEnd1 = otherPoints.get(0);
			Point otherEnd2 = otherPoints.get(otherPoints.size() - 1);
			if (otherEnd1.getX() > otherEnd2.getX()) {
				Point temp = otherEnd1;
				otherEnd1 = otherEnd2;
				otherEnd2 = temp;
			}

			for (int i = 1; i < getPoints().size(); i++) {
				Point this1 = points.get(i - 1);
				Point this2 = points.get(i);
				if (this1.getX() > this2.getX()) {
					Point temp = this1;
					this1 = this2;
					this2 = temp;
				}

				answer = segmentIntersection(this1, this2, otherEnd1, otherEnd2);
				if (answer[0] >= 0 && answer[0] <= 1) {
					double x = this2.getX();
					x = x - this1.getX();
					x = x * answer[0];
					x = x + this1.getX();

					// y = ((y1-y2) * distance along line) + y2
					double y = this2.getY();
					y = y - this1.getY();
					y = y * answer[0];
					y = y + this1.getY();

					Point newPoint = new Point(x, y);

					if (newPoint.distance(this1) < 1)
						return points.indexOf(this1);
					if (newPoint.distance(this2) < 1)
						return points.indexOf(this2);

					newPoint.setTime(this1.getTime()
							+ (this2.getTime() - this1.getTime()) / 2);// simple
					// average
					points.add(i, newPoint);

					returnIndex = i;

					flagExternalUpdate();

					break;
				}
			}
		}

		if (returnIndex == -1) {
			// System.out.println("did not add point");
		}
		flagExternalUpdate();
		return returnIndex;
	}

	/**
	 * Calculate intersection point along both line segments.
	 * 
	 * ua is 0 if the intersection is at alast and 1 if it is at ahere ua is
	 * 0.333 if it is 1/3 the way from alast to ahere.
	 * 
	 * ub acts a lot like ua, except with blast and bhere.
	 * 
	 * @param one
	 *            end point for first line
	 * @param two
	 *            endpoint for first line
	 * @param a
	 *            endpoint for second line
	 * @param b
	 *            endpoint for second line
	 * @return [ua,ub]
	 */
	public static double[] segmentIntersection(Point one, Point two, Point a, Point b) {

		double x1 = one.getX();
		double x2 = two.getX();
		double x3 = a.getX();
		double x4 = b.getX();
		double y1 = one.getY();
		double y2 = two.getY();
		double y3 = a.getY();
		double y4 = b.getY();

		double denom = ((y4 - y3) * (x2 - x1)) - ((x4 - x3) * (y2 - y1));
		if (Math.abs(denom) < 1e-5)
			return new double[] { Double.MAX_VALUE, Double.MAX_VALUE };
		// parallel (ish) lines

		double ua = ((x4 - x3) * (y1 - y3)) - ((y4 - y3) * (x1 - x3));
		double ub = ((x2 - x1) * (y1 - y3)) - ((y2 - y1) * (x1 - x3));

		return new double[] { ua / denom, ub / denom };
	}

	/**
	 * Calculate intersection point along both line segments.
	 * 
	 * ua is 0 if the intersection is at alast and 1 if it is at ahere ua is
	 * 0.333 if it is 1/3 the way from alast to ahere.
	 * 
	 * ub acts a lot like ua, except with blast and bhere.
	 * 
	 * @param one
	 *            end point for first line
	 * @param two
	 *            endpoint for first line
	 * @param a
	 *            endpoint for second line
	 * @param b
	 *            endpoint for second line
	 * @return [ua,ub]
	 */
	public double[] strokeIntersection(Stroke s) {
		Point one = getFirstPoint();
		Point two = getLastPoint();
		if (one.getX() > two.getX()) {
			Point temp = one;
			one = two;
			two = temp;
		}

		Point a = s.getFirstPoint();
		Point b = s.getLastPoint();
		if (a.getX() > b.getX()) {
			Point temp = a;
			a = b;
			b = temp;
		}

		return segmentIntersection(one, two, a, b);
	}

	@Override
	protected void calculateBBox() {
		double minX = Double.POSITIVE_INFINITY;
		double minY = minX;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = maxX;

		for (Point p : points) {
			if (p.x < minX)
				minX = p.x;
			if (p.x > maxX)
				maxX = p.x;
			if (p.y < minY)
				minY = p.y;
			if (p.y > maxY)
				maxY = p.y;
		}

		boundingBox = new BoundingBox(minX, minY, maxX, maxY);
	}

	public boolean equalsByContent(SComponent other) {
		if (this == other)
			return true;

		if (!(other instanceof Stroke))
			return false;

		Stroke o = (Stroke) other;
		if (points.size() != o.points.size())
			return false;

		for (int i = 0; i < points.size(); ++i) {
			if (!points.get(i).equalsByContent(o.points.get(i)))
				return false;
		}
		return true;
	}

	@Override
	public Iterator<Point> iterator() {
		return points.iterator();
	}

	private synchronized void rebuildTime(){
		timeStart = Long.MAX_VALUE;
		timeEnd = Long.MIN_VALUE;
		for(Point comp : points){
			long otherTimeEnd,otherTimeStart;
			otherTimeEnd = comp.getTime();
			otherTimeStart = comp.getTime();
			timeStart = Math.min(timeStart, otherTimeStart);
			timeEnd = Math.max(timeEnd, otherTimeEnd);
		}
	}
	
	@Override
	public long getTimeEnd() {
		if (timeEnd == -1L) {
			rebuildTime();
		}

		return timeEnd;
	}
	/**
	 * Get the starting time of this container. This is the minimum starting time of all contained SComponents and SContainers.
	 * @return
	 */
	public long getTimeStart(){
		if(timeStart == -1L){
			rebuildTime();
		}
		return timeStart;
	}
	/**
	 * Get the difference in time between the start time and end time;
	 * @return the difference in time between the start time and end time
	 */
	public long getTimeLength(){
		return Math.abs(getTimeEnd()-getTimeStart());
	}

	public String toString() {
		return super.toString() + " " + id;
	}

	/**
	 * Find the index of the point with the given id.
	 * 
	 * @param id
	 * @return the index of the point with the given id or -1;
	 */
	public int indexOf(UUID id) {
		for (int i = 0; i < points.size(); ++i)
			if (points.get(i).id.equals(id))
				return i;
		return -1;
	}
	
	@Override
	public SVGShape toSVGShape(){
		Path2D.Double path = new Path2D.Double();
		if (points.size() > 0) {
			path.moveTo(points.get(0).getX(), points.get(0).getY());

			for (int i = 1; i < points.size(); i++) {
				Point p2 = points.get(i);
				path.lineTo(p2.getX(), p2.getY());
			}
		}
		SVGPath svg =  new SVGPath(path);
		svg.setStyle(this.getStyle());
		return svg;
	}


	private static TimePeriodComparator comparator = new TimePeriodComparator();
	@Override
	public int compareTo(TimePeriod other) {
		return comparator.compare(this, other);
	}

}
