/**
 * Filtering.java
 * 
 * Revision History:<br>
 * Nov 11, 2008 jbjohns - File created
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
 * This class contains various types of filtering methods
 * 
 * @author jbjohns
 */
public class Filtering {

	/**
	 * Perform median filtering on the input using the given window size. Repeat
	 * edge values when the window "falls off the edge" of the input data.
	 * 
	 * @param input
	 *            Data to filter, cannot be null
	 * @param windowSize
	 *            Must be null and in the range of input.length
	 * @return Data filtered using median filtering.
	 */
	public static double[] medianFilter(double[] input, int windowSize) {
		if (input == null) {
			throw new NullPointerException("Input cannot be null");
		}
		if (input.length <= 2)
			return input;
		if (windowSize < 1 || windowSize > input.length || windowSize % 2 == 0) {
			throw new IllegalArgumentException(
					"Window size must be odd and in the range 1 >= windowSize < input.length; window size = "
							+ windowSize + " input length = " + input.length);
		}

		double[] output = new double[input.length];

		// loop over each position in the output to calculate the filtered
		// results
		for (int i = 0; i < output.length; i++) {
			double[] window = getWindow(input, windowSize, i);

			// median
			output[i] = Statistics.median(window);
		}

		return output;
	}

	/**
	 * Perform median filtering on the input using a default window size of the
	 * closest odd to the sqrt of the number of values in the input vector.
	 * 
	 * @param input
	 *            Data to filter, cannot be null
	 * @return Data filtered using median filtering.
	 */
	public static double[] medianFilter(double[] input) {
		return medianFilter(input, getDefaultWindowSize(input));
	}

	/**
	 * Perform average (arithmetic mean) filtering on the input using the given
	 * window size. Repeat edge values when the window "falls off the edge" of
	 * the input data.
	 * 
	 * @param input
	 *            Data to filter, cannot be null
	 * @param windowSize
	 *            Must be null and in the range of input.length
	 * @return Data filtered using mean filtering.
	 */
	public static double[] averageFilter(double[] input, int windowSize) {
		if (input == null) {
			throw new NullPointerException("Input cannot be null");
		}
		if (input.length <= 2)
			return input;
		if (windowSize < 1 || windowSize > input.length || windowSize % 2 == 0) {
			throw new IllegalArgumentException(
					"Window size must be odd and in the range 1 >= windowSize < input.length");
		}

		double[] output = new double[input.length];

		// loop over each position in the output to calculate the filtered
		// results
		for (int i = 0; i < output.length; i++) {
			double[] window = getWindow(input, windowSize, i);

			output[i] = Statistics.mean(window);
		}

		return output;
	}

	/**
	 * Perform average filtering on the input using a default window size of the
	 * closest odd to the sqrt of the number of values in the input vector.
	 * 
	 * @param input
	 *            Data to filter, cannot be null
	 * @return Data filtered using average filtering.
	 */
	public static double[] averageFilter(double[] input) {
		return averageFilter(input, getDefaultWindowSize(input));
	}

	/**
	 * Perform mode filtering on the input using the given window size. Repeat
	 * edge values when the window "falls off the edge" of the input data.
	 * 
	 * @param input
	 *            Data to filter, cannot be null
	 * @param windowSize
	 *            Must be null and in the range of input.length
	 * @return Data filtered using mode filtering.
	 */
	public static double[] modeFilter(double[] input, int windowSize) {
		if (input == null) {
			throw new NullPointerException("Input cannot be null");
		}
		if (input.length <= 2)
			return input;
		if (windowSize < 1 || windowSize > input.length || windowSize % 2 == 0) {
			throw new IllegalArgumentException(
					"Window size must be odd and in the range 1 >= windowSize < input.length");
		}

		double[] output = new double[input.length];

		// loop over each position in the output to calculate the filtered
		// results
		for (int i = 0; i < output.length; i++) {
			double[] window = getWindow(input, windowSize, i);

			output[i] = Statistics.mode(window);
		}

		return output;
	}

	/**
	 * Perform mode filtering on the input using a default window size of the
	 * closest odd to the sqrt of the number of values in the input vector.
	 * 
	 * @param input
	 *            Data to filter, cannot be null
	 * @return Data filtered using mode filtering.
	 */
	public static double[] modeFilter(double[] input) {
		return modeFilter(input, getDefaultWindowSize(input));
	}

	/**
	 * Get the i'th window of the given size (window of given size centered at
	 * data item at index i) from the data.
	 * <p>
	 * If a window edge lies beyond an end of the array, the edge value is
	 * REPEATED to fill the window. For instance, a call to
	 * <code>getWindow([1 2 3 4 5], 3, 0)</code> can't return a straight window
	 * of size 3 because part of the window would have "negative" array index.
	 * So the returned window in this case would be <code>[1 1 2]</code>,
	 * repeating the first value enough times to fill the part of the window
	 * that's out of bounds.
	 * 
	 * 
	 * @param data
	 *            Data to get the window from, not null
	 * @param windowSize
	 *            Size of the window, odd number 1 <= winSize < data.length
	 * @param i
	 *            Center the window at index i in the data, cannot be out of
	 *            bounds
	 * @return The window of given size centered around data[i], edge values
	 *         repeated if needed
	 */
	public static double[] getWindow(double[] data, int windowSize, int i) {
		if (data == null) {
			throw new NullPointerException("Data cannot be null");
		}
		if (windowSize < 1 || windowSize >= data.length || windowSize % 2 == 0) {
			throw new IllegalArgumentException(
					"Window size must be ODD number >=1, < data.length; window size = "
							+ windowSize + " data length = " + data.length);
		}
		if (i < 0 || i > data.length) {
			throw new ArrayIndexOutOfBoundsException(
					"Given value for i is out of bounds");
		}

		// loop over each position in the window and fill it with the
		// correct values
		double[] window = new double[windowSize];

		// how much on either side of i do we want?
		int halfWindow = (int) Math.floor(windowSize / 2.0);

		// we start half window width less than the center
		int start = i - halfWindow;

		// loop over window index
		for (int wIdx = 0; wIdx < window.length; wIdx++) {
			// where in the original input is this portion of the sliding
			// window?
			int dataIdx = start + wIdx;

			// check for out of bounds, and repeat edge values if we're out
			if (dataIdx < 0) {
				window[wIdx] = data[0];
			} else if (dataIdx >= data.length) {
				window[wIdx] = data[data.length - 1];
			}
			// not out of bounds
			else {
				window[wIdx] = data[dataIdx];
			}
		}

		return window;
	}

	/**
	 * Get a default window size of the closest odd number to the sqrt of the
	 * size (number of values) of the input
	 * 
	 * @param input
	 *            input values
	 * @return default window size
	 */
	protected static int getDefaultWindowSize(double[] input) {
		double sqrt = Math.sqrt(input.length);
		int window = (int) Math.round(sqrt);

		// window is even but we need odd window size; round to nearest odd
		if (window % 2 == 0) {
			if (window < sqrt)
				window++;
			else
				window--;
		}

		return window;
	}
}
