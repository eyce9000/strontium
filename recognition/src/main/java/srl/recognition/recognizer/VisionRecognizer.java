/**
 * VisionRecognizer.java
 * 
 * Revision History:<br>
 * Nov 18, 2008 jbjohns - File created
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
package srl.recognition.recognizer;

import java.util.List;


import srl.core.sketch.Stroke;
import srl.recognition.IRecognitionResult;


/**
 * A vision recognizer, for our purposes, should be though of as a single-shape
 * template-matching type of algorithm. It's not used to recognize an entire
 * sketch, but just a single shape in the sketch. Take a {@link List} of
 * {@link Stroke} and return a single {@link IRecognitionResult}.
 * 
 * @author jbjohns
 */
public abstract class VisionRecognizer implements
		ITimedRecognizer<List<Stroke>, IRecognitionResult> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.IRecognizer#submitForRecognition(java
	 * .lang.Object)
	 */
	@Override
	public void submitForRecognition(List<Stroke> submission) {
		for (Stroke stroke : submission) {
			submitForRecognition(stroke);
		}
	}

	/**
	 * Helper method for submitting a stroke at a time, rather than an entire
	 * list of strokes. This is useful for recognizers that accrue strokes as
	 * they come in, or for single-stroke vision recognizers.
	 * 
	 * @param submission
	 *            The stroke to submit for recognition
	 */
	public abstract void submitForRecognition(Stroke submission);
}
