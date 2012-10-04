/**
 * CoincidentConstraint.java
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
import srl.recognition.constraint.constrainable.ConstrainablePoint;



/**
 * Constraint that measures if two points are in the same location. This
 * constraint requires TWO {@link ConstrainablePoint}
 * 
 * @author jbjohns
 * 
 */
public class CoincidentConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public CoincidentConstraint newInstance() {
		return new CoincidentConstraint();
	}
	
	/**
	 * logger
	 */
	private Logger log = LoggerFactory.getLogger(CoincidentConstraint.class);
	
	/**
	 * Default threshold value for the Coincident Constraint
	 */
	public static final double DEFAULT_THRESHOLD = 0.15;
	
	/**
	 * Default name for the coincident constraint
	 */
	public static final String NAME = "Coincident";
	
	/**
	 * Default description for the coincident constraint
	 */
	public static final String DESCRIPTION = "The Coincident constraint holds if two points lie in the same location";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	
	/**
	 * Create a Coincident Constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public CoincidentConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Create a coincident constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use when solving the coincident constraint
	 */
	public CoincidentConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
		this.setScaleParameters(true);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// Get the first points of two shapes.
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Coincident requires " + getNumRequiredParameters()
			          + "constrainable points");
			return 0;
		}
		
		// Use the centers
		IConstrainable pt1 = getParameters().get(0);
		IConstrainable pt2 = getParameters().get(1);
		
		if (pt1 instanceof ConstrainablePoint
		    && pt2 instanceof ConstrainablePoint) {
			return solve((ConstrainablePoint) pt1, (ConstrainablePoint) pt2);
		}
		else {
			log.debug("Coincident requires TWO ConstrainablePoints");
			return 0;
		}
	}
	

	/**
	 * Shortcut to see if two points are coincident, or in the same location
	 * 
	 * @param pt1
	 *            The first point
	 * @param pt2
	 *            The second point
	 * @return The confidence that the two points are coincident
	 */
	public double solve(ConstrainablePoint pt1, ConstrainablePoint pt2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(pt1);
		parms.add(pt2);
		setParameters(parms);
		
		// distance between the two points
		double coincidentVal = pt1.distance(pt2);
		log.debug("distance : " + coincidentVal);
		
		double thisThreshold = 15;
		double avgParentSize = getAverageParentShapeSize();
		log.debug("Average parent size = " + avgParentSize);
		if (avgParentSize > 0) {
			thisThreshold = avgParentSize * getThreshold();
		}
		log.debug("m_threshold = " + getThreshold() + ", relative threshold = "
		          + thisThreshold);
		
		double conf = solveConfidence(coincidentVal, thisThreshold);
		
		// System.out.println(thisThreshold+" "+coincidentVal+" "+conf);
		
		log.debug("Confidence " + conf);
		
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
		return value < 0.25;
	}
	
}
