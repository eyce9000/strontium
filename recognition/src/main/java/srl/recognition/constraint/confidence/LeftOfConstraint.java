/**
 * LeftOfConstraint.java
 * 
 * Revision History:<br>
 * Aug 8, 2008 jbjohns - File created
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



/**
 * See if center of shape 1 is to the left of the center of second shape. This
 * constraint requires TWO {@link IConstrainable}
 * 
 * @author jbjohns
 */
public class LeftOfConstraint extends AbstractConfidenceConstraint implements
        SigmoidalConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public LeftOfConstraint newInstance() {
		return new LeftOfConstraint();
	}
	
	/**
	 * logger
	 */
	private Logger log = LoggerFactory.getLogger(LeftOfConstraint.class);
	
	/**
	 * The name for this constraint
	 */
	public static final String NAME = "LeftOf";
	
	/**
	 * The description for this constraint
	 */
	public static final String DESCRIPTION = "Tests if the center of the first shape is directly to the left of the center of the second";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * The default threshold for this constraint, used only in the case of
	 * points. If not points, the threshold is dynamic and based on shape
	 * heights
	 */
	public static final double DEFAULT_THRESHOLD = 15;
	
	/**
	 * Constraint to test if the shapes have the same Y values
	 */
	protected SameYConstraint m_sameY = new SameYConstraint();
	
	
	/**
	 * Create an leftOf constraint with the default threshold.
	 */
	public LeftOfConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use when solving the confidence for this
	 *            constraint
	 */
	public LeftOfConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
		this.setScaleParameters(true);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	@Override
	public double solve() {
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("LeftOf requires " + getNumRequiredParameters()
			          + " parameters");
			return 0;
		}
		
		IConstrainable shape1 = getParameters().get(0);
		IConstrainable shape2 = getParameters().get(1);
		
		return solve(shape1, shape2);
	}
	

	/**
	 * Shorthand method (no list of shapes) to see if the first shape is to the
	 * left of the second shape.
	 * 
	 * @param shape1
	 *            The first shape
	 * @param shape2
	 *            The second shape
	 * @return The confidence that the first shape is to the left of the second
	 *         shape
	 */
	public double solve(IConstrainable shape1, IConstrainable shape2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(shape1);
		parms.add(shape2);
		setParameters(parms);
		
		// the right edge of shape 1 is to the left of the left edge of shape 2
		// (lesser x value)
		double leftConf = 0;
		
		double right1 = shape1.getBoundingBox().getRight();
		log.debug("right1 = "+right1);
		
		double left2 = shape2.getBoundingBox().getLeft();
		log.debug("left2 = "+left2);
		
		double diff = left2 - right1;
		log.debug("diff = "+diff);
		
		leftConf = solveConfidence(diff);
		log.debug("conf = "+leftConf);
		
		return leftConf;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.AbstractConstraint#
	 * isClearlyFalse(double)
	 */
	@Override
	public boolean isClearlyFalse(double value) {
		return value < 0.30;
	}
}
