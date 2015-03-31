/**
 * DashedShapeConstraint.java
 * 
 * Revision History:<br>
 * Feb 24, 2009 jbjohns - File created
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

import srl.core.sketch.Shape;
import srl.core.util.IsAConstants;
import srl.recognition.constraint.IConstrainable;
import srl.recognition.constraint.constrainable.ConstrainableShape;


/**
 * Constraint to check to see if a shape is made of dashed strokes.
 * 
 * @author jbjohns
 */
public class DashedShapeConstraint extends AbstractConfidenceConstraint implements
        UnaryConstraint {
	
	/**
	 * Logger for this class
	 */
	private static final Logger log = LoggerFactory
	        .getLogger(DashedShapeConstraint.class);
	
	/**
	 * Name of this constraint
	 */
	public static final String NAME = "DashedShape";
	
	/**
	 * Description for this constraint
	 */
	public static final String DESCRIPTION = "This shape is made up of dashed strokes (binary constraint)";
	
	/**
	 * Number of parameters required by this constraint
	 */
	public static final int NUM_PARAMETERS = 1;
	
	/**
	 * Default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 1.0;
	
	
	/**
	 * Default constructor for this constraint, sets the name, desc, number
	 * paramters, and thrshold.
	 */
	public DashedShapeConstraint() {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, DEFAULT_THRESHOLD);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public DashedShapeConstraint newInstance() {
		return new DashedShapeConstraint();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	@Override
	public double solve() {
		if (getParameters() == null || getParameters().size() < 1) {
			log.debug("Requires 1 parameter");
			throw new IllegalArgumentException(
			        "DashedShape requires 1 parameter");
		}
		
		ConstrainableShape shape = (ConstrainableShape) getParameters().get(0);
		
		return solve(shape);
	}
	

	/**
	 * See if the given shape is made of dashed lines.
	 * 
	 * @param s
	 *            The shape
	 * @return 1.0 if hte shape is
	 */
	public double solve(ConstrainableShape s) {
		
		List<IConstrainable> params = new ArrayList<IConstrainable>();
		params.add(s);
		setParameters(params);
		
		Shape shape = (Shape) s.getShape();
		boolean isDashed = shape.hasAttribute(IsAConstants.DASHED);
		log.debug("Shape isDashed: " + isDashed);
		
		double conf = (isDashed) ? 1.0 : 0.0;
		log.debug("confidence : " + conf);
		
		return conf;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.UnaryConstraint#
	 * isUnaryConstraint()
	 */
	@Override
	public boolean isUnaryConstraint() {
		return true;
	}
	
}
