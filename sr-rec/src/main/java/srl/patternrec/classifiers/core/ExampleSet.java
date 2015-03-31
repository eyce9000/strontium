/**
 * ExampleSet.java
 * 
 * Revision History:<br>
 * Jan 13, 2009 bpaulson - File created
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
package srl.patternrec.classifiers.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import srl.patternrec.classifiers.core.helper.MatrixHelper;
import Jama.Matrix;

/**
 * Example set class - contains examples (vector of classifiables) for one
 * particular classification class
 * 
 * @author bpaulson
 */
public class ExampleSet extends Vector<Classifiable> implements Cloneable {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1014220088333691537L;

	/**
	 * Covariance matrix
	 */
	private Matrix m_cov;

	/**
	 * Feature value weights (vector)
	 */
	private Matrix m_weights;

	/**
	 * Number of features used the example set
	 */
	private int m_numFeatures = -1;

	/**
	 * Label for example set
	 */
	private String m_label = "";

	/**
	 * Get the label of the set
	 */
	public String getLabel() {
		return m_label;
	}

	/**
	 * Set the label of the example set
	 * 
	 * @param label
	 *            label
	 */
	public void setLabel(String label) {
		m_label = label;
	}

	/**
	 * Calculate the feature vectors for all examples in the set
	 */
	public void calculateFeatureVectors() {
		for (Classifiable c : this) {
			if (c.getNumFeatures() <= 0)
				c.calculateFeatures();
			m_numFeatures = c.getNumFeatures();
		}
	}

	/**
	 * Add the entire contents of an example set to this example set
	 * 
	 * @param set
	 *            set to add examples from
	 */
	public void add(ExampleSet set) {
		for (Classifiable c : set)
			add(c);
		if (m_label == "")
			m_label = set.getLabel();
	}

	/**
	 * Convert the example set into matrix form
	 * 
	 * @return matrix form of example set
	 */
	public Matrix toMatrix() {
		if (size() <= 0)
			return null;
		if (m_numFeatures == -1)
			calculateFeatureVectors();
		Matrix m = new Matrix(getNumExamples(), getNumFeatures());
		int num = 0;
		for (Classifiable c : this) {
			for (int i = 0; i < c.getNumFeatures(); i++)
				m.set(num, i, c.getFeature(i));
			num++;
		}
		return m;
	}

	/**
	 * Return the feature mean of a given feature for all the examples in the
	 * set
	 * 
	 * @param featureNum
	 *            feature number to get the mean for
	 * @return mean of the given feature number
	 */
	public double getFeatureMean(int featureNum) {
		double sum = 0;
		double num = 0;
		if (size() == 0)
			return 0;
		for (Classifiable c : this) {
			double val = c.getFeature(featureNum);
			if (!Double.isNaN(val) && !Double.isInfinite(val)) {
				sum += c.getFeature(featureNum);
				num++;
			}
		}
		return sum / num;
	}

	/**
	 * Return the standard deviation of a given feature for all the examples in
	 * the set
	 * 
	 * @param featureNum
	 *            feature number to get the stddev for
	 * @return standard deviation of the given feature number
	 */
	public double stdDev(int featureNum) {
		double mean = getFeatureMean(featureNum);
		double stdDev = 0.0;
		for (Classifiable c : this)
			stdDev += Math.pow(c.getFeature(featureNum) - mean, 2);
		stdDev /= (m_numFeatures - 1);
		return Math.sqrt(stdDev);
	}

	/**
	 * Get a feature vector (single row vector) containing the values of a given
	 * feature number for each example in the set
	 * 
	 * @param featureNum
	 *            feature number
	 * @return row vector containing the value of the given feature number
	 */
	public Matrix getFeatureVector(int featureNum) {
		Matrix fv = new Matrix(1, size());
		for (int i = 0; i < size(); i++)
			fv.set(0, i, get(i).getFeature(featureNum));
		return fv;
	}

	/**
	 * Get the number of features being used by the example set
	 * 
	 * @return number of features used by set
	 */
	public int getNumFeatures() {

		return m_numFeatures;
	}

	/**
	 * Get a row vector containing the mean for each feature
	 * 
	 * @return row vector of feature means
	 */
	public Matrix getAvgFeatureVector() {
		Matrix avg = new Matrix(1, m_numFeatures);
		for (int i = 0; i < m_numFeatures; i++)
			avg.set(0, i, getFeatureMean(i));
		return avg;
	}

	/**
	 * Get the scatter matrix for this set
	 * 
	 * @return scatter matrix for set
	 */
	public Matrix getScatterMatrix() {
		Matrix data = toMatrix();
		Matrix mean = getMeanMatrix();
		Matrix diffT = data.minus(mean);
		diffT.transpose();
		Matrix scat = diffT.times(data.minus(mean));
		for (int i = 0; i < scat.getRowDimension(); i++)
			for (int j = 0; j < scat.getRowDimension(); j++)
				scat.set(i, j, scat.get(i, j) / getNumExamples());
		return scat;
	}

	/**
	 * Compute the covariance matrix of the example set
	 */
	public void computeCovMatrix() {
		Matrix data = toMatrix();
		m_cov = MatrixHelper.cov(data);
	}

	/**
	 * Get the covariance matrix for this example set
	 */
	public Matrix getCovMatrix() {
		return m_cov;
	}

	/**
	 * Get the feature weights
	 * 
	 * @return weights
	 */
	public Matrix getWeights() {
		return m_weights;
	}

	/**
	 * Set the feature weights
	 * 
	 * @param weights
	 *            weights
	 */
	public void setWeights(Matrix weights) {
		m_weights = weights;
	}

	/**
	 * Get the weight of a particular feature
	 * 
	 * @param featureNum
	 *            feature number of the weight to get
	 * @return feature weight
	 */
	public double getWeight(int featureNum) {
		return m_weights.get(0, featureNum);
	}

	/**
	 * Get a matrix containing the feature means
	 * 
	 * @return matrix containing feature means
	 */
	public Matrix getMeanMatrix() {
		Matrix m = new Matrix(getNumExamples(), getNumFeatures());
		for (int i = 0; i < getNumExamples(); i++)
			for (int j = 0; j < getNumFeatures(); j++)
				m.set(i, j, getFeatureMean(j));
		return m;
	}

	/**
	 * Number of examples in the set
	 * 
	 * @return number of examples in set
	 */
	public int getNumExamples() {
		return size();
	}

	/**
	 * Read in an example set from a comma separated value (csv) file
	 * 
	 * @param path
	 *            path of the file to read in from
	 * @return example set containing the data from the file
	 */
	public static ExampleSet csvRead(String path) throws IOException {
		ExampleSet s = new ExampleSet();
		int num = -1;
		BufferedReader read = new BufferedReader(new FileReader(path));
		String str;
		while ((str = read.readLine()) != null) {
			FVector v = FVector.parseCSVLine(str);
			if (num != -1 && v.getNumFeatures() != num)
				throw new IOException(
						"Data set contains an uneven number of features for each example.");
			if (num == -1)
				num = v.getNumFeatures();
			s.add(v);
		}
		return s;
	}

	/**
	 * Write an example set out to a comma separated value (csv) file
	 * 
	 * @param path
	 *            path of the file to write to
	 * @param append
	 *            append to file
	 */
	public void csvWrite(String path, boolean append) throws IOException {
		BufferedWriter write = new BufferedWriter(new FileWriter(path, append));
		for (Classifiable c : this) {
			write.write(c.toCSV());
			write.newLine();
		}
		write.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Vector#clone()
	 */
	@Override
	public Object clone() {
		ExampleSet s = new ExampleSet();
		for (Classifiable c : this)
			s.add(c);
		s.setLabel(m_label);
		return s;
	}

}
