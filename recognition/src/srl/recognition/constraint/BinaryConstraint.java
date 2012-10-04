/**
 * BinaryConstraint.java
 * 
 * Revision History:<br>
 * Mar 3, 2009 joshua - File created
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
package srl.recognition.constraint;

import java.util.List;

import srl.recognition.constraint.confidence.AbstractConfidenceConstraint;
import srl.recognition.constraint.confidence.SigmoidalConstraint;


/**
 * This class wraps a constraint and turns it from a continuous, real-valued
 * constraint into a binary constraint.
 * 
 * @author joshua
 */
public class BinaryConstraint implements IConstraint {

	/**
	 * The constraint to turn binary.
	 */
	private AbstractConfidenceConstraint m_constraint;

	/**
	 * Values less than this are false. Values greater are true.
	 */
	private double m_decisionValue;

	public BinaryConstraint(AbstractConfidenceConstraint constraint) {
		if (constraint == null) {
			throw new NullPointerException("Constraint to wrap cannot be null");
		}
		m_constraint = constraint;

		m_decisionValue = 0.5;
		if (m_constraint instanceof SigmoidalConstraint) {
			m_decisionValue = 0.75;
		}
	}

	/**
	 * Confidence values of the wrapped constraint turn into true/1 if they are
	 * above this value, and 0/false if they are below this value.
	 * 
	 * @return The cutoff decision boundary value used to turn the real-valued
	 *         confidence of the wrapped constraint into a binary decision.
	 */
	public double getDecisionValue() {
		return m_decisionValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#getDescription()
	 */
	public String getDescription() {
		return m_constraint.getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#getMaxConfidence()
	 */
	public double getMaxConfidence() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#getName()
	 */
	public String getName() {
		return m_constraint.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.IConstraint#getNumRequiredParameters()
	 */
	public int getNumRequiredParameters() {
		return m_constraint.getNumRequiredParameters();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#getParameters()
	 */
	public List<IConstrainable> getParameters() {
		return m_constraint.getParameters();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#getThreshold()
	 */
	public double getThreshold() {
		return m_constraint.getThreshold();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#isClearlyFalse(double)
	 */
	public boolean isClearlyFalse(double value) {
		return (value <= 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#isNegated()
	 */
	public boolean isNegated() {
		return m_constraint.isNegated();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.IConstraint#multiplyThreshold(double)
	 */
	public void multiplyThreshold(double thresholdMultiplier) {
		m_constraint.multiplyThreshold(thresholdMultiplier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	public BinaryConstraint newInstance() {
		AbstractConfidenceConstraint con = (AbstractConfidenceConstraint) m_constraint
				.newInstance();
		BinaryConstraint newBinary = new BinaryConstraint(con);

		return newBinary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#setNegated(boolean)
	 */
	public void setNegated(boolean neg) {
		m_constraint.setNegated(neg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.IConstraint#setParameters(java.util
	 * .List)
	 */
	public void setParameters(List<IConstrainable> params) {
		m_constraint.setParameters(params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#setThreshold(double)
	 */
	public void setThreshold(double threshold) {
		m_constraint.setThreshold(threshold);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve(java.util.List)
	 */
	public double solve(List<IConstrainable> params) {
		return solve(params, getThreshold());
	}

	/**
	 * Boolean version of {@link #solve(List)}. This method just converts an
	 * answer of 0 or 1 into false or true.
	 * 
	 * @param params
	 *            Parameters of the constraint
	 * @return True if the constraint holds, false if it does not.
	 */
	public boolean solveBinary(List<IConstrainable> params) {
		return solve(params) == 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve(java.util.List,
	 * double)
	 */
	public double solve(List<IConstrainable> params, double threshold) {
		double conf = m_constraint.solve(params, threshold);
		return (conf > m_decisionValue) ? 1 : 0;
	}

	/**
	 * Boolean version of {@link #solve(List, double)}. This method just
	 * converts an answer of 0 or 1 into false or true.
	 * 
	 * @param params
	 *            Parameters of the constraint
	 * @return True if the constraint holds, false if it does not.
	 */
	public boolean solveBinary(List<IConstrainable> params, double threshold) {
		return (solve(params, threshold) == 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		return solve(getParameters(), getThreshold());
	}

	/**
	 * Boolean version of {@link #solve()}. This method just converts an answer
	 * of 0 or 1 into false or true.
	 * 
	 * @param params
	 *            Parameters of the constraint
	 * @return True if the constraint holds, false if it does not.
	 */
	public boolean solveBinary() {
		return solve() == 1;
	}
}
