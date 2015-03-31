/**
 * SameHeightConstraint.java
 * 
 * Revision History:<br>
 * Aug 6, 2008 jbjohns - File created
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
 * See if two shapes bounding boxes have the same height
 * 
 * @author jbjohns
 */
public class SameHeightConstraint extends AbstractConfidenceConstraint {
	
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(SameHeightConstraint.class);
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public SameHeightConstraint newInstance() {
		return new SameHeightConstraint();
	}
	
	/**
	 * Name for this constraint
	 */
	public static final String NAME = "SameHeight";
	
	/**
	 * Description for this constraint
	 */
	public static final String DESCRIPTION = "Two shape bounding boxes have the same height";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * Default threshold for this constraint.
	 */
	public static final double DEFAULT_THRESHOLD = 0.5;
	
	/**
	 * Construct the constraint with {@link #DEFAULT_THRESHOLD}
	 */
	public SameHeightConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use for solving this constraint
	 */
	public SameHeightConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// requires 2 shapes
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Same Height requires " + getNumRequiredParameters()
			          + " shapes");
			return 0;
		}
		
		IConstrainable shape1 = getParameters().get(0);
		IConstrainable shape2 = getParameters().get(1);
		
		return solve(shape1, shape2);
	}
	

	/**
	 * Shorthand method (no list of shapes) to solve the confidence that two
	 * shapes have bounding boxes with the same heights
	 * 
	 * @param shape1
	 *            The first shape
	 * @param shape2
	 *            The second shape
	 * @return The confidence that the bounding boxes of the two shapes have the
	 *         same height
	 */
	public double solve(IConstrainable shape1, IConstrainable shape2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(shape1);
		parms.add(shape2);
		setParameters(parms);
		
		double conf = 0;
		
		double h1 = shape1.getBoundingBox().getHeight();
		if (h1 < 1) {
			h1 = 1;
		}
		log.debug("h1 = " + h1);
		
		double h2 = shape2.getBoundingBox().getHeight();
		if (h2 < 1) {
			h2 = 1;
		}
		log.debug("h2 = " + h2);
		
		// change to a ratio difference: h1/h2
		double larger = Math.max(h1, h2);
		double smaller = Math.min(h1, h2);
		// ratio will always be > 1, best is 1
		double ratio = larger / smaller; 
		log.debug("height ratio = " + ratio);
		
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
		return value < 0.08;
	}
}
