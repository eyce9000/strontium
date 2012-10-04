package srl.segmentation.combination.objectiveFunctions;

import org.openawt.geom.Line2D;
import java.util.Collections;
import java.util.List;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;
import srl.math.FeatureArea;


/**
 * Objective function for the FSS Combination algorithm that uses a feature area
 * error polyline fit
 * 
 * @author awolin
 */
public class PolylineFeatureAreaObjectiveFunction implements IObjectiveFunction {

	/**
	 * Default constructor
	 */
	public PolylineFeatureAreaObjectiveFunction() {
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

		for (int c = 1; c < corners.size(); c++) {

			Point corner1 = stroke.getPoint(corners.get(c - 1));
			Point corner2 = stroke.getPoint(corners.get(c));

			Line2D.Double optimalLine = new Line2D.Double();
			optimalLine.setLine(corner1.getX(), corner1.getY(), corner2.getX(),
					corner2.getY());

			List<Point> actualSegment = stroke.getPoints().subList(
					corners.get(c - 1), corners.get(c));

			totalError += FeatureArea.toLine(actualSegment, optimalLine);
		}

		return totalError;
	}
}
