/**
 * DiamondFit.java
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

import org.openawt.geom.AffineTransform;
import org.openawt.geom.GeneralPath;
import org.openawt.geom.Line2D;
import org.openawt.geom.Rectangle2D;
import org.openawt.svg.SVGPath;
import org.openawt.svg.SVGRectangle;
import org.openawt.svg.SVGShape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.core.util.IsAConstants;
import srl.segmentation.paleo.PaleoSegmenter;


/**
 * Fit stroke to a diamond
 * 
 * @author bpaulson
 */
public class DiamondFit extends Fit {

	/**
	 * Features of the rotated stroke
	 */
	protected StrokeFeatures m_rotatedFeatures;

	/**
	 * Ellipse fit used once stroke is rotated
	 */
	protected EllipseFit m_ellipseFit;

	/**
	 * Square fit that will be used once stroke is rotated
	 */
	protected SquareFit m_squareFit;

	/**
	 * Rectangle fit that will be used once stroke is rotated
	 */
	protected RectangleFit m_rectangleFit;

	/**
	 * Segmentation to use for rotated stroke
	 */
	protected Segmentation m_segmentation;

	/**
	 * Point to rotate around
	 */
	protected Point m_rotationPoint;

	/**
	 * Perimeter to stroke length ratio
	 */
	protected double m_perimeterStrokeLengthRatio;

	/**
	 * Location of top, bottom, right, left most points relative to bounding box
	 */
	protected double m_top, m_bottom, m_right, m_left;

	/**
	 * Rendered version of the diamond
	 */
	protected GeneralPath m_rendered_shape;
	
	/**
	 * Constructor for diamond fit
	 * 
	 * @param features
	 *            feature of the stroke
	 * @param config
	 *            paleo configuration
	 * @param segmentation
	 *            corner finding segmentation
	 */
	public DiamondFit(StrokeFeatures features, PaleoConfig config,
			Segmentation segmentation) {
		super(features);
		m_segmentation = segmentation;
		m_passed = true;
		m_rotationPoint = features.getBounds().getCenterPoint();

		// rotate stroke by 45 degrees and perform rectangle check
		Stroke newStroke = StrokeFeatures.rotatePoints(
				features.getOrigStroke(), Math.PI / 4.0, m_rotationPoint);
		if (config.getHeuristics().FILTER_DIR_GRAPH)
			m_rotatedFeatures = new StrokeFeatures(newStroke, true);
		else
			m_rotatedFeatures = new StrokeFeatures(newStroke, false);
		try {
			m_ellipseFit = new EllipseFit(m_rotatedFeatures);
			m_segmentation = new PaleoSegmenter(m_rotatedFeatures)
					.getSegmentations().get(0);
			m_rectangleFit = new RectangleFit(m_rotatedFeatures, m_ellipseFit,
					m_segmentation);
			m_squareFit = new SquareFit(m_rotatedFeatures, m_rectangleFit);
		} catch (Exception e) {
			m_passed = false;
			m_fail = 0;
			return;
		}
		m_err = m_squareFit.getError();
		double perimeter = m_features.getBounds().getPerimeter();
		m_perimeterStrokeLengthRatio = m_features.getStrokeLength() / perimeter;
		if (m_err > 0.59) {
			m_passed = false;
			m_fail = 1;
		}
		if (m_squareFit.getWidthHeightRatio() > 1.53) {
			m_passed = false;
			m_fail = 2;
		}
		if (m_features.getEndptStrokeLengthRatio() > 0.075) {
			m_passed = false;
			m_fail = 3;
		}
		if (m_rectangleFit.m_ratio > 0.235) {
			m_passed = false;
			m_fail = 4;
		}
		if ((m_rectangleFit.getPerimeterStrokeLengthRatio() < 0.7 && m_rectangleFit
				.getNumCorners() < 4)) {
			m_passed = false;
			m_fail = 5;
		}
		// m_passed = m_squareFit.passed();

		// check for bowties (indication of NOT diamond)
		if (m_segmentation.getSegmentedStrokes().size() == 4) {
			Stroke s1 = m_segmentation.getSegmentedStrokes().get(0);
			Stroke s2 = m_segmentation.getSegmentedStrokes().get(1);
			Stroke s3 = m_segmentation.getSegmentedStrokes().get(2);
			Stroke s4 = m_segmentation.getSegmentedStrokes().get(3);
			Line2D l1 = new Line2D.Double(s1.getFirstPoint().getX(), s1
					.getFirstPoint().getY(), s1.getLastPoint().getX(), s1
					.getLastPoint().getY());
			Line2D l2 = new Line2D.Double(s2.getFirstPoint().getX(), s2
					.getFirstPoint().getY(), s2.getLastPoint().getX(), s2
					.getLastPoint().getY());
			Line2D l3 = new Line2D.Double(s3.getFirstPoint().getX(), s3
					.getFirstPoint().getY(), s3.getLastPoint().getX(), s3
					.getLastPoint().getY());
			Line2D l4 = new Line2D.Double(s4.getFirstPoint().getX(), s4
					.getFirstPoint().getY(), s4.getLastPoint().getX(), s4
					.getLastPoint().getY());
			if (l1.intersectsLine(l3) || l2.intersectsLine(l4)) {
				m_passed = false;
				m_fail = 6;
			}
		}

		// new test: corner distance
		if (m_features.getAvgCornerStrokeDistance() < 0.22) {
			m_passed = false;
			m_fail = 7;
		}

		// new test: perim stroke length ratio
		if (m_perimeterStrokeLengthRatio > 0.85
				|| m_perimeterStrokeLengthRatio < 0.67) {
			m_passed = false;
			m_fail = 8;
		}

		// new test: top, bottom, left, right test
		m_top = (m_features.getTopMostPoint().getX() - m_features.getBounds()
				.getLeft()) / m_features.getBounds().width;
		m_bottom = (m_features.getBottomMostPoint().getX() - m_features
				.getBounds().getLeft()) / m_features.getBounds().width;
		m_left = (m_features.getLeftMostPoint().getY() - m_features.getBounds()
				.getTop()) / m_features.getBounds().height;
		m_right = (m_features.getRightMostPoint().getY() - m_features
				.getBounds().getTop()) / m_features.getBounds().height;
		if (m_top < 0.2 || m_top > 0.8 || m_bottom < 0.2 || m_bottom > 0.8
				|| m_right < 0.2 || m_right > 0.8 || m_left < 0.2
				|| m_left > 0.8) {
			m_passed = false;
			m_fail = 9;
		}

		// create shape/beautified object
		generateDiamond();
		try {
			computeBeautified();
			m_beautified.setAttribute(IsAConstants.CLOSED, "true");
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}

//		log.debug("Diamond Fit: passed = " + m_passed + "(" + m_fail
//				+ ") err = " + m_err + " ellipse err = "
//				+ m_ellipseFit.getError() + " dcr = " + m_features.getDCR()
//				+ " ndde = " + m_features.getNDDE() + " endpt:sl ratio = "
//				+ m_features.getEndptStrokeLengthRatio() + " num ss = "
//				+ m_segmentation.getSegmentedStrokes().size()
//				+ " rect passed = " + m_rectangleFit.passed()
//				+ " square passed = " + m_squareFit.passed() + " ratio = "
//				+ m_squareFit.m_widthHeightRatio + " major axis angle = "
//				+ (m_ellipseFit.m_majorAxisAngle * (180.0 / Math.PI))
//				+ " bb:major axis ratio = " + m_rectangleFit.m_ratio
//				+ " perim:sl ratio = "
//				+ m_rectangleFit.getPerimeterStrokeLengthRatio()
//				+ " dir window = " + m_features.dirWindowPassed()
//				+ " slope dir graph = " + m_features.getSlopeDirGraph()
//				+ " corner avg = " + m_features.getAvgCornerStrokeDistance()
//				+ " num ss = " + m_segmentation.getSegmentedStrokes().size()
//				+ " top = " + m_top + " bottom = " + m_bottom + " left = "
//				+ m_left + " right = " + m_right);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return Fit.DIAMOND;
	}

	/**
	 * Get the perimeter (of bounding box) to stroke length ratio - in this case
	 * we use the rotated rectangles bounding box
	 * 
	 * @return perimeter (of bounding box) to stroke length ratio
	 */
	public double getPerimeterStrokeLengthRatio() {
		if (m_rectangleFit != null)
			return m_rectangleFit.getPerimeterStrokeLengthRatio();
		return 0;
	}

	/**
	 * Number of segments in the segmentation of the stroke
	 * 
	 * @return number of segments
	 */
	public int getNumSubStrokes() {
		return m_segmentation.getSegmentedStrokes().size();
	}

	/**
	 * Get the width to height ratio of the square fit used for the diamond fit
	 * 
	 * @return width:height ratio
	 */
	public double getWidthHeightRatio() {
		if (m_squareFit != null)
			return m_squareFit.getWidthHeightRatio();
		return 0;
	}

	/**
	 * Get the major axis to bounding box diagonal length ratio
	 * 
	 * @return ratio
	 */
	public double getMajorAxisBBDiagRatio() {
		if (m_rectangleFit != null)
			return m_rectangleFit.getMajorAxisBBDiagRatio();
		return 0;
	}

	/**
	 * Generates the beautified diamond
	 */
	protected void generateDiamond() {
		
		Rectangle2D rect = new Rectangle2D.Double(m_rotatedFeatures.getBounds().getX(),
				m_rotatedFeatures.getBounds().getY(), m_squareFit.m_squareSize,
				m_squareFit.m_squareSize);
		m_rendered_shape = new GeneralPath();
		AffineTransform t = AffineTransform.getRotateInstance(-1.0 * Math.PI / 4.0, 
				this.m_rotationPoint.x, this.m_rotationPoint.y);
		m_rendered_shape.append(rect.getPathIterator(t), false);
		m_shape = new SVGPath(m_rendered_shape);
	}
	
	@Override
	public SVGShape toSVGShape(){
		return m_shape;
	}
}
