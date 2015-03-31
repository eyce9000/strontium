/**
 * Sigmoid.java
 * 
 * Revision History:<br>
 * Dec 7, 2008 Joshua - File created
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

/**
 * This class provides a simple method to compute the sigmoid function on a
 * variety of inputs.
 * 
 * @author Joshua
 */
public class Sigmoid {

	/**
	 * Compute the sigmoid function on the input x.
	 * 
	 * @param x
	 *            The input
	 * @return Sigmoid evaluated at x
	 */
	public static double sigmoid(double x) {
		return 1 / (1 + Math.exp(-1 * x));
	}

	/**
	 * Compute the sigmoid function for each input in the array x. The output
	 * will be an array of the same length, with y[i] == sigmoid(x[i])
	 * 
	 * @param x
	 *            Input array of length n, not null
	 * @return Output array of length n, which each y[i] == sigmoid(x[i])
	 */
	public static double[] sigmoid(double[] x) {
		if (x != null) {
			double[] y = new double[x.length];

			for (int i = 0; i < y.length; i++) {
				y[i] = sigmoid(x[i]);
			}

			return y;
		} else {
			throw new IllegalArgumentException("x cannot be null");
		}
	}
}
