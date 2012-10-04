package srl.segmentation.combination.objectiveFunctions;

import org.openawt.geom.Line2D;
import java.util.Collections;
import java.util.List;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;
import srl.math.LeastSquares;


/**
 * Objective function for the FSS Combination algorithm that uses a mean-squared
 * error polyline fit
 * 
 * @author awolin
 */
public class PolylineMSEObjectiveFunction implements IObjectiveFunction {

	/**
	 * Default constructor
	 * 
	 */
	public PolylineMSEObjectiveFunction() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.segmentation.combination.objectiveFunctions.IObjectiveFunction
	 * #solve(java.util.List, edu.tamu.core.sketch.Stroke)
	 */
	public double solve(List<Integer> corners, Stroke stroke) {

		Collections.sort(corners);
		double totalError = 0.0;
		double numPoints = 0.0;

		for (int c = 1; c < corners.size(); c++) {

			Point corner1 = stroke.getPoint(corners.get(c - 1));
			Point corner2 = stroke.getPoint(corners.get(c));

			Line2D.Double optimalLine = new Line2D.Double();
			optimalLine.setLine(corner1.getX(), corner1.getY(), corner2.getX(),
					corner2.getY());

			List<Point> actualSegment = stroke.getPoints().subList(
					corners.get(c - 1), corners.get(c));
			numPoints += actualSegment.size();

			totalError += LeastSquares.squaredError(actualSegment, optimalLine);
			// totalError += LeastSquares.error(actualSegment, optimalLine);
		}

		// Abs err threshold:
		// Average threshold = 2.349464086146307
		// Unbiased threshold estimate = 2.9299659220609895
		// Accuracy using avg. threshold on entire dataset = 0.9212962962962963
		// Accuracy using unbiased threshold on entire dataset =
		// 0.8703703703703703

		totalError = totalError / numPoints;

		// totalError = Math.sqrt(totalError);

		return totalError;
	}
}
