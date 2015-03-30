/**
 * ContainsConstraint.java
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
import srl.recognition.constraint.constrainable.ConstrainablePoint;


/**
 * See if the first shape contains the second shape (the second shape is
 * contained in the first shape). This method requires TWO
 * {@link IConstrainable}
 * 
 * @author jbjohns
 */
public class ContainsConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public ContainsConstraint newInstance() {
		return new ContainsConstraint();
	}
	
	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(ContainsConstraint.class);
	
	/**
	 * Name of this constraint
	 */
	public static final String NAME = "Contains";
	
	/**
	 * Description of this constraint
	 */
	public static final String DESCRIPTION = "The first shape's bounding box completely contains the second shape's bounding box";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * Default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 7.5;
	
	/**
	 * Constraint for seeing if the top of 1 is above top of 2
	 */
	private AboveConstraint m_above;
	
	/**
	 * Constraint for seeing if bottom of 1 is below bottom of 2
	 */
	private BelowConstraint m_below;
	
	/**
	 * Constraint for seeing is left side of 1 is leftof left side of 2.
	 */
	private LeftOfConstraint m_leftOf;
	
	/**
	 * Constraint for seeing if right side of 1 is rightof right side of 2
	 */
	private RightOfConstraint m_rightOf;
	
	
	/**
	 * Construct the constraint with {@link #DEFAULT_THRESHOLD}
	 */
	public ContainsConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            Threshold value to use when solving the constraint
	 */
	public ContainsConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
		this.setScaleParameters(true);
		m_above = new AboveConstraint();
		m_below = new BelowConstraint();
		m_leftOf = new LeftOfConstraint();
		m_rightOf = new RightOfConstraint();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.AbstractConstraint#
	 * multiplyThreshold(double)
	 */
	@Override
	public void multiplyThreshold(double thresholdMultiplier) {
		m_above.multiplyThreshold(thresholdMultiplier);
		m_below.multiplyThreshold(thresholdMultiplier);
		m_leftOf.multiplyThreshold(thresholdMultiplier);
		m_rightOf.multiplyThreshold(thresholdMultiplier);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// requires two shape
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("contains requires " + getNumRequiredParameters()
			          + " shapes");
			return 0;
		}
		
		IConstrainable shape1 = getParameters().get(0);
		IConstrainable shape2 = getParameters().get(1);
		
		return solve(shape1, shape2);
	}
	

	/**
	 * Shorthand method (no list of shapes) for solving the constraint that the
	 * first shape completely contains the second shape.
	 * 
	 * @param shape1
	 *            The first shape
	 * @param shape2
	 *            The second shape
	 * @return Confidence that the first shape completely encloses (second shape
	 *         is completely contained in) the second shape
	 */
	public double solve(IConstrainable shape1, IConstrainable shape2) {
		
		// Set the parameters that are being used
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(shape1);
		parms.add(shape2);
		setParameters(parms);
		
		log.debug("Shape 1 bbox : " + shape1.getBoundingBox());
		log.debug("Shape 2 bbox : " + shape2.getBoundingBox());
		
		// is the top of 1 above the top of 2?
		ConstrainablePoint top1 = new ConstrainablePoint(shape1
		        .getBoundingBox().getTopCenterPoint(), shape1.getParentShape());
		log.debug("top 1 = " + top1);
		ConstrainablePoint top2 = new ConstrainablePoint(shape2
		        .getBoundingBox().getTopCenterPoint(), shape2.getParentShape());
		log.debug("top 2 = " + top2);
		double topConf = m_above.solve(top1, top2);
		log.debug("above conf = " + topConf);
		
		// is the bottom of 1 below the bottom of 2?
		ConstrainablePoint bottom1 = new ConstrainablePoint(shape1
		        .getBoundingBox().getBottomCenterPoint(), shape1
		        .getParentShape());
		log.debug("bottom 1 = " + bottom1);
		ConstrainablePoint bottom2 = new ConstrainablePoint(shape2
		        .getBoundingBox().getBottomCenterPoint(), shape2
		        .getParentShape());
		log.debug("bottom 2 = " + bottom2);
		double bottomConf = m_below.solve(bottom1, bottom2);
		log.debug("bottom conf = " + bottomConf);
		
		// is the left of 1 to the left of the left of 2?
		ConstrainablePoint left1 = new ConstrainablePoint(shape1
		        .getBoundingBox().getCenterLeftPoint(), shape1.getParentShape());
		log.debug("left 1 = " + left1);
		ConstrainablePoint left2 = new ConstrainablePoint(shape2
		        .getBoundingBox().getCenterLeftPoint(), shape2.getParentShape());
		log.debug("left 2 = " + left2);
		double leftConf = m_leftOf.solve(left1, left2);
		log.debug("left conf = " + leftConf);
		
		// is the right of 1 to the right of the right of 2?
		ConstrainablePoint right1 = new ConstrainablePoint(shape1
		        .getBoundingBox().getCenterRightPoint(), shape1
		        .getParentShape());
		log.debug("right 1 = " + right1);
		ConstrainablePoint right2 = new ConstrainablePoint(shape2
		        .getBoundingBox().getCenterRightPoint(), shape2
		        .getParentShape());
		log.debug("right 2 = " + right2);
		double rightConf = m_rightOf.solve(right1, right2);
		log.debug("right conf = " + rightConf);
		
		// TODO best way to combine confidence values?
		// double conf = (topConf * bottomConf * rightConf * leftConf);// 4.0;
//		double conf = (topConf + bottomConf + rightConf + leftConf) / 4.0;
		
		// all four of these need to be high for the conf to be high. Any one
		// being low should make the confidence low. So let's make the confidence
		// of contains be the lowest of these.

		double conf = Math.min(Math.min(topConf, bottomConf), Math.min(leftConf, rightConf));
		log.debug("conf = " + conf);
		
		// this.setThreshold(threshold);
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
