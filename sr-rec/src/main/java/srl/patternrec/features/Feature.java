/**
 * Feature.java
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Class representing a named feature. This class is immutable after
 * construction time.
 * 
 * @author jbjohns
 */
public class Feature {

	/**
	 * Logger for this class
	 */
	private static Logger log = LoggerFactory.getLogger(Feature.class);

	/**
	 * Name of the feature
	 */
	private String m_name;

	/**
	 * Value of the feature
	 */
	private double m_value;

	/**
	 * Set the name and value of the feature at construction time. Name may not
	 * be null;
	 * 
	 * @param name
	 *            The name, may not be null
	 * @param value
	 *            The value
	 */
	public Feature(String name, double value) {
		setName(name);
		setValue(value);
	}

	/**
	 * Get the name of the feature.
	 * 
	 * @return the name
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Immutable to outsiders. Name may not be set to null.
	 * 
	 * @param name
	 *            the name to set
	 */
	private void setName(String name) {
		if (name == null) {
			log.error("Cannot set name to null");
			throw new IllegalArgumentException("Name may not be null");
		}
		m_name = name;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return m_value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	private void setValue(double value) {
		m_value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName() + "(" + getValue() + ")";
	}

}
