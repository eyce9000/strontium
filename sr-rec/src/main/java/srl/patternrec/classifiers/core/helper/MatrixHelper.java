/**
 * MatrixHelper.java
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

/**
 * Helper functions for a matrix that aren't provided by Jama
 * 
 * @author bpaulson
 */
public class MatrixHelper {

	/**
	 * Get the mean of a given row of a given matrix
	 * 
	 * @param m
	 *            matrix
	 * @param row
	 *            row number
	 * @return mean of the given row
	 */
	public static double getRowMean(Matrix m, int row) {
		double sum = 0;
		double num = 0;
		for (int i = 0; i < m.getColumnDimension(); i++) {
			sum += m.get(row, i);
			num++;
		}
		return sum / num;
	}

	/**
	 * Get the mean of a given column of a given matrix
	 * 
	 * @param m
	 *            matrix
	 * @param col
	 *            column number
	 * @return mean of the given column
	 */
	public static double getColMean(Matrix m, int col) {
		double sum = 0;
		double num = 0;
		for (int i = 0; i < m.getRowDimension(); i++) {
			if (!Double.isNaN(m.get(i, col))
					&& !Double.isInfinite(m.get(i, col))) {
				sum += m.get(i, col);
				num++;
			}
		}
		return sum / num;
	}

	/**
	 * Get the maximum value within a column of a given matrix
	 * 
	 * @param m
	 *            matrix
	 * @param col
	 *            column number
	 * @return maximum value in the column
	 */
	public static double getColMax(Matrix m, int col) {
		double max = Double.MIN_VALUE;
		for (int i = 0; i < m.getRowDimension(); i++) {
			if (!Double.isNaN(m.get(i, col))
					&& !Double.isInfinite(m.get(i, col)) && m.get(i, col) > max)
				max = m.get(i, col);
		}
		return max;
	}

	/**
	 * Get the minimum value within a column of a given matrix
	 * 
	 * @param m
	 *            matrix
	 * @param col
	 *            column number
	 * @return minimum value in the column
	 */
	public static double getColMin(Matrix m, int col) {
		double min = Double.MAX_VALUE;
		for (int i = 0; i < m.getRowDimension(); i++) {
			if (!Double.isNaN(m.get(i, col))
					&& !Double.isInfinite(m.get(i, col)) && m.get(i, col) < min)
				min = m.get(i, col);
		}
		return min;
	}

	/**
	 * Calculate the covariance matrix
	 * 
	 * @param m
	 *            matrix to calculate the covariance of
	 * @return covariance matrix
	 */
	public static Matrix cov(Matrix m) {
		Matrix cov = new Matrix(m.getColumnDimension(), m.getColumnDimension());
		for (int e = 0; e < m.getRowDimension(); e++) {
			for (int i = 0; i < m.getColumnDimension(); i++) {
				double fcei;
				fcei = m.get(e, i);
				if (Double.isNaN(fcei)) {
					fcei = getColMean(m, i);
				} else if (Double.isInfinite(fcei) || fcei == Double.MAX_VALUE) {
					fcei = getColMax(m, i);
				} else if (fcei == Double.MIN_VALUE) {
					fcei = getColMin(m, i);
				}
				for (int j = 0; j < m.getColumnDimension(); j++) {
					double fcej;
					fcej = m.get(e, j);
					if (Double.isNaN(fcej)) {
						fcej = getColMean(m, j);
					} else if (Double.isInfinite(fcej)
							|| fcei == Double.MAX_VALUE) {
						fcej = getColMax(m, j);
					} else if (fcei == Double.MIN_VALUE) {
						fcej = getColMin(m, j);
					}
					cov.set(i, j, cov.get(i, j) + (fcei - getColMean(m, i))
							* (fcej - getColMean(m, j)));
				}
			}
		}
		return cov;
	}

	/**
	 * Method used to add a row to a matrix
	 * 
	 * @param m
	 *            original matrix
	 * @param rVec
	 *            row vector to add to the end of m
	 */
	public static void addRow(Matrix m, Matrix rVec) {
		// bad row vector so return original matrix
		if (rVec.getColumnDimension() != m.getColumnDimension()
				|| rVec.getRowDimension() != 1)
			return;

		Matrix newM = new Matrix(m.getRowDimension() + 1,
				m.getColumnDimension());
		for (int r = 0; r < m.getRowDimension(); r++)
			for (int c = 0; c < m.getColumnDimension(); c++)
				newM.set(r, c, m.get(r, c));
		for (int c = 0; c < m.getColumnDimension(); c++)
			newM.set(m.getRowDimension(), c, rVec.get(0, c));
		m = newM;
	}

	/**
	 * Method used to add a column to a matrix
	 * 
	 * @param m
	 *            original matrix
	 * @param cVec
	 *            column vector to add to the end of m
	 */
	public static void addCol(Matrix m, Matrix cVec) {
		// bad column vector so return original matrix
		if (cVec.getRowDimension() != m.getRowDimension()
				|| cVec.getColumnDimension() != 1)
			return;

		Matrix newM = new Matrix(m.getRowDimension(),
				m.getColumnDimension() + 1);
		for (int r = 0; r < m.getRowDimension(); r++)
			for (int c = 0; c < m.getColumnDimension(); c++)
				newM.set(r, c, m.get(r, c));
		for (int r = 0; r < m.getRowDimension(); r++)
			newM.set(r, m.getColumnDimension(), cVec.get(r, 0));
		m = newM;
	}

	/**
	 * Set a particular row of a matrix
	 * 
	 * @param m
	 *            original matrix
	 * @param row
	 *            values of row
	 * @param rowNum
	 *            row number to set
	 */
	public static void setRow(Matrix m, Matrix row, int rowNum) {
		if (rowNum > m.getRowDimension()
				|| row.getColumnDimension() != m.getColumnDimension()
				|| row.getRowDimension() != 1)
			return;

		for (int c = 0; c < m.getColumnDimension(); c++)
			m.set(rowNum, c, row.get(0, c));
	}

	/**
	 * Set a particular column of a matrix
	 * 
	 * @param m
	 *            original matrix
	 * @param col
	 *            values of column
	 * @param colNum
	 *            column number to set
	 */
	public static void setCol(Matrix m, Matrix col, int colNum) {
		if (colNum > m.getColumnDimension()
				|| col.getRowDimension() != m.getRowDimension()
				|| col.getColumnDimension() != 1)
			return;

		for (int r = 0; r < m.getRowDimension(); r++)
			m.set(r, colNum, col.get(r, 0));
	}

	/**
	 * Performs simple regularization by adding a small value to the diagonal of
	 * a matrix; matrix must be square
	 * 
	 * @param m
	 *            matrix to regularize
	 * @param val
	 *            value to add to diagonal
	 * @return regularized version of matrix
	 */
	public static Matrix regularize(Matrix m, double val) {
		if (m.getColumnDimension() != m.getRowDimension())
			return m;
		Matrix regM = new Matrix(m.getRowDimension(), m.getColumnDimension());
		for (int i = 0; i < m.getRowDimension(); i++)
			for (int j = 0; j < m.getColumnDimension(); j++)
				regM.set(i, j, m.get(i, j));
		for (int i = 0; i < regM.getRowDimension(); i++)
			regM.set(i, i, regM.get(i, i) + val);
		return regM;
	}

	/**
	 * Performs simple regularization by adding a small value (equals to 1/1000
	 * of the minimum value in the matrix) to the diagonal of the matrix
	 * 
	 * @param m
	 *            square matrix to regularize
	 * @return regularized matrix
	 */
	public static Matrix regularize(Matrix m) {
		double val = absMin(m) * 0.001;
		if (m.getColumnDimension() != m.getRowDimension())
			return m;
		Matrix regM = new Matrix(m.getRowDimension(), m.getColumnDimension());
		for (int i = 0; i < m.getRowDimension(); i++)
			for (int j = 0; j < m.getColumnDimension(); j++)
				regM.set(i, j, m.get(i, j));
		for (int i = 0; i < regM.getRowDimension(); i++)
			regM.set(i, i, regM.get(i, i) + val);
		return regM;
	}

	/**
	 * Regularize the matrix using ridge regression
	 * 
	 * @param m
	 *            matrix to regularize
	 * @param regParam
	 *            regularization parameter epsilon (0<epsilon<1)
	 * @return regularized matrix
	 */
	public static Matrix regularizeRidgeRegression(Matrix m, double regParam) {
		if (m.getColumnDimension() != m.getRowDimension())
			return m;
		Matrix regM = new Matrix(m.getRowDimension(), m.getColumnDimension());
		Matrix trans = (Matrix) m.clone();
		trans.transpose();
		Matrix tran = trans.times(m);
		Matrix I = Matrix.identity(m.getRowDimension(), m.getColumnDimension())
				.times(1.0 / m.getRowDimension());
		regM = (((tran.times(1 - regParam)).plus(
				(I.times(tran.trace())).inverse()).times(trans).times(regParam)));
		return regM;
	}

	/**
	 * Gets a sub-matrix within the given matrix
	 * 
	 * @param m
	 *            matrix to get a sub-matrix from
	 * @param rows
	 *            row numbers to get
	 * @param cols
	 *            column numbers to get
	 * @return sub-matrix containing only the specified rows and columns
	 */
	public static Matrix subMatrix(Matrix m, List<Integer> rows,
			List<Integer> cols) {
		Matrix newM = new Matrix(rows.size(), cols.size());
		for (int r = 0; r < rows.size(); r++)
			for (int c = 0; c < cols.size(); c++)
				newM.set(r, c, m.get(rows.get(r), cols.get(c)));
		return newM;
	}

	/**
	 * Calculate the pseduo-inverse of a matrix
	 * 
	 * @param m
	 *            matrix to calculate pseudo-inverse for
	 * @return pseudo inverse of input matrix
	 */
	public static Matrix pInv(Matrix m) {
		Matrix mt = (Matrix) m.clone();
		mt.transpose();
		return (mt.times(m)).inverse().times(mt);
	}

	/**
	 * Calculate and return the absolute minimum value in the matrix
	 * 
	 * @param m
	 *            matrix to search
	 * @return minimum (non-zero) absolute value in the matrix
	 */
	private static double absMin(Matrix m) {
		double min = Double.MAX_VALUE;
		for (int r = 0; r < m.getRowDimension(); r++)
			for (int c = 0; c < m.getColumnDimension(); c++)
				if (Math.abs(m.get(r, c)) < min && m.get(r, c) != 0)
					min = Math.abs(m.get(r, c));
		return min;
	}

	/**
	 * Function used to write a matrix out to file (in csv form)
	 * 
	 * @param m
	 *            matrix to write
	 * @param file
	 *            path of file to write to
	 * @param append
	 *            flag denoting if matrix should be appended to file
	 */
	public static void writeMatrixToFile(Matrix m, String file, boolean append)
			throws IOException {
		BufferedWriter tw = new BufferedWriter(new FileWriter(file, append));
		tw.write("[");
		tw.newLine();
		for (int i = 0; i < m.getRowDimension(); i++) {
			for (int j = 0; j < m.getColumnDimension(); j++) {
				tw.write(Double.toString(m.get(i, j)));
				tw.write(',');
			}
			tw.newLine();
		}
		tw.write("]");
		tw.newLine();
		tw.close();
	}

	/**
	 * Function used to read matrices from a file (in csv form)
	 * 
	 * @param file
	 *            path of file to read from
	 * @return list of matrices read
	 * @throws IOException
	 */
	public static List<Matrix> readMatricesFromFile(String file)
			throws IOException {
		List<Matrix> matList = new ArrayList<Matrix>();
		BufferedReader rdr = new BufferedReader(new FileReader(file));
		List<List<Double>> mat = new ArrayList<List<Double>>();
		String line;
		while ((line = rdr.readLine()) != null) {

			// new matrix
			if (line.startsWith("]")) {
				if (mat.size() > 0) {
					matList.add(toMatrix(mat));
				}
				mat.clear();
			} else if (!line.startsWith("[")) {

				// parse line
				List<Double> lineVals = new ArrayList<Double>();
				while (line.contains(",")) {
					String num = line.trim().substring(0, line.indexOf(','));
					lineVals.add(Double.parseDouble(num));
					line = line.substring(line.indexOf(',') + 1);
				}

				// parse last value
				if (line.compareToIgnoreCase("") != 0)
					lineVals.add(Double.parseDouble(line));

				mat.add(lineVals);
			}
		}

		// add last matrix
		if (mat.size() > 0) {
			matList.add(toMatrix(mat));
		}

		return matList;
	}

	/**
	 * Convert a list of a list of doubles into an actual matrix object
	 * 
	 * @param matrix
	 *            list of a list of doubles to convert
	 * @return matrix object
	 */
	public static Matrix toMatrix(List<List<Double>> matrix) {
		if (matrix.size() < 1)
			return new Matrix(0, 0);
		if (matrix.get(0).size() < 1)
			return new Matrix(1, 0);
		int numRows = matrix.size();
		int numCols = matrix.get(0).size();
		for (int i = 0; i < matrix.size(); i++) {
			if (matrix.get(i).size() > numCols)
				numCols = matrix.get(i).size();
		}
		Matrix m = new Matrix(numRows, numCols);
		for (int i = 0; i < matrix.size(); i++) {
			for (int j = 0; j < matrix.get(i).size(); j++) {
				m.set(i, j, matrix.get(i).get(j));
			}
		}
		return m;
	}

	/**
	 * Prints matrix to console
	 * 
	 * @param m
	 *            matrix to print
	 */
	public static void printMatrix(Matrix m) {
		System.out.println('[');
		for (int r = 0; r < m.getRowDimension(); r++) {
			for (int c = 0; c < m.getColumnDimension() - 1; c++) {
				System.out.print(m.get(r, c) + ",");
			}
			System.out.println(m.get(r, m.getColumnDimension() - 1));
		}
		System.out.println(']');
	}
}
