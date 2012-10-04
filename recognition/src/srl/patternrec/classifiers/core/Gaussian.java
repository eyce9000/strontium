/**
 * Gaussian.java
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
package srl.patternrec.classifiers.core;

import srl.patternrec.classifiers.core.helper.MatrixHelper;

import Jama.Matrix;

/**
 * Class used to model a Gaussian distribution
 * 
 * @author bpaulson
 */
public class Gaussian {

	/**
	 * Mean vector
	 */
	protected Matrix m_mean;

	/**
	 * Covariance matrix
	 */
	protected Matrix m_cov;

	/**
	 * Inverse of the covariance matrix
	 */
	protected Matrix m_invCov;

	/**
	 * Create a multivariate Gaussian distribution with the given mean and
	 * covariance
	 * 
	 * @param meanVector
	 *            D-dimensional mean vector (1xD)
	 * @param covMatrix
	 *            D-dimensional covariance matrix (DxD)
	 */
	public Gaussian(Matrix meanVector, Matrix covMatrix) {
		m_mean = meanVector;
		m_cov = covMatrix;
		computeInvCov();
	}

	/**
	 * Get the mean vector
	 * 
	 * @return mean vector
	 */
	public Matrix getMean() {
		return m_mean;
	}

	/**
	 * Set the mean vector
	 * 
	 * @param mean
	 *            mean vector
	 */
	public void setMean(Matrix mean) {
		m_mean = mean;
	}

	/**
	 * Get the covariance matrix
	 * 
	 * @return covariance matrix
	 */
	public Matrix getCov() {
		return m_cov;
	}

	/**
	 * Set the covariance matrix
	 * 
	 * @param cov
	 *            covariance matrix
	 */
	public void setCov(Matrix cov) {
		m_cov = cov;
		computeInvCov();
	}

	/**
	 * Determine the maximum likelihood estimate of the input vector
	 * 
	 * @param inputVector
	 *            input vector
	 * @return MLE estimate
	 */
	public double MaximumLikelihoodEstimate(Matrix inputVector) {
		double D = m_mean.getColumnDimension();
		double p1 = 1.0 / (Math.pow(2 * Math.PI, D / 2.0));
		double p2 = 1.0 / Math.sqrt(m_cov.det());
		Matrix trans = inputVector.minus(m_mean);
		trans.transpose();
		Matrix sub = m_invCov.times(trans);
		sub = (inputVector.minus(m_mean)).times(sub);
		double p3 = Math.exp(-0.5 * sub.get(0, 0));
		return p1 * p2 * p3;
	}

	/**
	 * Computes the inverse covariance matrix (regularizes as needed)
	 */
	private void computeInvCov() {
		try {
			m_invCov = m_cov.inverse();
		} catch (Exception e) {
			Matrix reg = MatrixHelper.regularizeRidgeRegression(m_cov, 0.0001);
			try {
				m_invCov = reg.inverse();
			} catch (Exception e1) {
				reg = MatrixHelper.regularize(reg);
				m_invCov = reg.inverse();

			}
		}
	}
}
