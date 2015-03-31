package srl.recognition.rubine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import srl.core.sketch.Interpretation;
import srl.core.sketch.Point;
import srl.core.sketch.Shape;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;
import srl.recognition.IRecognitionResult;
import srl.recognition.RecognitionResult;
import srl.recognition.recognizer.IRecognizer;
import srl.recognition.rubine.RubineStroke.FeatureSet;
import Jama.Matrix;



/**
 * Rubine Classifier
 * 
 * @author awolin
 */
public class RubineClassifier implements
		IRecognizer<Stroke, IRecognitionResult> {

	/**
	 * If there are multiple strokes in a sketch, how should we handle them? Use
	 * the first stroke, last stroke, or merge all the strokes into one, end to
	 * end.
	 * 
	 * @author jbjohns
	 */
	public enum MultiStrokeMethod {
		/**
		 * Use the first stroke only
		 */
		FIRST,

		/**
		 * Use the last stroke only
		 */
		LAST,

		/**
		 * Merge all the strokes together, end to end
		 */
		MERGE
	}

	/**
	 * How do we handle multiple strokes?
	 */
	private MultiStrokeMethod m_multiStrokeMethod = MultiStrokeMethod.FIRST;

	/**
	 * N for N-best list
	 */
	private static final int S_N = 5;

	/**
	 * Rejection probability
	 */
	private static final double S_REJECT_PROBABILITY = 0.95;

	/**
	 * Feature set to use during classification
	 */
	private RubineStroke.FeatureSet m_featureSet;

	/**
	 * Weights to use for classification
	 */
	private Map<String, List<Double>> m_weights = null;

	/**
	 * Stroke to consider for classification
	 */
	private Stroke m_stroke = null;

	/**
	 * RubineStroke to consider for classification
	 */
	private RubineStroke m_rubineStroke = null;

	/**
	 * Average features for each class
	 */
	private Map<String, List<Double>> m_classFeatureAverages = null;

	/**
	 * Inverse of the common covariance matrix. This is used for gesture
	 * rejection.
	 */
	private Matrix m_commonCovMatrixInverse = null;

	/**
	 * Default constructor. Uses Rubine's features.
	 */
	public RubineClassifier() {
		this(RubineStroke.FeatureSet.Rubine);
	}

	/**
	 * Constructor that takes in a feature set to use during classification
	 * 
	 * @param featureCalc
	 *            Feature class, with the corresponding feature set, to use for
	 *            this classifier
	 */
	public RubineClassifier(RubineStroke.FeatureSet featureSet) {
		m_featureSet = featureSet;
	}

	/**
	 * @return the multiStrokeMethod
	 */
	public MultiStrokeMethod getMultiStrokeMethod() {
		return m_multiStrokeMethod;
	}

	/**
	 * @param multiStrokeMethod
	 *            the multiStrokeMethod to set
	 */
	public void setMultiStrokeMethod(MultiStrokeMethod multiStrokeMethod) {
		m_multiStrokeMethod = multiStrokeMethod;
	}

	

	/**
	 * Classifies a stroke
	 * 
	 * @param stroke
	 *            the stroke
	 * @return String classifying the gesture
	 */
	public NavigableMap<Double, String> classify(Stroke stroke) {

		List<Double> features = null;

		m_rubineStroke = new RubineStroke(stroke, m_featureSet);
		features = m_rubineStroke.getFeatures();

		return classify(features);
	}

	/**
	 * Classifies a given gesture given the features
	 * 
	 * @param features
	 *            Features of the stroke to classify
	 * @return String classifying the gesture
	 */
	public NavigableMap<Double, String> classify(List<Double> features) {

		NavigableMap<Double, String> classRanking = new TreeMap<Double, String>();

		for (String classLabel : m_weights.keySet()) {
			double val = classifyValue(classLabel, features);
			classRanking.put(val, classLabel);
		}

		// Sort the items in descending order
		classRanking = classRanking.descendingMap();

		// Check for rejection
		// double denominator = 0.0;
		// NavigableSet<Double> valuesInOrder = classRanking.descendingKeySet();
		// Object[] objArray = valuesInOrder.toArray();
		// Double[] valueArray = new Double[objArray.length];
		// for (int i = 0; i < valueArray.length; i++) {
		// valueArray[i] = (Double) objArray[i];
		// }
		//
		// for (int i = 1; i < valueArray.length; i++) {
		// denominator += Math.exp(valueArray[i] - valueArray[0]);
		// }
		//
		// double pReject = 1.0 / denominator;
		//
		// double rejectThreshold = 0.5 * (features.size() * features.size());

		if (m_commonCovMatrixInverse != null) {

			List<Double> mahalDists = new ArrayList<Double>();
			double bestClassMahalDist = 0.0;

			String bestClassLabel = classRanking.firstEntry().getValue();

			// Get each class's Mahalanobis distance
			for (String classLabel : classRanking.values()) {

				double mahalDist = 0.0;

				for (int j = 0; j < features.size(); j++) {
					for (int k = 0; k < features.size(); k++) {
						mahalDist += m_commonCovMatrixInverse.get(j, k)
								* (features.get(j) - m_classFeatureAverages
										.get(classLabel).get(j))
								* (features.get(k) - m_classFeatureAverages
										.get(classLabel).get(k));
					}
				}

				mahalDists.add(mahalDist);

				if (classLabel.equals(bestClassLabel)) {
					bestClassMahalDist = mahalDist;
				}
			}

			// Calculate the rejection threshold
			double mahalDistAvg = 0.0;
			for (Double dist : mahalDists) {
				mahalDistAvg += dist;
			}
			mahalDistAvg /= mahalDists.size();

			double stdDev = 0.0;
			for (Double dist : mahalDists) {
				stdDev += (dist - mahalDistAvg) * (dist - mahalDistAvg);
			}
			stdDev = Math.sqrt(stdDev / mahalDists.size());

			double rejectThreshold = mahalDistAvg - stdDev;

			if (bestClassMahalDist > rejectThreshold) {
				return null;
			}
		}

		return classRanking;
	}

	/**
	 * Calculates the dot product of the class's weights and the given features
	 * 
	 * @param features
	 *            Features of a stroke
	 * @return Dot product of the weights and features
	 */
	private double classifyValue(String classLabel, List<Double> features) {

		List<Double> classWeights = m_weights.get(classLabel);
		double val = classWeights.get(0);

		for (int i = 0; i < features.size(); i++) {
			val += (classWeights.get(i + 1) * features.get(i));
		}

		return val;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	public IRecognitionResult recognize() {

		if (m_stroke != null) {
			NavigableMap<Double, String> classRanking = classify(m_stroke);
			IRecognitionResult recResult = new RecognitionResult();

			// Initialize a list of N Shapes
			List<Shape> nBestList = new ArrayList<Shape>(S_N);

			double worstRankingValue = classRanking.lastKey();

			// Loop until we have no more gesture classes in the ranking or we
			// have reached our maximum N for the N-best list
			int n = 0;
			while (!classRanking.isEmpty() && n < S_N) {
				Entry<Double, String> confAndClass = classRanking
						.pollFirstEntry();

				// Copy the stroke
				Shape recognizedShape = new Shape();
				recognizedShape.add(new Stroke(m_stroke));

				// Set the confidence and label
				double confidence = confAndClass.getKey()
						/ (confAndClass.getKey() + worstRankingValue);
				recognizedShape.setInterpretation(new Interpretation(
						confAndClass.getValue(), confidence));

				// Set the shape in the N-best list
				nBestList.set(n, recognizedShape);
			}

			recResult.setNBestList(nBestList);

			return recResult;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.IRecognizer#submitForRecognition(java
	 * .lang.Object)
	 */
	public void submitForRecognition(Stroke submission) {
		m_stroke = submission;
	}

	/**
	 * Get the feature set used for the Rubine classifier
	 * 
	 * @return The feature set used in classification
	 */
	public RubineStroke.FeatureSet getFeatureSet() {
		return m_featureSet;
	}

	/**
	 * Return the features of the submitted stroke.
	 * 
	 * @return List of feature values.
	 */
	public List<Double> getFeatures() {
		return getFeatures(m_stroke);
	}

	/**
	 * Return the list of features of a given stroke.
	 * 
	 * @param stroke
	 * @return List of feature values.
	 */
	public List<Double> getFeatures(Stroke stroke) {
		m_rubineStroke = new RubineStroke(stroke);
		return m_rubineStroke.getFeatures();
	}

	/**
	 * Return the list of features of a given stroke.
	 * 
	 * @param stroke
	 * @return List of feature values.
	 */
	public List<Double> getFeatures(Stroke stroke, boolean standardized,
			boolean cartesian) {

		m_rubineStroke = new RubineStroke(stroke, m_featureSet, standardized,
				cartesian);
		return m_rubineStroke.getFeatures();
	}

	/**
	 * Return the features of the submitted stroke.
	 * 
	 * @return List of feature labels.
	 */
	public List<String> getFeatureLabels() {
		return getFeatureLabels(m_stroke);
	}

	/**
	 * Return the list of features of a given stroke.
	 * 
	 * @param stroke
	 * @return List of feature labels.
	 */
	public List<String> getFeatureLabels(Stroke stroke) {
		m_rubineStroke = new RubineStroke(stroke);
		return m_rubineStroke.getFeatureLabels();
	}

	/**
	 * Returns the
	 * 
	 * @return
	 */
	public RubineStroke getRubineStroke() {
		return m_rubineStroke;
	}

	
	/**
	 * Train on given RubineStroke data
	 * 
	 * @param trainingData
	 *            Training data stored as a mapping from class labels to a list
	 *            of example RubineStrokes
	 */
	protected void trainOnData(Map<String, List<RubineStroke>> trainingData) {

		// Calculate the feature averages
		Map<String, List<Double>> classFeatureAverages = calcClassAverageFeatures(trainingData);

		// Create a covariance matrix for each class
		Map<String, Matrix> classCovMatrices = calcClassCovMatrices(
				trainingData, classFeatureAverages);

		// Create a common covariance matrix
		Matrix commonCovMatrix = calcCommonCovMatrix(trainingData,
				classCovMatrices);

		// Calculate and store the weights
		m_weights = calcWeights(commonCovMatrix, classFeatureAverages);
	}

	/**
	 * Calculate the feature averages for each class
	 * 
	 * @param trainingData
	 *            Training data stored as a mapping from class labels to a list
	 *            of example RubineStrokes
	 * @return A mapping from class labels to the class feature averages
	 */
	private Map<String, List<Double>> calcClassAverageFeatures(
			Map<String, List<RubineStroke>> trainingData) {

		m_classFeatureAverages = new HashMap<String, List<Double>>();

		for (String classLabel : trainingData.keySet()) {

			List<RubineStroke> classData = trainingData.get(classLabel);

			// Sum the data
			int numExamples = classData.size();
			List<Double> averageFeatures = new ArrayList<Double>();
			for (int e = 0; e < numExamples; e++) {

				List<Double> exampleFeatures = classData.get(e).getFeatures();

				if (e == 0) {
					averageFeatures = exampleFeatures;
				} else {
					for (int i = 0; i < averageFeatures.size(); i++) {
						averageFeatures.set(i, averageFeatures.get(i)
								+ exampleFeatures.get(i));
					}
				}
			}

			// Average the data
			for (int i = 0; i < averageFeatures.size(); i++) {
				averageFeatures.set(i, averageFeatures.get(i) / numExamples);
			}

			// Store the feature averages
			m_classFeatureAverages.put(classLabel, averageFeatures);
		}

		return m_classFeatureAverages;
	}

	/**
	 * Calculate the covariance matrix for each class
	 * 
	 * @param trainingData
	 *            Training data stored as a mapping from class labels to a list
	 *            of example RubineStrokes
	 * @param classFeatureAverages
	 *            Class feature averages stored as a mapping from class labels
	 *            to a list of average feature values
	 * @return A mapping from class labels to the class covariance matrices
	 */
	private Map<String, Matrix> calcClassCovMatrices(
			Map<String, List<RubineStroke>> trainingData,
			Map<String, List<Double>> classFeatureAverages) {

		// Create a covariance matrix for each class
		Map<String, Matrix> classCovMatrices = new HashMap<String, Matrix>();

		// For each class
		for (String classLabel : trainingData.keySet()) {

			List<RubineStroke> exampleData = trainingData.get(classLabel);
			List<Double> featureAverages = classFeatureAverages.get(classLabel);

			int numExamples = exampleData.size();
			int numFeatures = featureAverages.size();

			double[][] covMatrixValues = new double[numFeatures][numFeatures];

			// For each example
			for (int e = 0; e < numExamples; e++) {

				List<Double> exampleFeatures = exampleData.get(e).getFeatures();

				// Add the current value at (i,j)
				// (example[i] - avg[i]) * (example[j] - avg[j])
				for (int i = 0; i < numFeatures; i++) {
					for (int j = 0; j < numFeatures; j++) {
						double val = (exampleFeatures.get(i) - featureAverages
								.get(i))
								* (exampleFeatures.get(j) - featureAverages
										.get(j));
						covMatrixValues[i][j] += val;
					}
				}
			}

			Matrix covMatrix = new Matrix(covMatrixValues);
			classCovMatrices.put(classLabel, covMatrix);
		}

		return classCovMatrices;
	}

	/**
	 * Calculate the common covariance matrix for all the classes
	 * 
	 * @param trainingData
	 *            Training data stored as a mapping from class labels to a list
	 *            of example RubineStrokes
	 * @param classCovMatrices
	 *            A mapping from class labels to the class covariance matrices
	 * @return A common covariance matrix for all the class data
	 */
	private Matrix calcCommonCovMatrix(
			Map<String, List<RubineStroke>> trainingData,
			Map<String, Matrix> classCovMatrices) {

		Matrix commonCovMatrix = null;
		int totalNumExamples = 0;
		int numClasses = 0;

		// Sum the class covariance matrices and the number of examples
		for (String classLabel : classCovMatrices.keySet()) {

			Matrix classCovMatrix = classCovMatrices.get(classLabel);
			int numExamples = trainingData.get(classLabel).size();

			if (commonCovMatrix == null) {
				commonCovMatrix = classCovMatrix;
			} else {
				commonCovMatrix.plus(classCovMatrix);
			}

			totalNumExamples += numExamples;
			numClasses++;
		}

		// Average the common covariance matrix by a normalizing term
		double normalizingTerm = 1.0 / (-numClasses + totalNumExamples);
		commonCovMatrix.timesEquals(normalizingTerm);

		return commonCovMatrix;
	}

	/**
	 * Calculates the weights for the classifier given the common covariance
	 * matrix and the feature averages
	 * 
	 * @param commonCovMatrix
	 *            Common covariance matrix
	 * @param classFeatureAverages
	 *            Class feature averages stored as a mapping from class labels
	 *            to a list of average feature values
	 * @return Weights for the classes
	 */
	private Map<String, List<Double>> calcWeights(Matrix commonCovMatrix,
			Map<String, List<Double>> classFeatureAverages) {

		m_commonCovMatrixInverse = commonCovMatrix.inverse();

		Map<String, List<Double>> weights = new HashMap<String, List<Double>>();

		for (String classLabel : classFeatureAverages.keySet()) {

			List<Double> featureAverages = classFeatureAverages.get(classLabel);
			double[] classWeights = new double[featureAverages.size() + 1];

			// Calculate the class weights
			for (int j = 0; j < featureAverages.size(); j++) {
				for (int i = 0; i < featureAverages.size(); i++) {
					double val = m_commonCovMatrixInverse.get(i, j)
							* featureAverages.get(i);
					classWeights[j + 1] += val;
				}
			}

			// Calculate the initial weight, w_0
			for (int i = 0; i < featureAverages.size(); i++) {
				classWeights[0] += classWeights[i + 1] * featureAverages.get(i);
			}

			classWeights[0] /= -2.0;

			// Convert the array to a list
			List<Double> weightList = new ArrayList<Double>();
			for (double w : classWeights) {
				weightList.add(w);
			}

			weights.put(classLabel, weightList);
		}

		return weights;
	}

	/*
	 * Saving and loading weights, training data, and user data via my own XML
	 * format (extension .rub)
	 * 
	 * The format used follows:
	 * 
	 * <?xml version="1.0" encoding="UTF-8" ?> <classes> <class> label="name",
	 * weights="[w0, w1, w2, ... , wn]"> </class> ... </classes>
	 * 
	 * Much of the code in this section was taken and modified from an XML Java
	 * tutorial at http://www.totheriver.com/learn/xml/xmltutorial.html#6.2
	 */

	/**
	 * Saves the weights, training data, and user data to an XML file,
	 * weights.xml
	 */
	public void saveWeights(String filename) {

		// Get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			// Get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// Create an instance of DOM
			Document dom = db.newDocument();
			createDOMTree(dom);

			// Create the physical file
			//printToFile(dom, filename);
		} catch (ParserConfigurationException pce) {
			System.out
					.println("Error while trying to instantiate DocumentBuilder "
							+ pce);
			System.exit(1);
		}
	}

	/**
	 * Creates the DOM tree with the main branches being the Class elements,
	 * where each Class element houses the information needed to initialize a
	 * ClassClassifier.
	 * 
	 * @param dom
	 *            Document to create
	 */
	private void createDOMTree(Document dom) {

		// Create the root element
		Element rootEle = dom.createElement("classes");
		rootEle.setAttribute("featureSet", m_featureSet.toString());
		dom.appendChild(rootEle);

		for (String classLabel : m_weights.keySet()) {
			Element classEle = createClassElement(classLabel, dom);
			rootEle.appendChild(classEle);
		}
	}

	/**
	 * Creates a Class element that holds all of the information needed for a
	 * ClassClassifier. ClassName and Weights are attributes for the element,
	 * since they are required. TrainingData and UserData are child element, and
	 * they are technically optional.
	 * 
	 * @param c
	 *            ClassClassifier that is mapped to this element
	 * @param dom
	 *            Document to create
	 * @return The created element that houses the ClassClassifier information
	 */
	private Element createClassElement(String classLabel, Document dom) {

		Element classEle = dom.createElement("class");

		// Create attributes for the class name and weights
		classEle.setAttribute("label", classLabel);
		classEle.setAttribute("weights",
				generateXMLStringFromDoubleArray(m_weights.get(classLabel)));

		return classEle;
	}

	/**
	 * This method uses Xerces specific classes prints the XML document to file.
	 */
	/*private void printToFile(Document dom, String filename) {
		try {
			// Print
			OutputFormat format = new OutputFormat(dom);
			format.setIndenting(true);

			// To generate output to console use this serializer
			// XMLSerializer serializer = new XMLSerializer(System.out, format);

			// To generate a file output use FileOutputStream instead of
			// System.Out
			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(
					new File(filename + ".rub")), format);

			serializer.serialize(dom);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}*/

	/**
	 * Load classification weights and training data from a file
	 * 
	 * @param file
	 *            The weights.xml file to load
	 */
	public void loadWeights(File file) {

		// Get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		// Initialize the weights
		m_weights = new HashMap<String, List<Double>>();

		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// Parse using builder to get DOM representation of the XML file
			Document dom = db.parse(file);

			// Read in the XML document and add all of the data to the
			// LinearClassifier
			parseDocument(dom);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Reads the ClassClassifier elements from the document and places them into
	 * the newly created Hashtable<string, ClassClassifier> for this
	 * LinearClassifier.
	 * 
	 * @param dom
	 *            Document to parse
	 */
	private void parseDocument(Document dom) {

		// Get the root element
		Element docEle = dom.getDocumentElement();

		// Get the feature set
		String featureSetString = docEle.getAttribute("featureSet");
		m_featureSet = FeatureSet.valueOf(featureSetString);

		// Get a nodelist of class elements
		NodeList nl = docEle.getElementsByTagName("class");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);

				// Get the class label and weights
				String classLabel = el.getAttribute("label");
				List<Double> weights = readDoubleArrayFromXMLString(el
						.getAttribute("weights"));

				// Store the values
				m_weights.put(classLabel, weights);
			}
		}
	}

	/**
	 * Generates a string of the form "[d1, d2, d3, ... , dn]" from a given
	 * double array.
	 * 
	 * @param dArray
	 *            Double array to generate a string for
	 * @return The double array's string
	 */
	private String generateXMLStringFromDoubleArray(List<Double> dArray) {
		String s = "[";

		for (int d = 0; d < dArray.size(); d++) {
			s += dArray.get(d);

			if (d < dArray.size() - 1)
				s += ", ";
			else
				s += "]";
		}

		return s;
	}

	/**
	 * Reads a string of the form "[d1, d2, d3, ... , dn]" and parses the string
	 * into a double array.
	 * 
	 * @param s
	 *            String to parse
	 * @return Double array
	 */
	private List<Double> readDoubleArrayFromXMLString(String s) {
		ArrayList<Double> dArrayList = new ArrayList<Double>();

		int startIndex = 1;
		int endIndex = s.indexOf(", ", startIndex);

		while (endIndex != -1) {
			double d = Double.parseDouble(s.substring(startIndex, endIndex));

			startIndex = endIndex + 2;
			endIndex = s.indexOf(", ", startIndex);

			dArrayList.add(d);
		}

		dArrayList.add(Double.parseDouble(s.substring(startIndex,
				s.length() - 1)));

		// Create the double[]
		return dArrayList;
	}

	/**
	 * Given a sketch and a {@link #getMultiStrokeMethod()}), get the
	 * appropriate stroke
	 * 
	 * @param sketch
	 *            The sketch to get strokes from
	 * @return The stroke representing the sketch based on
	 *         {@link #getMultiStrokeMethod()}
	 */
	public Stroke getStrokeFromSketch(Sketch sketch) {
		if (m_multiStrokeMethod == MultiStrokeMethod.FIRST) {
			return sketch.getFirstStroke();
		} else if (m_multiStrokeMethod == MultiStrokeMethod.LAST) {
			return sketch.getLastStroke();
		} else {
			// stitch the strokes together
			Stroke mergedStroke = new Stroke();
			for (Iterator<Stroke> strokeIter = sketch.getStrokes().iterator(); strokeIter
					.hasNext();) {
				Stroke stroke = strokeIter.next();
				for (Iterator<Point> ptIter = stroke.getPoints().iterator(); ptIter
						.hasNext();) {
					mergedStroke.addPoint(ptIter.next());
				}
			}
			return mergedStroke;
		}
	}
}
