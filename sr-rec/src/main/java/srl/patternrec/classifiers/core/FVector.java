/**
 * FVector.java
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature Vector class which can be used to wrap existing classifiable objects
 * (in order to save memory)
 * 
 * @author bpaulson
 */
public class FVector extends Classifiable implements Serializable{

	/**
	 * List of feature names
	 */
	private List<String> m_featureNames = new ArrayList<String>();

	/**
	 * Constructor for feature vector
	 * 
	 * @param c
	 *            the classifiable object to wrap
	 */
	public FVector(Classifiable c) {
		if (c.getNumFeatures() <= 0)
			c.calculateFeatures();
		for (int i = 0; i < c.getNumFeatures(); i++)
			m_features.set(i, c.getFeature(i));
	}

	/**
	 * Default constructor
	 */
	public FVector() {

	}

	/**
	 * Parses a line from a CSV file and returns a feature vector
	 * 
	 * @param s
	 *            line from a CSV file
	 * @return feature vector containing the values from the string
	 */
	public static FVector parseCSVLine(String s) {
		FVector v = new FVector();
		int i = s.indexOf(',');
		while (i > 0) {
			String ss = s.substring(0, i);
			ss = ss.trim();
			v.getFeatures().add(Double.parseDouble(ss));
			s = s.substring(i + 1);
			i = s.indexOf(',');
		}
		s = s.trim();
		v.getFeatures().add(Double.parseDouble(s));
		return v;
	}

	/**
	 * Add a feature value to the vector
	 * 
	 * @param d
	 *            feature value
	 * @throws Exception
	 */
	public void add(double d) throws Exception {
		// if (Double.isInfinite(d) || Double.isNaN(d))
		// throw new Exception("bad value: " + d);
		m_featureNames.add("Unknown");
		getFeatures().add(d);
	}

	/**
	 * Add a feature value along with its name to the vector
	 * 
	 * @param d
	 *            feature value
	 * @param name
	 *            feature name
	 * @throws Exception
	 */
	public void add(double d, String name) throws Exception {
		m_featureNames.add(name);
		getFeatures().add(d);
	}
	
	/**
	 * Appends features from another <code>FVector</code> to this one. 
	 * If there are two features with the same name, the value from this <code>FVector</code> will be retained and the other will be disregarded.
	 * @param other Another <code>FVector</code> to copy fields from.
	 * @throws Exception 
	 */
	public void appendAll(FVector other) throws Exception{
		Map<String,Double> featureMap = this.getFeatureMap();
		for(int i=0; i<other.getNumFeatures(); i++){
			String name = other.getFeatureName(i);
			if(!featureMap.containsKey(name))
				this.add(other.getFeature(i), name);
		}
	}

	/**
	 * Get the name of the feature at index i
	 * 
	 * @param i
	 *            index
	 */
	public String getFeatureName(int i) {
		return m_featureNames.get(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.classifiers.core.Classifiable#calculateFeatures()
	 */
	@Override
	public void calculateFeatures() {

		// should have been calculated in constructor
		return;
	}

	public Map<String,Double> getFeatureMap(){
		HashMap<String,Double> map = new HashMap<String,Double>();
		for(int i=0; i<m_featureNames.size(); i++){
			String name = m_featureNames.get(i);
			Double value = getFeatures().get(i);
			map.put(name, value);
		}
		return map;
	}
}
