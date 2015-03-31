/**
 * IClassifiableConfidenceComparator.java
 * 
 * Revision History:<br>
 * Oct 3, 2008 jbjohns - File created
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

import java.util.Comparator;

import srl.core.sketch.IClassifiable;


/**
 * Compare two IClassifiables based on their confidence values.
 * 
 * @author jbjohns
 */
public class ConfidenceComparator implements Comparator<IClassifiable> {

	/**
	 * Compare two IClassifiables based on their confidence values. NULL values
	 * for confidence are ALWAYS LESS THAN any value. If both IClassifiables
	 * have null confidence, this method deems them equal.
	 * <p>
	 * Otherwise, this method returns < 0 if s1 has lower confidence than s2, ==
	 * 0 if the confidences are equal, or > 1 if s1 has higher confidence than
	 * s2.
	 * 
	 * @param s1
	 *            The first IClassifiable
	 * @param s2
	 *            The second IClassifiable
	 * @return < 0 if s1 confidence is lower, == 0 if confidence is equal, or >
	 *         0 if s1 confidence is higher
	 */
	public int compare(IClassifiable s1, IClassifiable s2) {
		// null references?
		// both null equal
		if (s1 == null && s2 == null) {
			return 0;
		}
		// 1 null < 2 not null
		else if (s1 == null) {
			return -1;
		}
		// 1 not null > 2 null
		else if (s2 == null) {
			return 1;
		}

		// null confidenes?
		if (s1.getInterpretation() == null && s2.getInterpretation() == null) {
			return 0;
		}
		// else one or the other null? NULL is always LESS
		else if (s1.getInterpretation() == null) {
			// s1 less than s2, because s1 is null and not both null
			return -1;
		} else if (s2.getInterpretation() == null) {
			// s2 less than s1 (s1 greater), because s2 null and not both null
			return 1;
		}

		// both not null, compare confidence values
		return Double.compare(s1.getInterpretation().confidence,
				s2.getInterpretation().confidence);
	}

}
