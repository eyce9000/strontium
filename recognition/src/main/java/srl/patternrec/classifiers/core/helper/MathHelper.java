/**
 * MathHelper.java
 * 
 * Revision History:<br>
 * Jan 14, 2009 bpaulson - File created
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
package srl.patternrec.classifiers.core.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class that provides miscellaneous math functions
 * 
 * @author bpaulson
 */
public class MathHelper {

	/**
	 * Get a random list of permuted integers
	 * 
	 * @param size
	 *            size of permutation list to generate
	 * @return list of permuted integers
	 */
	public static List<Integer> randomPerm(int size) {
		List<Integer> perm = new ArrayList<Integer>();
		for (int i = 0; i < size; i++)
			perm.add(i);
		Random rand = new Random();
		for (int i = perm.size() - 1; i > 0; i--) {
			int swapIndex = rand.nextInt(i + 1);
			if (swapIndex != i) {
				int tmp = perm.get(swapIndex);
				perm.set(swapIndex, perm.get(i));
				perm.set(i, tmp);
			}
		}
		return perm;
	}
}
