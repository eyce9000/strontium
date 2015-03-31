/**
 * UnivariateGaussianDistribution.java
 * 
 * Revision History: <br>
 * Mar 10, 2008 jbjohns -- Original
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple function to compute the PDF of a univariate Gaussian function with a
 * certain mean and standard deviation.
 * 
 * @author jbjohns
 */
public class UnivariateGaussianDistribution {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory
			.getLogger(UnivariateGaussianDistribution.class);

	/**
	 * The mean of this distribution
	 */
	private double m_mean;

	/**
	 * The standard deviation of this distribution
	 */
	private double m_stdDev;

	/**
	 * Create a distribution with a mean of 0 and std dev of 1
	 */
	public UnivariateGaussianDistribution() {
		this(0, 1);
	}

	/**
	 * Create a distribution with the given mean and a std dev of 1
	 * 
	 * @param mean
	 *            The mean for this distribution
	 */
	public UnivariateGaussianDistribution(double mean) {
		this(mean, 1);
	}

	/**
	 * Create a distribution with the given mean and std dev
	 * 
	 * @param mean
	 *            The mean for this distribution
	 * @param stdDev
	 *            The std dev for this distribution
	 */
	public UnivariateGaussianDistribution(double mean, double stdDev) {
		setMean(mean);
		setStdDev(stdDev);
	}

	/**
	 * Get the mean of this distribution
	 * 
	 * @return The mean of this distribution
	 */
	public double getMean() {
		return m_mean;
	}

	/**
	 * Set the mean of this distribution.
	 * 
	 * @param mean
	 *            The new mean of this distribution
	 */
	public void setMean(double mean) {
		this.m_mean = mean;
	}

	/**
	 * Get the std dev of this distribution
	 * 
	 * @return The std dev of this distribution
	 */
	public double getStdDev() {
		return m_stdDev;
	}

	/**
	 * Set the std dev for this distribution
	 * 
	 * @param stdDev
	 *            The new std dev for this distribution, must be > 0
	 * @throws IllegalArgumentException
	 *             if the stddev <= 0
	 */
	public void setStdDev(double stdDev) throws IllegalArgumentException {
		if (stdDev > 0.0) {
			m_stdDev = stdDev;
		} else if (stdDev <= 0.0) {
			throw new IllegalArgumentException("Std dev must be > 0");
		} else {
			throw new IllegalArgumentException("Std dev cannot be equal to "
					+ stdDev);
		}
	}

	/**
	 * Compute the probability density for the point x using this probability
	 * distribution defined by the mean and std dev.
	 * 
	 * @param x
	 *            The point to compute the pdf at
	 * @return The prob density of this distribution, defined by the mean and
	 *         std dev, at the given point
	 */
	public double probabilityDensity(double x) {
		// use the static call so we don't replicate code
		return probabilityDensity(x, m_mean, m_stdDev);
	}

	/**
	 * Compute the probability of the point x wrt the normal probability
	 * distribution defined by the given mean and std dev
	 * 
	 * @param x
	 *            the point to compute the prob. density of
	 * @param mean
	 *            the mean of the probability distribution
	 * @param stdDev
	 *            the std dev of the probability distribution
	 * @return The probability density of x wrt the distribution defined by the
	 *         given mean and std dev
	 */
	public static double probabilityDensity(double x, double mean, double stdDev) {
		log.debug("x = " + x);
		log.debug("mean = " + mean);
		log.debug("stdDev = " + stdDev);

		double twoStdDev = 2.0 * stdDev * stdDev; // 2 * sigma^2
		log.debug("2 * sigma^2 = " + twoStdDev);
		double xmm = x - mean; // x minus mean
		log.debug("x - mean = " + xmm);
		double gaussPdf = (1.0 / Math.sqrt(twoStdDev * Math.PI) * Math.exp(-1.0
				* xmm * xmm / twoStdDev));
		log.debug("gauss pdf = " + gaussPdf);
		return gaussPdf;
	}

	public static double cumulativeProbabilityTail(double x, double mean,
			double stdDev) {
		log.debug("x = " + x);
		log.debug("mean = " + mean);
		log.debug("stdDev = " + stdDev);

		double taylorSeries = 0;
		double factorial = 1;
		double z = x / Math.sqrt(2);
		for (int i = 0; i < 100; i++) {
			if (i > 0)
				factorial *= i;
			taylorSeries += Math.pow(-1, i) * Math.pow(z, i * 2 + 1)
					/ (factorial * (2 * i + 1));
		}
		double erf = 2 * taylorSeries / Math.sqrt(Math.PI);
		if ((1 + erf) / 2 > .5)
			return 1 - (1 + erf) / 2;
		return (1 + erf) / 2;
	}
}
