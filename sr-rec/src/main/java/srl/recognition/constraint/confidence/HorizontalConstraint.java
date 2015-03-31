/**
 * HorizontalConstraint.java
 * 
 * Revision History: <br>
 * Jun 9, 2008 jbjohns -- Original
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
 * Constraint to test if the slope of a line is 0 or 180. This constraint
 * requires ONE {@link ConstrainableLine}
 * 
 * @author jbjohns
 * 
 */
public class HorizontalConstraint extends AbstractConfidenceConstraint implements
        UnaryConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public HorizontalConstraint newInstance() {
		return new HorizontalConstraint();
	}
	
	/**
	 * Logger
	 */
	private Logger log = LoggerFactory.getLogger(HorizontalConstraint.class);
	
	/**
	 * the default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 15;
	
	/**
	 * The default name for this constraint
	 */
	public static final String NAME = "Horizontal";
	
	/**
	 * The default description for this constraint
	 */
	public static final String DESCRIPTION = "This constraint holds if the slope of the line is 0";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 1;
	
	
	/**
	 * Construct a horizontal constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public HorizontalConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct a vertical constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use when solving this constraint
	 */
	public HorizontalConstraint(final double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Horizontal requires " + getNumRequiredParameters()
			          + " line");
			return 0;
		}
		
		IConstrainable line = getParameters().get(0);
		
		if (line instanceof ConstrainableLine) {
			return solve((ConstrainableLine) line);
		}
		else {
			log.debug("Horizontal requires 1 line");
			return 0;
		}
		
	}
	

	/**
	 * Shortcut call so solve the horizontal constraint on one shape, so you
	 * don't have to wrap in a list of shapes. Compute the confidence that the
	 * slope of the line == 0 or 180.
	 * 
	 * @param line
	 *            The line to solve the horizontal constraint on
	 * @return The confidence that the line is horizontal
	 */
	public double solve(ConstrainableLine line) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(line);
		setParameters(parms);
		
		// get the angle of the stroke
		double angle = line.getAngleInDegrees();
		log.debug("Angle of shape : " + angle);
		
		// get the angle between 0 and 180
		while (angle < 0) {
			angle += 180;
		}
		while (angle >= 180) {
			angle -= 180;
		}
		log.debug("Rotated to : " + angle);
		
		// 0 and 180 are both horizontal, which one are we closest to?
		double angleDelta = Math.min(angle, 180 - angle);
		log.debug("Dist from horizontal : " + angleDelta);
		
		// the delta is our value, the closer we are to horizontal this smaller
		// this delta will be.
		// The farther we are from horizontal, the larger this delta will be
		double conf = solveConfidence(angleDelta);
		log.debug("Confidence with thresh<" + getThreshold() + "> = " + conf);
		return conf;
	}

	//Maybe this will work and then I can do this my way...
	public double solve(double slope)
	{
		double conf = solveConfidence(slope);
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
		// was 0.5
		return value < 0.4;
	}
}
