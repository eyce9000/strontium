/**
 * AdjacencyList.java
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
import java.util.List;

/**
 * Adjacency list for graph nodes and edges (undirected)
 * 
 * @author bpaulson
 */
public class Graph {

	/**
	 * Adjacency list
	 */
	private AdjacencyList m_adjList;

	/**
	 * Node list
	 */
	private List<GraphNode> m_nodes;

	/**
	 * Default constructor
	 */
	public Graph() {
		clear();
	}

	/**
	 * Create a new edge and add it to the adjacency list
	 * 
	 * @param node1
	 *            node 1 of edge
	 * @param node2
	 *            node 2 of edge
	 */
	public void addEdge(GraphNode node1, GraphNode node2) {

		// add nodes to hashmap
		m_adjList.addNode(node1);
		m_adjList.addNode(node2);

		// add edge for both nodes
		m_adjList.addEdge(node1, node2);
		m_adjList.addEdge(node2, node1);
		m_nodes.add(node1);
		m_nodes.add(node2);
	}

	/**
	 * Get the nodes of the graph
	 * 
	 * @return set of nodes
	 */
	public List<GraphNode> getNodes() {
		return m_nodes;
	}

	/**
	 * Get the set of adjacent nodes to the given node
	 * 
	 * @param node
	 *            input node
	 * @return adjacent nodes
	 */
	public List<GraphNode> getAdjacentNodes(GraphNode node) {
		return m_adjList.get(node);
	}

	/**
	 * Remove a node from a graph
	 * 
	 * @param node
	 *            node to remove
	 */
	public void removeNode(GraphNode node) {
		for (List<GraphNode> list : m_adjList.values())
			list.remove(node);
		m_adjList.remove(node);
		m_nodes.remove(node);
	}

	/**
	 * Reset all Tarjan values for all nodes
	 */
	public void resetNodes() {
		for (int i = 0; i < m_nodes.size(); i++) {
			m_nodes.get(i).setLowPt(GraphNode.NOT_NUMBERED);
			m_nodes.get(i).setNumber(GraphNode.NOT_NUMBERED);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "# nodes: " + m_nodes.size() + "\n";
		for (GraphNode n : m_adjList.keySet()) {
			str += n.toString() + ": ";
			for (GraphNode e : m_adjList.get(n)) {
				str += e.toString() + ", ";
			}
			str += "\n";
		}
		return str;
	}

	/**
	 * Clear the graph
	 */
	public void clear() {
		m_adjList = new AdjacencyList();
		m_nodes = new ArrayList<GraphNode>();
	}
}
