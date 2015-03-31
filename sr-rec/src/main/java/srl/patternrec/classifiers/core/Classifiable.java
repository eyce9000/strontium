/**
 * Classifiable.java
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

import Jama.Matrix;

/**
 * Classifiable abstract class
 * 
 * @author bpaulson
 */
public abstract class Classifiable{

	/**
	 * Vector containing the feature values (should be doubles)
	 */
	protected CList m_features = new CList();

	/**
	 * Calculate the features - should be overridden by subclass
	 */
	abstract public void calculateFeatures();

	/**
	 * Class number that this example is from
	 */
	protected int m_classNum = -1;

	/**
	 * User number that this example is from
	 */
	protected int m_userNum = -1;

	/**
	 * Return the number of features
	 * 
	 * @return number of features
	 */
	public int getNumFeatures() {
		return m_features.size();
	}

	/**
	 * Get the features in matrix (row vector) form
	 * 
	 * @return row vector of features
	 */
	public CList getFeatures() {
		return m_features;
	}

	/**
	 * Set the feature list
	 * 
	 * @param features
	 *            feature list
	 */
	public void setFeatures(CList features) {
		m_features = features;
	}

	/**
	 * Class number that the object belongs to
	 * 
	 * @return class number
	 */
	public int getClassNum() {
		return m_classNum;
	}

	/**
	 * Set the class number that the object belongs to
	 * 
	 * @param classNum
	 *            class number
	 */
	public void setClassNum(int classNum) {
		m_classNum = classNum;
	}

	/**
	 * Set the user number that the class belongs to
	 * 
	 * @return user number
	 */
	public int getUserNum() {
		return m_userNum;
	}

	/**
	 * Set the user number that the class belongs to
	 * 
	 * @param userNum
	 *            user number
	 */
	public void setUserNum(int userNum) {
		m_userNum = userNum;
	}

	/**
	 * Get the value of the given feature number
	 * 
	 * @param featureNum
	 *            feature number
	 * @return value of the given feature number
	 */
	public double getFeature(int featureNum) {
		return m_features.get(featureNum);
	}

	/**
	 * Compare the feature similarities of two Classifiable objects - can be
	 * overridden to use different distance metric; uses Euclidean by default
	 * 
	 * @param c
	 *            classifiable object to compare to
	 * @return distance between two objects
	 */
	public double compareFeatures(Classifiable c) {
		double err = 0.0;
		for (int i = 0; i < getNumFeatures(); i++) {
			err += Math.sqrt(Math.pow(c.getFeature(i) - getFeature(i), 2));
		}
		return err;
	}

	/**
	 * Converts feature vector to a matrix
	 * 
	 * @return matrix (row vector) version of feature values
	 */
	public Matrix toMatrix() {
		Matrix m = new Matrix(1, getNumFeatures());
		for (int i = 0; i < getNumFeatures(); i++)
			m.set(0, i, getFeature(i));
		return m;
	}

	/**
	 * Convert classifiable object to csv line
	 * 
	 * @return string version of csv line
	 */
	public String toCSV() {
		String x = "";
		if (getNumFeatures() > 0) {
			for (int i = 0; i < getNumFeatures() - 1; i++)
				x += m_features.get(i) + ",";
			x += m_features.get(getNumFeatures() - 1);
		}
		return x;
	}
}
