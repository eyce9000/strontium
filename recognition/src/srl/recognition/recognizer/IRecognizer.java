/**
 * IRecognizer.java
 * 
 * Revision History:<br>
 * Jun 6, 2008 jbjohns - File created <br>
 * Jun 25, 2008 bpaulson - Removed parameters from recognize()
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
package srl.recognition.recognizer;

import java.util.List;


import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.recognition.IRecognitionResult;


;

/**
 * A basic interface for a shape recognizer in the LADDER system. The interface
 * is made generic so that you can implement a recognizer that takes any type of
 * object as input and gives any type of object as output, depending on the use
 * of the recognizer.
 * <p>
 * A low-level, primitive recognizer might take in {@link Stroke} and return
 * either {@link Shape} or {@link IRecognitionResult} (for multiple
 * interpretations of the same stroke).
 * <p>
 * A vision algorithm might take in an {@link Stroke} at a time, or possibly a
 * {@link List} of {@link Stroke}, and return an {@link IRecognitionResult}.
 * <p>
 * A high-level recognizer, like a constraint-based language, might take
 * {@link Shape} and return either {@link IRecognitionResult} or a {@link List}
 * of {@link IRecognitionResult}
 * <p>
 * It is recommended that you implement your {@link IRecognizer} to run
 * single-threaded. This way, users can wrap your recognizer in a threading
 * system if they want, but aren't forced to use threading. However, it is
 * essential that all recognizers are thread safe.
 * 
 * @author Joshua Johnston
 * @param <Rec>
 *            The type of object that is RECognized.
 * @param <Res>
 *            The type of object that is returned as a RESult of recognition
 */
public interface IRecognizer<Rec, Res> {

	/**
	 * Submit the given object for recognition. This method should block.
	 * 
	 * @param submission
	 *            The object you want to submit for recognition.
	 */
	public void submitForRecognition(Rec submission);

	/**
	 * Recognize all the objects that have been submitted for recognition. Block
	 * until recognition is complete and then return the results of recognition.
	 * 
	 * @return A list of results recognized from the input to the recognizer
	 */
	public Res recognize();
}
