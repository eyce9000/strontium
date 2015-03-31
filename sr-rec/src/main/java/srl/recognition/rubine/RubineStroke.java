package srl.recognition.rubine;

import java.util.ArrayList;
import java.util.List;

import org.openawt.geom.Point2D;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Point;
import srl.core.sketch.Stroke;


/**
 * A stroke implementation that adds feature storage to the stroke
 * 
 * @author awolin
 */
public class RubineStroke extends Stroke {

	/**
	 * Feature sets available
	 */
	public enum FeatureSet {
		Rubine, Long, COA
	}

	/**
	 * Default standardize bounding box width and height
	 */
	private static final double S_STD_BOUNDING_BOX = 600.0;

	/**
	 * Should the strokes be standardized to a default bounding box width and
	 * height?
	 */
	private boolean m_standardized = false;

	/**
	 * Orient to have y-axis swapped.
	 */
	private boolean m_cartesianed = false;

	/**
	 * The feature set we use with this classifier
	 */
	private FeatureSet m_featureSet = FeatureSet.Rubine;

	/**
	 * List of features found
	 */
	private List<Double> m_features;

	/**
	 * Angle changes
	 */
	private double[] m_angleDeltas;

	/**
	 * List of feature labels found
	 */
	private List<String> m_featureLabels;

	/**
	 * Default constructor
	 */
	public RubineStroke() {
		super();
	}

	/**
	 * Constructor that takes in a stroke and finds the Rubine features
	 * 
	 * @param stroke
	 *            Stroke to calculate the Rubine features for
	 */
	public RubineStroke(Stroke stroke) {
		this(stroke, FeatureSet.Rubine);
	}

	/**
	 * Constructor that takes in a stroke and a {@link FeatureSet}.
	 * 
	 * @param stroke
	 *            Stroke to calculate the FeatureSet features for
	 * @param featureSet
	 *            Set of features
	 */
	public RubineStroke(Stroke stroke, FeatureSet featureSet) {
		this(stroke, featureSet, false);
	}

	/**
	 * Constructor that takes in a stroke and a {@link FeatureSet}.
	 * 
	 * @param stroke
	 *            Stroke to calculate the FeatureSet features for
	 * @param featureSet
	 *            Set of features
	 */
	public RubineStroke(Stroke stroke, FeatureSet featureSet,
			boolean standardized) {

		super(stroke);

		m_featureSet = featureSet;
		m_standardized = standardized;
		calcFeatures();
	}

	/**
	 * Constructor that takes in a stroke and a {@link FeatureSet}.
	 * 
	 * @param stroke
	 *            Stroke to calculate the FeatureSet features for
	 * @param featureSet
	 *            Set of features
	 * @param standardized
	 *            Whether the stroke is normalized to a (0,0) axis
	 * @param cartesian
	 *            If true, the origin is in the bottom left corner
	 */
	public RubineStroke(Stroke stroke, FeatureSet featureSet,
			boolean standardized, boolean cartesian) {

		super(stroke);

		m_featureSet = featureSet;
		m_standardized = standardized;
		m_cartesianed = cartesian;

		calcFeatures();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.patternrec.classifiers.IClassifiable#getFeatures()
	 */
	public List<Double> getFeatures() {
		return m_features;
	}

	/**
	 * Return the names of features.
	 * 
	 * @return String representations of feature labels.
	 */
	public List<String> getFeatureLabels() {
		return m_featureLabels;
	}

	/**
	 * Is the stroke standardized?
	 * 
	 * @return True if the stroke is standardized, false otherwise
	 */
	public boolean isStandardized() {
		return m_standardized;
	}

	/**
	 * Returns the feature set of our Features object
	 * 
	 * @return The feature set we use
	 */
	public FeatureSet getFeatureSet() {
		return m_featureSet;
	}

	/**
	 * Calculates the features for a gesture stroke and stores the results in a
	 * feature vector the feature vector
	 * 
	 * @param stroke
	 *            Gesture stroke
	 * @return Feature ArrayList
	 */
	private void calcFeatures() {

		// Feature ArrayList
		m_features = new ArrayList<Double>();
		m_featureLabels = new ArrayList<String>();

		// Remove overlapping points from the stroke
		List<Point> featurePoints = cleanStroke(this);

		// Standardize the stroke points
		if (m_standardized) {
			featurePoints = standardizeStrokePoints(featurePoints);
		}
		if (m_cartesianed) {
			featurePoints = transposeStrokePoints(featurePoints);
			featurePoints = swapYAxis(featurePoints, this.getBoundingBox()
					.getHeight());
		}

		int numPoints = featurePoints.size();

		// Calculate intersegment distance changes
		Point2D.Double[] distDeltas = new Point2D.Double[numPoints];
		for (int p = 0; p < numPoints - 1; p++) {
			distDeltas[p] = distChangeAtPoint(p, featurePoints);
		}

		// Calculate intersegment angle changes
		m_angleDeltas = new double[numPoints];
		for (int p = 1; p < numPoints - 1; p++) {
			m_angleDeltas[p] = angleChangeAtPoint(p, featurePoints, distDeltas);
		}

		// Calculate intersegment time changes
		long[] timeDeltas = new long[numPoints];
		for (int p = 0; p < numPoints - 1; p++) {
			timeDeltas[p] = timeChangeAtPoint(p, featurePoints);
		}

		// Get the features for the corresponding feature set
		switch (m_featureSet) {
		case Rubine:
			addRubineFeatures(featurePoints, distDeltas, m_angleDeltas,
					timeDeltas);
			break;
		case Long:
			addLongFeatures(featurePoints, distDeltas, m_angleDeltas,
					timeDeltas);
			break;
		case COA:
			addCOAFeatures(featurePoints, distDeltas, m_angleDeltas, timeDeltas);
			break;
		default:
			addRubineFeatures(featurePoints, distDeltas, m_angleDeltas,
					timeDeltas);
			break;
		}
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
	private List<Point> cleanStroke(Stroke stroke) {

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
	 * Standardize the stroke to a new bounding box size of scaledX x scaledY at
	 * the origin (0,0).
	 * 
	 * @param s
	 *            Gesture stroke
	 * @return Stroke standardized to a certain bounding box width and height
	 */
	private List<Point> standardizeStrokePoints(List<Point> origPoints) {

		// Create a copied version of the points
		List<Point> scaledPoints = new ArrayList<Point>();
		List<Point> transposedPoints = new ArrayList<Point>();

		for (Point p : origPoints) {
			scaledPoints.add((Point) p.clone());
		}

		// Find the bounding box
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		for (Point p : scaledPoints) {
			if (p.getX() < minX) {
				minX = p.getX();
			}
			if (p.getX() > maxX) {
				maxX = p.getX();
			}
			if (p.getY() < minY) {
				minY = p.getY();
			}
			if (p.getY() > maxY) {
				maxY = p.getY();
			}
		}

		// Width and height of the original stroke
		double width = maxX - minX;
		double height = maxY - minY;

		double stdWidth, stdHeight;
		if (width > height) {
			stdWidth = S_STD_BOUNDING_BOX;
			stdHeight = height * (S_STD_BOUNDING_BOX / width);
		} else {
			stdWidth = width * (S_STD_BOUNDING_BOX / height);
			stdHeight = S_STD_BOUNDING_BOX;
		}

		// Transpose the stroke to the top left corner
		for (int i = 0; i < scaledPoints.size(); i++) {
			Point point = scaledPoints.get(i);
			transposedPoints.add(new Point(point.getX() - minX, point.getY()
					- minY, point.getTime()));
		}

		scaledPoints.clear();
		// Scale everything to a standard bounding box size
		for (int i = 0; i < transposedPoints.size(); i++) {
			Point point = transposedPoints.get(i);

			double scaledX = (point.getX() / width) * stdWidth;
			double scaledY = (point.getY() / height) * stdHeight;

			scaledPoints.add(new Point(scaledX, scaledY, point.getTime()));
		}

		return scaledPoints;
	}

	/**
	 * Transpose the stroke to the origin (0,0).
	 * 
	 * @param s
	 *            Gesture stroke
	 * @return Stroke standardized to a certain bounding box width and height
	 */
	private List<Point> transposeStrokePoints(List<Point> origPoints) {

		// Create a copied version of the points
		List<Point> transposedPoints = new ArrayList<Point>();

		// Find the bounding box
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		for (Point p : origPoints) {
			if (p.getX() < minX) {
				minX = p.getX();
			}
			if (p.getX() > maxX) {
				maxX = p.getX();
			}
			if (p.getY() < minY) {
				minY = p.getY();
			}
			if (p.getY() > maxY) {
				maxY = p.getY();
			}
		}

		// Transpose the stroke to the top left corner
		for (int i = 0; i < origPoints.size(); i++) {
			Point point = origPoints.get(i);
			transposedPoints.add(new Point(point.getX() - minX, point.getY()
					- minY, point.getTime()));
		}

		return transposedPoints;
	}

	/**
	 * 
	 * @param points
	 * @param maxY
	 * @return
	 */
	private List<Point> swapYAxis(List<Point> points, double maxY) {

		List<Point> swappedPoints = new ArrayList<Point>();

		for (int i = 0; i < points.size(); i++) {
			Point point = points.get(i);
			swappedPoints.add(new Point(point.getX(), maxY - point.getY(),
					point.getTime(), point.getId()));
		}

		return swappedPoints;
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
	private Point2D.Double distChangeAtPoint(int p, List<Point> featurePoints) {

		Point point = featurePoints.get(p);
		Point nextPoint = featurePoints.get(p + 1);

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
	private double angleChangeAtPoint(int p, List<Point> featurePoints,
			Point2D.Double[] distDeltas) {

		double y = (distDeltas[p].getX() * distDeltas[p - 1].getY())
				- (distDeltas[p - 1].getX() * distDeltas[p].getY());
		double x = (distDeltas[p].getX() * distDeltas[p - 1].getX())
				+ (distDeltas[p].getY() * distDeltas[p - 1].getY());

		if (x == 0) {
			if (y > 0)
				return Math.PI / 2.0;
			else
				return -Math.PI / 2.0;
		}

		double omegaP = Math.atan2(y, x);

		return omegaP;
	}

	/**
	 * Calculates the time change between a point and its following point
	 * 
	 * @param p
	 *            Index of the initial point
	 * @param featurePoints
	 *            List of points to use in feature calculation
	 * @return Time change between two points
	 */
	private long timeChangeAtPoint(int p, List<Point> featurePoints) {
		return featurePoints.get(p + 1).getTime()
				- featurePoints.get(p).getTime();
	}

	/**
	 * Adds the Rubine features to the feature set
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 */
	private void addRubineFeatures(List<Point> featurePoints,
			Point2D.Double[] distDeltas, double[] angleDeltas, long[] timeDeltas) {

		// Get the bounding box
		BoundingBox boundingBox = getBoundingBox(featurePoints);

		// single point
		if (featurePoints.size() < 2) {
			for (int i = 0; i < 13; i++)
				m_features.add(0.0);
		} else {

			// R1
			double initialCosine = initialCosine(featurePoints);
			m_features.add(initialCosine);
			m_featureLabels.add("R1");

			// R2
			double initialSine = initialSine(featurePoints);
			m_features.add(initialSine);
			m_featureLabels.add("R2");

			// R3
			double boundingBoxSize = boundingBox.getDiagonalLength();
			m_features.add(boundingBoxSize);
			m_featureLabels.add("R3");

			// R4
			double boundingBoxAngle = boundingBox.getDiagonalAngle();
			m_features.add(boundingBoxAngle);
			m_featureLabels.add("R4");

			// R5
			double firstLastPtDist = firstLastPtDist(featurePoints);
			m_features.add(firstLastPtDist);
			m_featureLabels.add("R5");

			// R6
			double firstLastCosine = firstLastCosine(featurePoints,
					firstLastPtDist);
			m_features.add(firstLastCosine);
			m_featureLabels.add("R6");

			// R7
			double firstLastSine = firstLastSine(featurePoints, firstLastPtDist);
			m_features.add(firstLastSine);
			m_featureLabels.add("R7");

			// R8
			double totalLength = totalLength(featurePoints, distDeltas);
			m_features.add(totalLength);
			m_featureLabels.add("R8");

			// R9
			double totalAngle = totalAngle(featurePoints, angleDeltas);
			m_features.add(totalAngle);
			m_featureLabels.add("R9");

			// R10
			double totalAbsAngle = totalAbsAngle(featurePoints, angleDeltas);
			m_features.add(totalAbsAngle);
			m_featureLabels.add("R10");

			// R11
			double smoothness = smoothness(featurePoints, angleDeltas);
			m_features.add(smoothness);
			m_featureLabels.add("R11");

			// R12
			double maxSpeed = maxSpeed(featurePoints, distDeltas, timeDeltas);
			m_features.add(maxSpeed);
			m_featureLabels.add("R12");

			// R13
			double totalTime = totalTime(featurePoints);
			m_features.add(totalTime);
			m_featureLabels.add("R13");
		}
	}

	/**
	 * Adds the Long features to the feature set
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 */
	private void addLongFeatures(List<Point> featurePoints,
			Point2D.Double[] distDeltas, double[] angleDeltas, long[] timeDeltas) {

		// Start with the Rubine feature set
		addRubineFeatures(featurePoints, distDeltas, angleDeltas, timeDeltas);

		// Remove the last two features from the Rubine set: max speed and total
		// time
		m_features.remove(12);
		m_featureLabels.remove("R13");

		m_features.remove(11);
		m_featureLabels.remove("R12");

		// R3
		double boundingBoxSize = m_features.get(2);

		// R4
		double boundingBoxAngle = m_features.get(3);

		// R5
		double firstLastPtDist = m_features.get(4);

		// R8
		double totalLength = m_features.get(7);

		// R9
		double totalAngle = m_features.get(8);

		// R10
		double totalAbsAngle = m_features.get(9);

		// L12
		double aspect = aspect(boundingBoxAngle);
		m_features.add(aspect);
		m_featureLabels.add("L12");

		// L13
		double curviness = curviness(angleDeltas);
		m_features.add(curviness);
		m_featureLabels.add("L13");

		// L14
		double totalAngleDivTotalLength = totalAngleDivTotalLength(totalAngle,
				totalLength);
		m_features.add(totalAngleDivTotalLength);
		m_featureLabels.add("L14");

		// L15
		double density1 = density1(totalLength, firstLastPtDist);
		m_features.add(density1);
		m_featureLabels.add("L15");

		// L16
		double density2 = density2(totalLength, boundingBoxSize);
		m_features.add(density2);
		m_featureLabels.add("L16");

		// L17
		double openness = openness(firstLastPtDist, boundingBoxSize);
		m_features.add(openness);
		m_featureLabels.add("L17");

		// L18
		// Recalculate the bounding box
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		for (Point p : featurePoints) {
			if (p.getX() < minX) {
				minX = p.getX();
			}
			if (p.getX() > maxX) {
				maxX = p.getX();
			}
			if (p.getY() < minY) {
				minY = p.getY();
			}
			if (p.getY() > maxY) {
				maxY = p.getY();
			}
		}

		BoundingBox boundingBox = new BoundingBox(minX, minY, maxX, maxY);
		double boundingBoxArea = boundingBox.getArea();
		m_features.add(boundingBoxArea);
		m_featureLabels.add("L18");

		// L19
		double logArea = logArea(boundingBoxArea);
		m_features.add(logArea);
		m_featureLabels.add("L19");

		// L20
		double totalAngleDivTotalAbsAngle = totalAngleDivTotalAbsAngle(
				totalAngle, totalAbsAngle);
		m_features.add(totalAngleDivTotalAbsAngle);
		m_featureLabels.add("L20");

		// L21
		double logLength = logLength(totalLength);
		m_features.add(logLength);
		m_featureLabels.add("L21");

		// L22
		double logAspect = logAspect(aspect);
		m_features.add(logAspect);
		m_featureLabels.add("L22");
	}

	/**
	 * Adds the COA features to the feature set. Assume a standardized bounding
	 * box for the stroke
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 */
	private void addCOAFeatures(List<Point> featurePoints,
			Point2D.Double[] distDeltas, double[] angleDeltas, long[] timeDeltas) {

		// Get the bounding box
		BoundingBox boundingBox = getBoundingBox(featurePoints);

		// A measure of main orientation
		m_features.add(boundingBox.getDiagonalAngle());

		// Density - how much ink is used in the bounding box
		double totalLength = totalLength(featurePoints, distDeltas);
		double density = 0.0;
		if (boundingBox.getArea() > 0) {
			density = totalLength / boundingBox.getArea();
		}
		m_features.add(density);

		// Total curvature
		double totalAngle = totalAngle(featurePoints, angleDeltas);
		m_features.add(totalAngle);

		// Total absolute angle
		double totalAbsAngle = totalAbsAngle(featurePoints, angleDeltas);
		m_features.add(totalAbsAngle);

		// Number of direction changes
		double dirChanges = 0.0;
		for (int i = 1; i < angleDeltas.length; i++) {
			double signum1 = Math.signum(angleDeltas[i - 1]);
			double signum2 = Math.signum(angleDeltas[i]);

			if (signum1 == 0.0)
				signum1 = 1.0;
			if (signum2 == 0.0)
				;
			signum2 = 1.0;

			if (signum1 != signum2) {
				dirChanges += 1;
			}
		}
		m_features.add(dirChanges);

		// Average distance from center
		double avgDistFromCenter = 0.0;
		for (Point pt : featurePoints) {
			avgDistFromCenter += pt.distance(boundingBox.getCenterPoint());
		}
		avgDistFromCenter /= boundingBox.getDiagonalLength();
		m_features.add(avgDistFromCenter);
	}

	/**
	 * Get the bounding box for the feature points
	 * 
	 * @param featurePoints
	 *            Feature points of the stroke
	 * @return The bounding box of the points
	 */
	private BoundingBox getBoundingBox(List<Point> featurePoints) {

		// Get the bounding box of the feature points (to be used in a few
		// features)
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		for (Point p : featurePoints) {
			if (p.getX() < minX) {
				minX = p.getX();
			}
			if (p.getX() > maxX) {
				maxX = p.getX();
			}
			if (p.getY() < minY) {
				minY = p.getY();
			}
			if (p.getY() > maxY) {
				maxY = p.getY();
			}
		}

		BoundingBox boundingBox = new BoundingBox(minX, minY, maxX, maxY);

		return boundingBox;
	}

	/**
	 * Rubine features
	 */

	/**
	 * Calculates the cosine of the initial angle
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @return Cosine of the initial angle
	 */
	private double initialCosine(List<Point> featurePoints) {

		Point p0 = featurePoints.get(0);

		double dist = 0.0;
		int i = 1;

		// Loop until we have moved to a non-overlapping point
		do {
			dist = p0.distance(featurePoints.get(i));
			i++;
		} while (dist == 0.0 && i < featurePoints.size());

		double cos = (featurePoints.get(i - 1).getX() - p0.getX()) / dist;

		return cos;
	}

	/**
	 * Calculates the sine of the initial angle
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @return Sine of the initial angle
	 */
	private double initialSine(List<Point> featurePoints) {

		Point p0 = featurePoints.get(0);

		double dist = 0.0;
		int i = 1;

		// Loop until we have moved to a non-overlapping point
		do {
			dist = p0.distance(featurePoints.get(i));
			i++;
		} while (dist == 0.0 && i < featurePoints.size());

		double sin = (featurePoints.get(i - 1).getY() - p0.getY()) / dist;

		return sin;
	}

	/**
	 * Calculates the distance between the first and last point
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @return Distance between the first and last point
	 */
	private double firstLastPtDist(List<Point> featurePoints) {
		Point firstPoint = featurePoints.get(0);
		Point lastPoint = featurePoints.get(featurePoints.size() - 1);

		return firstPoint.distance(lastPoint);
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
	private double firstLastCosine(List<Point> featurePoints,
			double firstLastPtDist) {

		Point firstPoint = featurePoints.get(0);
		Point lastPoint = featurePoints.get(featurePoints.size() - 1);

		if (firstLastPtDist != 0.0) {
			return (lastPoint.getX() - firstPoint.getX()) / firstLastPtDist;
		} else {
			return (lastPoint.getX() - firstPoint.getX());
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
	private double firstLastSine(List<Point> featurePoints,
			double firstLastPtDist) {

		Point firstPoint = featurePoints.get(0);
		Point lastPoint = featurePoints.get(featurePoints.size() - 1);

		if (firstLastPtDist != 0.0) {
			return (lastPoint.getY() - firstPoint.getY()) / firstLastPtDist;
		} else {
			return (lastPoint.getY() - firstPoint.getY());
		}
	}

	/**
	 * Computes the total gesture length
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @param distDeltas
	 *            Array of distance changes between points
	 * @return Total gesture length
	 */
	private double totalLength(List<Point> featurePoints,
			Point2D.Double[] distDeltas) {

		double pathLength = 0.0;

		for (int p = 0; p < featurePoints.size() - 1; p++) {
			pathLength += Math.sqrt((distDeltas[p].getX() * distDeltas[p]
					.getX()) + (distDeltas[p].getY() * distDeltas[p].getY()));
		}

		return pathLength;
	}

	/**
	 * Computes the total angle traversed by the stroke
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @param angleDeltas
	 *            Array of angle changes between points
	 * @return Total angle traversed
	 */
	private double totalAngle(List<Point> featurePoints, double[] angleDeltas) {

		double angleSum = 0.0;

		for (int p = 1; p < featurePoints.size() - 1; p++) {
			angleSum += angleDeltas[p];
		}

		return angleSum;
	}

	/**
	 * Computes the sum of the absolute value of the angles at each mouse point
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @param angleDeltas
	 *            Array of angle changes between points
	 * @return Sum of the absolute value of the angles at each mouse point
	 */
	private double totalAbsAngle(List<Point> featurePoints, double[] angleDeltas) {

		double absAngleSum = 0.0;

		for (int p = 1; p < featurePoints.size() - 1; p++) {
			absAngleSum += Math.abs(angleDeltas[p]);
		}

		return absAngleSum;
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
	private double smoothness(List<Point> featurePoints, double[] angleDeltas) {

		double sqAngleSum = 0.0;

		for (int p = 1; p < featurePoints.size() - 1; p++) {
			sqAngleSum += (angleDeltas[p] * angleDeltas[p]);
		}

		return sqAngleSum;
	}

	/**
	 * Computes the maximum speed of the gesture stroke
	 * 
	 * @param featurePoints
	 *            Stroke points to calculate features for
	 * @param distDeltas
	 *            Array of distance changes between points
	 * @param time
	 *            Array of time changes between points
	 * @return Maximum speed of the gesture
	 */
	private double maxSpeed(List<Point> featurePoints,
			Point2D.Double[] distDeltas, long[] time) {

		double maxSpeed = 0.0;

		for (int p = 0; p < featurePoints.size() - 1; p++) {
			if (time[p] == 0) {
				continue;
			} else {

				double deltaXSq = distDeltas[p].getX() * distDeltas[p].getX();
				double deltaYSq = distDeltas[p].getY() * distDeltas[p].getY();
				double timeSq = time[p] * time[p];

				double speed = (deltaXSq + deltaYSq) / timeSq;

				if (speed > maxSpeed) {
					maxSpeed = speed;
				}
			}
		}

		return maxSpeed;
	}

	/**
	 * Computes the duration of the gesture
	 * 
	 * @param s
	 *            Gesture stroke
	 * @return Duration of the gesture
	 */
	private double totalTime(List<Point> featurePoints) {
		return this.getTimeLength();
	}

	/**
	 * Long & Landay Features
	 */

	/**
	 * Calculates the gesture aspect, which is abs(45 degrees - angle of
	 * bounding box)
	 * 
	 * @return Aspect...
	 */
	private double aspect(double boundingBoxAngle) {
		return Math.abs((Math.PI / 4.0) - boundingBoxAngle);
	}

	/**
	 * The sum of gesture intersegment angles whose absolute value is less than
	 * 19 degrees
	 * 
	 * @param angleDeltas
	 *            Array of angle changes between points
	 * @return Curviness of the gesture
	 */
	private double curviness(double[] angleDeltas) {

		double threshold = (19.0 * Math.PI) / 180.0;
		double curviness = 0.0;

		for (int p = 1; p < angleDeltas.length - 1; p++) {
			if (angleDeltas[p] < threshold)
				curviness += Math.abs(angleDeltas[p]);
		}

		return curviness;
	}

	/**
	 * Returns the total angle traversed / total length of gesture stroke
	 * 
	 * @param totalAngle
	 *            Total angle traversed
	 * @param totalLength
	 *            Total length of stroke
	 * @return totalAngle / totalLength
	 */
	private double totalAngleDivTotalLength(double totalAngle,
			double totalLength) {

		if (totalLength != 0.0) {
			return totalAngle / totalLength;
		} else {
			return 0.0;
		}
	}

	/**
	 * A density metric for the gesture stroke that uses the stroke's length and
	 * distance between the first and last point
	 * 
	 * @param totalLength
	 *            Total length of the stroke
	 * @param firstLastPtDist
	 *            Distance between the first and last point
	 * @return totalLength / firstLastPtDist
	 */
	private double density1(double totalLength, double firstLastPtDist) {

		if (firstLastPtDist != 0.0) {
			return totalLength / firstLastPtDist;
		} else {
			return totalLength;
		}
	}

	/**
	 * A density metric for the gesture stroke that uses the stroke's length and
	 * bounding box size
	 * 
	 * @param totalLength
	 *            Total length of the stroke
	 * @param boundingBoxSize
	 *            Length of the bounding box diagonal
	 * @return totalLength / boundingBoxSize
	 */
	private double density2(double totalLength, double boundingBoxSize) {

		if (boundingBoxSize != 0.0) {
			return totalLength / boundingBoxSize;
		} else {
			return totalLength;
		}
	}

	/**
	 * How "open" or spaced out is a gesture
	 * 
	 * @param firstLastPtDist
	 *            Distance between the first and last point
	 * @param boundingBoxSize
	 *            Length of the bounding box diagonal
	 * @return firstLastPtDist / boundingBoxSize
	 */
	private double openness(double firstLastPtDist, double boundingBoxSize) {

		if (boundingBoxSize != 0.0) {
			return firstLastPtDist / boundingBoxSize;
		} else {
			return firstLastPtDist;
		}
	}

	/**
	 * Returns the log of the bounding box area
	 * 
	 * @param area
	 *            Bounding box area
	 * @return log(area)
	 */
	private double logArea(double area) {

		if (area != 0) {
			return Math.log(area);
		} else {
			return 0.0;
		}
	}

	/**
	 * Returns the total angle divided by the total absolute angle
	 * 
	 * @param totalAngle
	 *            Total angle
	 * @param totalAbsAngle
	 *            Total absolute angle
	 * @return totalAngle / totalAbsAngle
	 */
	private double totalAngleDivTotalAbsAngle(double totalAngle,
			double totalAbsAngle) {

		if (totalAbsAngle != 0.0) {
			return totalAngle / totalAbsAngle;
		} else {
			return totalAngle;
		}
	}

	/**
	 * Returns the log of the total length
	 * 
	 * @param totalLength
	 *            Length of the stroke
	 * @return log(totalLength)
	 */
	private double logLength(double totalLength) {

		if (totalLength != 0.0) {
			return Math.log(totalLength);
		} else {
			return 0.0;
		}
	}

	/**
	 * Returns the log of the aspect
	 * 
	 * @param aspect
	 *            Aspect
	 * @return log(aspect)
	 */
	private double logAspect(double aspect) {

		if (aspect != 0) {
			return Math.log(aspect);
		} else {
			return 0.0;
		}
	}

	/**
	 * Returns the x-values of the stroke.
	 * 
	 * @return x-values of the stroke.
	 */
	public double[] getXValues() {
		double[] xs = new double[getNumPoints()];

		for (int i = 0; i < xs.length; i++) {
			xs[i] = getPoint(i).getX();
		}

		return xs;
	}

	/**
	 * Returns the y-values of the stroke.
	 * 
	 * @return y-values of the stroke.
	 */
	public double[] getYValues() {
		double[] ys = new double[getNumPoints()];

		for (int i = 0; i < ys.length; i++) {
			ys[i] = getPoint(i).getY();
		}

		return ys;
	}

	/**
	 * Returns the y-values of the stroke.
	 * 
	 * @return y-values of the stroke.
	 */
	public double[] getAtanValues() {
		double[] angles = new double[getNumPoints()];
		angles[0] = Math.atan2(getPoint(0).getY(), getPoint(0).getX());

		// Calculate intersegment distance changes
		Point2D.Double[] distDeltas = new Point2D.Double[getNumPoints()];
		for (int p = 0; p < getNumPoints() - 1; p++) {
			distDeltas[p] = distChangeAtPoint(p, getPoints());
		}

		// Calculate intersegment angle changes
		double[] angleDeltas = new double[getNumPoints()];
		for (int p = 1; p < getNumPoints() - 1; p++) {
			angleDeltas[p] = angleChangeAtPoint(p, getPoints(), distDeltas);
		}

		// Angles
		for (int i = 1; i < angles.length; i++) {
			angles[i] = angles[i - 1] + angleDeltas[i];
		}

		return angles;
	}

}