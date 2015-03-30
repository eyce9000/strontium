/**
 * GraphNode.java
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

import java.util.UUID;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;


/**
 * Node used for constructing graph of connected (continuation) strokes
 * 
 * @author bpaulson
 */
public class GraphNode implements Comparable<GraphNode> {

	/**
	 * Constant indicating an index or number has not been set
	 */
	public final static int NOT_NUMBERED = -1;

	/**
	 * Low point value (used for Tarjan's algorithm)
	 */
	private int m_lowPt = NOT_NUMBERED;

	/**
	 * Number value (used for Tarjan's algorithm)
	 */
	private int m_number = NOT_NUMBERED;

	/**
	 * Stroke that represents this node
	 */
	private Stroke m_stroke;

	/**
	 * End point of the stroke that represents this node
	 */
	private Point m_endpoint;

	/**
	 * Unique ID of node
	 */
	private UUID m_id;

	/**
	 * Label for node
	 */
	private String m_label;

	/**
	 * Constructor for node
	 */
	public GraphNode(Stroke stroke, Point endpoint) {
		m_stroke = stroke;
		m_endpoint = endpoint;
		m_id = m_endpoint.getId();
		m_label = m_endpoint.getName();
	}

	/**
	 * Get the stroke being held by the node
	 * 
	 * @return stroke
	 */
	public Stroke getStroke() {
		return m_stroke;
	}

	/**
	 * Get the endpoint being held by the node
	 * 
	 * @return endpoint
	 */
	public Point getEndpoint() {
		return m_endpoint;
	}

	/**
	 * Get the number value of the node (used for Tarjan's)
	 * 
	 * @return number
	 */
	public int getNumber() {
		return m_number;
	}

	/**
	 * Get the low point value of the node (used for Tarjan's)
	 * 
	 * @return low point value
	 */
	public int getLowPt() {
		return m_lowPt;
	}

	/**
	 * Set the number value of the node (used for Tarjan's)
	 * 
	 * @param number
	 *            number
	 */
	public void setNumber(int number) {
		m_number = number;
	}

	/**
	 * Set the low point value of the node (used for Tarjan's)
	 * 
	 * @param lowPt
	 *            low point value
	 */
	public void setLowPt(int lowPt) {
		m_lowPt = lowPt;
	}

	/**
	 * Get the node's UUID
	 * 
	 * @return id
	 */
	public UUID getID() {
		return m_id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GraphNode o) {
		if (m_id.compareTo(o.m_id) == 0)
			return 0;
		return (int) (o.getStroke().getTimeEnd() - getStroke().getTimeEnd());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof GraphNode)
			return m_id.equals(((GraphNode) o).m_id);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return m_id.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return m_label;
	}
}