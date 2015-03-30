/**
 * Classifier.java
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

import java.util.List;

/**
 * Classifier abstract class
 * 
 * @author bpaulson
 */
public abstract class Classifier {

	/**
	 * Function used to train the classifier
	 */
	public abstract void train();

	/**
	 * Function used to classify a query
	 * 
	 * @param query
	 *            query to classify
	 * @return list of classification results
	 */
	public abstract List<CResult> classify(Classifiable query);

	/**
	 * Training data
	 */
	protected DataSet m_train;

	/**
	 * Number of features
	 */
	protected int m_numFeatures = -1;

	/**
	 * Set the training data for the classifier
	 * 
	 * @param trainExamples
	 *            training examples
	 * @throws Exception
	 */
	public void setTrainData(DataSet trainExamples) throws Exception {
		m_train = trainExamples;
		if (m_train.size() == 0)
			throw new Exception("No Training Examples!");
		for (int i = 0; i < m_train.size(); i++)
			if (m_train.get(i).size() == 0)
				throw new Exception("Example size zero for set #" + i + "!");
		for (int i = 0; i < m_train.size(); i++)
			m_train.get(i).calculateFeatureVectors();
		m_numFeatures = m_train.get(0).getNumFeatures();
		for (int i = 0; i < m_train.size(); i++)
			for (int j = 0; j < m_train.get(i).size(); j++)
				if (m_train.get(i).get(j).getNumFeatures() != m_numFeatures)
					throw new Exception("Number Of Features Not Uniform!");
	}

	/**
	 * String description of classifier
	 * 
	 * @return string description
	 */
	@Override
	public abstract String toString();
}
