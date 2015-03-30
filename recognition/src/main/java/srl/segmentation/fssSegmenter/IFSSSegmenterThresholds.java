/**
 * IMergeCFThresholds.java
 * 
 * Revision History:<br>
 * August 27, 2008 awolin - File created
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
package srl.segmentation.fssSegmenter;

/**
 * Interface containing various thresholds used throughout the MergeCF stroke
 * segmenter. CURRENTLY UNUSED (My Sezgin thresholds are the same)
 * 
 * @author awolin
 */
@Deprecated
public interface IFSSSegmenterThresholds {

	/**
	 * The percentage of the average curvature a point needs to be above to be
	 * considered a corner
	 */
	public static final double S_AVG_CURVATURE_THRESHOLD = 1.00;

	/**
	 * The percentage of the average speed a point needs to be below to be
	 * considered a corner
	 */
	public static final double S_AVG_SPEED_THRESHOLD = 0.90;

	/**
	 * The percentage of the average speed a point needs to be below to be
	 * considered a corner
	 */
	public static final double S_STRICT_AVG_SPEED_THRESHOLD = 0.75;

	/**
	 * Percentage of stroke bounding box diagonal length from the start point we
	 * should ignore corners
	 */
	public static final double S_PERC_HOOK_THRESHOLD = 0.1;

	/**
	 * Maximum pixel length from the start point we should ignore corners
	 */
	public static final double S_MAX_HOOK_THRESHOLD = 15.0;

	/**
	 * How many pixels away from another corner do we allow
	 */
	public static final double S_CLOSE_POINT_DIST_THRESHOLD = 0.025;

	/**
	 * How many indices away from another corner do we require
	 */
	public static final int S_CLOSE_POINT_INDICES_THRESHOLD = 2;

	/**
	 * The percentage of straight line / path distance that a line test must
	 * pass
	 */
	public static final double S_LINE_VS_ARC_THRESHOLD = 0.92;
}
