/**
 * FeatureVector.java
 * 
 * Revision History:<br>
 * Dec 12, 2008 jbjohns - File created
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
package srl.patternrec.features;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 
 * @author jbjohns
 */
public class FeatureVector {


	/**
	 * The list of features.
	 */
	private List<Feature> m_featureVector;

	/**
	 * Create an empty feature vector.
	 */
	public FeatureVector() {
		m_featureVector = new ArrayList<Feature>();
	}

	/**
	 * Get the feature vector.
	 * 
	 * @return the featureVector
	 */
	public List<Feature> getFeatureVector() {
		return m_featureVector;
	}

	/**
	 * Set the features. You're not allowed to set nulls, and if you try this
	 * method will set the features to an empty list.
	 * 
	 * @param featureVector
	 *            the featureVector to set
	 */
	public void setFeatureVector(List<Feature> featureVector) {
		if (featureVector != null) {
			m_featureVector = featureVector;
		} else {
			m_featureVector = new ArrayList<Feature>();
		}
	}

	/**
	 * How many features are in this list?
	 * 
	 * @return the number of features;
	 */
	public int size() {
		if (m_featureVector == null) {
			return 0;
		}

		return m_featureVector.size();
	}

	/**
	 * Add the given feature to the end of this feature vector. Cannot add a
	 * null reference.
	 * 
	 * @param feature
	 *            The feature to add
	 */
	public void add(Feature feature) {
		if (feature == null) {
			throw new IllegalArgumentException("Cannot set a null feature");
		}
		if (m_featureVector == null) {
			m_featureVector = new ArrayList<Feature>();
		}

		m_featureVector.add(feature);
	}

	/**
	 * Remove the feature at the given index. Make sure index is not out of
	 * bounds.
	 * 
	 * @param idx
	 *            The index of the feature to remove.
	 * @return True if the element was removed
	 */
	public boolean remove(int idx) {
		boolean ret = false;
		if (m_featureVector != null) {
			m_featureVector.remove(idx);
			ret = true;
		}
		return ret;
	}

	/**
	 * Remove the first occurrence of a feature with the given name
	 * 
	 * @param name
	 *            The name of the feature to remove.
	 * @return True if a feature with the given name was found and removed
	 */
	public boolean remove(String name) {
		if (m_featureVector != null && name != null) {
			for (Iterator<Feature> featIter = m_featureVector.iterator(); featIter
					.hasNext();) {
				Feature feature = featIter.next();
				if (name.equalsIgnoreCase(feature.getName())) {
					featIter.remove();
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Get the feature at the given index. Make sure the index is not out of
	 * bounds.
	 * 
	 * @param idx
	 *            Index to the feature.
	 * @return The feature at the given index
	 */
	public Feature get(int idx) {
		if (m_featureVector == null) {
			return null;
		}
		return m_featureVector.get(idx);
	}

	/**
	 * Get the feature with the given name from this feature vector. Runs in
	 * O(n). Returns null if there are no features with the given name, or if
	 * the given name is null.
	 * 
	 * @param featureName
	 *            The name of the feature to get
	 * @return The Feature from the feature vector that has the given name
	 */
	public Feature get(String featureName) {

		if (m_featureVector == null || featureName == null) {
			return null;
		}

		Feature ret = null;
		for (Feature feature : m_featureVector) {
			if (feature != null
					&& featureName.equalsIgnoreCase(feature.getName())) {

				ret = feature;
				break;
			}
		}

		return ret;
	}

	/**
	 * Get the list of values, comma delimited
	 * 
	 * @return String representation of this feature vector
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Feature f : m_featureVector) {
			if (sb.length() > 0) {
				// sb.append("\t");
				sb.append(',');
			}
			// sb.append(f.getName()).append('(').append(f.getValue()).append(')');
			sb.append(f.getValue());
		}

		return sb.toString();
	}
}
