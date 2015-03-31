/**
 * DotFit.java
 * 
 * Revision History:<br>
 * Nov 13, 2008 bpaulson - File created
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

import org.openawt.svg.SVGCircle;

import srl.core.sketch.Point;
import srl.core.util.IsAConstants;


/**
 * Fit a point to a dot (filled in circle)
 * 
 * @author bpaulson
 */
public class DotFit extends Fit {


	/**
	 * Stroke density
	 */
	protected double m_density;

	/**
	 * Height:Width ratio
	 */
	protected double m_heightWidthRatio;

	/**
	 * Constructor for dot fit
	 * 
	 * @param features
	 *            features of the stroke to fit
	 */
	public DotFit(StrokeFeatures features) {
		super(features);
		m_passed = false;

		// compute density
		if (features.getNumPoints() <= 2)
			m_passed = true;
		else {
			m_density = m_features.getStrokeLength()
					/ m_features.getBounds().getArea();
			if (m_density > 0.4) // was 0.5
				m_passed = true;
			else
				m_fail = 0;

			// loosen if multiple revolutions present
			if (m_density > 0.3 && m_features.getNumRevolutions() > 2.0)
				m_passed = true;
			else
				m_fail = 1;
		}

		// compute height to width ratio
		if (m_features.getBounds() != null) {
			m_heightWidthRatio = Math.abs(m_features.getBounds().height
					/ m_features.getBounds().width - 1.0);
			if (m_heightWidthRatio > 3.0) {
				m_passed = false;
				m_fail = 2;
			}
		}

		if (m_features.getNumRevolutions() <= 1.5 && m_density < 0.56) {
			m_passed = false;
			m_fail = 3;
		}

		// generate shape
		generateDot();
		try {
			computeBeautified();
			m_beautified.setAttribute(IsAConstants.CLOSED, "true");
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("DotFit: passed = " + m_passed + "(" + m_fail
//				+ ") density = " + m_density + " ratio = " + m_heightWidthRatio
//				+ " num revs = " + m_features.getNumRevolutions()
//				+ " stroke length = " + m_features.getStrokeLength()
//				+ " endpt ratio = " + m_features.getEndptStrokeLengthRatio()
//				+ " perim:sl ratio = " + m_features.getStrokeLengthPerimRatio()
//				+ " revs*density = " + m_density
//				* m_features.getNumRevolutions());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return Fit.DOT;
	}

	/**
	 * Get stroke density
	 * 
	 * @return stroke density
	 */
	public double getDensity() {
		return m_density;
	}

	/**
	 * Height to width ratio of bounding box
	 * 
	 * @return ratio
	 */
	public double getHeightWidthRatio() {
		return m_heightWidthRatio;
	}

	/**
	 * Create dot shape
	 */
	protected void generateDot() {
		if (m_features.getNumPoints() <= 2) {
			m_shape = new SVGCircle(m_features.getFirstOrigPoint()
					.getX(), m_features.getFirstOrigPoint().getY(), 4);
		} else {
			Point center = m_features.getBounds().getCenterPoint();
			double radius = (m_features.getBounds().height + m_features
					.getBounds().width) / 4;
			m_shape = new SVGCircle(center.getX() ,
					center.getY(), radius );
		}
	}
}
