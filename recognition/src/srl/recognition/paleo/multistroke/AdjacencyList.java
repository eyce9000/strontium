/**
 * AdjacencyList.java
 * 
 * Revision History:<br>
 * Jun 9, 2009 bpaulson - File created
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
import java.util.HashMap;
import java.util.List;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;


/**
 * 
 * @author bpaulson
 */
public class AdjacencyList extends HashMap<GraphNode, List<GraphNode>> {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 5751296444216392649L;

	/**
	 * Add a node to the adjacency list
	 * 
	 * @param n
	 *            node to add
	 */
	public void addNode(GraphNode n) {
		if (!containsKey(n))
			put(n, new ArrayList<GraphNode>());
	}

	/**
	 * Add an edge from node 1 to node 2
	 * 
	 * @param from
	 *            node from
	 * @param to
	 *            node to
	 */
	public void addEdge(GraphNode from, GraphNode to) {
		if (containsKey(from) && !get(from).contains(to)) {
			if (get(from).size() <= 0)
				get(from).add(to);

			// same stroke so order is 1st
			else if (from.getStroke() == to.getStroke())
				get(from).add(0, to);

			else {
				// order according to angle
				boolean added = false;
				for (int i = 0; i < get(from).size() && !added; i++) {
					Stroke testStroke = get(from).get(i).getStroke();
					double angle1 = angleBetween(testStroke, from.getStroke());
					double angle2 = angleBetween(to.getStroke(),
							from.getStroke());
					if (angle2 > angle1 && testStroke != from.getStroke()) {
						get(from).add(i, to);
						added = true;
					}
				}
				if (!added)
					get(from).add(to);
			}
		}
	}

	/**
	 * Get the slope of a given Stroke
	 * 
	 * @param s
	 *            stroke to find slope of
	 * @return slope of stroke (assumed to be a line)
	 */
	private double getSlope(Stroke s) {
		Point p1 = s.getFirstPoint();
		Point p2 = s.getLastPoint();
		return (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
	}

	/**
	 * Determines the angle between two strokes
	 * 
	 * @param str1
	 *            stroke 1
	 * @param str2
	 *            stroke 2
	 * @return angle between two strokes
	 */
	private double angleBetween(Stroke str1, Stroke str2) {
		double s1 = getSlope(str1);
		double s2 = getSlope(str2);
		double value = Math.atan((s1 - s2) / (1 + s1 * s2)) * (180 / Math.PI);
		if (Double.isNaN(value))
			value = 90.0;
		value = Math.abs(value);
		return value;
	}
}
