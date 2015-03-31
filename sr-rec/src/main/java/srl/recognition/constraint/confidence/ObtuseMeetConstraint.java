/**
 * ObtuseMeetConstraint.java
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
import srl.recognition.constraint.constrainable.ConstrainablePoint;

/**
 * Constraint holds if two lines meet at an endpoint and the angle between them
 * is obtuse (> 90). This constraint requires TWO {@link ConstrainableLine}.
 * 
 * @author jbjohns
 */
public class ObtuseMeetConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public ObtuseMeetConstraint newInstance() {
		return new ObtuseMeetConstraint();
	}
	
	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory
	        .getLogger(ObtuseMeetConstraint.class);
	
	/**
	 * Name of this constraint
	 */
	public static final String NAME = "ObtuseMeet";
	
	/**
	 * Description for this constraint
	 */
	public static final String DESCRIPTION = "Two lines meet at a pair of endpoints, and the angle between those lines is obtuse";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * Default threshold for this constraint. This isn't actually used.
	 */
	public static final double DEFAULT_THRESHOLD = 35;
	
	/**
	 * Constraint to see if the endpoints meet.
	 */
	protected CoincidentConstraint m_coincident = new CoincidentConstraint();
	
	/**
	 * Compute the confidence that the angle between the lines is obtuse
	 */
	protected ObtuseAngleConstraint m_obtuseAngle = new ObtuseAngleConstraint();
	
	
	/**
	 * Constrcut this constraint with the default threshold
	 */
	public ObtuseMeetConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct this constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use
	 */
	public ObtuseMeetConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.AbstractConstraint#
	 * multiplyThreshold(double)
	 */
	@Override
	public void multiplyThreshold(double thresholdMultiplier) {
		super.multiplyThreshold(thresholdMultiplier);
		m_coincident.multiplyThreshold(thresholdMultiplier);
		m_obtuseAngle.multiplyThreshold(thresholdMultiplier);
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
			log.debug("Obtuse Meet requires " + getNumRequiredParameters()
			          + " ConstrainableLines");
			return 0;
		}
		
		IConstrainable line1 = getParameters().get(0);
		IConstrainable line2 = getParameters().get(1);
		
		if (line1 instanceof ConstrainableLine
		    && line2 instanceof ConstrainableLine) {
			return solve((ConstrainableLine) line1, (ConstrainableLine) line2);
		}
		else {
			log.debug("Obtuse Meet requires two ConstrainableLines");
			return 0;
		}
	}
	

	/**
	 * Shortcut method to see if the two lines meet at an endpoint, and the
	 * angle between them is obtuse
	 * 
	 * @param line1
	 *            The first line
	 * @param line2
	 *            The second line
	 * @return The confidence that the lines meet at an endpoint and the angle
	 *         between them is obtuse
	 */
	public double solve(ConstrainableLine line1, ConstrainableLine line2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(line1);
		parms.add(line2);
		setParameters(parms);
		
		// two endpoints for each shape, first and last
		// first shape
		ConstrainablePoint pt11 = line1.getEnd1();
		ConstrainablePoint pt12 = line1.getEnd2();
		log.debug("Line 1, pt11 " + pt11 + ",  and pt12 " + pt12);
		// second shape
		ConstrainablePoint pt21 = line2.getEnd1();
		ConstrainablePoint pt22 = line2.getEnd2();
		log.debug("Line 2, pt21 " + pt21 + ",  and pt22 " + pt22);
		
		// distances between the 4 sets of points
		double dist11 = pt11.distance(pt21); // 11 to 21
		double dist12 = pt11.distance(pt22); // 11 to 22
		double dist21 = pt12.distance(pt21); // 12 to 21
		double dist22 = pt12.distance(pt22); // 12 to 22
		log.debug("dist11: " + dist11 + ", dist12: " + dist12 + ", dist21: "
		          + dist21 + ", dist22: " + dist22);
		
		// two points we'll use to test if they're coincident, endpoints meet
		ConstrainablePoint point1;
		ConstrainablePoint point2;
		
		// which pair of endpoints is closest to touching?
		double minDist = Math.min(Math.min(dist11, dist12), Math.min(dist21,
		        dist22));
		log.debug("minDist = " + minDist);
		
		// From that pair, construct our two lines moving AWAY from those points
		if (minDist == dist11) {
			// 11 is closest to 21
			point1 = pt11;
			point2 = pt21;
			line1 = new ConstrainableLine(pt11, pt12, line1.getParentShape());
			line2 = new ConstrainableLine(pt21, pt22, line2.getParentShape());
		}
		else if (minDist == dist12) {
			// 11 closest to 22
			point1 = pt11;
			point2 = pt22;
			line1 = new ConstrainableLine(pt11, pt12, line1.getParentShape());
			line2 = new ConstrainableLine(pt22, pt21, line2.getParentShape());
		}
		else if (minDist == dist21) {
			// 12 is closest to 21
			point1 = pt12;
			point2 = pt21;
			line1 = new ConstrainableLine(pt12, pt11, line1.getParentShape());
			line2 = new ConstrainableLine(pt21, pt22, line2.getParentShape());
		}
		else {
			// 12 is closest to 22
			point1 = pt12;
			point2 = pt22;
			line1 = new ConstrainableLine(pt12, pt11, line1.getParentShape());
			line2 = new ConstrainableLine(pt22, pt21, line2.getParentShape());
		}
		
		// how sure are we the endpoints meet?
		double coincidentConfidence = m_coincident.solve(point1, point2);
		log.debug("coincident conf = " + coincidentConfidence);
		
		// how sure we are the lines are acute
		double obtuseConfidence = m_obtuseAngle.solve(line1, line2);
		log.debug("obtuse conf = " + obtuseConfidence);
		
		// TODO how do we combine these confidences? mean for now
		double conf = (coincidentConfidence + obtuseConfidence) / 2.0;
		log.debug("combined conf = " + conf);
		
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
		return value < 0.2;
	}
}
