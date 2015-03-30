/**
 * PolygonFit.java
 *
 * Revision History:<br>
 * Jun 24, 2008 bpaulson - File created
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
package srl.recognition.paleo;

import org.openawt.geom.GeneralPath;
import org.openawt.geom.Line2D;
import org.openawt.geom.Point2D;
import org.openawt.svg.SVGLine;
import org.openawt.svg.SVGPolygon;

import java.util.ArrayList;
import java.util.List;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;
import srl.core.util.IsAConstants;


/**
 * Fit stroke to a polygon (polyline that is closed at the end points)
 * 
 * @author bpaulson
 */
public class PolygonFit extends Fit {

	/**
	 * Polyline fit of the stroke
	 */
	protected PolylineFit m_polylineFit;

	/**
	 * Constructor for polygon fit
	 * 
	 * @param features
	 *            features of the stroke
	 * @param polylineFit
	 *            polyline fit of the stroke
	 */
	public PolygonFit(StrokeFeatures features, PolylineFit polylineFit) {
		super(features);
		m_polylineFit = polylineFit;

		// test 1: endpoints of stroke must be close
		m_err = m_features.getEndptStrokeLengthRatio();
		if (m_err > Thresholds.active.M_POLYGON_PCT) {
			m_passed = false;
			m_fail = 0;
		}

		// TODO: remove eventually, this is for COA only
		// test 2: must have at least 5 lines
		if (m_polylineFit.getNumSubStrokes() < 5) {
			m_passed = false;
			m_fail = 1;
		}

		// generate beautified polygon
		generatePolygon();
		try {
			computeBeautified();
			// m_beautified.getAttributes().remove(IsAConstants.PRIMITIVE);
			m_beautified.setAttribute(IsAConstants.CLOSED, "true");
			m_beautified.setShapes(m_polylineFit.getSubShapes());
			m_beautified.setLabel(Fit.POLYGON + " ("
					+ m_polylineFit.getSubShapes().size() + ")");
		} catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage(),e);
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return POLYGON;
	}

	/**
	 * Generate the beautified polygon
	 */
	protected void generatePolygon() {
		List<Stroke> subStrokes = m_polylineFit.getSubStrokes();

		// just a single line
		if (subStrokes.size() <= 1) {
			m_shape = new SVGLine(new Line2D.Double(m_features.getFirstOrigPoint().toPoint2D(),
					m_features.getLastOrigPoint().toPoint2D()));
			
			return;
		}

		// multiple lines; ensure original endpoints are intact
		List<Point2D> points = new ArrayList<Point2D>();
		points.add(m_features.getFirstOrigPoint().toPoint2D());
		for(int i=0; i<subStrokes.size() - 1; i++){
			points.add(subStrokes.get(i).getPoints()
					.get(subStrokes.get(i).getNumPoints() - 1).toPoint2D());
		}
		m_shape = new SVGPolygon(points);
	}

	/**
	 * Get the sub stroke lines
	 * 
	 * @return sub strokes
	 */
	public List<Stroke> getSubStrokes() {
		return m_polylineFit.getSubStrokes();
	}
}
