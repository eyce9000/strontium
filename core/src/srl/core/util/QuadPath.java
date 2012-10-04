package srl.core.util;


import org.openawt.geom.GeneralPath;
import org.openawt.geom.Line2D;
import org.openawt.geom.Point2D;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;


public class QuadPath {
	private static final double skew = 0.5;

	public static GeneralPath buildQuadPath(Stroke s) {
		GeneralPath path;

		if (s.getNumPoints() < 3) {
			path = new GeneralPath();
			path.moveTo(s.getFirstPoint().getX(), s.getFirstPoint().getY());

			for (int i = 1; i < s.getNumPoints(); i++) {
				Point p2 = s.getPoints().get(i);
				path.lineTo(p2.getX(), p2.getY());
			}
		} else {
			// Fitting curve code taken from
			// http://forums.sun.com/thread.jspa?threadID=5302199
			int n = s.getNumPoints() - 1;
			// Initialize arrays.
			Line2D.Double[] tangents = new Line2D.Double[n - 1];
			Point2D.Double[] ctrls = new Point2D.Double[n];
			for (int j = 0; j < tangents.length; j++) {
				tangents[j] = new Line2D.Double();
			}
			for (int j = 0; j < ctrls.length; j++) {
				ctrls[j] = new Point2D.Double();
			}

			path = new GeneralPath();
			path.moveTo(s.getFirstPoint().getX(), s.getFirstPoint().getY());

			// Set tangent lines.
			for (int j = 1, k = 0; j < n; j++, k = j - 1) {
				double theta = getAngularDifference(s, j);
				theta *= skew;
				tangents[j - 1] = getTangentLine(s, j, theta);
			}

			// Set control points and advance path to next point.
			for (int j = 0; j < n; j++) {
				Point2D.Double ctrl = getCtrlPoint(s, tangents, j);
				ctrls[j].setLocation(ctrl.x, ctrl.y);

				Point2D.Double midpoint = new Point2D.Double((s.getPoint(j)
						.getX() + s.getPoint(j + 1).getX()) / 2, (s.getPoint(j)
						.getY() + s.getPoint(j + 1).getY()) / 2);
				double distance = Math.sqrt(Math.pow(midpoint.x - ctrl.x, 2)
						+ Math.pow(midpoint.y - ctrl.y, 2));
				double radius = Math.sqrt(Math.pow(
						midpoint.x - s.getPoint(j + 1).getX(), 2)
						+ Math.pow(midpoint.y - s.getPoint(j + 1).getY(), 2));

				if (distance <= radius)
					path.quadTo(ctrl.x, ctrl.y, s.getPoint(j + 1).getX(), s
							.getPoint(j + 1).getY());
				else
					path.lineTo(s.getPoint(j + 1).getX(), s.getPoint(j + 1)
							.getY());
			}
		}

		return path;
	}

	private static double getAngularDifference(Stroke s, int n) {
		double dy = s.getPoint(n + 1).getY() - s.getPoint(n).getY();
		double dx = s.getPoint(n + 1).getX() - s.getPoint(n).getX();
		double theta2 = Math.atan2(dy, dx);
		dy = s.getPoint(n).getY() - s.getPoint(n - 1).getY();
		dx = s.getPoint(n).getX() - s.getPoint(n - 1).getX();
		double theta1 = Math.atan2(dy, dx);
		return theta2 - theta1;
	}

	private static Line2D.Double getTangentLine(Stroke s, int n, double relTheta) {
		double d = 200;
		double dy = s.getPoint(n).getY() - s.getPoint(n - 1).getY();
		double dx = s.getPoint(n).getX() - s.getPoint(n - 1).getX();
		double theta1 = Math.atan2(dy, dx);
		double theta = theta1 + relTheta;
		double x1 = s.getPoint(n).getX() + d * Math.cos(theta - Math.PI);
		double y1 = s.getPoint(n).getY() + d * Math.sin(theta - Math.PI);
		double x2 = s.getPoint(n).getX() + d * Math.cos(theta);
		double y2 = s.getPoint(n).getY() + d * Math.sin(theta);
		return new Line2D.Double(x1, y1, x2, y2);
	}

	private static Point2D.Double getCtrlPoint(Stroke s,
			Line2D.Double[] tangents, int n) {
		if (n <= 1 || n == s.getNumPoints() - 2) {
			double d = 200;
			int index = (n == 0) ? 0 : n - 1;
			double tangentTheta = getTheta(tangents[index]);
			double lineTheta = getTheta(
					new Point2D.Double(s.getPoint(n).getX(), s.getPoint(n)
							.getY()),
					new Point2D.Double(s.getPoint(n + 1).getX(), s.getPoint(
							n + 1).getY()));
			// Find center of line between points[n] and points[n+1].
			double dist = s.getPoint(n).distance(s.getPoint(n + 1)) / 2;
			double cx = s.getPoint(n).getX() + dist * Math.cos(lineTheta);
			double cy = s.getPoint(n).getY() + dist * Math.sin(lineTheta);
			// Construct perpendicular from cx, cy to intersect tangents[index].
			double phi = lineTheta - Math.PI / 2;
			double x = cx + d * Math.cos(phi);
			double y = cy + d * Math.sin(phi);
			Line2D.Double line = new Line2D.Double(cx, cy, x, y);
			return getIntersection(tangents[index], line);
		} else {
			return getIntersection(tangents[n - 1], tangents[n]);
		}
	}

	private static double getTheta(Line2D.Double line) {
		Point2D.Double p1 = new Point2D.Double(line.x1, line.y1);
		Point2D.Double p2 = new Point2D.Double(line.x2, line.y2);
		return getTheta(p1, p2);
	}

	private static double getTheta(Point2D.Double p1, Point2D.Double p2) {
		double dy = p2.y - p1.y;
		double dx = p2.x - p1.x;
		return Math.atan2(dy, dx);
	}

	/** http://mathworld.wolfram.com/Line-LineIntersection.html */
	private static Point2D.Double getIntersection(Line2D.Double line1,
			Line2D.Double line2) {
		double x1 = line1.x1, y1 = line1.y1;
		double x2 = line1.x2, y2 = line1.y2;
		double x3 = line2.x1, y3 = line2.y1;
		double x4 = line2.x2, y4 = line2.y2;

		Point2D.Double p = new Point2D.Double();
		double determinant = getDeterminant(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
		double d1 = getDeterminant(x1, y1, x2, y2);
		double d2 = getDeterminant(x3, y3, x4, y4);
		p.x = getDeterminant(d1, x1 - x2, d2, x3 - x4) / determinant;
		d1 = getDeterminant(x1, y1, x2, y2);
		d2 = getDeterminant(x3, y3, x4, y4);
		p.y = getDeterminant(d1, y1 - y2, d2, y3 - y4) / determinant;
		return p;
	}

	private static double getDeterminant(double a, double b, double c, double d) {
		return a * d - b * c;
	}
}
