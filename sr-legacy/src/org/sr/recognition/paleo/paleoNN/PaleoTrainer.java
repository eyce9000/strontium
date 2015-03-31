/**
 * PaleoTrainer.java
 * 
 * Revision History:<br>
 * Feb 17, 2009 bpaulson - File created
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
package org.sr.recognition.paleo.paleoNN;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import srl.core.sketch.Shape;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoFeatureExtractor;
import srl.recognition.paleo.StrokeFeatures;
import srl.recognition.paleo.PaleoConfig.Option;
import srl.recognition.paleo.multistroke.MultiStrokePaleoRecognizer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.classifiers.functions.MultilayerPerceptron;
import org.sr.legacy.serialization.SousaReader;
import org.sr.legacy.serialization.SousaConverter;


/**
 * Trainer for the neural network (creates the ARFF file)
 * 
 * @author bpaulson
 */
public class PaleoTrainer {

	/**
	 * Training data
	 */
	Instances m_data = null;

	/**
	 * Paleo configuration
	 */
	PaleoConfig m_paleoConfig = new PaleoConfig(Option.Arc, Option.Curve, Option.Complex,
				Option.Line, Option.Polyline, Option.Circle, Option.Helix,
				Option.Spiral, Option.Ellipse);

	/**
	 * Sketch object
	 */
	Sketch m_sketch = new Sketch();

	/**
	 * Set of unlabeled files encountered
	 */
	Set<String> m_unlabeledExamples = new TreeSet<String>();

	/**
	 * List of unknown primitive labels encountered (primitive is key, list of
	 * file names is value)
	 */
	Map<String, List<String>> m_unknownPrimitives = new HashMap<String, List<String>>();

	private FileFilter xmlFilter = new FileFilter() {

		@Override
		public boolean accept(File file) {
			return file.getName().endsWith(".xml");
		}

	};

	/**
	 * Default Constructor
	 * 
	 * @throws Exception
	 */
	public PaleoTrainer(File trainData, File outputFile) throws Exception {

		// get the list of individual shape directories
		File[] shapeDirs = trainData.listFiles(new FileFilter() {

			@Override
			public boolean accept(File dir) {
				return dir.isDirectory();
			}

		});

		// loop through all shape directories
		for (File shapeDir : shapeDirs) {
			System.out.println("Checking directory "+shapeDir.getAbsolutePath());
			File[] shapeFiles = shapeDir.listFiles(xmlFilter);
			System.out.println("Checking "+shapeFiles.length+" files");
			// loop through all shape files in the directory
			for (File shapeFile : shapeFiles) {
				trainShape(shapeFile);
			}

		}

		// write training data out to file
		ArffSaver saver = new ArffSaver();
		saver.setInstances(m_data);
		saver.setFile(outputFile);
		saver.writeBatch();

		// write libSVM version
		// libSVMToFile("C:\\paleo_orig_test_everything_nocomplex.txt", m_data);
	}

	/**
	 * Trains with shapes in the shape file
	 * 
	 * @param shapeFile
	 *            shape file to train
	 * @throws Exception
	 */
	private void trainShape(File shapeFile) throws Exception {
		m_sketch = new SousaReader().parseDocument(shapeFile);

		// train on shapes (assumed multi-stroke primitives)
		for (int i = 0; i < m_sketch.getShapes().size(); i++) {
			Shape shape = m_sketch.getShapes().get(i);
			Stroke stroke = MultiStrokePaleoRecognizer.combineStrokes(shape
					.getStrokes());
			stroke.getInterpretation().label = (shape.getInterpretation().label);

			// make sure shape has a label
			if (shape.getInterpretation().label == null
					|| shape.getInterpretation().label.compareToIgnoreCase("") == 0) {

				// no label so we can't compute accuracy
				m_unlabeledExamples.add(shapeFile.getName());
			}

			// make sure stroke's label matches a primitive
			else if (!isValidLabel(shape.getInterpretation().label)) {
				if (!m_unknownPrimitives
						.containsKey(shape.getInterpretation().label))
					m_unknownPrimitives.put(shape.getInterpretation().label,
							new ArrayList<String>());
				m_unknownPrimitives.get(shape.getInterpretation().label).add(
						shapeFile.getName());
			}

			// label is good so train with stroke
			else if (stroke.getNumPoints() > 1) {
				System.out.println("Analyzing shape " + i + " of "
						+ shapeFile.getName());
				PaleoFeatureExtractor pfe = new PaleoFeatureExtractor(
						new StrokeFeatures(stroke, false), m_paleoConfig);
				pfe.computeFeatureVector();

				// make sure data set exists
				if (m_data == null) {
					m_data = pfe.getNewDataset();
				}

				m_data.add(pfe.getInstance(stroke.getInterpretation().label
						.trim()));
			}
		}

		// train on all strokes in sketch
		for (int i = 0; i < m_sketch.getStrokes().size(); i++) {
			Stroke stroke = m_sketch.getStrokes().get(i);

			// make sure stroke has a label
			if (stroke.getInterpretation().label == null
					|| stroke.getInterpretation().label.compareToIgnoreCase("") == 0) {

				// no label so we can't compute accuracy
				m_unlabeledExamples.add(shapeFile.getName());
			}

			// make sure stroke's label matches a primitive
			else if (!isValidLabel(stroke.getInterpretation().label)) {
				if (!m_unknownPrimitives
						.containsKey(stroke.getInterpretation().label))
					m_unknownPrimitives.put(stroke.getInterpretation().label,
							new ArrayList<String>());
				m_unknownPrimitives.get(stroke.getInterpretation().label).add(
						shapeFile.getName());
			}

			// label is good so train with stroke
			else if (stroke.getNumPoints() > 1) {
				System.out.println("Analyzing stroke " + i + " of "
						+ shapeFile.getName());
				PaleoFeatureExtractor pfe = new PaleoFeatureExtractor(
						new StrokeFeatures(stroke, false), m_paleoConfig);
				pfe.computeFeatureVector();

				// make sure data set exists
				if (m_data == null) {
					m_data = pfe.getNewDataset();
				}

				m_data.add(pfe.getInstance(stroke.getInterpretation().label
						.trim()));
			}

		}
	}

	/**
	 * Checks to make sure the given label is a valid PaleoSketch primitive
	 * 
	 * @param label
	 *            label to check
	 * @return true if valid, else false
	 */
	private boolean isValidLabel(String label) {
		for (int i = 0; i < m_paleoConfig.getShapesTurnedOn().size(); i++) {
			String prim = m_paleoConfig.getShapesTurnedOn().get(i);

			// startsWith is a check for Polyline (x)
			if (prim.compareTo(label) == 0 || label.startsWith(prim + " "))
				return true;
		}
		return false;
	}

	/**
	 * Convert a string label into an integer
	 * 
	 * @param label
	 *            label
	 * @return integer version of label
	 * @throws Exception
	 *             if bad label
	 */
	/*
	 * private int labelToInt(String label) throws Exception { label =
	 * label.trim(); if (label.equals(Fit.ARC)) return 0; if
	 * (label.equals(Fit.ARROW)) return 1; if (label.equals(Fit.CIRCLE)) return
	 * 2; if (label.equals(Fit.COMPLEX)) return 3; if (label.equals(Fit.CURVE))
	 * return 4; if (label.equals(Fit.DIAMOND)) return 5; if
	 * (label.equals(Fit.DOT)) return 6; if (label.equals(Fit.ELLIPSE)) return
	 * 7; if (label.equals(Fit.GULL)) return 8; if (label.equals(Fit.HELIX))
	 * return 9; if (label.equals(Fit.INFINITY)) return 10; if
	 * (label.equals(Fit.LINE)) return 11; if (label.equals(Fit.NBC)) return 12;
	 * if (label.equals(Fit.POLYGON)) return 13; if (label.equals(Fit.POLYLINE))
	 * return 14; if (label.equals(Fit.RECTANGLE)) return 15; if
	 * (label.equals(Fit.SPIRAL)) return 16; if (label.equals(Fit.SQUARE))
	 * return 17; if (label.equals(Fit.WAVE)) return 18; throw new
	 * Exception("bad label: [" + label + "]"); }
	 */

	/**
	 * Converts ARFF instances into a format readable by LibSVM and outputs it
	 * to file
	 * 
	 * @param filename
	 *            output file name
	 * @param data
	 *            ARFF instances (the data)
	 * @throws IOException
	 *             if output fails
	 */
	public static void libSVMToFile(String filename, Instances data)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		for (int i = 0; i < data.numInstances(); i++) {
			Instance inst = data.instance(i);
			double label = inst.value(inst.numValues() - 1);
			writer.write(label + "\t");
			for (int a = 0; a < inst.numValues() - 1; a++) {
				double value = inst.value(a);
				if (Double.isInfinite(value) || Double.isNaN(value))
					value = mean(data.attributeToDoubleArray(a));
				writer.write((a + 1) + ":" + value);
				if (a < inst.numValues() - 2)
					writer.write(" ");
			}
			writer.newLine();
		}
	}

	/**
	 * Calculate the mean of an array of values (skips inf and NaN)
	 * 
	 * @param values
	 * @return
	 */
	public static double mean(double[] values) {
		double sum = 0.0;
		double num = 0.0;
		for (int i = 0; i < values.length; i++) {
			if (Double.isInfinite(values[i]) || Double.isNaN(values[i]))
				continue;
			sum += values[i];
			num++;
		}
		return sum / num;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		PaleoTrainer t = new PaleoTrainer(new File(args[0]), new File(args[1]));
		MultilayerPerceptron.main(new String[]{"-t",args[1],"-d",args[2]});
	}

}
