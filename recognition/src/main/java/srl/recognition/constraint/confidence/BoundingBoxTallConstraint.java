/**
 * BoundingBoxTallConstraint.java
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
 * Solve the confidence that a shape's bounding box is taller than it is wide
 * 
 * @author jbjohns
 */
public class BoundingBoxTallConstraint extends AbstractConfidenceConstraint implements UnaryConstraint {
	
	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(BoundingBoxTallConstraint.class);
	
	/**
	 * Name of the constraint
	 */
	public static final String NAME = "BoundingBoxTall";
	
	/**
	 * Description of the constraint
	 */
	public static final String DESCRIPTION = "Bounding box of a shape is taller than it is wide";
	
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
	public BoundingBoxTallConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint to use the given threshold.
	 * 
	 * @param threshold
	 *            The threshold to use to solve confidence.
	 */
	public BoundingBoxTallConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public BoundingBoxTallConstraint newInstance() {
		return new BoundingBoxTallConstraint();
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
	 * Solve the confidence that the given shape's bounding box is taller than
	 * it is wide.
	 * 
	 * @param shape1
	 *            The shape
	 * @return The confidence that the bounding box is taller than it is wide,
	 *         in the range 0...1
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
		
		// we want larger height, which drives the ratio DOWN to 0
		// if width is larger, this is larger than 1.
		double ratio = width / height;
		log.debug("ratio = " + ratio);
		
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
