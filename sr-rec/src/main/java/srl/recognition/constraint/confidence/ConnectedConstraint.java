/**
 * ConnectedConstraint.java
 * 
 * Revision History:<br>
 * Jul 14, 2008 jbjohns - File created
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
import srl.recognition.constraint.constrainable.ConstrainablePoint;


/**
 * Takes two line shapes, and has high confidence if an endpoint from one line
 * is coincident with an endpoint from the other. This constraint requires TWO
 * {@link ConstrainableLine}
 * 
 * @author jbjohns
 */
public class ConnectedConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public ConnectedConstraint newInstance() {
		return new ConnectedConstraint();
	}
	
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(ConnectedConstraint.class);
	
	/**
	 * The name of this constraint
	 */
	public static final String NAME = "Connected";
	
	/**
	 * The description for this constraint
	 */
	public static final String DESCRIPTION = "Two lines are connected if an endpoint from the first is coincident with an endpoint from the second";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * This constraint does not use it's own threshold as it uses coincident
	 */
	public static final double DEFAULT_THRESHOLD = 1;
	
	/**
	 * Coincident constraint to see if endpoints pairs of the lines are in the
	 * same location
	 */
	protected CoincidentConstraint m_coincident = new CoincidentConstraint();
	
	
	/**
	 * Construct the constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public ConnectedConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use for this constraint
	 */
	public ConnectedConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
		this.setScaleParameters(true);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// requires two lines
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Connected requires " + getNumRequiredParameters()
			          + " lines");
			return 0;
		}
		
		IConstrainable line1 = getParameters().get(0);
		IConstrainable line2 = getParameters().get(1);
		
		if (line1 instanceof ConstrainableLine
		    && line2 instanceof ConstrainableLine) {
			return solve((ConstrainableLine) line1, (ConstrainableLine) line2);
		}
		else {
			log.debug("Connected requires two lines");
			return 0;
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.AbstractConstraint#
	 * multiplyThreshold(double)
	 */
	@Override
	public void multiplyThreshold(double thresholdMultiplier) {
		m_coincident.multiplyThreshold(thresholdMultiplier);
	}
	

	/**
	 * Shortcut method to see if the two lines are connected at some pair of
	 * endpoints.
	 * 
	 * @param line1
	 *            The first line
	 * @param line2
	 *            The second line
	 * @return The confidence that one pair of endpoints between the two lines
	 *         is coincident, hence the lines are connected to each other
	 */
	public double solve(ConstrainableLine line1, ConstrainableLine line2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(line1);
		parms.add(line2);
		setParameters(parms);
		
		// get the two lines, and the two endpoints of each line
		ConstrainablePoint pt11 = line1.getEnd1();
		ConstrainablePoint pt12 = line1.getEnd2();
		log.debug("Line 1 from " + pt11 + " to " + pt12);
		ConstrainablePoint pt21 = line2.getEnd1();
		ConstrainablePoint pt22 = line2.getEnd2();
		log.debug("Line 2 from " + pt21 + " to " + pt22);
		
		// get the confidences that the four pairs of endpoints are coincident
		double coinc11 = m_coincident.solve(pt11, pt21);
		log.debug("coinc 11 with 21 = " + coinc11);
		double coinc12 = m_coincident.solve(pt11, pt22);
		log.debug("coinc 11 with 22 = " + coinc12);
		double coinc21 = m_coincident.solve(pt12, pt21);
		log.debug("coinc 12 with 21 = " + coinc21);
		double coinc22 = m_coincident.solve(pt12, pt22);
		log.debug("coinc 12 with 22 = " + coinc22);
		
		// the chance that we're connected is the max of all the confidences of
		// coincidence. We're only sure that we're connected via some endpoint
		// pair if those endpoints are coincident
		return Math.max(Math.max(coinc11, coinc12), Math.max(coinc21, coinc22));
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.AbstractConstraint#
	 * isClearlyFalse(double)
	 */
	@Override
	public boolean isClearlyFalse(double value) {
		return value < 0.03;
	}
}
