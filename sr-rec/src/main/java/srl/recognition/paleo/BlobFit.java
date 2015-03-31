/**
 * BlobFit.java
 * 
 * Revision History:<br>
 * Dec 18, 2008 bpaulson - File created
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
 * Fit stroke to an amorphous closed blob
 * 
 * @author bpaulson
 */
public class BlobFit extends Fit {

	/**
	 * Constructor for blob fit
	 * 
	 * @param features
	 *            features of stroke
	 */
	public BlobFit(StrokeFeatures features) {
		super(features);
		m_passed = true;
		m_err = m_features.getBestFitDirGraphError();

		// test 1: closed shape
		if (!m_features.isClosed()) {
			m_passed = false;
			m_fail = 0;
		}

		// test 2: HIGH error (direction graph line error will be high)
		if (m_err < 0.531) {
			m_passed = false;
			m_fail = 1;
		}

		// test 3: high ndde
		if (m_features.getNDDE() < 0.81) {
			m_passed = false;
			m_fail = 2;
		}

		// test 4: need long stroke
		if (m_features.getStrokeLength() < 70.0) {
			m_passed = false;
			m_fail = 3;
		}

		// create shape/beautified object
		generateBlob();
		try {
			computeBeautified();
			m_beautified.setAttribute(IsAConstants.CLOSED, "true");
		} catch (Exception e) {

			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("BlobFit: passed = " + m_passed + "(" + m_fail
//				+ ") closed = " + m_features.isClosed() + "  NDDE = "
//				+ m_features.getNDDE() + " err = " + m_err + " strokelength = "
//				+ m_features.getStrokeLength());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return Fit.BLOB;
	}

	/**
	 * Method used to generate the beautified blob
	 */
	protected void generateBlob() {
		// making beautified shape the actual stroke instead of beautified blob
		GeneralPath path = new GeneralPath();
		path.moveTo(m_features.getFirstOrigPoint().getX(),
				m_features.getFirstOrigPoint().getY());
		for (int i = 1; i < m_features.getOrigPoints().size(); i++)
			path.lineTo(m_features.getOrigPoints().get(i)
					.getX(), m_features.getOrigPoints().get(i).getY());
		m_shape = new SVGPath(path);
	}
}
