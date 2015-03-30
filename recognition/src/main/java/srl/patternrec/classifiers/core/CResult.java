/**
 * CResult.java
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

/**
 * Classification result object
 * 
 * @author bpaulson
 */
public class CResult implements Comparable<CResult> {

	/**
	 * Constant representing a rejected result
	 */
	public static final int REJECTED = -1;

	/**
	 * Class chosen for classification
	 */
	private int m_chosen;

	/**
	 * Confidence value for result
	 */
	private double m_value;

	/**
	 * Constructor for classification result
	 * 
	 * @param classChosen
	 *            class chosen for classification
	 * @param confValue
	 *            confidence value for result
	 */
	public CResult(int classChosen, double confValue) {
		m_chosen = classChosen;
		m_value = confValue;
	}

	/**
	 * Get the chosen class
	 * 
	 * @return chosen class number
	 */
	public int getClassChosen() {
		return m_chosen;
	}

	/**
	 * Get the confidence value
	 * 
	 * @return confidence value
	 */
	public double getConfValue() {
		return m_value;
	}

	/**
	 * Set the confidence value
	 * 
	 * @param confValue
	 *            confidence value
	 */
	public void setConfValue(double confValue) {
		m_value = confValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Chosen: " + m_chosen + "\tConfidence: " + m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CResult o) {
		double vc = ((CResult) o).getConfValue();
		if (m_value > vc)
			return 1;
		if (m_value < vc)
			return -1;
		return 0;
	}

}
