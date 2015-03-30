/**
 * Statistics.java
 * 
 * Revision History:<br>
 * Nov 12, 2008 jbjohns - File created
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
package srl.math;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Compute statistics on arrays of doubles
 * 
 * @author jbjohns
 */
public class Statistics {

	/**
	 * Compute the arithmetic mean (average) of the values in x
	 * <p>
	 * O(n)
	 * 
	 * @param x
	 *            Array of values x
	 * @return The mean of the values in x
	 */
	public static double mean(double[] x) {
		double sum = 0;
		for (int i = 0; i < x.length; i++) {
			sum += x[i];
		}
		return sum / (double) x.length;
	}

	/**
	 * Compute the median (number that's > 50% of the sorted values of x) of the
	 * values in x
	 * <p>
	 * O(n*log(n))
	 * 
	 * @param x
	 *            Array of values
	 * @return The median of the values in x
	 */
	public static double median(double[] x) {
		double[] temp = x;
		Arrays.sort(temp); // n * log(n)
		return temp[temp.length / 2];
	}

	/**
	 * Compute the mode (number that appears most often) of the values in x. If
	 * there is a tie, the SMALLEST value is returned.
	 * <p>
	 * O(n)
	 * 
	 * @param x
	 *            Array of values
	 * @return The mode of the values in x
	 */
	public static double mode(double[] x) {
		Map<Double, Integer> counts = new HashMap<Double, Integer>(x.length);

		double maxCount = 0;
		double mode = x[0];

		for (int i = 0; i < x.length; i++) {
			// count how many times each number appears
			Double d = new Double(x[i]);
			Integer count = counts.get(d);
			if (count == null) {
				count = new Integer(0);
			}
			count = new Integer(count.intValue() + 1);
			counts.put(d, count);

			if (count.intValue() > maxCount) {
				// mode is the one with the max count
				maxCount = count.intValue();
				mode = d.doubleValue();
			} else if (count.intValue() == maxCount) {
				// tied, so mode is smallest
				mode = Math.min(mode, d.doubleValue());
			}
		}

		return mode;
	}
}
