/**
 * GraphCycle.java
 * 
 * Revision History:<br>
 * Jun 5, 2009 bpaulson - File created
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

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;


/**
 * Graph cycle set (contains a set of nodes that make a complete cycle)
 * 
 * @author bpaulson
 */
public class GraphCycle extends ArrayList<GraphNode> implements
		Comparable<GraphCycle> {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 899877548744573378L;

	/**
	 * Flag denoting if cycle found is a full cycle (i.e. a closed shape)
	 */
	private boolean m_fullCycle = false;

	/**
	 * Get the strokes found in the cycle
	 * 
	 * @return list of strokes found
	 */
	public List<Stroke> getStrokes() {
		List<Stroke> list = new ArrayList<Stroke>();
		for (GraphNode n : this) {
			if (!list.contains(n.getStroke()))
				list.add(n.getStroke());
		}

		return orderStrokes(list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	@Override
	public boolean add(GraphNode n) {
		if (!contains(n))
			return super.add(n);
		return false;
	}

	/**
	 * Orders the stroke list such that connected strokes are in order of
	 * consistency (not time)
	 * 
	 * @param list
	 *            stroke list
	 * @return re-ordered stroke list
	 */
	private List<Stroke> orderStrokes(List<Stroke> list) {
		if (list.size() < 3) {
			Collections.sort(list);
			return list;
		}
		List<Stroke> orderedList = new ArrayList<Stroke>();
		orderedList.add(list.get(0));
		list.remove(0);
		while (list.size() > 0) {
			Point curr = orderedList.get(orderedList.size() - 1).getLastPoint();
			Point curr2 = orderedList.get(orderedList.size() - 1)
					.getFirstPoint();
			double closest = Double.MAX_VALUE;
			Stroke closestStr = null;
			for (Stroke s : list) {
				double dist1 = curr.distance(s.getFirstPoint());
				double dist2 = curr.distance(s.getLastPoint());
				double dist3 = curr2.distance(s.getFirstPoint());
				double dist4 = curr2.distance(s.getLastPoint());
				if (dist1 < closest) {
					closest = dist1;
					closestStr = s;
				}
				if (dist2 < closest) {
					closest = dist2;
					closestStr = s;
				}
				if (dist3 < closest) {
					closest = dist3;
					closestStr = s;
				}
				if (dist4 < closest) {
					closest = dist4;
					closestStr = s;
				}
			}
			orderedList.add(closestStr);
			list.remove(closestStr);
		}
		return orderedList;
	}

	/**
	 * Flag denoting if cycle is a full cycle (i.e. closed shape)
	 * 
	 * @return true if closed; else false
	 */
	public boolean isFullCycle() {
		return m_fullCycle;
	}

	/**
	 * Set flag denoting if cycle if a full or partial cycle
	 * 
	 * @param fullCycle
	 *            true if full; else false
	 */
	public void setFullCycle(boolean fullCycle) {
		m_fullCycle = fullCycle;
	}

	/**
	 * Returns the nodes of a particular stroke that are found in the cycle
	 * 
	 * @param s
	 *            stroke
	 * @return nodes that belong to that stroke
	 */
	public List<GraphNode> getNodesOfStroke(Stroke s) {
		List<GraphNode> nodes = new ArrayList<GraphNode>();
		for (GraphNode n : this) {
			if (n.getStroke().equals(s))
				nodes.add(n);
		}
		return nodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GraphCycle o) {
		return o.size() - size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof GraphCycle) {
			GraphCycle gc = (GraphCycle) o;
			if (gc.size() != size())
				return false;
			for (int i = 0; i < size(); i++) {
				GraphNode n1 = (GraphNode) toArray()[i];
				GraphNode n2 = (GraphNode) gc.toArray()[i];
				if (!n1.equals(n2))
					return false;
			}
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " full cycle = " + m_fullCycle;
	}
}
