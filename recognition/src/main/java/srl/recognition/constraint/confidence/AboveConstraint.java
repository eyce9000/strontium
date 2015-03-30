/**
 * AboveConstraint.java
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
 * (INCLUDITNG NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package srl.recognition.constraint.confidence;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.recognition.constraint.IConstrainable;

/**
 * Constraint to determine if the first shape in the shape list is above the
 * second shape in the shape list. This constraint requires TWO
 * {@link IConstrainable} of any type.
 * 
 * @author jbjohns
 */
public class AboveConstraint extends AbstractConfidenceConstraint implements
        SigmoidalConstraint {
	
	/* (non-Javadoc)
     * @see org.ladder.recognition.constraint.IConstraint#newInstance()
     */
    @Override
    public AboveConstraint newInstance() {
	    return new AboveConstraint();
    }

    /**
     * logger
     */
    Logger log = LoggerFactory.getLogger(AboveConstraint.class);
	
	/**
	 * The name for this constraint
	 */
	public static final String NAME = "Above";
	
	/**
	 * The description for this constraint
	 */
	public static final String DESCRIPTION = "Tests if the center of the first shape is above the center of the second";
	
	/**
	 * Number of parameters this constraint takes.
	 */
	public static final int NUM_PARAMS = 2;
	
	/**
	 * The default threshold for this constraint, used only in the case of
	 * points. If not points, the threshold is dynamic and based on shape
	 * heights
	 */
	public static final double DEFAULT_THRESHOLD = 15;
	
	
	// /**
	// * Constraint to test if the shapes have the same X values
	// */
	// private SameXConstraint m_sameX = new SameXConstraint();
	
	/**
	 * Create an above constraint with {@link #DEFAULT_THRESHOLD}
	 */
	public AboveConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use when solving the confidence for this
	 *            constraint
	 */
	public AboveConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMS, threshold);
		this.setScaleParameters(true);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve(java.util.List)
	 */
	public double solve() {
		// we need two shapes
		if (getParameters() == null || getParameters().size() < 2) {
			log.debug("Above requires two shapes");
			return 0;
		}
		
		IConstrainable shape1 = getParameters().get(0);
		IConstrainable shape2 = getParameters().get(1);
		
		return solve(shape1, shape2);
	}
	

	/**
	 * Shorthand method (no list of shapes) for solving the constraint that the
	 * first shape is above the second shape
	 * 
	 * @param shape1
	 *            The first shape
	 * @param shape2
	 *            The second shape
	 * @return Confidence that the first shape is above the second shape
	 */
	public double solve(IConstrainable shape1, IConstrainable shape2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(shape1);
		parms.add(shape2);
		setParameters(parms);
		
		// shape 1 is above shape 2, means that the bottom of shape 1 is above
		// the top edge of shape 1. 
		double aboveConf = 0;
		
		double bottom1 = shape1.getBoundingBox().getBottom();
		log.debug("bottom 1 y = "+bottom1);
		
		double top2 = shape2.getBoundingBox().getTop();
		log.debug("top 2 = "+top2);
		
		// if shape1 is above, it will have a lesser y value. Thus, this 
		// difference will be large. Larger values mean more confidence.
		double diff = top2 - bottom1;
		log.debug("diff = "+diff);
		
		aboveConf = solveConfidence(diff);
		log.debug("above conf = "+aboveConf);
		
		return aboveConf;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ladder.recognition.constraint.confidence.AbstractConfidenceConstraint#isClearlyFalse(double)
	 */
	@Override
    public boolean isClearlyFalse(double value) {
	    // empirically derived from ConstraintConfidenceReport
		return value < 0.30;
    }
}
