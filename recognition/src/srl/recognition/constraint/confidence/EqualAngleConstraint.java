/**
 * EqualAngleConstraint.java
 * 
 * Revision History:<br>
 * Jul 15, 2008 jbjohns - File created
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
 * Compute the constraint determining if the angles between two pairs lines are
 * equal. This constraint requires FOUR {@link ConstrainableLine}
 * 
 * @author jbjohns
 */
public class EqualAngleConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public EqualAngleConstraint newInstance() {
		return new EqualAngleConstraint();
	}
	
	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(EqualAngleConstraint.class);
	
	/**
	 * The default name for this constraint
	 */
	public static final String NAME = "EqualAngle";
	
	/**
	 * The default description for this constraint
	 */
	public static final String DESCRIPTION = "This constraint holds if the angles between two pairs of lines are equal";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 4;
	
	/**
	 * The default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 15;
	
	
	/**
	 * Construct the constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public EqualAngleConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use when solving this constraint
	 */
	public EqualAngleConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// requires 4 lines, two pairs
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Equal Angle requires " + getNumRequiredParameters()
			          + " lines (two pairs)");
			return 0;
		}
		
		IConstrainable line1 = getParameters().get(0);
		IConstrainable line2 = getParameters().get(1);
		IConstrainable line3 = getParameters().get(2);
		IConstrainable line4 = getParameters().get(3);
		
		if (line1 instanceof ConstrainableLine
		    && line2 instanceof ConstrainableLine
		    && line3 instanceof ConstrainableLine
		    && line4 instanceof ConstrainableLine) {
			
			return solve((ConstrainableLine) line1, (ConstrainableLine) line2,
			        (ConstrainableLine) line3, (ConstrainableLine) line4);
		}
		else {
			log.debug("Equal Angle requires two pairs of lines (4 total)");
			return 0;
		}
		
	}
	

	/**
	 * Shortcut method for solving this constraint, so you don't have to wrap
	 * the four shapes in a list of shapes. Computes the confidence that the two
	 * angles between the two pairs of lines are equal. We treat 0 and 180 as
	 * both horizontal, and thus equal.
	 * 
	 * @param line1
	 *            First line in the first pair, to compute the first angle
	 * @param line2
	 *            Second line in the first pair, to compute the first angle
	 * @param line3
	 *            First line in the second pair, to compute the second angle
	 * @param line4
	 *            Second line in the second pair, to compute the second angle
	 * @return The confidence that the first and second angle are equal (the
	 *         difference between them is 0).
	 */
	public double solve(ConstrainableLine line1, ConstrainableLine line2,
	        ConstrainableLine line3, ConstrainableLine line4) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(line1);
		parms.add(line2);
		parms.add(line3);
		parms.add(line4);
		setParameters(parms);
		
		// get the angles between the two pairs
		double angle12 = line1.getAngleBetweenInDegrees(line2);
		log.debug("angle 12 = " + angle12);
		double angle34 = line3.getAngleBetweenInDegrees(line4);
		log.debug("angle 34 = " + angle34);
		
		// difference between the two angles
		double diff = angle12 - angle34;
		log.debug("diff = " + diff);
		
		// Aaron doesn't want to do this any more. But blame Josh if I break
		// something.
		//
		// 0 and 180 are both considered horizontal, and would be equal angles
		// so we move diff into 0...90
		// while (diff > 90) {
		// diff -= 90;
		// }
		// log.debug("moved diff = " + diff);
		
		// confidence is how far from 0 we are
		double conf = solveConfidence(diff);
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
		return value < 0.05;
	}
	
}
