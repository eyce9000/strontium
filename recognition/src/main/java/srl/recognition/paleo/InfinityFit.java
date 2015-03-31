/**
 * InfinityFit.java
 * 
 * Revision History:<br>
 * Jan 7, 2009 bpaulson - File created
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
package srl.recognition.paleo;

import org.openawt.geom.GeneralPath;
import org.openawt.svg.SVGPath;

import srl.core.util.IsAConstants;


/**
 * Fit stroke to an infinity symbol
 * 
 * @author bpaulson
 */
public class InfinityFit extends Fit {

	/**
	 * Constructor for infinity fit
	 * 
	 * @param features
	 *            features of the stroke to fit
	 */
	public InfinityFit(StrokeFeatures features) {
		super(features);
		m_passed = true;

		// mid range ndde
		if (m_features.getNDDE() > 0.75) {
			m_passed = false;
			m_fail = 0;
		}
		if (m_features.getNDDE() < 0.25) {
			m_passed = false;
			m_fail = 1;
		}

		// low dcr
		if (m_features.getDCR() > 5.9) {
			m_passed = false;
			m_fail = 2;
		}

		// near closed
		if (m_features.getEndptStrokeLengthRatio() > 0.1) {
			m_passed = false;
			m_fail = 3;
		}

		// revolutions should be low (because it cancels itself out)
		if (m_features.getNumRevolutions() > 0.5) {
			m_passed = false;
			m_fail = 4;
		}

		// try to distinguish from bowties
		// if (m_features.getDCR() > 5.3 &&
		// m_features.getMaxCurvToAvgCurvRatio() > 7.75) {
		// m_passed = false;
		// m_fail = 5;
		// }

		// generate shape
		try {
			generateInfinity();
			computeBeautified();
			m_beautified.setAttribute(IsAConstants.CLOSED, "true");
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("InfinityFit: passed = " + m_passed + "(" + m_fail + ")"
//				+ " ndde = " + m_features.getNDDE() + " dcr = "
//				+ m_features.getDCR() + " endpt:sl ratio = "
//				+ m_features.getEndptStrokeLengthRatio() + " revs = "
//				+ m_features.getNumRevolutions() + " max curv = "
//				+ m_features.getMaxCurv() + " max curv ratio = "
//				+ m_features.getMaxCurvToAvgCurvRatio());
	}

	/**
	 * Creates beautified infinity
	 */
	protected void generateInfinity() {
		final double step = 0.05;
		double a;
		double ox, oy;
		double radius;

		ox = m_features.getBounds().getCenterX();
		oy = m_features.getBounds().getCenterY();

		radius = m_features.getBounds().width / 4;
		a = radius * 2;

		// start generating shape (from center point out)
		GeneralPath infinity = new GeneralPath();
		double theta = 0.0;
		double r = Math.sqrt(2 * a * a * Math.cos(2 * theta));
		infinity.moveTo((r * Math.cos(theta) + ox),
				(r * Math.sin(theta) + oy));

		// generate Lemniscate of Bernoulli

		// part 1: 0.0 to 0.8
		while (theta < 0.8) {
			theta += step;
			r = Math.sqrt(2 * a * a * Math.cos(2 * theta));
			if (!Double.isNaN(r)) {
				double newx = r * Math.cos(theta) + ox;
				double newy = r * Math.sin(theta) + oy;
				infinity.lineTo(newx, newy);
			}
		}

		// part 2: 4.0 to 2.4
		theta = 4.0;
		while (theta > 2.4) {
			theta -= step;
			r = Math.sqrt(2 * a * a * Math.cos(2 * theta));
			if (!Double.isNaN(r)) {
				double newx = r * Math.cos(theta) + ox;
				double newy = r * Math.sin(theta) + oy;
				infinity.lineTo(newx, newy);
			}
		}

		// part 3: 5.0 to end
		theta = 5.0;
		while (theta < 7.0) {
			theta += step;
			r = Math.sqrt(2 * a * a * Math.cos(2 * theta));
			if (!Double.isNaN(r)) {
				double newx = r * Math.cos(theta) + ox;
				double newy = r * Math.sin(theta) + oy;
				infinity.lineTo(newx, newy);
			}
		}
		m_shape = new SVGPath(infinity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return Fit.INFINITY;
	}

}
