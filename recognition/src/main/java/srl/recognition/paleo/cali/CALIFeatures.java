/**
 * CALIFeatures.java
 * 
 * Revision History:<br>
 * Oct 20, 2009 bpaulson - File created
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
package srl.recognition.paleo.cali;

import org.openawt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;


/**
 * Computes features from CALI (convex hull related features)
 * 
 * @author bpaulson
 */
public class CALIFeatures {

	protected List<Point> m_points = new ArrayList<Point>();

	private double m_length = 0.0;

	private Polygon m_boundingBox;

	private Polygon m_convexHull;

	private Polygon m_largestTriangle;

	private Polygon m_largestQuad;

	private Polygon m_enclosingRect;

	private static final double M_PI_2 = Math.PI / 2.0;

	private static final double BIG = 1E30;

	public CALIFeatures(Stroke str) {
		m_length = str.getPathLength();
		for (Point p : str.getPoints())
			m_points.add(p);
		compute();
	}

	public void addPoint(Point p) {
		m_points.add(p);
	}

	public void compute() {
		computeConvexHull();
		computeBoundingBox();
		computeLargestTriangle();
		computeLargestQuad();
		computeEnclosingRect();
	}

	public double Tl_Pch() {
		return m_length / m_convexHull.perimeter();
	}

	public double Pch2_Ach() {
		return Math.pow(m_convexHull.perimeter(), 2) / m_convexHull.area();
	}

	public double Pch_Ns_Tl() {
		return m_convexHull.perimeter() / m_length;
	}

	public double Hollowness() {
		return ptsInSmallTri();
	}

	public double Hm_Wbb() {
		List<Point> pbb = m_boundingBox;

		return Math.abs((double) (pbb.get(0).getX() - pbb.get(1).getX())
				/ hMovement());
	}

	public double Vm_Hbb() {
		List<Point> pbb = m_boundingBox;

		return Math.abs((double) (pbb.get(2).getY() - pbb.get(1).getY())
				/ vMovement());
	}

	public double Hbb_Wbb() {
		List<Point> pbb = m_boundingBox;

		double dw, dh;

		dw = pbb.get(1).getX() - pbb.get(0).getX();
		dh = pbb.get(2).getY() - pbb.get(1).getY();

		if (dw == 0 || dh == 0)
			return 0;

		double tmp = Math.abs((double) dh / dw);
		if (tmp > 1)
			tmp = 1 / tmp;
		return tmp;
	}

	public double Her_Wer() {
		List<Point> pbb = m_enclosingRect;

		double dw, dh;

		dw = pbb.get(2).distance(pbb.get(1));
		dh = pbb.get(1).distance(pbb.get(0));

		if (dw == 0 || dh == 0)
			return 0;

		double tmp = dh / dw;
		if (tmp > 1)
			tmp = 1 / tmp;
		return tmp;
	}

	// Area ratios
	public double Alt_Ach() {
		return m_largestTriangle.area() / m_convexHull.area();
	}

	public double Ach_Aer() {
		return m_convexHull.area() / m_enclosingRect.area();
	}

	public double Alt_Aer() {
		return m_largestTriangle.area() / m_enclosingRect.area();
	}

	public double Ach_Abb() {
		return m_convexHull.area() / m_boundingBox.area();
	}

	public double Alt_Abb() {
		return m_largestTriangle.area() / m_boundingBox.area();
	}

	public double Alq_Ach() {
		return m_largestQuad.area() / m_convexHull.area();
	}

	public double Alq_Aer() {
		return m_largestQuad.area() / m_enclosingRect.area();
	}

	public double Alt_Alq() {
		return m_largestTriangle.area() / m_largestQuad.area();
	}

	// Perimeter ratios
	public double Plt_Pch() {
		return m_largestTriangle.perimeter() / m_convexHull.perimeter();
	}

	public double Pch_Per() {
		return m_convexHull.perimeter() / m_enclosingRect.perimeter();
	}

	public double Plt_Per() {
		return m_largestTriangle.perimeter() / m_enclosingRect.perimeter();
	}

	public double Pch_Pbb() {
		return m_convexHull.perimeter() / m_boundingBox.perimeter();
	}

	public double Plt_Pbb() {
		return m_largestTriangle.perimeter() / m_boundingBox.perimeter();
	}

	public double Plq_Pch() {
		return m_largestQuad.perimeter() / m_convexHull.perimeter();
	}

	public double Plq_Per() {
		return m_largestQuad.perimeter() / m_enclosingRect.perimeter();
	}

	public double Plt_Plq() {
		return m_largestTriangle.perimeter() / m_largestQuad.perimeter();
	}

	private void computeConvexHull() {
		m_convexHull = new Polygon();
		List<Point> ordedPoints = new ArrayList<Point>();
		Point min;
		int np, i;
		List<Point> pts;
		min = findLowest();
		ordedPoints = ordPoints(min);
		np = ordedPoints.size();
		m_convexHull.add(ordedPoints.get(np - 1));
		m_convexHull.add(ordedPoints.get(0));
		i = 1;
		pts = m_convexHull;
		int nc = m_convexHull.size();
		while (np > 2 && i < np) {
			if (left(pts.get(nc - 2), pts.get(nc - 1), ordedPoints.get(i))) {
				m_convexHull.add(ordedPoints.get(i));
				i++;
				nc++;
			} else {
				m_convexHull.remove(m_convexHull.size() - 1);
				nc--;
			}
		}
		m_convexHull = filterConvexHull(); // reduce the number of points
	}

	private void computeBoundingBox() {
		m_boundingBox = new Polygon();
		List<Point> pts = m_convexHull;
		int np = m_convexHull.size();
		double x1, x2, y1, y2;
		x1 = x2 = pts.get(0).getX();
		y1 = y2 = pts.get(0).getY();
		for (int i = 0; i < np; i++) {
			if (pts.get(i).getX() < x1)
				x1 = pts.get(i).getX();
			if (pts.get(i).getX() > x2)
				x2 = pts.get(i).getX();
			if (pts.get(i).getY() < y1)
				y1 = pts.get(i).getY();
			if (pts.get(i).getY() > y2)
				y2 = pts.get(i).getY();
		}
		// Tranfer the points to a polygon
		m_boundingBox.add(new Point(x1, y1));
		m_boundingBox.add(new Point(x2, y1));
		m_boundingBox.add(new Point(x2, y2));
		m_boundingBox.add(new Point(x1, y2));
		m_boundingBox.add(new Point(x1, y1));
	}

	private void computeLargestTriangle() {
		int ia, ib, ic, i;
		int ripa = 0, ripb = 0, ripc = 0; // indexes of rooted triangle
		double area, triArea;
		int numPts = m_convexHull.size();
		List<Point> pts = m_convexHull;

		if (numPts <= 3) {
			m_largestTriangle = new Polygon();
			for (i = 0; i < numPts; i++)
				m_largestTriangle.add(pts.get(i));
			for (i = numPts; i < 4; i++)
				m_largestTriangle.add(pts.get(0));
			return;
		}

		// computes one rooted triangle with root in the first point of the
		// convex hull
		ia = 0;
		area = 0;
		triArea = 0;
		for (ib = 1; ib <= numPts - 2; ib++) {
			if (ib >= 2)
				ic = ib + 1;
			else
				ic = 2;
			List<Object> result = compRootedTri(pts, ia, ib, ic, numPts);
			area = (Double) result.get(0);
			ic = (Integer) result.get(1);
			if (area > triArea) {
				triArea = area;
				ripa = ia;
				ripb = ib;
				ripc = ic;
			}
		} // ripa, ripb and ripc are the indexes of the points of the rooted
			// triangle

		// computes other triangles and choose the largest one
		double finalArea = triArea;
		int pf0, pf1, pf2; // indexes of the final points
		int fipa = 0, fipb = 0, fipc = 0;
		int ib0;
		pf0 = ripa;
		pf1 = ripb;
		pf2 = ripc;

		for (ia = ripa + 1; ia <= ripb; ia++) {
			triArea = 0;
			if (ia == ripb)
				ib0 = ripb + 1;
			else
				ib0 = ripb;
			area = 0;
			for (ib = ib0; ib <= ripc; ib++) {
				if (ib == ripc)
					ic = ripc + 1;
				else
					ic = ripc;
				List<Object> result = compRootedTri(pts, ia, ib, ic, numPts);
				area = (Double) result.get(0);
				ic = (Integer) result.get(1);
				if (area > triArea) {
					triArea = area;
					fipa = ia;
					fipb = ib;
					fipc = ic;
				}
			}
			if (triArea > finalArea) {
				finalArea = triArea;
				pf0 = fipa;
				pf1 = fipb;
				pf2 = fipc;
			}
		}

		// Tranfer the points to a polygon
		m_largestTriangle = new Polygon();
		m_largestTriangle.add(pts.get(pf0));
		m_largestTriangle.add(pts.get(pf1));
		m_largestTriangle.add(pts.get(pf2));
		m_largestTriangle.add(pts.get(pf0));
	}

	private void computeLargestQuad() {
		int i, ia, ib, ic, ic0;
		int ripa = 0, ripb = 0, ripc = 0; // indexes for rooted triangle
		double area, triArea;
		int numPts = m_convexHull.size();
		List<Point> pts = m_convexHull;

		if (numPts <= 4) {
			m_largestQuad = new Polygon();
			for (i = 0; i < numPts; i++)
				m_largestQuad.add(pts.get(i));
			for (i = numPts; i < 5; i++)
				m_largestQuad.add(pts.get(0));
			return;
		}

		// computes one rooted triangle
		ia = 0;
		area = 0;
		triArea = 0;
		for (ib = 1; ib <= numPts - 2; ib++) {
			if (ib >= 2)
				ic = ib + 1;
			else
				ic = 2;
			List<Object> result = compRootedTri(pts, ia, ib, ic, numPts);
			area = (Double) result.get(0);
			ic = (Integer) result.get(1);
			if (area > triArea) {
				triArea = area;
				ripa = ia;
				ripb = ib;
				ripc = ic;
			}
		}

		// computes the rooted quadrilateral based on a rooted triangle
		int fipa = 0, fipb = 0, fipc = 0, fipd = 0; // indexes for final values
		int id, ib0;
		double quadArea;

		quadArea = 0;
		for (ib = ripa + 1; ib <= ripb; ib++) {
			if (ib == ripb)
				ic0 = ripb + 1;
			else
				ic0 = ripb;
			for (ic = ic0; ic <= ripc; ic++) {
				if (ic == ripc)
					id = ripc + 1;
				else
					id = ripc;
				List<Object> result = compRootedQuad(pts, ia, ib, ic, id,
						numPts);
				area = (Double) result.get(0);
				id = (Integer) result.get(1);
				if (area > quadArea) {
					quadArea = area;
					fipa = ia;
					fipb = ib;
					fipc = ic;
					fipd = id;
				}
			}
		}

		// computes other quadrilaterals and choose the largest one
		int pf0, pf1, pf2, pf3;
		double finalArea = quadArea;
		pf0 = fipa;
		pf1 = fipb;
		pf2 = fipc;
		pf3 = fipd;
		ripa = fipa;
		ripb = fipb;
		ripc = fipc;
		int ripd = fipd;

		for (ia = ripa + 1; ia <= ripb; ia++) {
			if (ia == ripb)
				ib0 = ripb + 1;
			else
				ib0 = ripb;

			quadArea = 0;
			area = 0;
			for (ib = ib0; ib <= ripc; ib++) {
				if (ib == ripc)
					ic0 = ripc + 1;
				else
					ic0 = ripc;
				for (ic = ic0; ic <= ripd; ic++) {
					if (ic == ripd)
						id = ripd + 1;
					else
						id = ripd;
					List<Object> result = compRootedQuad(pts, ia, ib, ic, id,
							numPts);
					area = (Double) result.get(0);
					id = (Integer) result.get(1);
					if (area > quadArea) {
						quadArea = area;
						fipa = ia;
						fipb = ib;
						fipc = ic;
						fipd = id;
					}
				}
			}
			if (quadArea > finalArea) {
				finalArea = quadArea;
				pf0 = fipa;
				pf1 = fipb;
				pf2 = fipc;
				pf3 = fipd;
			}
		}

		// Tranfer the points to a polygon
		m_largestQuad = new Polygon();
		m_largestQuad.add(pts.get(pf0));
		m_largestQuad.add(pts.get(pf1));
		m_largestQuad.add(pts.get(pf2));
		m_largestQuad.add(pts.get(pf3));
		m_largestQuad.add(pts.get(pf0));
	}

	private void computeEnclosingRect() {
		int num = m_convexHull.size();
		List<Point> pts = m_convexHull;
		double minx = 0, miny = 0, maxx = 0, maxy = 0;
		int minxp = 0, minyp = 0, maxxp = 0, maxyp = 0;
		double ang, dis;
		double xx, yy;
		double area = 0, min_area = 0;
		double p1x = 0, p1y = 0, p2x = 0, p2y = 0, p3x = 0, p3y = 0, p4x = 0, p4y = 0;

		if (num < 2) { // is just a point
			m_enclosingRect = new Polygon();
			m_enclosingRect.add(pts.get(0));
			m_enclosingRect.add(pts.get(0));
			m_enclosingRect.add(pts.get(0));
			m_enclosingRect.add(pts.get(0));
			m_enclosingRect.add(pts.get(0));
		} else if (num < 3) { // is a line with just two points
			m_enclosingRect = new Polygon();
			m_enclosingRect.add(pts.get(0));
			m_enclosingRect.add(pts.get(1));
			m_enclosingRect.add(pts.get(1));
			m_enclosingRect.add(pts.get(0));
			m_enclosingRect.add(pts.get(0));
		} else { // ok it's normal :-)
			for (int i = 0; i < num - 1; i++) {
				for (int a = 0; a < num; a++) {
					Line2D v1 = new Line2D.Double(pts.get(i).getX(), pts.get(i)
							.getY(), pts.get(i + 1).getX(), pts.get(i + 1)
							.getY());
					Line2D v2 = new Line2D.Double(pts.get(i).getX(), pts.get(i)
							.getY(), pts.get(a).getX(), pts.get(a).getY());
					ang = angle(v1, v2);

					dis = v2.getP1().distance(v2.getP2());
					xx = dis * Math.cos(ang);
					yy = dis * Math.sin(ang);

					if (a == 0) {
						minx = maxx = xx;
						miny = maxy = yy;
						minxp = maxxp = minyp = maxyp = 0;
					}
					if (xx < minx) {
						minxp = a;
						minx = xx;
					}
					if (xx > maxx) {
						maxxp = a;
						maxx = xx;
					}
					if (yy < miny) {
						minyp = a;
						miny = yy;
					}
					if (yy > maxy) {
						maxyp = a;
						maxy = yy;
					}
				}
				Point p1 = closest(pts.get(i), pts.get(i + 1), pts.get(minxp));
				Point p2 = closest(pts.get(i), pts.get(i + 1), pts.get(maxxp));

				Point paux = new Point(pts.get(i).getX() + 100, pts.get(i)
						.getY());
				Line2D v3 = new Line2D.Double(pts.get(i).getX(), pts.get(i)
						.getY(), paux.getX(), paux.getY());
				Line2D v4 = new Line2D.Double(pts.get(i).getX(), pts.get(i)
						.getY(), pts.get(i + 1).getX(), pts.get(i + 1).getY());
				ang = angle(v3, v4);

				Point paux1 = new Point(p1.getX() + 100
						* Math.cos(ang + M_PI_2), p1.getY() + 100
						* Math.sin(ang + M_PI_2));
				Point paux2 = new Point(p2.getX() + 100
						* Math.cos(ang + M_PI_2), p2.getY() + 100
						* Math.sin(ang + M_PI_2));

				Point p3 = closest(p2, paux2, pts.get(maxyp));
				Point p4 = closest(p1, paux1, pts.get(maxyp));

				area = quadArea(p1, p2, p3, p4);

				if ((i == 0) || (area < min_area)) {
					min_area = area;
					p1x = p1.getX();
					p1y = p1.getY();
					p2x = p2.getX();
					p2y = p2.getY();
					p3x = p3.getX();
					p3y = p3.getY();
					p4x = p4.getX();
					p4y = p4.getY();
				}
			}
			m_enclosingRect = new Polygon();
			m_enclosingRect.add(new Point(p1x, p1y));
			m_enclosingRect.add(new Point(p2x, p2y));
			m_enclosingRect.add(new Point(p3x, p3y));
			m_enclosingRect.add(new Point(p4x, p4y));
			m_enclosingRect.add(new Point(p1x, p1y));
		}
	}

	private Point findLowest() {
		Point min;
		int np, p;
		List<Point> pts;
		min = m_points.get(0);
		np = m_points.size();
		pts = m_points;
		for (p = 0; p < np; p++)
			if (pts.get(p).getY() < min.getY())
				min = pts.get(p);
			else if (pts.get(p).getY() == min.getY()
					&& pts.get(p).getX() > min.getX())
				min = pts.get(p);
		return min;
	}

	private Polygon filterConvexHull() {
		Polygon convexHull2 = new Polygon();
		int np, i;
		List<Point> pts;
		Point pt, pti;
		pts = m_convexHull;
		np = m_convexHull.size();
		pt = pts.get(0);
		convexHull2.add(pt);
		for (i = 1; i < np; i++) {
			pti = pts.get(i);
			if (pt.distance(pti) > 5) {
				convexHull2.add(pti);
				pt = pti;
			} else if (i == np - 1)
				convexHull2.add(pts.get(0));
		}
		return convexHull2;
	}

	private List<Point> ordPoints(Point min) {
		int np, p;
		List<Point> pts;
		Point ptr;
		double ang;
		Map<Double, Point> angMap = new TreeMap<Double, Point>();
		angMap.put(0.0, min);

		np = m_points.size();
		pts = m_points;
		for (p = 0; p < np; p++) {
			ang = theta(min, pts.get(p));
			ptr = angMap.get(ang);
			if (ptr != null) {
				// there is another point with the same angle
				// so we compute the distance and save the big one.
				if (min.distance(pts.get(p)) > min.distance(ptr))
					angMap.put(ang, pts.get(p));
			} else
				angMap.put(ang, pts.get(p));
		}
		return new ArrayList<Point>(angMap.values());
	}

	private double theta(Point p, Point q) {
		int dx = (int) q.getX() - (int) p.getX();
		int ax = Math.abs(dx);
		int dy = (int) q.getY() - (int) p.getY();
		int ay = Math.abs(dy);
		double t = (ax + ay == 0) ? 0 : (double) dy / (ax + ay);
		if (dx < 0)
			t = 2 - t;
		else if (dy < 0)
			t = 4 + t;
		return t * 90;
	};

	private boolean left(Point a, Point b, Point c) {
		return (a.getX() * b.getY() - a.getY() * b.getX() + a.getY() * c.getX()
				- a.getX() * c.getY() + b.getX() * c.getY() - c.getX()
				* b.getY()) > 0;
	}

	private List<Object> compRootedTri(List<Point> pts, int ripa, int ripb,
			int ripc, int np) {
		List<Object> result = new ArrayList<Object>();
		Point pa, pb, pc;
		int ia, ib, ic;
		double area, _trigArea = 0;

		// computes one rooted triangle
		ia = ripa;
		ib = ripb;
		for (ic = ripc; ic < np - 1; ic++) {
			pa = pts.get(ia);
			pb = pts.get(ib);
			pc = pts.get(ic);
			if ((area = triangleArea(pa, pb, pc)) > _trigArea) {
				ripc = ic;
				_trigArea = area;
			} else {
				break;
			}
		}
		result.add(_trigArea);
		result.add(ripc);
		return result;
	}

	private List<Object> compRootedQuad(List<Point> pts, int ripa, int ripb,
			int ripc, int ripd, int np) {
		Point pa, pb, pc, pd;
		int id;

		double area, _trigArea = 0;

		// computes one rooted triangle
		pa = pts.get(ripa);
		pb = pts.get(ripb);
		pc = pts.get(ripc);
		for (id = ripd; id < np - 1; id++) {
			pd = pts.get(id);
			if ((area = quadArea(pa, pb, pc, pd)) > _trigArea) {
				ripd = id;
				_trigArea = area;
			} else {
				break;
			}
		}
		List<Object> result = new ArrayList<Object>();
		result.add(_trigArea);
		result.add(ripd);
		return result;
	}

	private double triangleArea(Point p1, Point p2, Point p3) {
		double area = p1.getX() * p2.getY() - p2.getX() * p1.getY();
		area += p2.getX() * p3.getY() - p3.getX() * p2.getY();
		area += p3.getX() * p1.getY() - p1.getX() * p3.getY();

		return Math.abs(area / 2.0);
	}

	private double quadArea(Point p1, Point p2, Point p3, Point p4) {
		double area = p1.getX() * p2.getY() - p2.getX() * p1.getY();
		area += p2.getX() * p3.getY() - p3.getX() * p2.getY();
		area += p3.getX() * p4.getY() - p4.getX() * p3.getY();
		area += p4.getX() * p1.getY() - p1.getX() * p4.getY();

		return Math.abs(area / 2.0);
	}

	private double angle(Line2D a, Line2D b) {
		return Math.atan2(cross(a, b), dot(a, b));
	}

	private double cross(Line2D a, Line2D b) {
		double dx1 = a.getP2().getX() - a.getP1().getX(), dx2 = b.getP2()
				.getX() - b.getP1().getX();
		double dy1 = a.getP2().getY() - a.getP1().getY(), dy2 = b.getP2()
				.getY() - b.getP1().getY();
		return dx1 * dy2 - dy1 * dx2;
	}

	private double dot(Line2D a, Line2D b) {
		double dx1 = a.getP2().getX() - a.getP1().getX(), dx2 = b.getP2()
				.getX() - b.getP1().getX();
		double dy1 = a.getP2().getY() - a.getP1().getY(), dy2 = b.getP2()
				.getY() - b.getP1().getY();
		return dx1 * dx2 + dy1 * dy2;
	}

	private Point closest(Point p1, Point p2, Point p3) {
		double d = p2.getX() - p1.getX();

		if (d == 0)
			return new Point(p1.getX(), p3.getY());

		if (p1 == p3)
			return p3;

		if (p2 == p3)
			return p3;

		double m = (p2.getY() - p1.getY()) / d;

		if (m == 0)
			return new Point(p3.getX(), p1.getY());

		double b1 = p2.getY() - m * p2.getX(), b2 = p3.getY() + 1 / m
				* p3.getX(), x = (b2 - b1) / (m + 1 / m), y = m * x + b1;

		return new Point(x, y);
	}

	private double hMovement() {
		int np;
		List<Point> pts;
		double mov;

		mov = 0;

		np = m_points.size();
		pts = m_points;
		for (int p = 0; p < np - 1; p++) {
			mov += Math.abs((double) pts.get(p + 1).getX()
					- (double) pts.get(p).getX());
		}

		return mov;
	}

	private double vMovement() {
		int np;
		List<Point> pts;
		double mov;

		mov = 0;
		np = m_points.size();
		pts = m_points;
		for (int p = 0; p < np - 1; p++) {
			mov += Math.abs((double) pts.get(p + 1).getY()
					- (double) pts.get(p).getY());
		}
		return mov;
	}

	private Polygon smallTriangle() {
		Polygon tri = new Polygon();
		List<Point> points;
		Point p1, p2, p3;
		Point m1, m2, m3;
		Point t1, t2, t3;

		points = m_largestTriangle;

		p1 = points.get(0);
		p2 = points.get(1);
		p3 = points.get(2);

		m1 = new Point(p3.getX() + (p1.getX() - p3.getX()) / 2, p3.getY()
				+ (p1.getY() - p3.getY()) / 2);
		m2 = new Point(p1.getX() + (p2.getX() - p1.getX()) / 2, p1.getY()
				+ (p2.getY() - p1.getY()) / 2);
		m3 = new Point(p2.getX() + (p3.getX() - p2.getX()) / 2, p2.getY()
				+ (p3.getY() - p2.getY()) / 2);

		t1 = new Point((int) (m3.getX() + (p1.getX() - m3.getX()) * 0.6),
				(int) (m3.getY() + (p1.getY() - m3.getY()) * 0.6));
		t2 = new Point((int) (m1.getX() + (p2.getX() - m1.getX()) * 0.6),
				(int) (m1.getY() + (p2.getY() - m1.getY()) * 0.6));
		t3 = new Point((int) (m2.getX() + (p3.getX() - m2.getX()) * 0.6),
				(int) (m2.getY() + (p3.getY() - m2.getY()) * 0.6));

		tri.add(t1);
		tri.add(t2);
		tri.add(t3);
		tri.add(t1);

		return tri;
	}

	private int ptsInSmallTri() {
		// CIPolygon *inP = new CIPolygon();
		Polygon tri = smallTriangle();
		List<Point> points = tri;
		Point[] pt = new Point[3]; // points of the triangle
		Point cp; // current point of the scribble
		double[] m = new double[3];
		double dx, dy;
		double[] x = new double[3];
		int i, inter;
		int np;
		List<Point> pts;
		int empty = 0; // number of points inside the triangle

		for (i = 0; i < 3; i++)
			pt[i] = points.get(i); // just to be faster!

		for (i = 0; i < 3; i++) {
			dx = pt[i].getX() - pt[(i + 1) % 3].getX();
			if (dx == 0) {
				m[i] = BIG;
				continue;
			}
			dy = pt[i].getY() - pt[(i + 1) % 3].getY();
			m[i] = dy / dx;
		}

		// Computation of the number of points of the scribble inside the
		// triangle

		np = m_points.size();
		pts = m_points;
		for (int p = 0; p < np; p++) {
			cp = pts.get(p);
			inter = 0;
			if (cp.getX() >= pt[0].getX() && cp.getX() >= pt[1].getX()
					&& cp.getX() >= pt[2].getX())
				continue;
			else if (cp.getX() <= pt[0].getX() && cp.getX() <= pt[1].getX()
					&& cp.getX() <= pt[2].getX())
				continue;
			else if (cp.getY() >= pt[0].getY() && cp.getY() >= pt[1].getY()
					&& cp.getY() >= pt[2].getY())
				continue;
			else if (cp.getY() <= pt[0].getY() && cp.getY() <= pt[1].getY()
					&& cp.getY() <= pt[2].getY())
				continue;
			else {
				for (i = 0; i < 3; i++) {
					if (m[i] == 0)
						continue;
					else if (m[i] >= BIG) {
						x[i] = pt[i].getX();
						if (x[i] >= cp.getX())
							inter++;
					} else {
						x[i] = (double) (cp.getY() - pt[i].getY() + m[i]
								* pt[i].getX())
								/ m[i];
						if (x[i] >= cp.getX()
								&& (x[i] < ((pt[i].getX() > pt[(i + 1) % 3]
										.getX()) ? pt[i].getX()
										: pt[(i + 1) % 3].getX())))
							inter++;
					}
				}
				if ((inter % 2) != 0)
					empty++;
			}
		}

		return empty;
	}
}
