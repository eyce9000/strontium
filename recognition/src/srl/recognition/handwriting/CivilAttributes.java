package srl.recognition.handwriting;

import java.util.ArrayList;
import java.util.List;

import org.openawt.geom.Point2D;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Point;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;



import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Generate the WEKA attribute Instance for a given sketch.
 * 
 * @author awolin
 * 
 */
public class CivilAttributes {

	/**
	 * Pixel width and height for pixelizing and scaling sketch.
	 */
	public static final int S_PIXELSIZE = 8;

	/**
	 * Generate the feature vector instance for a sketch for CivilSketch.
	 * 
	 * @param sketch
	 *            sketch to featurefy.
	 * @param dataSet
	 *            CivilSketch dataset.
	 * @return the WEKA Instance for the sketch.
	 */
	public static Instance generateCivilFeatures(Sketch sketch,
			Instances dataSet) {

		// return generateCivilFeatures_ORIG(sketch, dataSet);
		// return generateCivilFeatures_NEW(sketch, dataSet);
		return generateCivilFeatures_HYBRID(sketch, dataSet);
	}

	/**
	 * Original feature set used in the COA handwriting recognizer.
	 * 
	 * @param sketch
	 *            sketch to featurefy.
	 * @param dataSet
	 *            CivilSketch dataset.
	 * @return the WEKA Instance for the sketch.
	 */
	private static Instance generateCivilFeatures_ORIG(Sketch sketch,
			Instances dataSet) {

		Instance characterInstance = StrokePixelator.getInstance(sketch,
				S_PIXELSIZE, dataSet);

		// Stroke count
		Attribute strokeCountAtt = dataSet.attribute("StrokeCount");
		characterInstance.setValue(strokeCountAtt, sketch.getNumStrokes());

		// Bounding box ratio
		Attribute bbRatio = dataSet.attribute("BoundingBoxRatio");

		BoundingBox bb = sketch.getBoundingBox();
		if (bb.getWidth() > 0) {
			characterInstance.setValue(bbRatio, bb.getHeight() / bb.getWidth());
		} else {
			characterInstance.setValue(bbRatio, bb.getHeight() / 1.0);
		}

		return characterInstance;
	}

	/**
	 * New features, based partly on Rubine's features.
	 * 
	 * @param sketch
	 *            sketch to featurefy.
	 * @param dataSet
	 *            CivilSketch dataset.
	 * @return the WEKA Instance for the sketch.
	 */
	private static Instance generateCivilFeatures_NEW(Sketch sketch,
			Instances dataSet) {

		Instance inst = new DenseInstance(dataSet.numAttributes());
		inst.setDataset(dataSet);

		// Rubine-esque features
		double smoothness = 0.0;

		for (Stroke stroke : sketch.getStrokes()) {
			List<Point> cleanedPoints = cleanStroke(stroke);

			int numPoints = cleanedPoints.size();

			// Calculate intersegment distance changes
			Point2D.Double[] distDeltas = new Point2D.Double[numPoints];
			for (int p = 0; p < numPoints - 1; p++) {
				distDeltas[p] = distChangeAtPoint(p, cleanedPoints);
			}

			// Calculate intersegment angle changes
			double[] angleDeltas = new double[numPoints];
			for (int p = 1; p < numPoints - 1; p++) {
				angleDeltas[p] = angleChangeAtPoint(p, distDeltas);
			}

			smoothness += smoothness(angleDeltas);
		}

		// Smoothness
		Attribute smoothnessAttr = dataSet.attribute("Smoothness");
		inst.setValue(smoothnessAttr, smoothness);

		// First-last point attrs
		Point firstPt = sketch.getFirstStroke().getFirstPoint();
		Point lastPt = sketch.getLastStroke().getLastPoint();
		double firstLastPtDist = firstLastPtDist(firstPt, lastPt);

		Attribute firstLastDistAttr = dataSet.attribute("FirstLastDist");
		inst.setValue(firstLastDistAttr, firstLastPtDist);

		Attribute firstLastCosineAttr = dataSet.attribute("FirstLastCosine");
		inst.setValue(firstLastCosineAttr, firstLastCosine(firstPt, lastPt,
				firstLastPtDist));

		Attribute firstLastSineAttr = dataSet.attribute("FirstLastSine");
		inst.setValue(firstLastSineAttr, firstLastSine(firstPt, lastPt,
				firstLastPtDist));

		// Number of strokes
		Attribute strokeCount = dataSet.attribute("StrokeCount");
		inst.setValue(strokeCount, sketch.getNumStrokes());

		// Bounding box ratio
		BoundingBox bb = sketch.getBoundingBox();
		Attribute bbRatio = dataSet.attribute("BoundingBoxRatio");
		if (bb.getWidth() > 0) {
			inst.setValue(bbRatio, bb.getHeight() / bb.getWidth());
		} else {
			inst.setValue(bbRatio, bb.getHeight() / 1.0);
		}

		// Pixel-based attributes
		int numLeftPixels = 0;
		int numRightPixels = 0;
		int numTopPixels = 0;
		int numBottomPixels = 0;

		for (Point pt : sketch.getPoints()) {
			if (pt.getX() - bb.getMinX() < bb.getWidth() / 2.0) {
				numLeftPixels++;
			} else {
				numRightPixels++;
			}

			if (pt.getY() - bb.getMinY() < bb.getHeight() / 2.0) {
				numTopPixels++;
			} else {
				numBottomPixels++;
			}
		}

		int totalPixels = numLeftPixels + numRightPixels;

		// % of pixels in left half
		Attribute leftPixels = dataSet.attribute("LeftPixels");
		inst.setValue(leftPixels, (double) numLeftPixels / totalPixels);

		// % of pixels in right half
		Attribute rightPixels = dataSet.attribute("RightPixels");
		inst.setValue(rightPixels, (double) numRightPixels / totalPixels);

		// % of pixels in top half
		Attribute topPixels = dataSet.attribute("TopPixels");
		inst.setValue(topPixels, (double) numTopPixels / totalPixels);

		// % of pixels in bottom half
		Attribute bottomPixels = dataSet.attribute("BottomPixels");
		inst.setValue(bottomPixels, (double) numBottomPixels / totalPixels);

		// Density
		double pathLength = 0.0;
		for (Stroke st : sketch.getStrokes()) {
			pathLength += st.getPathLength();
		}
		double density = 1.0;
		if (bb.getArea() > 0) {
			density = pathLength / bb.getArea();
		}

		Attribute densityAttr = dataSet.attribute("Density");
		inst.setValue(densityAttr, density);

		return inst;
	}

	/**
	 * Combination of old and new features.
	 * 
	 * @param sketch
	 *            sketch to featurefy.
	 * @param dataSet
	 *            CivilSketch dataset.
	 * @return the WEKA Instance for the sketch.
	 */
	private static Instance generateCivilFeatures_HYBRID(Sketch sketch,
			Instances dataSet) {

		Instance inst = StrokePixelator.getInstance(sketch, S_PIXELSIZE,
				dataSet);

		// Rubine-esque features
		double smoothness = 0.0;

		for (Stroke stroke : sketch.getStrokes()) {
			List<Point> cleanedPoints = cleanStroke(stroke);

			int numPoints = cleanedPoints.size();

			// Calculate intersegment distance changes
			Point2D.Double[] distDeltas = new Point2D.Double[numPoints];
			for (int p = 0; p < numPoints - 1; p++) {
				distDeltas[p] = distChangeAtPoint(p, cleanedPoints);
			}

			// Calculate intersegment angle changes
			double[] angleDeltas = new double[numPoints];
			for (int p = 1; p < numPoints - 1; p++) {
				angleDeltas[p] = angleChangeAtPoint(p, distDeltas);
			}

			smoothness += smoothness(angleDeltas);
		}

		// Smoothness
		Attribute smoothnessAttr = dataSet.attribute("Smoothness");
		inst.setValue(smoothnessAttr, smoothness);

		// First-last point attrs
		Point firstPt = sketch.getFirstStroke().getFirstPoint();
		Point lastPt = sketch.getLastStroke().getLastPoint();
		double firstLastPtDist = firstLastPtDist(firstPt, lastPt);

		Attribute firstLastDistAttr = dataSet.attribute("FirstLastDist");
		inst.setValue(firstLastDistAttr, firstLastPtDist);

		Attribute firstLastCosineAttr = dataSet.attribute("FirstLastCosine");
		inst.setValue(firstLastCosineAttr, firstLastCosine(firstPt, lastPt,
				firstLastPtDist));

		Attribute firstLastSineAttr = dataSet.attribute("FirstLastSine");
		inst.setValue(firstLastSineAttr, firstLastSine(firstPt, lastPt,
				firstLastPtDist));

		// Number of strokes
		Attribute strokeCount = dataSet.attribute("StrokeCount");
		inst.setValue(strokeCount, sketch.getNumStrokes());

		// Bounding box ratio
		BoundingBox bb = sketch.getBoundingBox();
		Attribute bbRatio = dataSet.attribute("BoundingBoxRatio");
		if (bb.getWidth() > 0) {
			inst.setValue(bbRatio, bb.getHeight() / bb.getWidth());
		} else {
			inst.setValue(bbRatio, bb.getHeight() / 1.0);
		}

		// Pixel-based attributes
		int numLeftPixels = 0;
		int numRightPixels = 0;
		int numTopPixels = 0;
		int numBottomPixels = 0;

		for (Point pt : sketch.getPoints()) {
			if (pt.getX() - bb.getMinX() < bb.getWidth() / 2.0) {
				numLeftPixels++;
			} else {
				numRightPixels++;
			}

			if (pt.getY() - bb.getMinY() < bb.getHeight() / 2.0) {
				numTopPixels++;
			} else {
				numBottomPixels++;
			}
		}

		int totalPixels = numLeftPixels + numRightPixels;

		// % of pixels in left half
		Attribute leftPixels = dataSet.attribute("LeftPixels");
		inst.setValue(leftPixels, (double) numLeftPixels / totalPixels);

		// % of pixels in right half
		Attribute rightPixels = dataSet.attribute("RightPixels");
		inst.setValue(rightPixels, (double) numRightPixels / totalPixels);

		// % of pixels in top half
		Attribute topPixels = dataSet.attribute("TopPixels");
		inst.setValue(topPixels, (double) numTopPixels / totalPixels);

		// % of pixels in bottom half
		Attribute bottomPixels = dataSet.attribute("BottomPixels");
		inst.setValue(bottomPixels, (double) numBottomPixels / totalPixels);

		// Density
		double pathLength = 0.0;
		for (Stroke st : sketch.getStrokes()) {
			pathLength += st.getPathLength();
		}
		double density = 1.0;
		if (bb.getArea() > 0) {
			density = pathLength / bb.getArea();
		}

		Attribute densityAttr = dataSet.attribute("Density");
		inst.setValue(densityAttr, density);

		return inst;
	}

	/**
	 * Cleans a stroke by removing overlapping points. Also updates the time
	 * values to ensure that no two times are similar.
	 * 
	 * @param stroke
	 *            Stroke to get the non-overlapping points from
	 * @return Non-overlapping points of the stroke. These points are deep
	 *         copies of the original stroke's points and changing them will not
	 *         affect the original stroke.
	 */
	private static List<Point> cleanStroke(Stroke stroke) {

		// Create a copied version of the points
		List<Point> cleanedPoints = new ArrayList<Point>();

		for (Point p : stroke.getPoints()) {
			cleanedPoints.add((Point) p.clone());
		}

		// Remove overlapping points
		int i = 0;
		if (cleanedPoints.size() > 1) {
			do {
				double x0 = cleanedPoints.get(i).getX();
				double y0 = cleanedPoints.get(i).getY();

				double x1 = cleanedPoints.get(i + 1).getX();
				double y1 = cleanedPoints.get(i + 1).getY();

				long t0 = cleanedPoints.get(i).getTime();
				long t1 = cleanedPoints.get(i + 1).getTime();

				if (x0 == x1 && y0 == y1) {
					cleanedPoints.remove(i + 1);
				} else if (t0 == t1) {
					cleanedPoints.remove(i + 1);
				} else {
					i++;
				}
			} while (i < cleanedPoints.size() - 1);
		}

		return cleanedPoints;
	}

	/**
	 * Calculates the distance change between a point and its following point
	 * 
	 * @param p
	 *            Index of the initial point
	 * @param featurePoints
	 *            List of points to use in feature calculation
	 * @return Distance change between two points
	 */
	private static Point2D.Double distChangeAtPoint(int p, List<Point> points) {

		Point point = points.get(p);
		Point nextPoint = points.get(p + 1);

		double deltaX = nextPoint.getX() - point.getX();
		double deltaY = nextPoint.getY() - point.getY();

		return new Point2D.Double(deltaX, deltaY);
	}

	/**
	 * Calculates the angle change between a point and its following point
	 * 
	 * @param p
	 *            Index of the initial point
	 * @param featurePoints
	 *            List of points to use in feature calculation
	 * @param distDeltas
	 *            Array of distance changes between points
	 * @return Angle change between two points
	 */
	private static double angleChangeAtPoint(int p, Point2D.Double[] distDeltas) {

		double y = (distDeltas[p].getX() * distDeltas[p - 1].getY())
				- (distDeltas[p - 1].getX() * distDeltas[p].getY());
		double x = (distDeltas[p].getX() * distDeltas[p - 1].getX())
				+ (distDeltas[p].getY() * distDeltas[p - 1].getY());

		double omegaP = Math.atan2(y, x);

		return omegaP;
	}

	/**
	 * Calculates the distance between the first and last point
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @return Distance between the first and last point
	 */
	private static double firstLastPtDist(Point firstPt, Point lastPt) {

		return firstPt.distance(lastPt);
	}

	/**
	 * Calculates the cosine between the first and last point
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @param firstLastPtDist
	 *            Length between the first and last point
	 * @return Cosine between the first and last point
	 */
	private static double firstLastCosine(Point firstPt, Point lastPt,
			double firstLastPtDist) {

		if (firstLastPtDist != 0.0) {
			return (lastPt.getX() - firstPt.getX()) / firstLastPtDist;
		} else {
			return (lastPt.getX() - firstPt.getX());
		}
	}

	/**
	 * Calculates the sine between the first and last point
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @param firstLastPtDist
	 *            Length between the first and last point
	 * @return Sine between the first and last point
	 */
	private static double firstLastSine(Point firstPt, Point lastPt,
			double firstLastPtDist) {

		if (firstLastPtDist != 0.0) {
			return (lastPt.getY() - firstPt.getY()) / firstLastPtDist;
		} else {
			return (lastPt.getY() - firstPt.getY());
		}
	}

	/**
	 * Computes the sum of the squared values of the angles at each mouse point.
	 * This is a measure of smoothness.
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @param angleDeltas
	 *            Array of angle changes between points
	 * @return Sum of the squared values of the angles at each mouse point
	 */
	private static double smoothness(double[] angleDeltas) {

		double sqAngleSum = 0.0;

		for (int p = 0; p < angleDeltas.length; p++) {
			sqAngleSum += (angleDeltas[p] * angleDeltas[p]);
		}

		return sqAngleSum;
	}

}
