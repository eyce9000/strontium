/**
 * PositiveSlopeConstraint.java
 * 
 * Revision History:<br>
 * Jul 8, 2008 jbjohns - File created
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
 * This class solves confidence for the positive slope constraint. Slope is
 * computed as the angle of a line (so positive slope moves up-right on the
 * monitor, regardless of the coordinate system). This constraint requires ONE
 * {@link ConstrainableLine} shape.
 * 
 * @author jbjohns
 */
public class PositiveSlopeConstraint extends AbstractConfidenceConstraint implements
        UnaryConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public PositiveSlopeConstraint newInstance() {
		return new PositiveSlopeConstraint();
	}
	
	/**
	 * Logger for this class
	 */
	private static Logger log = LoggerFactory
	        .getLogger(PositiveSlopeConstraint.class);
	
	/**
	 * name for this constraint
	 */
	public static final String NAME = "PositiveSlope";
	
	/**
	 * description for this constraint
	 */
	public static final String DESCRIPTION = "This constraint holds if the slope/angle of a line is positive";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 1;
	
	/**
	 * Default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 30;
	
	
	/**
	 * Construct a positive constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public PositiveSlopeConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct a positive constraint with the given threshold
	 * 
	 * @param threshold
	 *            Threshold for this constraint
	 */
	public PositiveSlopeConstraint(double threshold) {
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
			log.debug("Must have at least " + getNumRequiredParameters()
			          + " shape");
			return 0;
		}
		
		// TODO limit to only lines? what about arcs
		IConstrainable line = getParameters().get(0);
		
		if (line instanceof ConstrainableLine) {
			return solve((ConstrainableLine) line);
		}
		else {
			log.debug("Positive slope requires a line");
			return 0;
		}
	}
	

	/**
	 * Shortcut call if you know the types of things this constraint
	 * specifically requires, so you don't have to use a list.
	 * 
	 * @param line
	 *            The line to test for positive slope
	 * @return Confidence that the slope of the line is positive
	 */
	public double solve(ConstrainableLine line) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(line);
		setParameters(parms);
		
		// get the angle
		double angle = line.getAngleInDegrees();
		log.debug("angle = " + angle);
		
		// optimal is 45, how far from optimal?
		final double target = 45;
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
