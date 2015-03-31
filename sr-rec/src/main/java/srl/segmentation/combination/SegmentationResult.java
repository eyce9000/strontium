/**
 * SegmentationResult.java
 * 
 * Revision History:<br>
 * Oct 17, 2008 bpaulson - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
package srl.segmentation.combination;

import srl.core.sketch.Segmentation;

/**
 * Container that stores a segmentation, along with its error
 * 
 * @author bpaulson
 */
public class SegmentationResult {

	/**
	 * Error for segmentation
	 */
	private double m_error;

	/**
	 * Segmentation
	 */
	private Segmentation m_segmentation;

	/**
	 * Name of segmenter that produced segmentation
	 */
	private String m_name;

	/**
	 * Constructor for segmentation result
	 * 
	 * @param segmentation
	 *            segmentation
	 * @param error
	 *            error rate
	 * @param name
	 *            name of segmenter that produced segmentation
	 */
	public SegmentationResult(Segmentation segmentation, double error,
			String name) {
		m_segmentation = segmentation;
		m_error = error;
		m_name = name;
	}

	/**
	 * Get the error of the segmentation
	 * 
	 * @return error
	 */
	public double getError() {
		return m_error;
	}

	/**
	 * Get the segmentation
	 * 
	 * @return segmentation
	 */
	public Segmentation getSegmentation() {
		return m_segmentation;
	}

	/**
	 * Get the name of the segmenter that produced the segmentation
	 * 
	 * @return name of the segmenter that produced the segmentation
	 */
	public String getName() {
		return m_name;
	}
}
