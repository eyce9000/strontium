/**
 * ISegmentation.java
 * 
 * Revision History: <br>
 * (5/23/08) bpaulson - interface created
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
package srl.core.sketch;

import java.util.List;
import java.util.UUID;


/**
 * Interface for a segmentation interpretation (from a corner finder). ISS is a
 * segmentable stroke (ISegmentableStroke).
 * 
 * @author bpaulson
 */
public interface ISegmentation extends Cloneable {

	/**
	 * Returns a list of sub-strokes (ISegmentableStrokes) from the segmentation
	 * 
	 * @return list of sub-strokes (ISegmentableStrokes) from the segmentation
	 */
	public List<IStroke> getSegmentedStrokes();

	/**
	 * Get the ID of the ISegmentation
	 * 
	 * @return UUID of the ISegmentation
	 */
	public UUID getID();

	/**
	 * Sets the list of segmented/sub-strokes
	 * 
	 * @param segmentedStrokes
	 *            list of sub-strokes
	 */
	public void setSegmentedStrokes(List<IStroke> segmentedStrokes);

	/**
	 * Clone an ISegmentation
	 * 
	 * @return A new, deep copied ISegmentation
	 */
	public Object clone();
}
