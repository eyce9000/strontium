/**
 * SquareFit.java
 * 
 * Revision History:<br>
 * Aug 22, 2008 bpaulson - File created
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

import org.openawt.geom.Rectangle2D;
import org.openawt.svg.SVGRectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.util.IsAConstants;


/**
 * Fit stroke to a square
 * 
 * @author bpaulson
 */
public class SquareFit extends Fit{

	/**
	 * Rectangle fit of stroke
	 */
	protected RectangleFit m_rectangleFit;

	/**
	 * Ratio between width and height
	 */
	protected double m_widthHeightRatio;

	/**
	 * Size of the square
	 */
	protected double m_squareSize;

	/**
	 * Constructor for square fit
	 * 
	 * @param features
	 *            features of stroke
	 * @param rectangleFit
	 *            rectangle fit of stroke
	 */
	public SquareFit(StrokeFeatures features, RectangleFit rectangleFit) {
		super(features);
		m_rectangleFit = rectangleFit;
		m_passed = m_rectangleFit.passed();
		m_fail = 0;
		m_err = m_rectangleFit.getError();
		m_widthHeightRatio = Math.max(m_rectangleFit.m_width,
				m_rectangleFit.m_height)
				/ Math.min(m_rectangleFit.m_width, m_rectangleFit.m_height);
		m_squareSize = (m_rectangleFit.m_width + m_rectangleFit.m_height) / 2.0;

		// check ratio of width to height
		if (m_passed) {
			if (m_widthHeightRatio > 1.2) {
				m_passed = false;
				m_fail = 1;
			}
		}

		// create shape/beautified object
		generateSquare();
		try {
			computeBeautified();
			m_beautified.setAttribute(IsAConstants.CLOSED, "true");
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("Square Fit: passed = " + m_passed + "(" + m_fail
//				+ ") err = " + m_err + " dcr = " + m_features.getDCR()
//				+ " ndde = " + m_features.getNDDE() + " endpt:sl ratio = "
//				+ m_features.getEndptStrokeLengthRatio() + " ratio = "
//				+ m_widthHeightRatio);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return Fit.SQUARE;
	}

	/**
	 * Get the width to height ratio of the square
	 * 
	 * @return width:height ratio
	 */
	public double getWidthHeightRatio() {
		return m_widthHeightRatio;
	}

	/**
	 * Generates the beautified square
	 */
	protected void generateSquare() {
		m_shape = new SVGRectangle(new Rectangle2D.Double(m_features.getBounds().getX(),
				m_features.getBounds().getY(), m_squareSize, m_squareSize));
	}
}
