/**
 * IShortStrawThresholds.java
 * 
 * Revision History:<br>
 * July 21, 2008 awolin - File created
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
package srl.segmentation.shortstraw;

/**
 * Interface containing various thresholds used throughout the ShortStraw stroke
 * segmenter
 * 
 * @author awolin
 */
public interface IShortStrawThresholds {

	/**
	 * Radial window of points to either side of a point to examine. Straws are
	 * of length (2 * S_WINDOW) + 1, multiplied by the resample spacing
	 */
	public static final int S_WINDOW = 3; // 2;

	/**
	 * Percentage below the median for which straws are considered to be
	 * relevant
	 */
	public static final double S_MEDIAN_PERCENTAGE = 0.95; // 0.95; // 0.89; //
															// 0.95;

	/**
	 * The percentage of straight line / path distance that a line test must
	 * pass
	 */
	public static final double S_LINE_VS_ARC_THRESHOLD = 0.95; // 0.92; // 0.95

	/**
	 * How many points should be on a diagonal line
	 */
	public static final double S_PTS_PER_DIAGONAL = 80.0;

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
	 * The shortest resample spacing we allow in a stroke
	 */
	public static final double S_SMALL_RESAMPLE_SPACING_THRESHOLD = 0.1;

	/**
	 * Minimum number of points we need
	 */
	public static final int S_MIN_PTS_THRESHOLD = S_WINDOW * 3;
}
