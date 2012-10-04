/*
 * $Id: FittingUtil.java,v 1.8 2003/11/21 00:24:47 hwawen Exp $
 *
 * Copyright (c) 2003-2004 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.fragmentation;

import org.hhreco.recognition.TimedStroke;

/**
 * This class contains routines used during fragmentation and fitting.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class FittingUtil {
	/**
	 * Return a new instance of TimedStroke in the reverse direction of 's'.
	 */
	private static TimedStroke reverse(TimedStroke s) {
		int num = s.getVertexCount();
		TimedStroke reversed = new TimedStroke(num);
		for (int i = num - 1; i >= 0; i--) {
			reversed.addVertex((float) s.getX(i), (float) s.getY(i),
					s.getTimestamp(num - i - 1));
		}
		return reversed;
	}

	/**
	 * Find the eigenvalues for 2x2 matrix m. indices starts at 1, similar to
	 * matlab format.
	 */
	public static void eigen(double m[][], double eigenvalues[],
			double eigenvectors[][]) {
		double a, b, c, d, v, lambda1, lambda2;
		a = m[1][1];// [row][col]
		b = m[1][2];
		c = m[2][1];
		d = m[2][2];
		v = a * a + d * d - 2 * a * d + 4 * b * c;
		lambda1 = (a + d + Math.sqrt(v)) / 2;
		lambda2 = (a + d - Math.sqrt(v)) / 2;
		eigenvalues[1] = lambda1;
		eigenvalues[2] = lambda2;
		double e, f, v1, v2;
		e = lambda1 - a;
		f = -b;
		if (e == 0) {
			if (c != 0) {
				// System.out.println("A");
				v1 = (lambda1 - d) / c;
				v2 = 1;
				double norm = Math.sqrt(v1 * v1 + v2 * v2);
				eigenvectors[1][1] = v1 / norm;
				eigenvectors[2][1] = v2 / norm;
			} else {// [0 -b; 0 lambda1-d]
					// System.out.println("B");
				eigenvectors[1][1] = 1;
				eigenvectors[2][1] = 0;
			}
		} else {
			// System.out.println("C");
			v1 = -f / e;
			v2 = 1;
			double norm = Math.sqrt(v1 * v1 + v2 * v2);
			eigenvectors[1][1] = v1 / norm;
			eigenvectors[2][1] = v2 / norm;
		}
		e = lambda2 - a;
		f = -b;
		if (e == 0) {
			if (c != 0) {
				// System.out.println("D");
				v1 = (lambda2 - d) / c;
				v2 = 1;
				double norm = Math.sqrt(v1 * v1 + v2 * v2);
				eigenvectors[1][2] = v1 / norm;
				eigenvectors[2][2] = v2 / norm;
			} else {// [0 -b; 0 lambda2-d]
					// System.out.println("E");
				eigenvectors[1][2] = 1;
				eigenvectors[2][2] = 0;
			}
		} else {
			// System.out.println("F");
			v1 = -f / e;
			v2 = 1;
			double norm = Math.sqrt(v1 * v1 + v2 * v2);
			eigenvectors[1][2] = v1 / norm;
			eigenvectors[2][2] = v2 / norm;
		}
	}

	/**
	 * ////////////////////////////////////////////////////////////////// The
	 * code below was written by Maurizio Pilu , University of Edinburgh. It
	 * implements matrix routines for ellipse-specific direct fitting method
	 * presented in the papers: M. Pilu, A. Fitzgibbon, R.Fisher
	 * ``Ellipse-specific Direct least-square Fitting '' , IEEE International
	 * Conference on Image Processing, Lausanne, September 1996. (poscript)
	 * (HTML) A. Fitzgibbon, M. Pilu , R.Fisher ``Direct least-square fitting of
	 * Ellipses '' , International Conference on Pattern Recognition, Vienna,
	 * August 1996. (poscript) - Extended version available as DAI Research
	 * Paper #794
	 * //////////////////////////////////////////////////////////////////
	 */

	/**
	 * Given a set of points, calculate the ellipse that best fit this set of
	 * points. Return the parameters of the ellipse. Return true if an
	 * elliptical fit is successful, otherwise return false. False is returned
	 * when the number of points is less than 6 and when all parameters are 0.
	 * 
	 * The properties array is of size 2. Index 0 stores the fit error, and
	 * index 1 stores the eccentricity of the ellipse.
	 */
	public static boolean ellipticalFit(double xvals[], double yvals[], int np,
			double params[]) {
		if (np < 6) {
			return false;
			// throw new
			// IllegalArgumentException("not enough data point to fit an arc");
		}
		double D[][] = new double[np + 1][7];
		double S[][] = new double[7][7];
		double Const[][] = new double[7][7];
		double temp[][] = new double[7][7];
		double L[][] = new double[7][7];
		double C[][] = new double[7][7];
		double invL[][] = new double[7][7];
		double d[] = new double[7];
		double V[][] = new double[7][7];
		double sol[][] = new double[7][7];
		double tx, ty;
		int nrot = 0;
		int npts = 50;
		double XY[][] = new double[3][npts + 1];
		Const[1][3] = -2;
		Const[2][2] = 1;
		Const[3][1] = -2;
		// Now first fill design matrix
		for (int i = 1; i <= np; i++) {
			tx = xvals[i - 1];
			ty = yvals[i - 1];
			D[i][1] = tx * tx;
			D[i][2] = tx * ty;
			D[i][3] = ty * ty;
			D[i][4] = tx;
			D[i][5] = ty;
			D[i][6] = 1.0;
		}

		// Now compute scatter matrix S
		A_TperB(D, D, S, np, 6, np, 6);
		choldc(S, 6, L);
		inverse(L, invL, 6);
		AperB_T(Const, invL, temp, 6, 6, 6, 6);
		AperB(invL, temp, C, 6, 6, 6, 6);
		jacobi(C, 6, d, V, nrot);
		A_TperB(invL, V, sol, 6, 6, 6, 6);

		for (int j = 1; j <= 6; j++) { // Scan columns
			double mod = 0.0;
			for (int i = 1; i <= 6; i++)
				mod += sol[i][j] * sol[i][j];
			for (int i = 1; i <= 6; i++)
				sol[i][j] /= Math.sqrt(mod);
		}

		double zero = 10e-20;
		// double minev=10e+20;
		int solind = 0;
		for (int i = 1; i <= 6; i++)
			if (d[i] < 0 && Math.abs(d[i]) > zero)
				solind = i;

		boolean allZero = true;
		double pvec[] = new double[7];
		// Now fetch the right solution
		for (int j = 1; j <= 6; j++) {
			pvec[j] = sol[j][solind];
			if (pvec[j] != 0) {
				allZero = false;
			}
		}
		if (allZero) {
			return false;
		}
		// compute fit error
		double normFactor = Math.sqrt(pvec[1] * pvec[3]);
		if (pvec[1] < 0) {// if a (and c, implied) are negative, use the
							// negative square root value
			normFactor = -normFactor;
		}
		for (int j = 1; j <= 6; j++) {
			pvec[j] = pvec[j] / normFactor;
		}

		for (int j = 1; j <= 6; j++) {
			params[j - 1] = pvec[j];
		}
		return true;// successful
	}

	/**
	 * Computes the eigenvectors(v) and eigenvalues(d) of matrix 'a'. 'n'
	 * specifies the dimension of the matrix.
	 */
	public static void jacobi(double a[][], int n, double d[], double v[][],
			int nrot) {
		int j, iq, ip, i;
		double tresh, theta, tau, t, sm, s, h, g, c;

		double b[] = new double[n + 1];
		double z[] = new double[n + 1];

		for (ip = 1; ip <= n; ip++) {
			for (iq = 1; iq <= n; iq++)
				v[ip][iq] = 0.0;
			v[ip][ip] = 1.0;
		}
		for (ip = 1; ip <= n; ip++) {
			b[ip] = d[ip] = a[ip][ip];
			z[ip] = 0.0;
		}
		nrot = 0;
		for (i = 1; i <= 50; i++) {
			sm = 0.0;
			for (ip = 1; ip <= n - 1; ip++) {
				for (iq = ip + 1; iq <= n; iq++)
					sm += Math.abs(a[ip][iq]);
			}
			if (sm == 0.0) {
				/*
				 * free_vector(z,1,n); free_vector(b,1,n);
				 */
				return;
			}
			if (i < 4)
				tresh = 0.2 * sm / (n * n);
			else
				tresh = 0.0;
			for (ip = 1; ip <= n - 1; ip++) {
				for (iq = ip + 1; iq <= n; iq++) {
					g = 100.0 * Math.abs(a[ip][iq]);
					if (i > 4 && Math.abs(d[ip]) + g == Math.abs(d[ip])
							&& Math.abs(d[iq]) + g == Math.abs(d[iq]))
						a[ip][iq] = 0.0;
					else if (Math.abs(a[ip][iq]) > tresh) {
						h = d[iq] - d[ip];
						if (Math.abs(h) + g == Math.abs(h))
							t = (a[ip][iq]) / h;
						else {
							theta = 0.5 * h / (a[ip][iq]);
							t = 1.0 / (Math.abs(theta) + Math.sqrt(1.0 + theta
									* theta));
							if (theta < 0.0)
								t = -t;
						}
						c = 1.0 / Math.sqrt(1 + t * t);
						s = t * c;
						tau = s / (1.0 + c);
						h = t * a[ip][iq];
						z[ip] -= h;
						z[iq] += h;
						d[ip] -= h;
						d[iq] += h;
						a[ip][iq] = 0.0;
						for (j = 1; j <= ip - 1; j++) {
							rotate(a, j, ip, j, iq, tau, s);
						}
						for (j = ip + 1; j <= iq - 1; j++) {
							rotate(a, ip, j, j, iq, tau, s);
						}
						for (j = iq + 1; j <= n; j++) {
							rotate(a, ip, j, iq, j, tau, s);
						}
						for (j = 1; j <= n; j++) {
							rotate(v, j, ip, j, iq, tau, s);
						}
						++nrot;
					}
				}
			}
			for (ip = 1; ip <= n; ip++) {
				b[ip] += z[ip];
				d[ip] = b[ip];
				z[ip] = 0.0;
			}
		}
		// printf("Too many iterations in routine JACOBI");
	}

	// Perform the Cholesky decomposition
	// Return the lower triangular L such that L*L'=A
	public static void choldc(double a[][], int n, double l[][]) {
		int i, j, k;
		double sum;
		double p[] = new double[n + 1];

		for (i = 1; i <= n; i++) {
			for (j = i; j <= n; j++) {
				for (sum = a[i][j], k = i - 1; k >= 1; k--)
					sum -= a[i][k] * a[j][k];
				if (i == j) {
					if (sum <= 0.0)
					// printf("\nA is not poitive definite!");
					{
					} else
						p[i] = Math.sqrt(sum);
				} else {
					a[j][i] = sum / p[i];
				}
			}
		}
		for (i = 1; i <= n; i++)
			for (j = i; j <= n; j++)
				if (i == j)
					l[i][i] = p[i];
				else {
					l[j][i] = a[j][i];
					l[i][j] = 0.0;
				}
	}

	/********************************************************************/
	/** Calcola la inversa della matrice B mettendo il risultato **/
	/** in InvB . Il metodo usato per l'inversione e' quello di **/
	/** Gauss-Jordan. N e' l'ordine della matrice . **/
	/** ritorna 0 se l'inversione corretta altrimenti ritorna **/
	/** SINGULAR . **/
	/********************************************************************/
	public static int inverse(double TB[][], double InvB[][], int N) {
		int k, i, j, p, q;
		double mult;
		double D, temp;
		double maxpivot;
		int npivot;
		double B[][] = new double[N + 1][N + 2];
		double A[][] = new double[N + 1][2 * N + 2];
		double C[][] = new double[N + 1][N + 1];
		double eps = 10e-20;

		for (k = 1; k <= N; k++)
			for (j = 1; j <= N; j++)
				B[k][j] = TB[k][j];

		for (k = 1; k <= N; k++) {
			for (j = 1; j <= N + 1; j++)
				A[k][j] = B[k][j];
			for (j = N + 2; j <= 2 * N + 1; j++)
				A[k][j] = (float) 0;
			A[k][k - 1 + N + 2] = (float) 1;
		}
		for (k = 1; k <= N; k++) {
			maxpivot = Math.abs((double) A[k][k]);
			npivot = k;
			for (i = k; i <= N; i++)
				if (maxpivot < Math.abs((double) A[i][k])) {
					maxpivot = Math.abs((double) A[i][k]);
					npivot = i;
				}
			if (maxpivot >= eps) {
				if (npivot != k)
					for (j = k; j <= 2 * N + 1; j++) {
						temp = A[npivot][j];
						A[npivot][j] = A[k][j];
						A[k][j] = temp;
					}
				;
				D = A[k][k];
				for (j = 2 * N + 1; j >= k; j--)
					A[k][j] = A[k][j] / D;
				for (i = 1; i <= N; i++) {
					if (i != k) {
						mult = A[i][k];
						for (j = 2 * N + 1; j >= k; j--)
							A[i][j] = A[i][j] - mult * A[k][j];
					}
				}
			} else { // printf("\n The matrix may be singular !!") ;
				return (-1);
			}
			;
		}
		/** Copia il risultato nella matrice InvB ***/
		for (k = 1, p = 1; k <= N; k++, p++)
			for (j = N + 2, q = 1; j <= 2 * N + 1; j++, q++)
				InvB[p][q] = A[k][j];
		return (0);
	} /* End of INVERSE */

	private static void AperB(double _A[][], double _B[][], double _res[][],
			int _righA, int _colA, int _righB, int _colB) {
		int p, q, l;
		for (p = 1; p <= _righA; p++)
			for (q = 1; q <= _colB; q++) {
				_res[p][q] = 0.0;
				for (l = 1; l <= _colA; l++)
					_res[p][q] = _res[p][q] + _A[p][l] * _B[l][q];
			}
	}

	private static void A_TperB(double _A[][], double _B[][], double _res[][],
			int _righA, int _colA, int _righB, int _colB) {
		int p, q, l;
		for (p = 1; p <= _colA; p++)
			for (q = 1; q <= _colB; q++) {
				_res[p][q] = 0.0;
				for (l = 1; l <= _righA; l++)
					_res[p][q] = _res[p][q] + _A[l][p] * _B[l][q];
			}
	}

	private static void AperB_T(double _A[][], double _B[][], double _res[][],
			int _righA, int _colA, int _righB, int _colB) {
		int p, q, l;
		for (p = 1; p <= _colA; p++)
			for (q = 1; q <= _colB; q++) {
				_res[p][q] = 0.0;
				for (l = 1; l <= _righA; l++)
					_res[p][q] = _res[p][q] + _A[p][l] * _B[q][l];
			}
	}

	private static void rotate(double a[][], int i, int j, int k, int l,
			double tau, double s) {
		double g, h;
		g = a[i][j];
		h = a[k][l];
		a[i][j] = g - s * (h + g * tau);
		a[k][l] = h + s * (g - h * tau);
	}

	public static void draw_conic(double pvec[], int nptsk, double points[][],
			double thetas[]) {
		int npts = nptsk / 2;
		double u[][] = new double[3][npts + 1];
		double Aiu[][] = new double[3][npts + 1];
		double L[][] = new double[3][npts + 1];
		double B[][] = new double[3][npts + 1];
		double Xpos[][] = new double[3][npts + 1];
		double Xneg[][] = new double[3][npts + 1];
		double ss1[][] = new double[3][npts + 1];
		double ss2[][] = new double[3][npts + 1];
		double lambda[] = new double[npts + 1];
		double uAiu[][] = new double[3][npts + 1];
		double A[][] = new double[3][3];
		double Ai[][] = new double[3][3];
		double Aib[][] = new double[3][2];
		double b[][] = new double[3][2];
		double r1[][] = new double[2][2];
		double Ao, Ax, Ay, Axx, Ayy, Axy;

		double theta;
		int i;
		int j;
		double kk;

		Ao = pvec[6];
		Ax = pvec[4];
		Ay = pvec[5];
		Axx = pvec[1];
		Ayy = pvec[3];
		Axy = pvec[2];

		A[1][1] = Axx;
		A[1][2] = Axy / 2;
		A[2][1] = Axy / 2;
		A[2][2] = Ayy;
		b[1][1] = Ax;
		b[2][1] = Ay;

		// Generate normals linspace
		for (i = 1, theta = 0.0; i <= npts; i++, theta += (Math.PI / npts)) {
			u[1][i] = Math.cos(theta);
			u[2][i] = Math.sin(theta);
			thetas[i] = theta;
		}
		for (; i <= nptsk; i++) {
			theta += (Math.PI / npts);
			thetas[i] = theta;
		}

		inverse(A, Ai, 2);

		AperB(Ai, b, Aib, 2, 2, 2, 1);

		A_TperB(b, Aib, r1, 2, 1, 2, 1);
		r1[1][1] = r1[1][1] - 4 * Ao;

		AperB(Ai, u, Aiu, 2, 2, 2, npts);

		for (i = 1; i <= 2; i++)
			for (j = 1; j <= npts; j++)
				uAiu[i][j] = u[i][j] * Aiu[i][j];

		for (j = 1; j <= npts; j++) {
			if ((kk = (r1[1][1] / (uAiu[1][j] + uAiu[2][j]))) >= 0.0)
				lambda[j] = Math.sqrt(kk);
			else
				lambda[j] = -1.0;
		}

		// Builds up B and L
		for (j = 1; j <= npts; j++)
			L[1][j] = L[2][j] = lambda[j];
		for (j = 1; j <= npts; j++) {
			B[1][j] = b[1][1];
			B[2][j] = b[2][1];
		}

		for (j = 1; j <= npts; j++) {
			ss1[1][j] = 0.5 * (L[1][j] * u[1][j] - B[1][j]);
			ss1[2][j] = 0.5 * (L[2][j] * u[2][j] - B[2][j]);
			ss2[1][j] = 0.5 * (-L[1][j] * u[1][j] - B[1][j]);
			ss2[2][j] = 0.5 * (-L[2][j] * u[2][j] - B[2][j]);
		}

		AperB(Ai, ss1, Xpos, 2, 2, 2, npts);
		AperB(Ai, ss2, Xneg, 2, 2, 2, npts);

		for (j = 1; j <= npts; j++) {
			if (lambda[j] == -1.0) {
				points[1][j] = -1.0;
				points[2][j] = -1.0;
				points[1][j + npts] = -1.0;
				points[2][j + npts] = -1.0;
			} else {
				points[1][j] = Xpos[1][j];
				points[2][j] = Xpos[2][j];
				points[1][j + npts] = Xneg[1][j];
				points[2][j + npts] = Xneg[2][j];
			}
		}
	}
}
