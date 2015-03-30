/**
 * DashRecognizer.java
 * 
 * Revision History:<br>
 * Feb 24, 2009 bpaulson - File created
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
package srl.recognition.paleo.multistroke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import srl.core.sketch.SComponent;
import srl.core.sketch.Shape;
import srl.core.sketch.comparators.TimePeriodComparator;
import srl.core.util.IsAConstants;


/**
 * Recognizer that recognizes dashed line, rectangles, diamonds, ellipses, etc.
 * 
 * @author bpaulson
 */
public class DashRecognizer {

	/**
	 * Input shapes
	 */
	private List<Shape> m_input;

	/**
	 * Output shapes
	 */
	private List<Shape> m_output;

	/**
	 * Constructor
	 * 
	 * @param input
	 *            input shapes
	 */
	public DashRecognizer(List<Shape> input) {
		m_input = input;
		Collections.sort(m_input, new TimePeriodComparator());
	}

	/**
	 * Recognize dashed shapes within a list of shapes (given in constructor)
	 * 
	 * @return new set of shapes
	 */
	public List<Shape> recognize() {
		m_output = new ArrayList<Shape>();
		if (m_input.size() <= 0)
			return m_output;
		Collections.sort(m_input, new TimePeriodComparator());

		// find dashed lines
		DashLineRecognizer dlr = new DashLineRecognizer(m_input);
		m_output = dlr.recognize();

		// find dashed rectangles/diamonds
		DashBoundaryRecognizer dbr = new DashBoundaryRecognizer(m_output);
		m_output = dbr.recognize();

		// find dashed ellipses
		DashEllipseRecognizer der = new DashEllipseRecognizer(m_output);
		m_output = der.recognize();

		// needed b/c DashEllipseRecognizer removes dashed lines
		dlr = new DashLineRecognizer(m_output);
		m_output = dlr.recognize();

		// find stars
		StarRecognizer sr = new StarRecognizer(m_output);
		m_output = sr.recognize();

		Collections.sort(m_output, new TimePeriodComparator());
		return m_output;
	}

	/**
	 * Returns whether a given shape is dashed or not.
	 * 
	 * @param shape
	 *            shape to check for dashed attributes.
	 * @return {@code true} if the shape is dashed, {@code false} otherwise.
	 */
	public static boolean isDashed(Shape shape) {

		String dashed = shape.getAttribute(IsAConstants.DASHED);

		if (dashed != null && dashed == IsAConstants.DASHED) {
			return true;
		} else {
			return false;
		}
	}
}
