/**
 * BisectsConstraint.java
 * 
 * Revision History:<br>
 * Jul 11, 2008 jbjohns - File created
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
 * A point bisects a line if it is located at the center of a line. This
 * constraint requires TWO parameters: the first must be a
 * {@link ConstrainablePoint} and the second must be a {@link ConstrainableLine}
 * .
 * 
 * @author jbjohns
 */
public class BisectsConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public BisectsConstraint newInstance() {
		return new BisectsConstraint();
	}
	
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(BisectsConstraint.class);
	
	/**
	 * The name of this constraint
	 */
	public static final String NAME = "Bisects";
	
	/**
	 * The description for this constraint
	 */
	public static final String DESCRIPTION = "A point bisects a line if the point is located at the center of the line";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * This constraint does not have it's own threshold. It uses coincident and
	 * closer
	 */
	public static final double DEFAULT_THRESHOLD = 1;
	
	/**
	 * Constraint to use to see if we're closer to the center than the
	 * endpoints.
	 */
	protected CloserConstraint m_closer = new CloserConstraint();
	
	/**
	 * Constraint to see if the point is coincident to the center of the line
	 */
	protected CoincidentConstraint m_coincident = new CoincidentConstraint();
	
	
	/**
	 * Construct the constraint with {@link #DEFAULT_THRESHOLD}
	 */
	public BisectsConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            threshold to use when solving
	 */
	public BisectsConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
		this.setScaleParameters(true);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// 2 shapes, point and a line
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Bisects requires " + getNumRequiredParameters()
			          + " shapes");
			return 0;
		}
		
		IConstrainable point = getParameters().get(0);
		IConstrainable line = getParameters().get(1);
		
		if (point instanceof ConstrainablePoint
		    && line instanceof ConstrainableLine) {
			return solve((ConstrainablePoint) point, (ConstrainableLine) line);
		}
		else {
			log.debug("Bisects requires a point and a line");
			return 0;
		}
	}
	

	/**
	 * Shortcut method to see if the point bisects a line (falls at the
	 * midpoint).
	 * 
	 * @param point
	 *            The point that bisects
	 * @param line
	 *            The line that is being bisected
	 * @return The confidence that the point bisects the line
	 */
	public double solve(ConstrainablePoint point, ConstrainableLine line) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(point);
		parms.add(line);
		setParameters(parms);
		
		ConstrainablePoint pt1 = line.getEnd1();
		log.debug("first line point = " + pt1);
		ConstrainablePoint pt2 = line.getEnd2();
		log.debug("second line points = " + pt2);
		ConstrainablePoint center = line.getMidpoint();
		log.debug("second line points = " + pt2);
		
		double dist1 = point.distance(pt1);
		log.debug("dist to pt1 = " + dist1);
		double dist2 = point.distance(pt2);
		log.debug("dist to pt2 = " + dist2);
		
		// which endpoint, of the two, are we closest to? Are we closer to that
		// endpoint than to the center?
		double closerToCenterConf = 0;
		if (dist1 < dist2) {
			// closer to endpoint 1?
			closerToCenterConf = m_closer.solve(point, center, pt1);
		}
		else {
			// closer to endpoint 2?
			closerToCenterConf = m_closer.solve(point, center, pt2);
		}
		log.debug("closer to center confidence = " + closerToCenterConf);
		
		// now we measure the confidence that we're coincident with center.
		double coincidentConfidence = m_coincident.solve(point, center);
		log.debug("coincident conf = " + coincidentConfidence);
		
		// TODO how do we combine the confidences? mean for now
		double conf = (closerToCenterConf + coincidentConfidence) / 2.0;
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
		return value < 0.5;
	}
	
}
