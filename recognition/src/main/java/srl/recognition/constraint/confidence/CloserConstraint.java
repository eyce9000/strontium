/**
 * CloserConstraint.java
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

import srl.core.sketch.Point;
import srl.recognition.constraint.IConstrainable;


/**
 * Constraint that holds if the center of the first shape is closer to the
 * second shape than to the third shape. This constraint requires THREE
 * {@link IConstrainable}.
 * 
 * @author jbjohns
 */
public class CloserConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public CloserConstraint newInstance() {
		return new CloserConstraint();
	}
	
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(CloserConstraint.class);
	
	/**
	 * name for the constraint
	 */
	public static final String NAME = "Closer";
	
	/**
	 * description for the constraint
	 */
	public static final String DESCRIPTION = "The center of Shape1 is closer to Shape2 than it is to Shape3";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 3;
	
	/**
	 * default threshold for the constraint. This constraint doesn't work like
	 * most others that use nominal distances or measures as thresholds. This
	 * one uses a ratio, so it's already normalized.
	 */
	public static final double DEFAULT_THRESHOLD = 1;
	
	
	/**
	 * Construct the constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public CloserConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            Threshold to use when solving this constraint
	 */
	public CloserConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
		this.setScaleParameters(true);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// requires three shapes
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Closer requires " + getNumRequiredParameters()
			          + " shapes");
			return 0;
		}
		
		// center of the three shapes
		IConstrainable shape1 = getParameters().get(0);
		IConstrainable shape2 = getParameters().get(1);
		IConstrainable shape3 = getParameters().get(2);
		
		return solve(shape1, shape2, shape3);
	}
	

	/**
	 * Shortcut method to see if the center of the first shape is closer to the
	 * center of the second shape than it is to the center of the third shape
	 * 
	 * @param shape1
	 *            The first shape
	 * @param shape2
	 *            The second shape, are we closer to this one?
	 * @param shape3
	 *            The third shape, are we farther from this one?
	 * @return The confidence that the first shape center is closer to #2's
	 *         center than it is to #3's center
	 */
	public double solve(IConstrainable shape1, IConstrainable shape2,
	        IConstrainable shape3) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(shape1);
		parms.add(shape2);
		parms.add(shape3);
		setParameters(parms);
		
		Point pt1 = shape1.getBoundingBox().getCenterPoint();
		Point pt2 = shape2.getBoundingBox().getCenterPoint();
		Point pt3 = shape3.getBoundingBox().getCenterPoint();
		
		// distance from 1 to 2 and 1 to 3
		double dist12 = pt1.distance(pt2);
		// System.out.println(dist12);
		double dist13 = pt1.distance(pt3);
		// System.out.println(dist13);
		log.debug("dist12 = " + dist12 + ", dist13 = " + dist13);
		
		// For this constraint to hold, we want dist12 < dist13. To turn this
		// into a confidence, we want high confidence when dist12 << dist13,
		// ambivalent confidence when they're about equal, and lower confidence
		// when dist12 >> dist13. A ratio of the distances meets these criterion
		// nicely, and is directly proportional to the confidence we want.
		
		// TODO depend on bounding box size.
		// *** if dist13 > dist12, then ratio is small <<-- this is our goal,
		// for the distance between 1 and 3 to be large, meaning we're closer
		// to 2 (since distance between 1 and 2 is smaller)
		// *** if dist13 == dist12, then ratio == 1
		// *** if dist13 < dist12, then ratio is large
		double distRatio = dist12 / dist13;
		log.debug("ratio = " + distRatio);
		// for conf to be high, ratio must be small (closer to 0 is better)
		double conf = solveConfidence(distRatio);
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
		return value < 0.38;
	}
	
}
