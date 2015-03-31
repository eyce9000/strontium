package srl.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openawt.geom.Line2D;

import srl.core.sketch.Point;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * Performs orthogonal regression on a set of points
 * 
 * @author pcorey
 * 
 */
public class OrthogonalRegression {

	/**
	 * Returns the best fit line to the set of points
	 * 
	 * @param points
	 *            The points to fit
	 * @return A Line2D fit to the points
	 */
	public static Line2D getLineFit(List<Point> points) {
		double xBar = 0;
		double yBar = 0;
		for (Point p : points) {
			xBar += p.getX();
			yBar += p.getY();
		}
		xBar /= points.size();
		yBar /= points.size();

		Matrix M = new Matrix(points.size(), 2);
		for (int i = 0; i < points.size(); i++) {
			M.set(i, 0, points.get(i).getX() - xBar);
			M.set(i, 1, points.get(i).getY() - yBar);
		}
		SingularValueDecomposition svd = M.svd();
		Matrix S = svd.getS();
		Matrix V = svd.getV();
		int index = 0;
		for (int i = 1; i < S.getColumnDimension(); i++)
			if (S.get(i, i) < S.get(index, index))
				index = i;
		double lineNormalDirection = Math.atan2(V.get(1, index),
				V.get(0, index));
		double lineDirection = lineNormalDirection - Math.PI / 2;
		double direction = Math.atan2(-M.get(0, 1), -M.get(0, 0));
		if (Math.abs(direction - lineDirection) > Math.PI / 2)
			lineDirection = lineDirection - Math.PI;
		return new Line2D.Double(xBar, yBar, xBar + Math.cos(lineDirection),
				yBar + Math.sin(lineDirection));
	}

	public static void main(String[] args) {
		List<Point> points = new ArrayList<Point>();
		Random rand = new Random();
		for (int i = 0; i < 10; i++) {

			points.add(new Point(i + (rand.nextDouble() - .5) / 100.0, i
					+ (rand.nextDouble() - .5) / 100.0));
		}
		Line2D l = getLineFit(points);
		double direction = Math.atan2(l.getY2() - l.getY1(),
				l.getX2() - l.getX1());
		System.out.println(Math.toDegrees(direction));
	}
}
