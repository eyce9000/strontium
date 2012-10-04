/**
 * FeatureArea.java
 *
 * Revision History:<br>
 * Jun 24, 2008 bpaulson - File created
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
package srl.math;

import java.util.ArrayList;
import java.util.List;

import org.openawt.geom.Line2D;
import org.openawt.geom.Point2D;
import org.openawt.geom.Rectangle2D;

import srl.core.sketch.Point;


/**
 * Class containing static methods used to conduct feature area tests
 * 
 * @author bpaulson
 */
public class FeatureArea {

	/**
	 * Calculate the feature area of the (x,y) points to the input line
	 * 
	 * @param x
	 *            x values
	 * @param y
	 *            y values
	 * @param line
	 *            line to find feature area to
	 * @return feature area from the (x,y) points to the input line
	 */
	public static double toLine(double[] x, double[] y, Line2D line) {
		double area = 0;
		double b1, b2, d, h;
		for (int i = 0; i < x.length - 1; i++) {
			b1 = line.ptSegDist(x[i], y[i]);
			b2 = line.ptSegDist(x[i + 1], y[i + 1]);
			d = Point2D.distance(x[i], y[i], x[i + 1], y[i + 1]);
			h = Math.sqrt(Math.abs(Math.pow(d, 2)
					- Math.pow(Math.abs(b1 - b2), 2)));
			area += Math.abs(0.5 * (b1 + b2) * h);
		}
		return area;
	}

	/**
	 * Calculate the feature area of the (x,y) points to the input line
	 * 
	 * @param points
	 *            points
	 * @param line
	 *            line to find feature area to
	 * @return feature area from the (x,y) points to the input line
	 */
	public static double toLine(Point2D[] points, Line2D line) {
		double area = 0;
		double b1, b2, d, h;
		for (int i = 0; i < points.length - 1; i++) {
			b1 = line.ptSegDist(points[i]);
			b2 = line.ptSegDist(points[i + 1]);
			d = points[i].distance(points[i + 1]);
			h = Math.sqrt(Math.abs(Math.pow(d, 2)
					- Math.pow(Math.abs(b1 - b2), 2)));
			area += Math.abs(0.5 * (b1 + b2) * h);
		}
		return area;
	}

	/**
	 * Calculate the feature area of the (x,y) points to the input line
	 * 
	 * @param points
	 *            points
	 * @param line
	 *            line to find feature area to
	 * @return feature area from the (x,y) points to the input line
	 */
	public static double toLine(List<Point> points, Line2D line) {
		double area = 0;
		double b1, b2, d, h;
		for (int i = 0; i < points.size() - 1; i++) {
			b1 = line.ptSegDist(points.get(i).getX(), points.get(i).getY());
			b2 = line.ptSegDist(points.get(i + 1).getX(), points.get(i + 1)
					.getY());
			d = points.get(i).distance(points.get(i + 1));
			h = Math.sqrt(Math.abs(Math.pow(d, 2)
					- Math.pow(Math.abs(b1 - b2), 2)));
			area += Math.abs(0.5 * (b1 + b2) * h);
		}
		return area;
	}

	/**
	 * Calculate the feature area of the (x,y) points to the input rectangle
	 * 
	 * @param points
	 *            points
	 * @param rect
	 *            rect to find feature area to
	 * @return feature area from the (x,y) points to the input rectangle
	 */
	public static double toRectangle(List<Point> points, Rectangle2D rect) {
		double area = 0;
		List<List<Point>> pList = new ArrayList<List<Point>>();
		for (int i = 0; i < 4; i++)
			pList.add(new ArrayList<Point>());
		Line2D l1 = new Line2D.Double(rect.getX(), rect.getY(), rect.getX()
				+ rect.getWidth(), rect.getY());
		Line2D l2 = new Line2D.Double(rect.getX() + rect.getWidth(),
				rect.getY(), rect.getX() + rect.getWidth(), rect.getY()
						+ rect.getHeight());
		Line2D l3 = new Line2D.Double(rect.getX() + rect.getWidth(),
				rect.getY() + rect.getHeight(), rect.getX(), rect.getY()
						+ rect.getHeight());
		Line2D l4 = new Line2D.Double(rect.getX(), rect.getY()
				+ rect.getHeight(), rect.getX(), rect.getY());
		List<Line2D> lList = new ArrayList<Line2D>();
		lList.add(l1);
		lList.add(l2);
		lList.add(l3);
		lList.add(l4);

		// assign each point to the line it is closest with
		for (Point p : points) {
			int index = 0;
			double dist = lList.get(0).ptSegDist(p.getX(), p.getY());
			for (int i = 1; i < 4; i++) {
				if (lList.get(i).ptSegDist(p.getX(), p.getY()) < dist) {
					index = i;
					dist = lList.get(i).ptSegDist(p.getX(), p.getY());
				}
			}
			pList.get(index).add(p);
		}

		// compute feature area of each set of points to its corresponding line
		for (int i = 0; i < 4; i++) {
			area += FeatureArea.toLine(pList.get(i), lList.get(i));
		}
		return area;
	}

	/**
	 * Calculate the feature area of the (x,y) points to the input point
	 * 
	 * @param x
	 *            x values
	 * @param y
	 *            y values
	 * @param p
	 *            point to find feature area to
	 * @return feature area from the (x,y) points to the input point
	 */
	public static double toPoint(double[] x, double[] y, Point2D p) {
		double area = 0;
		double a, b, c, s;
		for (int i = 0; i < x.length - 1; i++) {
			a = Point2D.distance(x[i], y[i], x[i + 1], y[i + 1]);
			b = Point2D.distance(x[i], y[i], p.getX(), p.getY());
			c = Point2D.distance(x[i + 1], y[i + 1], p.getX(), p.getY());
			s = (a + b + c) / 2;
			area += Math.sqrt(s * (s - a) * (s - b) * (s - c));
		}
		return area;
	}

	/**
	 * Calculate the feature area of the (x,y) points to the input point
	 * 
	 * @param points
	 *            points
	 * @param p
	 *            point to find feature area to
	 * @return feature area from the (x,y) points to the input point
	 */
	public static double toPoint(Point2D[] points, Point2D p) {
		double area = 0;
		double a, b, c, s;
		for (int i = 0; i < points.length - 1; i++) {
			a = points[i].distance(points[i + 1]);
			b = points[i].distance(p);
			c = points[i + 1].distance(p);
			s = (a + b + c) / 2;
			area += Math.sqrt(s * (s - a) * (s - b) * (s - c));
		}
		return area;
	}

	/**
	 * Calculate the feature area of the (x,y) points to the input point
	 * 
	 * @param points
	 *            points
	 * @param p
	 *            point to find feature area to
	 * @return feature area from the (x,y) points to the input point
	 */
	public static double toPoint(List<Point> points, Point2D p) {
		double area = 0;
		double a, b, c, s;
		for (int i = 0; i < points.size() - 1; i++) {
			a = points.get(i).distance(points.get(i + 1));
			b = points.get(i).distance(p.getX(), p.getY());
			c = points.get(i + 1).distance(p.getX(), p.getY());
			s = (a + b + c) / 2;
			area += Math.sqrt(s * (s - a) * (s - b) * (s - c));
		}
		return area;
	}
}
