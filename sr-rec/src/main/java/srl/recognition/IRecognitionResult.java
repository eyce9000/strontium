/**
 * IRecognitionResult.java
 * 
 * Revision History:<br>
 * Oct 5, 2008 jbjohns - File created
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
package srl.recognition;

import java.util.List;
import java.util.UUID;

import srl.core.sketch.Shape;


/**
 * Interface for Recognition Result classes. This class is a wrapper to hold
 * information about the recognition result of a recognizer when recognizing one
 * shape. It's purpose is to wrap the underlying data structures (like n-best
 * lists) and provide some helpful functionality and insulation for managing
 * recognition results.
 * <p>
 * Recognition results are marked with a unique identifier ({@link UUID}).
 * <p>
 * No shapes in the n-best list are to have null or negative confidence values.
 * If you call {@link #normalizeConfidences()}, any null or negative confidence
 * values will be set to 0.
 * 
 * @author jbjohns
 */
public interface IRecognitionResult {

	/**
	 * Add the given shape to the n-best list of shapes stored in this
	 * recognition result.
	 * 
	 * @param shape
	 *            The shape to add to the list of possibilities stored in this
	 *            recognition result's n-best list.
	 */
	public void addShapeToNBestList(Shape shape);

	/**
	 * Look through the n-best list and find the shape with the highest
	 * confidence value. If there is a tie for the highest confidence value, the
	 * first shape encountered in the n-best list with the highest value will be
	 * returned.
	 * <p>
	 * If the n-best list is empty, returns null.
	 * 
	 * @see ShapeConfidenceComparator
	 * @return The shape with the highest confidence value.
	 */
	public Shape getBestShape();

	/**
	 * Get this recognition result's unique identifier
	 * 
	 * @return The ID of this recognition result
	 */
	public UUID getID();

	/**
	 * Gets a locked {@link Shape} from this n-best list if one exists. If none
	 * exists, then this returns {@code null}. A locked shape has an attribute
	 * string {@code lock} that has a {@code true} value.
	 * <p>
	 * Only one shape should be locked in this IRecognitionResult, although this
	 * is currently not enforced anywhere.
	 * 
	 * @return a locked shape if one exists, otherwise returns {@code null}.
	 */
	public Shape getLockedShape();

	/**
	 * Get the n-best list of shapes that is stored in this recognition result.
	 * This n-best list represents the different possibilities of interpretation
	 * a recognizer finds in single grouping of strokes.
	 * <p>
	 * THIS LIST IS NOT NECESSARILY ORDERED BY CONFIDENCE VALUE.
	 * 
	 * @return The n-best list of recognition interpretations for this grouping
	 *         of strokes
	 */
	public List<Shape> getNBestList();

	/**
	 * Get the number of possible interpretations stored in the n-best list of
	 * this recognition result. If the n-best list is null, returns 0, else
	 * returns the size of the n-best list.
	 * 
	 * @return The number of possible interpretations stored in the n-best list
	 *         of this recognition result
	 */
	public int getNumInterpretations();

	/**
	 * Normalize all the confidence values in the shapes in the n-best list to
	 * sum to 1. If none of the shapes in the n-best list have a confidence
	 * (null), or all the confidences are 0 (if negative confidence, this method
	 * will set the confidence to 0), then no normalization will occur and all
	 * confidences will remain at 0.
	 * <p>
	 * This method sets any null or negative confidence values to 0.
	 */
	public void normalizeConfidences();

	/**
	 * Set the n-best list of shapes for this recognition result.
	 * 
	 * @param nBestList
	 *            The n-best list to use with this recognition result. If given
	 *            a null reference, this n-best list is initialized to an empty
	 *            list.
	 */
	public void setNBestList(List<Shape> nBestList);

	/**
	 * Convenience method to sort the n-best list by DESCENDING confidence
	 * values, so that the first element has the highest confidence
	 */
	public void sortNBestList();

	/**
	 * This method will sort the n-best list using {@link #sortNBestList()} and
	 * then trim the n-best list, if needed, so that no more than n entries are
	 * in the n-best list.
	 * <p>
	 * If {@link #getNumInterpretations()} > n, then after this call
	 * {@link #getNumInterpretations()} will return n, and
	 * {@link #getNBestList()} will contain the n interpretations with the
	 * highest confidence after {@link #sortNBestList()}. Otherwise, if
	 * {@link #getNumInterpretations()} < n, the list is not modified.
	 * 
	 * @param n
	 */
	public void trimToNInterpretations(int n);
}
