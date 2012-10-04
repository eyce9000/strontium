/**
 * VerticalConstraint.java
 * 
 * Revision History:<br>
 * Jun 20, 2008 jbjohns - File created
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
 * Constraint to test if the slope of a line is 90 or 270. This constraint
 * requires ONE {@link ConstrainableLine}
 * 
 * @author jbjohns
 */
public class VerticalConstraint extends AbstractConfidenceConstraint implements
        UnaryConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public VerticalConstraint newInstance() {
		return new VerticalConstraint();
	}
	
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(VerticalConstraint.class);
	
	/**
	 * Default name of this constraint
	 */
	public static final String NAME = "Vertical";
	
	/**
	 * Default description of this constraint
	 */
	public static final String DESCRIPTION = "This constraint holds if the slope of the line is vertical (90 degrees)";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 1;
	
	/**
	 * Default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 15;
	
	
	/**
	 * Construct this constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public VerticalConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct this constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use for solving this constraint
	 */
	public VerticalConstraint(final double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// we need at least one shape
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Must have at least " + getNumRequiredParameters()
			          + " shape");
			return 0;
		}
		
		IConstrainable line = getParameters().get(0);
		
		if (line instanceof ConstrainableLine) {
			return solve((ConstrainableLine) line);
		}
		else {
			log.debug("Vertical requires 1 line");
			return 0;
		}
	}
	

	/**
	 * Shortcut for solving this constraint. The parameters match the
	 * expectations of the constraint exactly, no list of wrapper shapes.
	 * 
	 * @param line
	 *            The line to measure the angle of
	 * @return The confidence the angle of the line is vertical
	 */
	public double solve(ConstrainableLine line) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(line);
		setParameters(parms);
		
		// angle/slope of the stroke
		double angle = line.getAngleInDegrees();
		log.debug("Angle: " + angle);
		
		// rotate angle to be between 0 and 180
		while (angle < 0) {
			angle += 180;
		}
		while (angle >= 180) {
			angle -= 180;
		}
		log.debug("Rotated angle " + angle);
		
		// vertical is 90 degrees, how far are we from it?
		final double target = 90;
		double angleDelta = Math.abs(target - angle);
		log.debug("delta from " + target + " = " + angleDelta);
		
		// confidence... the smaller the delta, the closer we are to 90 and the
		// more confident we are. Larger delta = farther from 90 and less
		// confident.
		double conf = solveConfidence(angleDelta);
		log.debug("Confidence " + conf);
		return conf;
	}
	
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
		return value < 0.25;
	}
}
