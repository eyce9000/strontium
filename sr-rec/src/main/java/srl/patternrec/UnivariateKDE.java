/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2012 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&M University 
 *        nor the names of its contributors may be used to endorse or promote 
 *        products derived from this software without specific prior written 
 *        permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *  
 *******************************************************************************/
package srl.patternrec;

import java.util.ArrayList;
import java.util.List;

/**
 * Univariate KDE implementation using Gaussian kernels
 * 
 * @author awolin
 */
public class UnivariateKDE {

	/**
	 * Default spacing for the P_KDE data values
	 */
	private static final double S_DEFAULT_Y_SPACING = 0.25;

	/**
	 * Default constructor
	 */
	public UnivariateKDE() {
		// Do nothing
	}

	/**
	 * Compute the univariate KDE from the data X and a kernel bandwidth h. If
	 * the P_KDE range, Y, is null, then this function automatically finds the
	 * KDE window. Y is updated with this window if Y is initially an empty
	 * list. Therefore, passing an empty list is highly preferred over a null
	 * object.
	 * 
	 * @param X
	 *            Data
	 * @param h
	 *            Kernel bandwidth
	 * @param Y
	 *            Window of the P_KDE
	 * @return A P_KDE spread over a window of values
	 */
	public List<Double> compute(List<Double> X, double h, List<Double> Y) {

		// If we do not have an array of values to calculate pKDEs for, create
		// one
		if (Y == null || Y.size() == 0) {
			// Get the minimum and maximum values
			double minX = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			for (int i = 0; i < X.size(); i++) {
				if (X.get(i) < minX) {
					minX = X.get(i);
				}

				if (X.get(i) > maxX) {
					maxX = X.get(i);
				}
			}

			// Initialize an array of values that are sampled at a default
			// interval
			double range = maxX - minX;
			if (Y == null)
				Y = new ArrayList<Double>();

			double startValue = minX - (0.10 * range);
			double endValue = maxX + (0.10 * range);
			Y.add(startValue);

			double value = startValue;
			while (value < endValue) {
				value = Y.get(Y.size() - 1) + S_DEFAULT_Y_SPACING;
				Y.add(value);
			}
		}

		// Normalization factor
		double normalize = 1 / (X.size() * h);
		List<Double> P = new ArrayList<Double>();

		// Loop through the Y values
		for (int y = 0; y < Y.size(); y++) {

			double sum = 0.0;
			for (int x = 0; x < X.size(); x++) {
				sum += kernel((Y.get(y) - X.get(x)) / h);
			}

			P.add(normalize * sum);
		}

		return P;
	}

	/**
	 * Univariate Gaussian Kernel
	 * 
	 * @param x
	 *            Scalar data value
	 * @return Kernel for the value
	 */
	private double kernel(double x) {
		// Normalization factor
		double normalize = 1 / Math.sqrt(2 * Math.PI);

		// Kernel value
		double k = normalize * Math.exp(-0.5 * (x * x));

		return k;
	}
}
