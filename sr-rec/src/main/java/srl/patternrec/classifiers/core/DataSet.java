/**
 * DataSet.java
 * 
 * Revision History:<br>
 * Jan 14, 2009 bpaulson - File created
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import Jama.Matrix;

/**
 * DataSet class - contains a list of example sets (one set per class)
 * 
 * @author bpaulson
 */
public class DataSet implements Cloneable {

	/**
	 * list of example sets (the data)
	 */
	private List<ExampleSet> m_data = new ArrayList<ExampleSet>();

	/**
	 * list of user numbers used in the data
	 */
	private List<Integer> m_users = new ArrayList<Integer>();

	/**
	 * Map that maps the set name to the actual set
	 */
	private Map<String, ExampleSet> m_map = new HashMap<String, ExampleSet>();

	/**
	 * Get an example set from the data set
	 * 
	 * @param i
	 *            index of example set to return
	 * @return example set at index i
	 */
	public ExampleSet get(int i) {
		return m_data.get(i);
	}

	/**
	 * Reference data set as you would a matrix
	 * 
	 * @param i
	 *            index of example set
	 * @param j
	 *            index of example within example set
	 * @return classifiable
	 */
	public Classifiable get(int i, int j) {
		return m_data.get(i).get(j);
	}

	/**
	 * Get all example sets
	 * 
	 * @return list of all example sets
	 */
	public List<ExampleSet> getAllSets() {
		return m_data;
	}

	/**
	 * Get an example set based on its name
	 * 
	 * @param name
	 *            name of set to return
	 * @return set (or null if set not found)
	 */
	public ExampleSet get(String name) {
		return m_map.get(name);
	}

	/**
	 * Get the number of total examples in the dataset
	 * 
	 * @return number of total examples
	 */
	public int getNumExamples() {
		int num = 0;
		for (ExampleSet e : m_data)
			num += e.getNumExamples();
		return num;
	}

	/**
	 * Number of example sets in the data set
	 * 
	 * @return number of example sets
	 */
	public int size() {
		return m_data.size();
	}

	/**
	 * List of user numbers used in the data set
	 * 
	 * @return list of user numbers
	 */
	public List<Integer> getUserNumbers() {
		return m_users;
	}

	/**
	 * Add an example set to the data set
	 * 
	 * @param set
	 *            set to add
	 */
	public void add(ExampleSet set) {
		for (Classifiable c : set) {
			c.setClassNum(m_data.size());
			if (!m_users.contains(c.getUserNum()))
				m_users.add(c.getUserNum());
		}
		m_data.add(set);
		m_map.put(set.getLabel(), set);
	}

	/**
	 * Add the contents of another dataset to this set
	 * 
	 * @param set
	 *            dataset to add to this set
	 */
	public void add(DataSet set) {
		for (ExampleSet s : set.m_data) {
			int index = indexOf(s);
			if (index == -1)
				add(s);
			else {
				for (Classifiable c : s) {
					c.setClassNum(index);
					if (!m_users.contains(c.getUserNum()))
						m_users.add(c.getUserNum());
				}
				m_data.get(index).add(s);
			}
			m_map.put(s.getLabel(), s);
		}
	}

	/**
	 * Find the index of a given example set
	 * 
	 * @param set
	 *            set to look for
	 * @return index of set (or -1 if not found)
	 */
	public int indexOf(ExampleSet set) {
		int index = -1;
		for (int i = 0; i < size(); i++) {
			if (set.getLabel().compareTo(get(i).getLabel()) == 0)
				return i;
		}
		return index;
	}

	/**
	 * Remove and example set from the data set
	 * 
	 * @param index
	 *            index of example set to remove
	 */
	public void remove(int index) {
		m_data.remove(index);
	}

	/**
	 * Remove all examples from the data set
	 */
	public void removeAll() {
		m_data.clear();
		m_users.clear();
	}

	/**
	 * Get the iterator for a data set
	 * 
	 * @return iterator for data set
	 */
	public Iterator<ExampleSet> getIterator() {
		return m_data.iterator();
	}

	/**
	 * Get the within-class scatter matrix for the data set
	 * 
	 * @return within-class scatter matrix for the data set
	 */
	public Matrix getWithinClassScatter() {
		if (size() <= 0)
			return null;
		Matrix sw = new Matrix(m_data.get(0).getNumFeatures(), m_data.get(0)
				.getNumFeatures());
		for (ExampleSet s : m_data)
			sw.plusEquals(s.getScatterMatrix());
		return sw;
	}

	/**
	 * Get the between-class scatter matrix for the data set
	 * 
	 * @return between-class scatter matrix for the data set
	 */
	public Matrix getBetweenClassScatter() {
		if (size() <= 0)
			return null;
		Matrix sb = new Matrix(m_data.get(0).getNumFeatures(), m_data.get(0)
				.getNumFeatures());
		Matrix mu = getMeanVector();
		mu.transpose();
		double sum = 0;
		for (ExampleSet s : m_data) {
			Matrix x = s.getAvgFeatureVector();
			x.transpose();
			Matrix trans = x.minus(mu);
			trans.transpose();
			sb.plusEquals(((x.minus(mu)).times(trans)).times(s.getNumExamples()));
			sum += s.getNumExamples();
		}
		for (int i = 0; i < sb.getRowDimension(); i++)
			for (int j = 0; j < sb.getColumnDimension(); j++)
				sb.set(i, j, sb.get(i, j) / sum);
		return sb.times(size());
	}

	/**
	 * Get the mean (row) vector for the entire data set
	 * 
	 * @return mean vector for the data set
	 */
	public Matrix getMeanVector() {
		if (size() <= 0)
			return null;
		Matrix mu = new Matrix(1, m_data.get(0).getNumFeatures());
		for (ExampleSet s : m_data) {
			for (Classifiable c : s) {
				mu.plusEquals(c.toMatrix());
			}
		}
		for (int i = 0; i < mu.getColumnDimension(); i++)
			mu.set(0, i, mu.get(0, i) / getNumExamples());
		return mu;
	}

	/**
	 * Convert the dataset into matrix form
	 * 
	 * @return matrix form of m_dataset
	 */
	public Matrix toMatrix() {
		if (size() <= 0)
			return null;
		if (m_data.get(0).getNumFeatures() == -1) {
			for (ExampleSet s : m_data)
				s.calculateFeatureVectors();
		}
		Matrix m = new Matrix(getNumExamples(), m_data.get(0).getNumFeatures());
		int num = 0;
		for (ExampleSet s : m_data) {
			for (Classifiable c : s) {
				for (int i = 0; i < c.getFeatures().size(); i++)
					m.set(num, i, c.getFeatures().get(i));
				num++;
			}
		}
		return m;
	}

	/**
	 * Convert the dataset into a vector array form
	 * 
	 * @return vector array form of dataset
	 */
	public List<Vector<Double>> toVectorArray() {
		if (size() <= 0)
			return null;
		if (m_data.get(0).getNumFeatures() == -1) {
			for (ExampleSet s : m_data)
				s.calculateFeatureVectors();
		}
		List<Vector<Double>> v = new ArrayList<Vector<Double>>(getNumExamples());
		int num = 0;
		for (ExampleSet s : m_data) {
			for (Classifiable c : s) {
				for (int i = 0; i < c.getFeatures().size(); i++)
					v.get(num).add(c.getFeatures().get(i));
				num++;
			}
		}
		return v;
	}

	/**
	 * Get a vector of class labels (which correspond to each row from a
	 * toMatrix() call)
	 * 
	 * @return vector (in matrix form) of class labels
	 */
	public Matrix getLabelMatrix() {
		if (size() <= 0)
			return null;
		Matrix m = new Matrix(getNumExamples(), 1);
		int num = 0;
		for (int i = 0; i < size(); i++) {
			for (int j = 0; j < m_data.get(i).size(); j++) {
				m.set(num, 0, i);
				num++;
			}
		}
		return m;
	}

	/**
	 * Return vector version of class labels (which correspond to each row from
	 * a ToMatrix() call)
	 * 
	 * @return vector of class labels
	 */
	public Vector<Integer> getLabelVector() {
		if (size() <= 0)
			return null;
		Vector<Integer> v = new Vector<Integer>(getNumExamples());
		int num = 0;
		for (int i = 0; i < m_data.size(); i++) {
			for (int j = 0; j < m_data.get(i).size(); j++) {
				v.set(num, i);
				num++;
			}
		}
		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		DataSet s = new DataSet();
		for (ExampleSet set : m_data)
			s.add((ExampleSet) set.clone());
		return s;
	}
}
