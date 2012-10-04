/**
 * PaleoHeuristics.java
 * 
 * Revision History:<br>
 * Oct 23, 2008 bpaulson - File created
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

import java.io.Serializable;

/**
 * Class used to turn on and off certain "heuristics" to boost recognition in
 * some domains
 * 
 * @author bpaulson
 */
public class PaleoHeuristics implements Serializable{

	/**
	 * Flag for using the multiple corner finder rather than the traditional
	 * Paleo corner finder. Default = true
	 */
	public boolean MULTI_CF = true;

	/**
	 * Flag for checking for small "V"s which are typically mis-recognized by
	 * most corner finders. Turn this on if your domain contains small "V"
	 * shapes. Default = true
	 */
	public boolean SMALL_V = true;

	/**
	 * In some domains, arcs only point down (half circles). If this is the case
	 * then this flag can be used to boost recognition by requiring that arcs
	 * point down. Default = false
	 */
	public boolean ARC_DOWN = false;

	/**
	 * Post-processing flag that occurs after corner finding. This will combine
	 * any segments that are considered too small to be relevant. This is
	 * helpful if the recognizer uses a corner finder that produces many
	 * false-positives. Default = true
	 */
	public boolean SMALL_POLYLINE_COMBINE = true;

	/**
	 * Post-processing flag that occurs after corner finding. This will combine
	 * any consecutive segments have similar slopes. This is helpful if the
	 * recognizer uses a corner finder that produces many false-positives.
	 * Default = true
	 */
	public boolean SIM_SLOPE_POLYLINE_COMBINE = true;

	/**
	 * Post-processing flag that occurs after corner finding. This will combine
	 * any consecutive segments that (when combined) pass a Paleo line test.
	 * This is helpful if the recognizer uses a corner finder that produces many
	 * false-positives. Default = true
	 */
	public boolean LINE_TEST_COMBINE = true;

	/**
	 * Post-processing flag that occurs after corner finding. Performs a test to
	 * see if substrokes (when combined) form an overtraced line.
	 */
	public boolean OVERTRACED_LINE_COMBINE = true;

	/**
	 * Apply median filtering to the direction graph.
	 */
	public boolean FILTER_DIR_GRAPH = false;

	/**
	 * Flag that turns off a test to help distinguish gull's from M's. Should be
	 * turned on only if a domain requires both a gull shape and an M shape.
	 */
	public boolean M_VS_GULL_CHECK = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof PaleoHeuristics) {
			PaleoHeuristics other = (PaleoHeuristics) o;
			return other.ARC_DOWN == ARC_DOWN
					&& other.FILTER_DIR_GRAPH == FILTER_DIR_GRAPH
					&& other.LINE_TEST_COMBINE == LINE_TEST_COMBINE
					&& other.M_VS_GULL_CHECK == M_VS_GULL_CHECK
					&& other.MULTI_CF == MULTI_CF
					&& other.OVERTRACED_LINE_COMBINE == OVERTRACED_LINE_COMBINE
					&& other.SIM_SLOPE_POLYLINE_COMBINE == SIM_SLOPE_POLYLINE_COMBINE
					&& other.SMALL_POLYLINE_COMBINE == SMALL_POLYLINE_COMBINE
					&& other.SMALL_V == SMALL_V;
		}
		return false;
	}
}
