/**
 * RightOfConstraint.java
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
 * See if shape 1 is to the right of the second shape. This constraint requires
 * TWO {@link IConstrainable} shapes.
 * 
 * @author jbjohns
 */
public class RightOfConstraint extends AbstractConfidenceConstraint implements
        SigmoidalConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public RightOfConstraint newInstance() {
		return new RightOfConstraint();
	}
	
	/**
	 * logger
	 */
	private Logger log = LoggerFactory.getLogger(RightOfConstraint.class);
	
	/**
	 * The name for this constraint
	 */
	public static final String NAME = "RightOf";
	
	/**
	 * The description for this constraint
	 */
	public static final String DESCRIPTION = "Tests if the center of the first shape is directly to the right of the center of the second";
	
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
	
	
	// /**
	// * Constraint to test if the shapes have the same Y values
	// */
	// protected SameYConstraint m_sameY = new SameYConstraint();
	//	
	
	/**
	 * Create an leftOf constraint with the default threshold.
	 */
	public RightOfConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use when solving the confidence for this
	 *            constraint
	 */
	public RightOfConstraint(double threshold) {
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
			log.debug("RightOf requires " + getNumRequiredParameters()
			          + " parameters");
			return 0;
		}
		
		IConstrainable shape1 = getParameters().get(0);
		IConstrainable shape2 = getParameters().get(1);
		
		return solve(shape1, shape2);
	}
	

	/**
	 * Short hand method (no list of shapes) for seeing if the first shape is to
	 * the right of the second shape
	 * 
	 * @param shape1
	 *            The first shape
	 * @param shape2
	 *            The second shape
	 * @return The confidence that the first shape is to the right of the second
	 *         shape
	 */
	public double solve(IConstrainable shape1, IConstrainable shape2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(shape1);
		parms.add(shape2);
		setParameters(parms);
		
		// is the left edge of shape 1 to the right of the right edge of shape
		// 2? (greater x value)
		double conf = 0;
		
		double left1 = shape1.getBoundingBox().getLeft();
		log.debug("left1 = "+left1);
		
		double right2 = shape2.getBoundingBox().getRight();
		log.debug("right2 = "+right2);
		
		double diff = left1 - right2;
		log.debug("diff = "+diff);
		
		conf = solveConfidence(diff);
		log.debug("conf = "+conf);
		
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
		return value < 0.30;
	}
}
