/**
 * TarjanAlgorithm.java
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

import srl.core.sketch.Stroke;


/**
 * Tarjan's algorithm for finding bi-connected components in a graph
 * 
 * @author bpaulson
 */
public class TarjanAlgorithm {

	/**
	 * Find and return all cycles found in the given graph
	 * 
	 * @param root
	 *            root node of graph
	 * @param graph
	 *            graph
	 * @return list of all cycles found
	 */
	public static List<GraphCycle> findCycles(Graph graph) {
		List<GraphCycle> cycles = new ArrayList<GraphCycle>();
		List<GraphNode> stack = new ArrayList<GraphNode>();
		int i = 0;
		for (GraphNode n : graph.getNodes()) {
			biConnect(n, graph, i, cycles, stack);
		}
		int num = cycles.size();
		for (i = 0; i < num; i++) {
			GraphCycle c = cycles.get(i);
			if (c.getStrokes().size() > 2 && c.size() > 4) {

				// get sub-cycle if it exists
				GraphCycle subCycle = new GraphCycle();
				for (Stroke s : c.getStrokes()) {
					List<GraphNode> nodes = c.getNodesOfStroke(s);
					if (nodes.size() > 1)
						subCycle.addAll(nodes);
				}
				if (subCycle.size() != c.size())
					cycles.add(subCycle);

				// get sub-sub cycle? - try last two strokes drawn
				if (subCycle.getStrokes().size() > 2 && subCycle.size() > 4) {
					GraphCycle subCycle2 = new GraphCycle();
					List<Stroke> lastTwo = getLastTwo(subCycle.getStrokes());
					for (Stroke s : lastTwo) {
						List<GraphNode> nodes = subCycle.getNodesOfStroke(s);
						if (nodes.size() > 1)
							subCycle2.addAll(nodes);
					}
					if (subCycle2.size() != subCycle.size())
						cycles.add(subCycle2);
				}
			}
		}
		return cycles;
	}

	/**
	 * Gets the last two strokes (based on time)
	 * 
	 * @param strokes
	 *            strokes to search
	 * @return last two strokes
	 */
	private static List<Stroke> getLastTwo(List<Stroke> strokes) {
		List<Stroke> last = new ArrayList<Stroke>();
		Stroke last1 = strokes.get(0);
		Stroke last2 = strokes.get(1);
		for (int i = 2; i < strokes.size(); i++) {
			if (strokes.get(i).getTimeEnd() > last1.getTimeEnd()) {
				last2 = last1;
				last1 = strokes.get(i);
			} else if (strokes.get(i).getTimeEnd() > last2.getTimeEnd())
				last2 = strokes.get(i);
		}
		last.add(last1);
		last.add(last2);
		return last;
	}

	/**
	 * Recursive sub-function of Tarjan's algorithm
	 * 
	 * @param v
	 *            current node v
	 * @param graph
	 *            master graph / adjacency list
	 * @param i
	 *            current index i
	 * @param cycles
	 *            list of cycles found
	 * @param stack
	 *            current edge stack
	 */
	private static void biConnect(GraphNode v, Graph graph, int i,
			List<GraphCycle> cycles, List<GraphNode> stack) {
		v.setNumber(i);
		v.setLowPt(i);
		i++;
		stack.add(0, v);
		for (GraphNode w : graph.getAdjacentNodes(v)) {
			if (w.getNumber() == GraphNode.NOT_NUMBERED) {
				biConnect(w, graph, i, cycles, stack);
				v.setLowPt(Math.min(v.getLowPt(), w.getLowPt()));
			} else if (stack.contains(w)) {
				v.setLowPt(Math.min(v.getLowPt(), w.getNumber()));
			}
		}
		if (v.getLowPt() == v.getNumber() && stack.size() > 0) {
			addCycle(v, graph, cycles, stack, true);
		}
	}

	/**
	 * Add a cycle to the list of cycles found
	 * 
	 * @param v
	 *            graph node
	 * @param graph
	 *            graph
	 * @param cycles
	 *            cycle list
	 * @param stack
	 *            current stack
	 * @param popFromStack
	 *            flag denoting if pop should take place
	 */
	private static void addCycle(GraphNode v, Graph graph,
			List<GraphCycle> cycles, List<GraphNode> stack, boolean popFromStack) {
		List<GraphNode> tmpStack = new ArrayList<GraphNode>();
		for (int i = 0; i < stack.size(); i++)
			tmpStack.add(stack.get(i));

		// every node should have at least 2 edges (ADDED STEP), otherwise its
		// not a full cycle
		boolean passed = true;
		for (int i = 0; i < stack.size() && passed; i++) {
			if (graph.getAdjacentNodes(stack.get(i)).size() < 2)
				passed = false;
		}

		GraphCycle c = new GraphCycle();
		GraphNode n = stack.remove(0);
		// if (graph.getAdjacentNodes(n).size() > 1)
		c.add(n);
		while (n != v && stack.size() > 0) {
			n = stack.remove(0);
			// if (graph.getAdjacentNodes(n).size() > 1)
			c.add(n);
		}
		c.setFullCycle(passed);
		if (c.size() > 1 && !cycles.contains(c)) // && passed)
			cycles.add(c);
		if (!popFromStack)
			stack = tmpStack;
	}
}
