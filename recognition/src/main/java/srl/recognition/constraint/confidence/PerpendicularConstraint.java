/**
 * PerpendicularConstraint.java
 * 
 * Revision History:<br>
 * Aug 5, 2008 jbjohns - File created
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
 * See if the slopes of two shapes are perpendicular.
 * 
 * @author jbjohns
 */
public class PerpendicularConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public PerpendicularConstraint newInstance() {
		return new PerpendicularConstraint();
	}
	
	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(PerpendicularConstraint.class);
	
	/**
	 * Name for this constraint
	 */
	public static final String NAME = "Perpendicular";
	
	/**
	 * Description of this constraint
	 */
	public static final String DESCRIPTION = "Confidence that two lines are perpendicular (difference between slopes is 90 degrees)";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * Default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 15;
	
	
	/**
	 * Construct this constraint with the default threshold
	 */
	public PerpendicularConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct this constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use when computing confidence
	 */
	public PerpendicularConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// require two shapes
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Perpendicular requires " + getNumRequiredParameters()
			          + " shapes");
			return 0;
		}
		
		IConstrainable line1 = getParameters().get(0);
		IConstrainable line2 = getParameters().get(1);
		
		// both lines?
		if (line1 instanceof ConstrainableLine
		    && line2 instanceof ConstrainableLine) {
			return solve((ConstrainableLine) line1, (ConstrainableLine) line2);
		}
		else {
			log.debug("Perpendicular requires two lines");
			return 0;
		}
	}
	

	/**
	 * Shorthand method (don't require a list of shapes) for solving the
	 * confidence that two shapes are perpendicular.
	 * 
	 * @param line1
	 *            First line
	 * @param line2
	 *            Second line
	 * @return Confidence that the angle between the shapes is 90 degrees --
	 *         perpendicular
	 */
	public double solve(ConstrainableLine line1, ConstrainableLine line2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(line1);
		parms.add(line2);
		setParameters(parms);
		
		// difference in the angles of the lines
		double angle = line1.getAngleBetweenInDegrees(line2);
		log.debug("angle = " + angle);
		
		// 90 degrees is perpendicular, how far are we?
		double deltaAngle = Math.abs(90 - angle);
		log.debug("dist from 90 = " + deltaAngle);
		
		double conf = solveConfidence(deltaAngle);
		log.debug("conf = " + conf);
		
		return conf;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.AbstractConstraint#
	 * isClearlyFalse(double)
	 */
	@Override
	public boolean isClearlyFalse(double value) {
		// was 0.4
		return value < 0.20;
	}
}
