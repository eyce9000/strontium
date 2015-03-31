/**
 * BoundingBoxSquareConstraint.java
 * 
 * Revision History:<br>
 * Mar 10, 2009 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
 * Solve the confidence that a given shape's bounding box is square (equal
 * height and width).
 * 
 * @author jbjohns
 */
public class BoundingBoxSquareConstraint extends AbstractConfidenceConstraint implements UnaryConstraint {
	
	/**
	 * Logger for the class
	 */
	private static Logger log = LoggerFactory
	        .getLogger(BoundingBoxSquareConstraint.class);
	
	/**
	 * Name of the constraint
	 */
	public static final String NAME = "BoundingBoxSquare";
	
	/**
	 * Description for the constraint
	 */
	public static final String DESCRIPTION = "Bounding box of a shape is square: width == height";
	
	/**
	 * Default threshold for the constraint
	 */
	public static final double DEFAULT_THRESHOLD = 1;
	
	/**
	 * Number of parameters required by this constraint
	 */
	public static final int NUM_PARAMETERS = 1;
	
	
	/**
	 * Construct the constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public BoundingBoxSquareConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold.
	 * 
	 * @param threshold
	 *            The threshold to use when solving confidence.
	 */
	public BoundingBoxSquareConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public BoundingBoxSquareConstraint newInstance() {
		return new BoundingBoxSquareConstraint();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	@Override
	public double solve() {
		if (getParameters() == null || getParameters().size() < NUM_PARAMETERS) {
			log.debug(NAME + " requires " + NUM_PARAMETERS + " shapes");
			return 0;
		}
		
		IConstrainable shape1 = getParameters().get(0);
		
		return solve(shape1);
	}
	

	/**
	 * Solve the confidence that the given shape's bounding box is square; that
	 * is, the height is the same as the width
	 * 
	 * @param shape1
	 *            The shape
	 * @return The confidence that the bounding box is square
	 */
	public double solve(IConstrainable shape1) {
		List<IConstrainable> params = new ArrayList<IConstrainable>();
		params.add(shape1);
		setParameters(params);
		
		double conf = 0;
		
		double width = shape1.getBoundingBox().getWidth();
		log.debug("width = " + width);
		
		double height = shape1.getBoundingBox().getHeight();
		log.debug("height = " + height);
		
		// Do the larger always on top so this ratio is always >= 1.
		double max = Math.max(width, height);
		double min = Math.min(width, height);
		double ratio = max / min;
		log.debug("ratio = " + ratio);
		
		// ratio is perfect when == 1, but always >= 1. Subtract 1 so the
		// perfect answer is now at 0, with all possible answers >= 0.
		// this gives nice input to half-gaussian
		ratio = 1 - ratio;
		log.debug("1 - ratio = " + ratio);
		
		conf = solveConfidence(ratio);
		log.debug("conf = " + conf);
		
		return conf;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.confidence.AbstractConfidenceConstraint
	 * #isClearlyFalse(double)
	 */
	@Override
	public boolean isClearlyFalse(double value) {
		return value < 0.4;
	}


	/* (non-Javadoc)
     * @see org.ladder.recognition.constraint.confidence.UnaryConstraint#isUnaryConstraint()
     */
    @Override
    public boolean isUnaryConstraint() {
	    return true;
    }
	
}
