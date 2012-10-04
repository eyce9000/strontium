/**
 * IConstraint.java
 * 
 * Revision History: <br>
 * Jun 9, 2008 jbjohns -- Original <br>
 * Sept 15, 2008 jbjohns -- get/sets, saves parameters <br>
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

/**
 * Interface for geometric constraints. Constraints should implement a method
 * that takes a list of IConstrainable objects and solves the constraint's
 * probability/confidence of holding.
 * 
 * @author jbjohns
 * 
 */
public interface IConstraint {

	/**
	 * Take a list of shapes and compute the confidence/probability that this
	 * particular constraint holds over those shape(s). It is up to the
	 * implementing classes as to what the confidence value represents. It may
	 * be a probability between 0 and 1, or a real valued confidence, or even
	 * 0/1 for false/true boolean evaluation.
	 * <p>
	 * It is also up to individual constraints what restrictions are placed on
	 * the list of shapes. For instance, some constraints might just need 1
	 * shape, others might need > 1.
	 * <p>
	 * Sets the parameters for the constraint. It should be the case that
	 * <code>params.size() == {@link #getNumRequiredParameters()}</code>
	 * 
	 * @see #solve()
	 * @see #setParameters(List)
	 * @param params
	 *            The list of parameters to evaluate this constraint over
	 * @return The confidence/probability that this constraint holds over the
	 *         list of shapes
	 */
	public double solve(List<IConstrainable> params);

	/**
	 * Sets the threshold for the constraint, then solves the constraint. This
	 * method is designed for constraints that have customizable thresholds.
	 * Sets the parameters for the constraint.
	 * <p>
	 * Sets the parameters for the constraint. It should be the case that
	 * <code>params.size() == {@link #getNumRequiredParameters()}</code>
	 * 
	 * @see #solve()
	 * @see #setParameters(List)
	 * @see #setThreshold(double)
	 * @param params
	 *            The parameters to use to solve the constraint
	 * @param threshold
	 *            The threshold to use when computing the confidence of the
	 *            constraint
	 * @return The confidence that the constraint holds over the list of shapes
	 */
	public double solve(List<IConstrainable> params, double threshold);

	/**
	 * Solves this constraint on the parameters that have been set. If the
	 * parameters have not been set or are invalid, returns 0.
	 * 
	 * @return The confidence that this constraint holds on the parameters that
	 *         have been set.
	 */
	public double solve();

	/**
	 * Get any parameters that have been set for this constraint. This method
	 * may return a null list, but only in the case that no paramters have been
	 * set yet. Note that {@link #setParameters(List)} with a null list is not
	 * allowed.
	 * 
	 * @return The list of paramters that have been set, or possibly null if
	 *         none have been set.
	 */
	public List<IConstrainable> getParameters();

	/**
	 * Method for converting a continuous confidence value into a discrete
	 * boolean value if we should consider this constraint to be "false." This
	 * method can be used to prune constraints when the constraint doesn't think
	 * it's confident enough to be true.
	 * 
	 * @param value
	 *            The confidence value to convert to a boolean
	 * @return True if this constraint thinks the given confidence value is high
	 *         enough, or false if the constraint thinks the value is not
	 *         confident enough.
	 */
	public boolean isClearlyFalse(double value);

	/**
	 * Set the parameters that we're going to use for this constraint. It should
	 * be the case that
	 * <code>params.size() == {@link #getNumRequiredParameters()}</code>. The
	 * list may not be null nor of size 0.
	 * 
	 * @param params
	 *            The parameters for this constraint.
	 */
	public void setParameters(List<IConstrainable> params);

	/**
	 * Set the threshold for this constraint to use when solving. Thresholds
	 * must be > 0 or you get an {@link IllegalArgumentException}
	 * 
	 * @param threshold
	 *            The threshold for this constraint
	 */
	public void setThreshold(double threshold);

	/**
	 * Apply the given multiplier to the threshold. Multipliers < 1 make the
	 * threshold smaller and a lot tighter, so confidence will drop faster.
	 * Multipliers > 1 make the threshold larger and looser, so confidence will
	 * drop more slowly.
	 * <p>
	 * Multipliers must be > 0, and any values <= 0 result in an
	 * {@link IllegalArgumentException}
	 * 
	 * @param thresholdMultiplier
	 *            The value to multiply the threshold by.
	 */
	public void multiplyThreshold(double thresholdMultiplier);

	/**
	 * Get the threshold used for solving
	 * 
	 * @return The threshold used when solving
	 */
	public double getThreshold();

	/**
	 * Get the name of this constraint
	 * 
	 * @return The name of this constraint
	 */
	public String getName();

	/**
	 * Get the description of this constraint
	 * 
	 * @return The description of this constraint.
	 */
	public String getDescription();

	/**
	 * Get the number of parameters this constraint requires.
	 * 
	 * @return The number of parameters this constraint requires.
	 */
	public int getNumRequiredParameters();

	/**
	 * See if this constraint is being negated.
	 * 
	 * @return True if this constraint is being negated.
	 */
	public boolean isNegated();

	/**
	 * Set if this constraint should be negated when solving.
	 * 
	 * @param neg
	 *            True if this constraint should be negated, false if it should
	 *            be handled regularly.
	 */
	public void setNegated(boolean neg);

	/**
	 * Get the maximum confidence possible the current way constraints solve
	 * confidence.
	 * 
	 * @return A double that is the maximum confidence value attainable.
	 */
	public double getMaxConfidence();

	/**
	 * Create a new instance of this constraint. This is NOT a clone() method.
	 * 
	 * @return A new instance of this constraint.
	 */
	public IConstraint newInstance();
}
