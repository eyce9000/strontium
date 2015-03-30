/**
 * SameSizeConstraint.java
 * 
 * Revision History:<br>
 * Jul 30, 2008 jbjohns - File created
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
import srl.recognition.constraint.constrainable.ConstrainableShape;

/**
 * Two objects are the same size. If the objects are both lines, if they are the
 * same length. If the objects are both shapes, the width and height of bounding
 * box must be about the same. This constraint requires TWO
 * {@link IConstrainable} shape.
 * 
 * @author jbjohns
 */
public class SameSizeConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public SameSizeConstraint newInstance() {
		return new SameSizeConstraint();
	}
	
	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(SameSizeConstraint.class);
	
	/**
	 * Name for this constraint
	 */
	public static final String NAME = "SameSize";
	
	/**
	 * Description for this constraint
	 */
	public static final String DESCRIPTION = "Two lines are the same length, or two shapes are the same width and height";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * Default threshold for this constraint.
	 */
	public static final double DEFAULT_THRESHOLD = 0.5;
	
	
	/**
	 * Construct constraint with default threshold, name, and description
	 */
	public SameSizeConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct constraint with the given threshold and defaults for name and
	 * description
	 * 
	 * @param threshold
	 *            Threshold to use for this constraint
	 */
	public SameSizeConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// two shapes
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Same Size requires " + getNumRequiredParameters()
			          + " shapes");
			return 0;
		}
		
		IConstrainable shape1 = getParameters().get(0);
		IConstrainable shape2 = getParameters().get(1);
		
		return solve(shape1, shape2);
	}
	

	/**
	 * Shorthand method (no list of shapes) for comparing the size of two
	 * shapes. If the two shapes are both points, return the max probability. If
	 * the two shapes are both lines, return the conf that they are the same
	 * length. If the two shapes are both complex shapes, return the conf that
	 * their bounding boxes have the same width and height (this method accounts
	 * for
	 * 
	 * @param shape1
	 *            The first shape
	 * @param shape2
	 *            The second shape
	 * @return The confidence that the bounding boxes of the two shapes have the
	 *         same width and height
	 */
	public double solve(IConstrainable shape1, IConstrainable shape2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(shape1);
		parms.add(shape2);
		setParameters(parms);
		
		double conf = 0;
		
		// two points?
		if (shape1 instanceof ConstrainablePoint
		    && shape2 instanceof ConstrainablePoint) {
			log.debug("Two points, max prob");
			conf = getMaxConfidence();
		}
		// two lines
		else if (shape1 instanceof ConstrainableLine
		         && shape2 instanceof ConstrainableLine) {
			log.debug("Two lines");
			
			ConstrainableLine line1 = (ConstrainableLine) shape1;
			ConstrainableLine line2 = (ConstrainableLine) shape2;
			
			double len1 = line1.getEnd1().distance(line1.getEnd2());
			double len2 = line2.getEnd1().distance(line2.getEnd2());
			conf = equalLengthConfidence(len1, len2);
		}
		else if (shape1 instanceof ConstrainableShape
		         && shape2 instanceof ConstrainableShape) {
			log.debug("Two shapes");
			
			// both the same height AND same width
			double width1 = shape1.getBoundingBox().getWidth();
			double height1 = shape1.getBoundingBox().getHeight();
			log.debug("shape 1, width=" + width1 + ", height=" + height1);
			double width2 = shape2.getBoundingBox().getWidth();
			double height2 = shape2.getBoundingBox().getHeight();
			log.debug("shape 2, width=" + width2 + ", height=" + height2);
			
			// compare width to width and width to height, to account for
			// 90 degree rotations
			log.debug("width width");
			double ww_conf = equalLengthConfidence(width1, width2);
			log.debug("height height");
			double hh_conf = equalLengthConfidence(height1, height2);
			double ww_hh_conf = Math.min(ww_conf, hh_conf);
			log.debug("same orientation conf = " + ww_hh_conf);
			
			double wh_conf = equalLengthConfidence(width1, height2);
			log.debug("width height conf = " + wh_conf);
			double hw_conf = equalLengthConfidence(height1, width2);
			log.debug("height width conf = " + hw_conf);
			double wh_hw_conf = Math.min(wh_conf, hw_conf);
			log.debug("90 orientation conf = " + wh_hw_conf);
			
			conf = Math.max(ww_hh_conf, wh_hw_conf);
			// conf = ww_hh_conf;
			log.debug("final conf = " + conf);
		}
		else {
			log
			        .error("Two shapes that are not the same type cannot be compared for Same Size");
			return 0;
		}
		
		return conf;
	}
	

	/**
	 * Compute the confidence that two lengths are equal.
	 * 
	 * @param len1
	 *            First length
	 * @param len2
	 *            Second length
	 * @return Confidence the lengths are equal
	 */
	private double equalLengthConfidence(double len1, double len2) {
		double conf = 0;
		
		// change to a ratio difference: len2/len1
		double larger = Math.max(len1, len2);
		if (larger < 1) {
			larger = 1;
		}
		double smaller = Math.min(len1, len2);
		if (smaller < 1) {
			smaller = 1;
		}
		// ratio will always be > 1, best is 1
		double ratio = larger / smaller;
		log.debug("ratio = " + ratio);
		
		// shift the best to 0 from 1
		ratio -= 1;
		
		conf = solveConfidence(ratio);
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
		// was 0.4
		return value < 0.1;
	}
}
