/*
 * $Id: Fragmenter.java,v 1.8 2003/10/03 02:08:42 hwawen Exp $
 *
 * Copyright (c) 2003-2004 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.fragmentation;


import org.hhreco.recognition.TimedStroke;
import org.openawt.geom.Line2D;

/**
 * Implementation of the fragmentation algorithms that fragment a symbol based
 * on templates specification. The algorithms use a dynamic programming
 * approach.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class Fragmenter {

	/**
	 * Fragment the given sequence of strokes with the number of ellipses and
	 * the number of lines specified by numE and numL.
	 */
	public static FitData fragmentWithTemplate(TimedStroke[] strokes, int numE,
			int numL) {
		DynaFit dyna = new DynaFit(strokes, numE, numL);
		return dyna.getFitData();
	}

	/**
	 * Fragment the given sequence of strokes with the specified template, an
	 * ordered sequence of E's and L's.
	 */
	public static FitData fragmentWithTemplate(TimedStroke[] strokes,
			String template) {
		DynaFit df = new DynaFit(strokes, template);
		return df.getFitData();
	}

	/**
	 * Fit an ellipse to the set of points. To do an elliptical fit, minimum of
	 * 6 data points is required to solve for the parameters of the elliptical
	 * equation: aX^2+bY^2+cXY+dX+eY+f=0; If the number of points is less than
	 * 6, return null. If the parameters of the ellipse are all 0's, return null
	 * as well, this is the result of an unsuccessful fit. Otherwise, return an
	 * EllipseBasis object with valid parameters.
	 */
	protected static EllipseBasis ellipseFit(double xvals[], double yvals[],
			int num) {
		if (num < 6) {
			return null;
		}
		double params[] = new double[6];
		boolean isSuccessful = FittingUtil.ellipticalFit(xvals, yvals, num,
				params);
		if (isSuccessful) {
			EllipseBasis b = new EllipseBasis(xvals, yvals, num, params);
			if (Double.isNaN(b.getFitError())) {
				return null;
			}
			return b;
		} else {
			return null;
		}
	}

	/**
	 * Use total regression to fit the best line to the given set of points.
	 */
	protected static LineBasis lineFit(double xvals[], double yvals[], int num) {
		if (num == 1) {
			// throw new
			// IllegalArgumentException("Only 1 data point, not enough to do a line fit.");
			return null;
		} else {
			double xavg, yavg, e11, e12, e21, e22;
			xavg = yavg = e11 = e12 = e21 = e22 = 0;
			for (int i = 0; i < num; i++) {
				xavg += xvals[i];
				yavg += yvals[i];
				e11 += Math.pow(xvals[i], 2);
				e12 += xvals[i] * yvals[i];
				e22 += Math.pow(yvals[i], 2);
			}
			xavg /= num;
			yavg /= num;
			e11 = ((e11) / num) - Math.pow(xavg, 2);
			e12 = ((e12) / num) - xavg * yavg;
			e21 = e12;
			e22 = ((e22) / num) - Math.pow(yavg, 2);
			double m[][] = new double[3][3];
			m[1][1] = e11;
			m[1][2] = e12;
			m[2][1] = e21;
			m[2][2] = e22;
			double d[] = new double[3];
			double V[][] = new double[3][3];
			FittingUtil.eigen(m, d, V);

			double lineParam1[] = new double[3];
			double lineParam2[] = new double[3];
			double v1 = -V[1][1] * xavg - V[2][1] * yavg;
			lineParam1[0] = V[1][1];
			lineParam1[1] = V[2][1];
			lineParam1[2] = v1;

			double v2 = -V[1][2] * xavg - V[2][2] * yavg;
			lineParam2[0] = V[1][2];
			lineParam2[1] = V[2][2];
			lineParam2[2] = v2;

			double dist1 = 0;
			double dist2 = 0;
			double v;
			for (int i = 0; i < num; i++) {
				v = lineParam1[0] * xvals[i] + lineParam1[1] * yvals[i]
						+ lineParam1[2];
				dist1 += v * v;
				v = lineParam2[0] * xvals[i] + lineParam2[1] * yvals[i]
						+ lineParam2[2];
				dist2 += v * v;
			}

			double lineParam[] = lineParam1;
			if (dist2 < dist1) {
				lineParam = lineParam2;
			}
			double fitError = 0;
			for (int i = 0; i < num; i++) {
				v = lineParam[0] * xvals[i] + lineParam[1] * yvals[i]
						+ lineParam[2];
				fitError += v * v;// sum((ax+by+c)^2)
			}

			double orig_a = lineParam[0];
			double orig_b = lineParam[1];
			double orig_c = lineParam[2];
			lineParam[0] /= lineParam[2];
			lineParam[1] /= lineParam[2];
			lineParam[2] /= lineParam[2];

			if (num == 2 || orig_c == 0) {// create the line and return
				Line2D line = new Line2D.Double(xvals[0], yvals[0], xvals[1],
						yvals[1]);
				return new LineBasis(xvals, yvals, num, lineParam, 0, line);
			}
			Line2D line = null;
			// figure out the line boundary
			double p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y;// p1 and p2 are
															// points on the
															// line, we're
															// trying to find
															// the orthogonal
															// projection of p3
															// onto the line.
			double a = lineParam[0];
			double b = lineParam[1];
			double c = lineParam[2];
			if (orig_a == 0) {// orig_a=0, horizontal line, figure out start x
								// and end x
				double maxxval = -100;
				double minxval = Double.MAX_VALUE;
				for (int i = 0; i < num; i++) {
					if (xvals[i] > maxxval) {
						maxxval = xvals[i];
					}
					if (xvals[i] < minxval) {
						minxval = xvals[i];
					}
				}
				p4y = -orig_c / orig_b;
				if (xvals[0] < xvals[num - 1]) {
					line = new Line2D.Double(minxval, p4y, maxxval, p4y);
				} else {
					line = new Line2D.Double(maxxval, p4y, minxval, p4y);
				}
			} else if (orig_b == 0) {// orig_b=0, vertical line
				double maxyval = -100;
				double minyval = Double.MAX_VALUE;
				for (int i = 0; i < num; i++) {
					if (yvals[i] > maxyval) {
						maxyval = yvals[i];
					}
					if (yvals[i] < minyval) {
						minyval = yvals[i];
					}
				}
				p4x = -orig_c / orig_a;
				if (yvals[0] < yvals[num - 1]) {
					line = new Line2D.Double(p4x, minyval, p4x, maxyval);
				} else {
					line = new Line2D.Double(p4x, maxyval, p4x, minyval);
				}
			} else {
				if (Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(c)) {
					return null;
				}
				p1x = xvals[0];
				p1y = (-c - a * p1x) / b;
				p2x = p1x + 100;
				p2y = (-c - a * p2x) / b;
				double projection[][] = new double[num][2];
				double maxxval = Double.NEGATIVE_INFINITY;
				double minxval = Double.POSITIVE_INFINITY;
				int maxxi = -1;
				int minxi = -1;
				double maxyval = Double.NEGATIVE_INFINITY;
				double minyval = Double.POSITIVE_INFINITY;
				int maxyi = -1;
				int minyi = -1;

				for (int i = 0; i < num; i++) {
					p3x = xvals[i];
					p3y = yvals[i];
					p4x = (b * (p3x * (p1x - p2x) + p3y * (p1y - p2y)) - c
							* (p2y - p1y))
							/ (b * (p1x - p2x) + a * (p2y - p1y));
					p4y = (-c - a * p4x) / b;

					projection[i][0] = p4x;
					projection[i][1] = p4y;
					if (p4x > maxxval) {
						maxxval = p4x;
						maxxi = i;
					}
					if (p4x < minxval) {
						minxval = p4x;
						minxi = i;
					}
					if (p4y > maxyval) {
						maxyval = p4y;
						maxyi = i;
					}
					if (p4y < minyval) {
						minyval = p4y;
						minyi = i;
					}
				}

				if (maxxval == minxval) {
					if (yvals[0] < yvals[num - 1]) {
						line = new Line2D.Double(projection[minyi][0],
								projection[minyi][1], projection[maxyi][0],
								projection[maxyi][1]);
					} else {
						line = new Line2D.Double(projection[maxyi][0],
								projection[maxyi][1], projection[minyi][0],
								projection[minyi][1]);
					}
				} else {
					if (xvals[0] < xvals[num - 1]) {
						line = new Line2D.Double(projection[minxi][0],
								projection[minxi][1], projection[maxxi][0],
								projection[maxxi][1]);
					} else {
						line = new Line2D.Double(projection[maxxi][0],
								projection[maxxi][1], projection[minxi][0],
								projection[minxi][1]);
					}
				}
			}
			return new LineBasis(xvals, yvals, num, lineParam, fitError, line);
		}
	}

	/**
	 * This class uses dynamic programming to compute the optimal set of
	 * breakpoints for a given sequence of strokes when fitting to a template.
	 */
	private static class DynaFit {
		TimedStroke[] _strokes = null;
		int _numE;
		int _numL;
		DPTable[][] _tables = null;
		Breakpoint[] _bpts = null;
		FitData _fitData = null;
		boolean _isSuccessful = false;

		/**
		 * Fitting the sequence of strokes using the specified template, an
		 * ordered sequence of E's and L's. The number of strokes has to be
		 * greater than the number of fragments requested (template.length),
		 * otherwise fragmentation cannot be performed, in which case,
		 * getFitData() returns null;
		 */
		public DynaFit(TimedStroke[] strokes, String template) {
			_strokes = strokes;
			int n = _strokes.length;
			int k = template.length() - _strokes.length;
			if (k >= 0) {
				_tables = new DPTable[1][1];
				_tables[0][0] = new DPTable(_strokes, k);
				int m = _strokes[n - 1].getVertexCount() - 1;// index of last pt
																// in last
																// stroke
				DPElem finalDE = dpfit(n - 1, m, k, template, _tables[0][0]);
				_isSuccessful = backTrack(finalDE, k, template);
			}
		}

		/**
		 * Fitting the sequence of strokes using the specified template
		 * consisting of the number of E's and the number of L's. The number of
		 * strokes has to be greater than the number of fragments requested
		 * (numE+numL), otherwise fragmentation cannot be performed, in which
		 * case, getFitData() returns null;
		 */
		public DynaFit(TimedStroke[] strokes, int numE, int numL) {
			int k = numE + numL - strokes.length;// # of bpts needed
			if (k >= 0) {
				_strokes = strokes;
				_numE = numE;
				_numL = numL;
				_tables = new DPTable[numE + 1][numL + 1];
				for (int i = 0; i < numE + 1; i++) {
					for (int j = 0; j < numL + 1; j++) {
						// int k = i+j-1;//# of bpts
						// if(k>=0){
						_tables[i][j] = new DPTable(_strokes, k);
						// }
					}
				}
				int n = _strokes.length - 1;// index of last stroke
				int m = _strokes[n].getVertexCount() - 1;// index of last pt in
															// last stroke
				DPElem finalDE = dpfit(n, m, k, numE, numL);
				_isSuccessful = backTrack(finalDE, k);
			}
		}

		/*
		 * This method computes the best fragmentation with the given number of
		 * E's and L's from the start of the first stroke to the specified point
		 * (index m) in the indicated stroke (index n) using k breakpoints. This
		 * method uses dynamic programming to figure out the best fragmentation
		 * so far before the current point using (k-1) breakpoints.
		 * 
		 * n = stroke index m = point index in stroke n k = number of
		 * breakpoints ne = number of E's nl = number of L's
		 */
		private DPElem dpfit(int n, int m, int k, int ne, int nl) {
			// System.out.println("numE = "+ne +", numL = "+nl);
			DPTable tbl = _tables[ne][nl];
			DPElem elem = tbl.get(n, k, m);
			if (elem != null) {
				return elem;
			}
			if (nl == 0) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < ne; i++) {
					buf.append('E');
				}
				String t = buf.toString();
				// System.out.println("Template:"+t);
				elem = dpfit(n, m, k, t, tbl);
			} else if (ne == 0) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < nl; i++) {
					buf.append('L');
				}
				String t = buf.toString();
				// System.out.println("Template:"+t);
				elem = dpfit(n, m, k, t, tbl);
			} else if (k == 0) {
				double costE = Double.POSITIVE_INFINITY;
				// current stroke up to m_th point, fit with E
				Basis eb = fit(_strokes[n], 0, m, Basis.TYPE_ELLIPSE);
				DPElem elemE = null;
				if (eb != null) {
					costE = eb.getFitError();
					if (n > 0) {
						// int lastIndex = _strokes[n-1].getNumPoints()-1;
						int lastIndex = _strokes[n - 1].getVertexCount() - 1;
						elemE = dpfit(n - 1, lastIndex, k, ne - 1, nl);
						costE += elemE.getTotalFitError();
					}
				}

				double costL = Double.POSITIVE_INFINITY;
				Basis lb = fit(_strokes[n], 0, m, Basis.TYPE_LINE);
				DPElem elemL = null;
				if (lb != null) {
					costL = lb.getFitError();
					if (n > 0) {
						// int lastIndex = _strokes[n-1].getNumPoints()-1;
						int lastIndex = _strokes[n - 1].getVertexCount() - 1;
						elemL = dpfit(n - 1, lastIndex, k, ne, nl - 1);
						costL += elemL.getTotalFitError();
					}
				}
				if (costE < costL) {
					elem = new DPElem(n, k, m, costE, eb, elemE);
				} else {
					elem = new DPElem(n, k, m, costL, lb, elemL);
				}
			} else {// k>0
				int i = 1;
				if (n == 0) {
					i = k;
				}
				double minCost = Double.POSITIVE_INFINITY;
				Basis bestSoFar = null;
				DPElem prev = null;
				for (; i < m; i++) {
					Basis eb = fit(_strokes[n], i, m, Basis.TYPE_ELLIPSE);
					if (eb != null) {
						DPElem a = dpfit(n, i, k - 1, ne - 1, nl);
						double c = eb.getFitError() + a.getTotalFitError();
						if (c < minCost) {
							minCost = c;
							bestSoFar = eb;
							prev = a;
						}
					}
					Basis lb = fit(_strokes[n], i, m, Basis.TYPE_LINE);
					if (lb != null) {
						DPElem a = dpfit(n, i, k - 1, ne, nl - 1);
						double c = lb.getFitError() + a.getTotalFitError();
						if (c < minCost) {
							minCost = c;
							bestSoFar = lb;
							prev = a;
						}
					}
				}
				if (n != 0) {// fit current stroke with E or L
					Basis eb = fit(_strokes[n], 0, m, Basis.TYPE_ELLIPSE);
					if (eb != null) {
						int lastIndex = _strokes[n - 1].getVertexCount() - 1;
						DPElem a = dpfit(n - 1, lastIndex, k, ne - 1, nl);
						double c = eb.getFitError() + a.getTotalFitError();
						if (c < minCost) {
							minCost = c;
							bestSoFar = eb;
							prev = a;
						}
					}
					Basis lb = fit(_strokes[n], 0, m, Basis.TYPE_LINE);
					if (lb != null) {
						int lastIndex = _strokes[n - 1].getVertexCount() - 1;
						DPElem a = dpfit(n - 1, lastIndex, k, ne, nl - 1);
						double c = lb.getFitError() + a.getTotalFitError();
						if (c < minCost) {
							minCost = c;
							bestSoFar = lb;
							prev = a;
						}
					}
				}
				elem = new DPElem(n, k, m, minCost, bestSoFar, prev);
			}
			// tbl[n][k][m] = elem;
			tbl.set(n, k, m, elem);
			return elem;
		}

		private DPElem dpfit(int n, int m, int k, String tp, DPTable tbl) {
			// System.out.println("dpFit("+n+", "+m+", "+k+", "+tp+")");
			DPElem elem = tbl.get(n, k, m);
			if (elem != null) {// have been computed
				// System.out.print(elem+"\n");
				return elem;
			} else if (k == 0) {// row 0, fit with no breakpoints
				// error checking
				if (tp.length() > n + 1) {
					int a = n + 1;
					throw new RuntimeException("Inconsistency: tp = " + tp
							+ ", but we have " + a + " strokes");
				}
				int which = Basis.TYPE_LINE;
				if (tp.charAt(tp.length() - 1) == 'E') {
					which = Basis.TYPE_ELLIPSE;
				}
				Basis b = fit(_strokes[n], 0, m, which);
				double cost = Double.POSITIVE_INFINITY;
				DPElem prev = null;
				if (b != null) {
					cost = b.getFitError();
					if (n > 0) {
						int lastIndex = _strokes[n - 1].getVertexCount() - 1;
						prev = dpfit(n - 1, lastIndex, k,
								tp.substring(0, tp.length() - 1), tbl);
						cost += prev.getTotalFitError();
					}
				}
				elem = new DPElem(n, 0, m, cost, b, prev);
				// System.out.print(elem+"\n");
				tbl.set(n, 0, m, elem);
				return elem;
			} else {// rows other than row 0
				String tpsub = tp.substring(0, tp.length() - 1);
				double minCost = Double.POSITIVE_INFINITY;
				Basis bestSoFar = null;
				DPElem prev = null;
				int i = 1;
				if (n == 0) {
					i = k;
				}
				int which = Basis.TYPE_LINE;
				if (tp.charAt(tp.length() - 1) == 'E') {
					which = Basis.TYPE_ELLIPSE;
				}
				int bpt = 0;
				for (; i < m; i++) {
					// whatever this dpFit returns, it is the best fit
					// using tpsub it can get
					Basis b = fit(_strokes[n], i, m, which);
					DPElem de = null;
					double cost = Double.POSITIVE_INFINITY;
					if (b != null) {
						de = dpfit(n, i, k - 1, tpsub, tbl);
						cost = b.getFitError() + de.getTotalFitError();
					}
					// System.out.print("i="+i+", cost="+cost+"\n");
					if (cost < minCost) {
						minCost = cost;
						bestSoFar = b;
						prev = de;
						bpt = i;
					}
				}
				// System.out.print("bpt = " + bpt+", minCost="+minCost+"\n");

				if (n > 0) {// one more thing to check
					// System.out.print("Check break at prev stroke\n");
					Basis b = fit(_strokes[n], 0, m, which);
					DPElem de = null;
					double cost = Double.POSITIVE_INFINITY;
					if (b != null) {
						int lastIndex = _strokes[n - 1].getVertexCount() - 1;
						de = dpfit(n - 1, lastIndex, k, tpsub, tbl);
						cost = b.getFitError() + de.getTotalFitError();
						// System.out.print("b="+b.getFitError()+", prev="+
						// de.getTotalFitError()+", total="+cost+"\n");
						// System.out.print("\t "+de.getPrev()+"\n");
					}
					if (cost < minCost) {
						// System.out.print("choose to break at prev stroke\n");
						minCost = cost;
						bestSoFar = b;
						prev = de;
					}
				}
				elem = new DPElem(n, k, m, minCost, bestSoFar, prev);
				// System.out.print(elem+"\n");
				tbl.set(n, k, m, elem);
				return elem;
			}
		}

		/**
		 * Try to fit the section of the stroke (start index to end index) with
		 * the specified basis type. Return null if the fitting routine is
		 * unsuccessful.
		 */
		private Basis fit(TimedStroke stroke, int start, int end, int type) {
			int size = end - start + 1;
			double[] xvals = new double[size];
			double[] yvals = new double[size];
			int ct = 0;
			for (int i = start; i <= end; i++) {
				xvals[ct] = stroke.getX(i);
				yvals[ct] = stroke.getY(i);
				ct++;
			}
			Basis b = null;
			if (type == Basis.TYPE_LINE) {
				b = lineFit(xvals, yvals, size);
			} else {
				b = ellipseFit(xvals, yvals, size);
			}
			return b;
		}

		private boolean backTrack(DPElem elem, int nbpts) {
			int ct = nbpts - 1;
			Breakpoint[] bpts = new Breakpoint[nbpts];
			DPElem de = elem;
			int n = _numE + _numL;
			Basis[] bases = new Basis[n];
			int[] strokeIndices = new int[n];
			n--;
			boolean isSuccessful = true;
			while (de != null) {
				// System.out.print(de+"\n");
				int strokeI = de.getTableIndex();
				int pointI = de.getColIndex();
				int lastIndex = _strokes[strokeI].getVertexCount() - 1;
				if (lastIndex != pointI) {
					bpts[ct--] = new Breakpoint(de.getTableIndex(),
							de.getColIndex());
				}
				Basis b = de.getFit();
				if (b == null) {
					isSuccessful = false;
					break;
				}
				bases[n] = b;
				strokeIndices[n] = de.getTableIndex();
				n--;
				de = de.getPrev();
			}
			if (isSuccessful) {
				_fitData = new FitData(bases, strokeIndices, _strokes.length);
				_bpts = bpts;
			} else {
				_fitData = null;
				_bpts = null;
			}
			return isSuccessful;
		}

		private boolean backTrack(DPElem elem, int nbpts, String template) {
			int ct = nbpts - 1;
			Breakpoint[] bpts = new Breakpoint[nbpts];
			DPElem de = elem;
			int n = template.length();
			Basis[] bases = new Basis[n];
			int[] strokeIndices = new int[n];
			n--;
			boolean isSuccessful = true;
			while (de != null) {
				// System.out.print(de+"\n");
				int strokeI = de.getTableIndex();
				int pointI = de.getColIndex();
				int lastIndex = _strokes[strokeI].getVertexCount() - 1;
				if (lastIndex != pointI) {
					bpts[ct--] = new Breakpoint(de.getTableIndex(),
							de.getColIndex());
				}
				Basis b = de.getFit();
				if (b == null) {
					isSuccessful = false;
					break;
				}
				bases[n] = b;
				strokeIndices[n] = de.getTableIndex();
				n--;
				de = de.getPrev();
			}
			if (isSuccessful) {
				_fitData = new FitData(bases, strokeIndices, _strokes.length);
				_bpts = bpts;
			} else {
				_fitData = null;
				_bpts = null;
			}
			return isSuccessful;
		}

		public FitData getFitData() {
			return _fitData;
		}

		// public Breakpoint[] getBreakpoints() {
		// return _bpts;
		// }

		// public boolean isSuccessful() {
		// return _isSuccessful;
		// }

		private static class DPTable {
			// stores DPElem, [strokeI][bpt#][pointI] ([tableI][rowI][colI])
			private DPElem[][][] _tables;

			public DPTable(TimedStroke[] strokes, int nbpts) {
				int n = strokes.length;
				_tables = new DPElem[n][][];
				for (int i = 0; i < n; i++) {
					int p = strokes[i].getVertexCount();
					_tables[i] = new DPElem[nbpts + 1][p];
					for (int j = 0; j < nbpts + 1; j++) {// row
						for (int m = 0; m < p; m++) {// col, start with j+1
														// upper tri
							_tables[i][j][m] = null;// init elements to null
						}
					}
				}
			}

			public DPElem get(int tableI, int rowI, int colI) {
				return _tables[tableI][rowI][colI];
			}

			public void set(int tableI, int rowI, int colI, DPElem e) {
				_tables[tableI][rowI][colI] = e;
			}
		}

		private class DPElem {
			private int _tableIndex;// stroke index
			private int _rowIndex;// number of breakpoints index
			private int _colIndex;// point index
			private double _totalFitError = Double.POSITIVE_INFINITY;// including
																		// this
																		// fit
			private Basis _fit;
			private DPElem _prev = null;

			public DPElem(int tableIndex, int rowIndex, int colIndex,
					double totalErr, Basis b, DPElem prev) {
				_totalFitError = totalErr;
				_fit = b;
				_tableIndex = tableIndex;
				_rowIndex = rowIndex;
				_colIndex = colIndex;
				_prev = prev;
			}

			public double getTotalFitError() {
				return _totalFitError;
			}

			public Basis getFit() {
				return _fit;
			}

			public int getTableIndex() {
				return _tableIndex;
			}

			public int getRowIndex() {
				return _rowIndex;
			}

			public int getColIndex() {
				return _colIndex;
			}

			public DPElem getPrev() {
				return _prev;
			}

			public String toString() {
				StringBuffer buf = new StringBuffer();
				buf.append("n=" + getTableIndex() + ", m=" + getColIndex()
						+ ", k=" + getRowIndex() + " (");
				if (_prev == null) {
					buf.append("null, ");
				} else {
					buf.append(_prev.getTotalFitError() + ", ");
				}
				if (_fit == null) {
					buf.append("null)");
				} else {
					buf.append(_fit.getFitError() + ")");
				}
				return buf.toString();
			}
		}
	}

	/**
	 * A data structure for storing break point information. A break point can
	 * be either a corner (high curvature) or an inflection point. This class
	 * records the stroke index, the point index, and the corner value of a
	 * break point. If the breakpoint is an inflection point, there is no corner
	 * value associated.
	 */
	private static class Breakpoint {
		/**
		 * Index of the stroke that this break point exists on.
		 */
		private int _strokeIndex;

		/**
		 * Index of this point on the stroke.
		 */
		private int _pointIndex;

		/**
		 * Instantiate a corner break point.
		 */
		public Breakpoint(int strokeIdx, int pointIdx) {
			_strokeIndex = strokeIdx;
			_pointIndex = pointIdx;
		}

		/**
		 * Return the index of the stroke that this break point exists on.
		 */
		public int getStrokeIndex() {
			return _strokeIndex;
		}

		/**
		 * Return the index of this point on the stroke.
		 */
		public int getPointIndex() {
			return _pointIndex;
		}

		/**
		 * String representation of this breakpoint information. [stroke point
		 * value]
		 */
		public String toString() {
			return "[" + _strokeIndex + "   " + _pointIndex + "]";
		}
	}
}
