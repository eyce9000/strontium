/**
 * SlantedConstraint.java
 * 
 * Revision History:<br>
 * Aug 4, 2008 jbjohns - File created
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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.recognition.constraint.IConstrainable;
import srl.recognition.constraint.constrainable.ConstrainableLine;


/**
 * A line is slanted if it has a positive or negative slope (anything but
 * horizontal or vertical). This constraint requires ONE
 * {@link ConstrainableLine} shape.
 * 
 * @author jbjohns
 */
public class SlantedConstraint extends AbstractConfidenceConstraint implements
        UnaryConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public SlantedConstraint newInstance() {
		return new SlantedConstraint();
	}
	
	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SlantedConstraint.class);
	
	/**
	 * Name of this constraint
	 */
	public static final String NAME = "Slanted";
	
	/**
	 * Description for this constraint
	 */
	public static final String DESCRIPTION = "A line is slanted if it has a positive or negative slope (is neither horizontal nor vertical)";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 1;
	
	/**
	 * This is currently not used, since this constraint uses other constraints
	 */
	public static final double DEFAULT_THRESHOLD = 1;
	
	/**
	 * Constraint to see if the line has a positive slope
	 */
	protected PositiveSlopeConstraint m_posSlope = new PositiveSlopeConstraint();
	
	/**
	 * Constraint to see if the line has a negative slope
	 */
	protected NegativeSlopeConstraint m_negSlope = new NegativeSlopeConstraint();
	
	
	/**
	 * Construct the constraint with the name, desc, and default threshold
	 */
	public SlantedConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the name, desc, and given threshold
	 * 
	 * @param threshold
	 *            The threshold to use when computing this constraint
	 */
	public SlantedConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.AbstractConstraint#
	 * multiplyThreshold(double)
	 */
	@Override
	public void multiplyThreshold(double thresholdMultiplier) {
		super.multiplyThreshold(thresholdMultiplier);
		m_posSlope.multiplyThreshold(thresholdMultiplier);
		m_negSlope.multiplyThreshold(thresholdMultiplier);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// requires just one shape
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Slanted requires " + getNumRequiredParameters()
			          + " shape");
			return 0;
		}
		
		IConstrainable line = getParameters().get(0);
		
		// TODO limit to only lines? what about arcs
		if (line instanceof ConstrainableLine) {
			return solve((ConstrainableLine) line);
		}
		else {
			log.debug("Slanted requires a line");
			return 0;
		}
	}
	
	/**
	 * Shorthand method (don't have to wrap in a list of shapes) for seeing if
	 * the given line is slanted. This constraint uses the negative and positive
	 * slope constraints to make sure the line is neither horizontal nor
	 * negative.
	 * 
	 * @param shape
	 *            See if this shape is slanted or not
	 * @return The confidence this shape is slanted
	 */
	public double solve(ConstrainableLine shape) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(shape);
		setParameters(parms);
		
		// TODO use the threshold here... plug it into constraints? or use as
		// a multiplier for confidences?
		double posConf = m_posSlope.solve(shape);
		log.debug("positive conf = " + posConf);
		
		double negConf = m_negSlope.solve(shape);
		log.debug("negative conf = " + negConf);
		
		// TODO how to or?
		return Math.max(posConf, negConf);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.UnaryConstraint#
	 * isUnaryConstraint()
	 */
	public boolean isUnaryConstraint() {
		return true;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.AbstractConstraint#
	 * isClearlyFalse(double)
	 */
	@Override
	public boolean isClearlyFalse(double value) {
		return value < 0.40;
	}
}
