/**
 * RectangleFit.java
 * 
 * Revision History:<br>
 * Jun 25, 2008 bpaulson - File created
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

import org.openawt.svg.SVGRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Segmentation;
import srl.core.util.IsAConstants;
import srl.math.FeatureArea;


/**
 * Fit stroke to a rectangle (vertical or horizontal only)
 * 
 * @author bpaulson
 */
public class RectangleFit extends Fit {

	/**
	 * Ellipse fit of the stroke
	 */
	protected EllipseFit m_ellipseFit;

	/**
	 * Least squares error
	 */
	protected double m_lsqe;

	/**
	 * Width of rectangle
	 */
	protected double m_width;

	/**
	 * Height of rectangle
	 */
	protected double m_height;

	/**
	 * Bounding box:major axis ratio
	 */
	protected double m_ratio;

	/**
	 * Perimeter (of bounding box) to stroke length ratio
	 */
	protected double m_perimeterStrokeLengthRatio;

	/**
	 * Number of corners found (from segmentation)
	 */
	protected int m_numCorners;

	/**
	 * Construct a rectangle fit for a stroke
	 * 
	 * @param features
	 *            features of the stroke
	 * @param ellipseFit
	 *            ellipse fit of the stroke
	 * @param segmentation
	 *            corner finding interpretation
	 */
	public RectangleFit(StrokeFeatures features, EllipseFit ellipseFit,
			Segmentation segmentation) {
		super(features);
		m_ellipseFit = ellipseFit;

		// test 1: feature area error must be low
		try {
			m_err = FeatureArea.toRectangle(m_features.getOrigPoints(),
					m_features.getBounds()) / m_features.getBounds().getArea();
		} catch (Exception e) {
			m_passed = false;
			m_fail = 0;
			return;
		}

		if (m_err > Thresholds.active.M_RECTANGLE_ERROR) {
			m_passed = false;
			m_fail = 1;
		}

		// test 2: diagonal of bounding box should be similar to major axis
		m_ratio = Math.abs(1.0 - m_ellipseFit.getMajorAxisLength()
				/ m_features.getBounds().getDiagonalLength());
		if (m_ratio > Thresholds.active.M_BB_MAJOR_AXIS_RATIO) {
			m_passed = false;
			m_fail = 2;
		}

		// test 3: endpoints of stroke must be close
		if (m_features.getEndptStrokeLengthRatio() > 0.09) {// 0.075)
			m_passed = false;
			m_fail = 3;
		}

		// test 4: check number of corners to make sure its not too high
		// if (segmentation.getSegmentedStrokes().size() > 6)
		// m_passed = false;

		// this is a check that checks for triangles
		m_numCorners = segmentation.getSegmentedStrokes().size();
		double perimeter = m_features.getBounds().getPerimeter();
		m_perimeterStrokeLengthRatio = m_features.getStrokeLength() / perimeter;
		if (m_perimeterStrokeLengthRatio < 0.85 && m_numCorners < 4) {
			m_passed = false;
			m_fail = 4;
		}

		// new test: corner distance
		if (m_features.getAvgCornerStrokeDistance() > 0.16)
			m_passed = false;

		// create shape/beautified object
		generateRectangle();
		try {
			computeBeautified();
			m_beautified.setAttribute(IsAConstants.CLOSED, "true");
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("Rectangle Fit: passed = " + m_passed + "(" + m_fail
//				+ ") err = " + m_err + " lsqe = " + m_lsqe + " ellipse err = "
//				+ m_ellipseFit.getError() + " major:bbdiag ratio = " + m_ratio
//				+ " dcr = " + m_features.getDCR() + " ndde = "
//				+ m_features.getNDDE() + " endpt:sl ratio = "
//				+ m_features.getEndptStrokeLengthRatio() + " num ss = "
//				+ m_numCorners + " avg corner/stroke dist = "
//				+ m_features.getAvgCornerStrokeDistance() + " num revs = "
//				+ m_features.numRevolutions() + " perimeter:sl ratio = "
//				+ m_perimeterStrokeLengthRatio);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return RECTANGLE;
	}

	/**
	 * Get the perimeter (of bounding box) to stroke length ratio
	 * 
	 * @return perimeter (of bounding box) to stroke length ratio
	 */
	protected double getPerimeterStrokeLengthRatio() {
		return m_perimeterStrokeLengthRatio;
	}

	/**
	 * Get the major axis to bounding box diagonal length ratio
	 * 
	 * @return ratio
	 */
	protected double getMajorAxisBBDiagRatio() {
		return m_ratio;
	}

	/**
	 * Get the number of corners found by the segmentation of the rectangle
	 * 
	 * @return number of corners
	 */
	public int getNumCorners() {
		return m_numCorners;
	}

	/**
	 * Generates the beautified rectangle
	 */
	protected void generateRectangle() {
		m_shape = new SVGRectangle(m_features.getBounds());
		m_width = m_features.getBounds().getWidth();
		m_height = m_features.getBounds().getHeight();
	}
}
