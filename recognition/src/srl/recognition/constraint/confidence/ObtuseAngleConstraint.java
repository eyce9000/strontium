/**
 * ObtuseAngleConstraint.java
 * 
 * Revision History:<br>
 * Aug 1, 2008 jbjohns - File created
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


/**
 * Given two lines, compute the confidence that the clockwise angle between the
 * lines is obtuse (> 90). This constraint requires TWO
 * {@link ConstrainableLine} shapes.
 * 
 * @author jbjohns
 */
public class ObtuseAngleConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public ObtuseAngleConstraint newInstance() {
		return new ObtuseAngleConstraint();
	}
	
	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(ObtuseAngleConstraint.class);
	
	/**
	 * Name for this constraint
	 */
	public static final String NAME = "ObtuseAngle";
	
	/**
	 * Description of this constraint
	 */
	public static final String DESCRIPTION = "Given two lines, compute the confidence that the clockwise angle between them is obtuse";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * Default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 45;
	
	
	/**
	 * Construct this constraint with the default threshold
	 */
	public ObtuseAngleConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct this constraint with the given threshold.
	 * 
	 * @param threshold
	 */
	public ObtuseAngleConstraint(double threshold) {
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
			log.debug("Obtuse Angle requires " + getNumRequiredParameters()
			          + " lines");
			return 0;
		}
		
		IConstrainable line1 = getParameters().get(0);
		IConstrainable line2 = getParameters().get(1);
		
		// both lines?
		if (line1 instanceof ConstrainableLine
		    && line2 instanceof ConstrainableLine) {
			return solve((ConstrainableLine) line1, (ConstrainableLine) line2);
		}
		else {
			log.debug("Obtuse angle requires two lines");
			return 0;
		}
	}
	

	/**
	 * Shortcut method (don't have to wrap the parameters in a list) to see if
	 * the angle between two lines is obtuse.
	 * 
	 * @param line1
	 *            The first line
	 * @param line2
	 *            The second line
	 * @return Confidence that the angle between the lines, when measured in the
	 *         clockwise fashion, is obtuse
	 */
	public double solve(ConstrainableLine line1, ConstrainableLine line2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(line1);
		parms.add(line2);
		setParameters(parms);
		
		double angle = line1.getAngleBetweenInDegrees(line2);
		log.debug("angle = " + angle);
		
		// optimal obtuse angle is 180--all the way open
		final double target = 180;
		double deltaAngle = Math.abs(target - angle);
		log.debug("deltaAngle = " + deltaAngle);
		
		double conf = solveConfidence(deltaAngle);
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
		return value < 0.3;
	}
}
