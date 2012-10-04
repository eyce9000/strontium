/**
 * ParallelConstraint.java
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
 * See if the length of the first line is equal to the distance between the second and third lines. 
 * This constraint requires THREE {@link ConstrainableLine} shapes.
 * 
 */
public class LengthConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public LengthConstraint newInstance() {
		return new LengthConstraint();
	}
	
	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(LengthConstraint.class);
	
	/**
	 * Name for this constraint
	 */
	public static final String NAME = "Length";
	
	/**
	 * Description for this constraint
	 */
	public static final String DESCRIPTION = "The length of the first line equals the distance between the second and third lines";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 3;
	
	/**
	 * Default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 15;
	
	
	/**
	 * Construct the constraint with the default threshold
	 */
	public LengthConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use for this constraint
	 */
	public LengthConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// requires two shapes
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Length requires " + getNumRequiredParameters()
			          + " shapes");
			return 0;
		}
		
		IConstrainable line1 = getParameters().get(0);
		IConstrainable line2 = getParameters().get(1);
		IConstrainable line3 = getParameters().get(2);
		
		// both lines?
		if (line1 instanceof ConstrainableLine
		    && line2 instanceof ConstrainableLine) {
			return solve((ConstrainableLine) line1, (ConstrainableLine) line2, (ConstrainableLine) line3);
		}
		else {
			log.debug("Length requires three lines");
			return 0;
		}
	}
	

	/**
	 * Shorthand method (no list of shapes needed) for solving the parallel
	 * constraint. Two lines are parallel if the difference between their slopes
	 * is 0 (or 180).
	 * 
	 * @param line1
	 *            The first line
	 * @param line2
	 *            The second line
	 * @return The confidence that the slopes of the two lines are the
	 *         same--they're parallel
	 */
	public double solve(ConstrainableLine line1, ConstrainableLine line2,ConstrainableLine line3 ) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(line1);
		parms.add(line2);
		parms.add(line3);
		setParameters(parms);
		
		double distDiff = line1.getPixelLength() - Math.abs(line2.getEnd1().distance(line3.getEnd1()));
		log.debug("difference between the length of first line and distance between second and third lines = " + distDiff);
		
		double conf = solveConfidence(distDiff);
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
		return value < 0.20;
	}
}
