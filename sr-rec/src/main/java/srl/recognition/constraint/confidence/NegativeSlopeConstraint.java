/**
 * NegativeSlopeConstraint.java
 * 
 * Revision History:<br>
 * Jul 9, 2008 jbjohns - File created
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
 * 
 * See if a line has a negative slope. This constraint requires ONE
 * {@link ConstrainableLine}
 * 
 * @author jbjohns
 */
public class NegativeSlopeConstraint extends AbstractConfidenceConstraint implements
        UnaryConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public NegativeSlopeConstraint newInstance() {
		return new NegativeSlopeConstraint();
	}
	
	/**
	 * logger
	 */
	private static final Logger log = LoggerFactory
	        .getLogger(NegativeSlopeConstraint.class);
	
	/**
	 * Default name for this constraint
	 */
	public static final String NAME = "NegativeSlope";
	
	/**
	 * Default description for this constraint
	 */
	public static final String DESCRIPTION = "The negative slope constraint holds if the slope of a line is negative";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 1;
	
	/**
	 * Default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 30;
	
	
	/**
	 * Construct the constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public NegativeSlopeConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold and default name and
	 * description
	 * 
	 * @param threshold
	 *            The threshold to use for the constraint
	 */
	public NegativeSlopeConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// we need one shape
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Constraint requires " + getNumRequiredParameters()
			          + " line");
			return 0;
		}
		IConstrainable sh = getParameters().get(0);
		if (sh instanceof ConstrainableLine) {
			return solve((ConstrainableLine) sh);
		}
		else {
			log.debug("Constraint requires one Line");
			return 0;
		}
	}
	

	/**
	 * See if the slope of the line is negative
	 * 
	 * @param line
	 *            The shape to test for negative slope
	 * @return The confidence that the slope of the shape is negative
	 */
	public double solve(ConstrainableLine line) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(line);
		setParameters(parms);
		
		// get the angle
		double angle = line.getAngleInDegrees();
		log.debug("angle = " + angle);
		
		// optimal is 135 (45+90), how far from optimal?
		final double target = 135;
		double deltaAngle = Math.abs(target - angle);
		log.debug("delta angle = " + deltaAngle);
		
		// rotate into -90...90
		while (deltaAngle > 90) {
			deltaAngle -= 180;
		}
		while (deltaAngle < -90) {
			deltaAngle += 180;
		}
		log.debug("rotated angle = " + angle);
		
		// compute confidence and return
		double conf = solveConfidence(Math.abs(deltaAngle));
		log.debug("conf = " + conf);
		return conf;
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
		return value < 0.25;
	}
}
