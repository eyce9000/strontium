/**
 * Polygon.java
 * 
 * Revision History:<br>
 * Oct 20, 2009 bpaulson - File created
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
package srl.recognition.paleo.cali;

import java.util.ArrayList;

import srl.core.sketch.Point;


/**
 * Polygon class
 * 
 * @author bpaulson
 */
public class Polygon extends ArrayList<Point> {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7282580307362370932L;

	/**
	 * Get the area of a polygon
	 * 
	 * @return area
	 */
	public double area() {
		double area = 0;
		if (size() < 3)
			return area;
		for (int i = 0; i < size() - 1; i++) {
			area += get(i).getX() * get(i + 1).getY() - get(i + 1).getX()
					* get(i).getY();
		}
		area /= 2.0;
		return Math.abs(area);
	}

	/**
	 * Get the perimeter of a polygon
	 * 
	 * @return perimeter
	 */
	public double perimeter() {
		double perim = 0;
		for (int i = 0; i < size() - 1; i++) {
			perim += get(i).distance(get(i + 1));
		}
		if (size() < 3)
			perim *= 2.0;
		return perim;
	}
}
