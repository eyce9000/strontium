/**
 * AndConstraint.java
 * 
 * Revision History:<br>
 * Jan 21, 2009 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
package srl.recognition.constraint.confidence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import srl.recognition.constraint.IConstraint;


/**
 * This class can be used to enforce consistent AND-ing of values for combining
 * multiple constraints together
 * 
 * @author jbjohns
 */
public class AndConstraint {
	
	/**
	 * Constraints that we're AND-ing together
	 */
	private List<IConstraint> m_parameters;
	
	
	/**
	 * Create the And with an empty list of parameters
	 */
	public AndConstraint() {
		m_parameters = new ArrayList<IConstraint>();
	}
	

	/**
	 * Create the And and add all the parameters in the given list to it.
	 * 
	 * @param parameters
	 *            The parameters to add, may not be null
	 */
	public AndConstraint(List<IConstraint> parameters) {
		this();
		addParameters(parameters);
	}
	

	/**
	 * Create the And and add all the parameters in the given array to it.
	 * 
	 * @param parameters
	 *            Parameters to add, may not be null
	 */
	public AndConstraint(IConstraint[] parameters) {
		this();
		addParameters(Arrays.asList(parameters));
	}
	

	/**
	 * Add the given constraint as a parameters to AND together
	 * 
	 * @param parameter
	 *            The constraint to add to the AND, may not be null
	 */
	public void addParameter(IConstraint parameter) {
		if (parameter == null) {
			throw new NullPointerException("Parameter cannot be null");
		}
		m_parameters.add(parameter);
	}
	

	/**
	 * Add all the constraints in the list as parameters to AND together
	 * 
	 * @param parameters
	 *            The constraints to add to the AND, list may not be null
	 */
	public void addParameters(List<IConstraint> parameters) {
		if (parameters == null) {
			throw new NullPointerException("List of parameters cannot be null");
		}
		m_parameters.addAll(parameters);
	}
	

	/**
	 * Perform the AND on all the constraints that have been added as parameters
	 * and return the confidence. If no parameters have been added, the
	 * confidence will be 0.
	 * 
	 * @return The confidence of AND-ing all the constraints added as parameters
	 */
	public double solve() {
		return AndConstraint.solve(m_parameters);
	}
	

	/**
	 * Static method for AND-ing all the constraints in the list and returning
	 * the confidence
	 * 
	 * @param parameters
	 *            List of constraints to use as parameters for the AND
	 * @return The confidence of the AND
	 */
	public static double solve(List<IConstraint> parameters) {
		double conf = 0;
		
		// TODO the best way to do things?
		if (parameters.size() > 0) {
//			conf = Double.MAX_VALUE;
			for (IConstraint constraint : parameters) {
				conf += constraint.solve();
			}
			conf /= parameters.size();
		}
		return conf;
	}
}
