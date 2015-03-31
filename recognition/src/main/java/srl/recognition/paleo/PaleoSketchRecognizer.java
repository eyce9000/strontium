/**
 * PaleoCombinedRecognizer.java
 * 
 * Revision History:<br>
 * Apr 15, 2009 bpaulson - File created
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



import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.paleoNN.PaleoNNRecognizer;

/**
 * Combines original Paleo with PaleoNN
 * 
 * @author bpaulson
 */
public class PaleoSketchRecognizer {

	/**
	 * Original PaleoSketch
	 */
	private OrigPaleoSketchRecognizer m_paleo;

	/**
	 * PaleoSketch neural network version
	 */
	private PaleoNNRecognizer m_paleoNN;

	/**
	 * Paleo config
	 */
	private PaleoConfig m_config;

	/**
	 * Threshold for pruning poor confidence shapes
	 */
	public static final double LOW_CONFIDENCE = 0.001;

	/**
	 * Neural network is only confident when confidence is above 98%
	 */
	public static final double NN_NOT_CONFIDENT = 0.98;

	/**
	 * Constructor
	 * 
	 * @param config
	 *            paleo config
	 */
	public PaleoSketchRecognizer(PaleoConfig config) {
		m_config = config;
		m_paleo = new OrigPaleoSketchRecognizer(m_config);
		if(m_config.getNNEnabled()){
			m_paleoNN = new PaleoNNRecognizer(m_config);
		}
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	public IRecognitionResult recognize(Stroke stroke) {
		m_paleo.setStroke(stroke);
		IRecognitionResult orig = m_paleo.recognize();
		IRecognitionResult nn = null;
		if(m_config.getNNEnabled()){
			m_paleoNN.submitForRecognition(stroke);
			m_paleoNN.setFeatures(m_paleo.getFeatures());
			nn = m_paleoNN.recognize();
		}

		
		// if nn is empty then it failed, return original paleo
		if (nn == null || nn.getNBestList().size() < 1) {
			return orig;
		}

		// combine the average confidences of both recognizers
		for (Shape s : nn.getNBestList()) {
			for (Shape ss : orig.getNBestList()) {
				if (s.getInterpretation().label
						.equals(ss.getInterpretation().label)) {
					s.getInterpretation().confidence = ((s.getInterpretation().confidence + ss
							.getInterpretation().confidence) / 2.0);
				}
			}
		}

		return pruneBad(nn);
	}

	/**
	 * Prune the bad results from the recognition result
	 * 
	 * @param r
	 *            result to prune
	 * @return pruned result
	 */
	private IRecognitionResult pruneBad(IRecognitionResult r) {
		if (r != null) {
			for (int i = r.getNBestList().size() - 1; i >= 0; i--) {
				if (r.getNBestList().get(i).getInterpretation().confidence < LOW_CONFIDENCE
						&& r.getNBestList().size() > 1)
					r.getNBestList().remove(i);
			}
			r.sortNBestList();
		}
		return r;
	}

}
