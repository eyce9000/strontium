/**
 * AbstractConfidenceConstraint.java
 * 
 * Revision History: <br>
 * Jun 9, 2008 jbjohns -- Original <br>
 * July 31, 2008 : jbjohns : solveHalfGaussian to private, and added new
 * solveConfidence <br>
 * 2008 Sept 15 jbjohns -- Negations, save parameters, num reqd parameters <br>
 * 
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
package srl.recognition.constraint.confidence;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Shape;
import srl.math.Sigmoid;
import srl.math.UnivariateGaussianDistribution;
import srl.recognition.constraint.IConstrainable;
import srl.recognition.constraint.IConstraint;


/**
 * Abstract constraint class to provide additional name/description
 * functionality to the basic interface requirements.
 * 
 * @author jbjohns
 * 
 */
public abstract class AbstractConfidenceConstraint implements IConstraint {

	/**
	 * logger
	 */
	private static final Logger log = LoggerFactory
			.getLogger(AbstractConfidenceConstraint.class);

	/**
	 * This is the mean of the Gaussian that is used to compute the
	 * probabilities for constraints using the half-Gaussian method. We assume
	 * the optimal constraint has a value of 0.
	 */
	public static final double GAUSS_MEAN = 0;

	/**
	 * This is the standard deviation of the Gaussian that is used to compute
	 * the probabilities for constraints using the half-Gaussian method. We
	 * assume constraint values have been normalized such that a value of one
	 * threshold away from optimal correlates to one standard deviation. This
	 * gives "boundary" values, where the constraint value is one threshold away
	 * from the optimal value (0), about a 50% probability of holding (slightly
	 * less). This also makes all constraints that use the half-Gaussian method
	 * comparable, regardless of the magnitude of the constraint values or
	 * thresholds for the various constraints.
	 */
	public static final double GAUSS_STD = 1;

	/**
	 * The name of this constraint
	 */
	private String m_name;

	/**
	 * The description for this constraint
	 */
	private String m_description;

	/**
	 * The number of parameters that I require.
	 */
	private int m_numRequiredParameters;

	/**
	 * The threshold for this constraint. This value is to be used to evaluate
	 * the confidence of the solve() function. For instance, if you have a
	 * horizontal constraint, you might have a threshold that determines how far
	 * from horizontal you'll let a line be.
	 */
	private double m_threshold;

	/**
	 * Flag to set if this constraint should be negated
	 */
	private boolean m_negated = false;

	/**
	 * The parameters that are saved in this constraint.
	 */
	private List<IConstrainable> m_parameters = null;

	/**
	 * Should we scale the parameters of this constraint when solving
	 * confidence?
	 */
	private boolean m_scaleParameters = false;

	/**
	 * Constructor which sets the given name (not null), description (not null),
	 * number of required parameters (> 0), and threshold (> 0).
	 * 
	 * @param name
	 *            Name for this constraint (not null)
	 * @param desc
	 *            Description for this constraint (not null)
	 * @param numParams
	 *            The number of parameters this constraint requires (> 0)
	 * @param threshold
	 *            Threshold for this constraint (> 0)
	 */
	public AbstractConfidenceConstraint(String name, String desc,
			int numParams, double threshold) {
		setName(name);
		setDescription(desc);
		setThreshold(threshold);
		setNumRequiredParameters(numParams);
		m_parameters = null;
		m_negated = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#getName()
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Set the name of this constraint
	 * 
	 * @param name
	 *            The name, must not be null
	 */
	private void setName(String name) {
		if (name == null) {
			log.error("Trying to set a null name");
			throw new IllegalArgumentException("Name is not allowed to be null");
		}
		m_name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#getDescription()
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * Set the description for this constraint
	 * 
	 * @param description
	 *            The description of this constraint, may not be null
	 */
	private void setDescription(String description) {
		if (description == null) {
			log.error("Trying to set a null description");
			throw new IllegalArgumentException(
					"Description is not allowed to be null");
		}
		m_description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#getThreshold()
	 */
	public double getThreshold() {
		return m_threshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#setThreshold(double)
	 */
	public void setThreshold(double threshold) {
		if (threshold <= 0) {
			log.error("Trying to set a threshold <= 0: " + threshold);
			throw new IllegalArgumentException("Threshold must not be <= 0");
		}
		m_threshold = threshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.IConstraint#multiplyThreshold(double)
	 */
	public void multiplyThreshold(double thresholdMultiplier) {
		if (thresholdMultiplier <= 0) {
			log.error("Trying to multiply threshold by a value <= 0: "
					+ thresholdMultiplier);
			throw new IllegalArgumentException(
					"Threshold multiplier must be > 0");
		}
		log.debug("multiply threshold by " + thresholdMultiplier);
		m_threshold *= thresholdMultiplier;
	}

	/**
	 * Solve a half-gaussian probability density using the val and threshold.
	 * Input to the half-gaussian is the value normalized by the threshold.
	 * <p>
	 * If the threshold value is 0, we return 0. If threshold < 0, we take the
	 * absolute value. This method divides the constraint value by the threshold
	 * value for the constraint. We plug the normalized value into a univariate
	 * Gaussian with mean 0 and standard deviation 1, finding the probability
	 * density of the normalized value. Because we've normalized the constraint
	 * value, all constraints that use this function will have similar and
	 * consistent probability/confidence values.
	 * <p>
	 * This method is private so you can't hard code a specific method for
	 * computing confidence. Instead, you make the call to
	 * {@link #solveConfidence(double, double)} and we decide how to compute the
	 * confidence, which might be by using this method. This enforces
	 * consistency across all sub-class constraints.
	 * 
	 * @param val
	 *            The constraint value
	 * @param threshold
	 *            The threshold to use for the solving of the constraint. This
	 *            value is <b>NOT</b> saved in the object, and is transient for
	 *            this invocation only.
	 * @return The value of this constraint
	 */
	private double solveHalfGaussian(double val, double threshold) {
		log.debug("value : " + val);
		log.debug("threshold : " + threshold);

		if (threshold == 0) {
			log.error("Threshold is set to 0, resulting in divide by 0");
			return 0;
		} else if (threshold < 0) {
			threshold *= -1;

		}

		if (val < 0) {
			val *= -1;
			log.debug("val not negative: " + val);
		}

		double normalizedVal = val / threshold;
		log.debug("normalized value = val / threshold = " + normalizedVal);
		double halfGauss = 2 * UnivariateGaussianDistribution
				.probabilityDensity(normalizedVal, GAUSS_MEAN, GAUSS_STD);
		log.debug("half gauss = " + halfGauss);
		return halfGauss;
	}

	/**
	 * Solve a sigmoid function using the input val. The threshold value (abs)
	 * is used to normalize the value. This ensures that val = 0 means 0.5
	 * confidence, and val = threshold has about 75% confidence. It also ensures
	 * that no matter what the magnitude of values you expect for this
	 * constraint, that all constraints evaluate on the same scale (normalized
	 * number of thresholds).
	 * <p>
	 * threshold may not be 0, or this method returns 0.
	 * <p>
	 * Val may be any value, negative, positive, or even 0.
	 * 
	 * @param val
	 *            The value to use to solve the sigmoid
	 * @param threshold
	 *            The threshold to scale the value by. The absolute value is
	 *            taken.
	 * @return The sigmoid function solved on the given val and scaled by the
	 *         threshold
	 */
	private double solveSigmoid(double val, double threshold) {
		log.debug("value : " + val);
		log.debug("threshold : " + threshold);

		if (threshold == 0) {
			log.error("given threshold is 0, returning 0 confidence");
			return 0;
		}

		double normalizedVal = val / Math.abs(threshold);
		log.debug("Normalized value : " + normalizedVal);

		double sigmoid = Sigmoid.sigmoid(normalizedVal);
		log.debug("Sigmoid(value) : " + sigmoid);
		return sigmoid;
	}

	/**
	 * Call {@link #solveConfidence(double, double)} with {@link #m_threshold}.
	 * 
	 * @param val
	 *            The value to compute the confidence of
	 * @return The confidence
	 */
	protected final double solveConfidence(double val) {
		return solveConfidence(val, m_threshold);
	}

	/**
	 * Solve the confidence of this constraint, in whatever manner the
	 * confidence du jour is computed. This method ensures all confidence values
	 * are computed in the same manner and are consistent. It also allows for
	 * the easy changing of the way things are computed in ALL constraints that
	 * are subclasses of this abstract class, just by changing the contents of
	 * this method.
	 * 
	 * @param val
	 *            The value to compute the confidence of
	 * @param threshold
	 *            The threshold used to compute the confidence
	 * @return The confidence
	 */
	protected final double solveConfidence(double val, double threshold) {
		log.debug("val = " + val);
		log.debug("threshold = " + threshold);

		double conf = 0;

		// scaledVal is the scaled value only if isScaleParameters() returns
		// true, else it's just the original value
		log.debug("sigmoidal? " + (this instanceof SigmoidalConstraint));
		if (this instanceof SigmoidalConstraint) {
			conf = solveSigmoid(val, threshold);
		} else {
			conf = solveHalfGaussian(val, threshold);
		}
		log.debug("conf = " + conf);

		log.debug("is negated? " + isNegated());
		if (isNegated()) {
			conf = getMaxConfidence() - conf;
			log.debug("new conf (max-conf) = " + conf);
		}
		return conf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#getMaxConfidence()
	 */
	public final double getMaxConfidence() {
		// TODO better way than this hack?
		if (this instanceof SigmoidalConstraint) {
			// sigmoid never actually gets to 1, but asymptotically approaches
			// it
			return 0.999999999;
		} else {
			return solveConfidence(0.0, 1.0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve(java.util.List,
	 * double)
	 */
	public final double solve(List<IConstrainable> params, double threshold) {
		setThreshold(threshold);
		setParameters(params);
		return solve();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve(java.util.List)
	 */
	public final double solve(List<IConstrainable> params) {
		setParameters(params);
		return solve();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.IConstraint#getNumRequiredParameters()
	 */
	public int getNumRequiredParameters() {
		return m_numRequiredParameters;
	}

	/**
	 * Set the number of parameters this constraint requires. Must be > 0 or
	 * throws {@link IllegalArgumentException}
	 * 
	 * @param numParams
	 *            The number of parameters required by this constraint.
	 */
	private void setNumRequiredParameters(int numParams) {
		if (numParams <= 0) {
			log.error("Trying to set number req'd parameters <= 0: "
					+ numParams);
			throw new IllegalArgumentException(
					"Number of parameters must be > 0");
		}
		m_numRequiredParameters = numParams;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.IConstraint#setParameters(java.util
	 * .List)
	 */
	public void setParameters(List<IConstrainable> params) {
		if (params == null) {
			log.error("Cannot set parameters to null");
			throw new IllegalArgumentException(
					"Parameters are not allowed to be null");
		}
		if (params.size() != getNumRequiredParameters()) {
			log.error("Error in number of provided params (" + params.size()
					+ "), num required: " + getNumRequiredParameters());
			throw new IllegalArgumentException("You gave " + params.size()
					+ " parameters, " + getName() + " requires "
					+ getNumRequiredParameters());
		}

		log.debug("Setting parameters to " + params);
		m_parameters = params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#getParameters()
	 */
	public List<IConstrainable> getParameters() {
		return m_parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#isNegated()
	 */
	public boolean isNegated() {
		return m_negated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#setNegated(boolean)
	 */
	public void setNegated(boolean neg) {
		m_negated = neg;
	}

	/**
	 * Is this constraint performing scaling on its parameters when it solves
	 * confidence?
	 * 
	 * @return true if this constraint scales its parameters when solving
	 *         confidence
	 */
	public boolean isScaleParameters() {
		return m_scaleParameters;
	}

	/**
	 * Set if this constraint should scale its parameters before solving
	 * confidence.
	 * 
	 * @param scaleParameters
	 *            true if this constraint should scale its parameters before
	 *            solving
	 */
	public void setScaleParameters(boolean scaleParameters) {
		m_scaleParameters = scaleParameters;
	}

	/**
	 * Function used to determine a "cut-off" of when a constraint is clearly
	 * not met
	 * 
	 * @param value
	 *            value to check
	 * @return true if value denotes a constraint that is clearly not met; else
	 *         false
	 */
	public boolean isClearlyFalse(double value) {
		return value < 0.5;
	}

	/**
	 * Get the average size of the parent shapes set on the parameters. If there
	 * are no parameters or no parents set, this method returns 0.
	 * 
	 * @return the average size of the parameter parent shapes
	 */
	public double getAverageParentShapeSize() {
		// the threshold is m_threshold (a percentage) of average stroke length
		double averageParentSize = 0;
		double numParentsCounted = 0;
		for (IConstrainable parm : getParameters()) {
			Shape parent = parm.getParentShape();

			if (parent != null) {
				++numParentsCounted;
				double parentSize = parent.getBoundingBox().getDiagonalLength();
				averageParentSize += parentSize;
			}
		}

		if (numParentsCounted > 0) {
			averageParentSize /= numParentsCounted;
		}

		return averageParentSize;
	}
}
