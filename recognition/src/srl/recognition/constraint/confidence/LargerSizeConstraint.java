/**
 * LargerSizeConstraint.java
 * 
 * Revision History:<br>
 * Jul 31, 2008 jbjohns - File created
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
import srl.recognition.constraint.constrainable.ConstrainableShape;

/**
 * See if the first shape is larger than the second. For two lines, this means
 * length. For two shapes, this means a combination of bounding box width and
 * height. This constraint requires either TWO {@link ConstrainableLine} or TWO
 * {@link ConstrainableShape}
 * 
 * @author jbjohns
 */
public class LargerSizeConstraint extends AbstractConfidenceConstraint implements
        SigmoidalConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public LargerSizeConstraint newInstance() {
		return new LargerSizeConstraint();
	}
	
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(LargerSizeConstraint.class);
	
	/**
	 * default name for this constraint
	 */
	public final static String NAME = "LargerSize";
	
	/**
	 * default description for this constraint
	 */
	public final static String DESCRIPTION = "Line 1 is longer than Line 2, or shape 1's bounding box is larger than shape 2's";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * default threshold for this constraint. Threshold means that at 50%
	 * confidence, (shape 1 / shape 2) - 1 == threshold.
	 */
	public final static double DEFAULT_THRESHOLD = 0.75;
	
	
	/**
	 * Construct the constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public LargerSizeConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with given threshold
	 * 
	 * @param threshold
	 *            The threshold
	 */
	public LargerSizeConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// require two shapes
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Larger Size requires " + getNumRequiredParameters()
			          + " shapes");
			return 0;
		}
		
		IConstrainable shape1 = getParameters().get(0);
		IConstrainable shape2 = getParameters().get(1);
		
		return solve(shape1, shape2);
	}
	

	/**
	 * Shorthand method (no list of shapes) to see if the first shape is larger
	 * than the second shape. If two lines, checks the confidence by length. If
	 * two shapes, examines the width and height of the bounding box (and
	 * accounts for 90 degree rotations).
	 * 
	 * @param shape1
	 *            The first shape (or line)
	 * @param shape2
	 *            The second shape (or line)
	 * @return The confidence that the two shapes/lines are the same length/size
	 */
	public double solve(IConstrainable shape1, IConstrainable shape2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(shape1);
		parms.add(shape2);
		setParameters(parms);
		
		double conf = 0;
		
		if (shape1 instanceof ConstrainableLine
		    && shape2 instanceof ConstrainableLine) {
			log.debug("Two lines");
			ConstrainableLine line1 = (ConstrainableLine) shape1;
			ConstrainableLine line2 = (ConstrainableLine) shape2;
			double len1 = line1.getPixelLength();
			log.debug("length line 1 = " + len1);
			double len2 = line2.getPixelLength();
			log.debug("length line 2 = " + len2);
			
			if (len1 == 0) {
				conf = 0;
			}
			else if (len2 == 0) {
				conf = 1;
			}
			else {
				conf = getLongerConfidence(len1, len2);
			}
		}
		else if (shape1 instanceof ConstrainableShape
		         && shape2 instanceof ConstrainableShape) {
			log.debug("Two shapes");
			
			shape1 = (ConstrainableShape) shape1;
			shape2 = (ConstrainableShape) shape2;
			
			double w1 = shape1.getBoundingBox().getWidth();
			double h1 = shape1.getBoundingBox().getHeight();
			log.debug("bbox 1, w = " + w1 + ", h1 = " + h1);
			
			double w2 = shape2.getBoundingBox().getWidth();
			double h2 = shape2.getBoundingBox().getHeight();
			log.debug("bbox 2, w = " + w2 + ", h2 = " + h2);
			
			// compare same orientation
			double ww_conf = getLongerConfidence(w1, w2);
			double hh_conf = getLongerConfidence(h1, h2);
			log.debug("same orientation, width1-width2 = " + ww_conf
			          + ", height1-height2 = " + hh_conf);
			// TODO best way to combine?
			double sameOrientationConf = (ww_conf + hh_conf) / 2.0;
			log.debug("Same orientation conf = " + sameOrientationConf);
			
			// // compare rotation by 90
			double wh_conf = getLongerConfidence(w1, h2);
			double hw_conf = getLongerConfidence(h1, w2);
			log.debug("90 deg. rotation, width1-height2 = " + wh_conf
			          + ", height1-width2 = " + hw_conf);
			// TODO best way to combine?
			double rotOrientationConf = (hw_conf + wh_conf) / 2.0;
			log.debug("Rot orientation conf = " + rotOrientationConf);
			
			// the max conf
			conf = Math.min(sameOrientationConf, rotOrientationConf);
		}
		else {
			// if not same type can't compare, or if two points can't be larger
			log.warn("Shapes not of same type, or are points.");
			conf = 0;
		}
		
		return conf;
	}
	

	/**
	 * Compute the confidence that the first length is longer than the second
	 * length
	 * 
	 * @param len1
	 *            The first length
	 * @param len2
	 *            The second length
	 * @return The confidence that the first length is longer than the second
	 *         length
	 */
	protected double getLongerConfidence(double len1, double len2) {
		// ratio of the lengths. We want ratio to be high for high confidence.
		// We want high confidence when len1 is longer than len2. This means
		// when len1 is higher we want a higher ratio.
		double lenRatio = len1 / len2;
		log.debug("length ratio = " + lenRatio);
		
		// we subtract 1 from the ratio so that equal lengths are now at 0.
		lenRatio -= 1.0;
		log.debug("minus 1 = " + lenRatio);
		
		// confidence of the ratio
		double conf = solveConfidence(lenRatio);
		log.debug("confidence " + conf);
		
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
		// was 30
		return value < 0.15;
	}
}
