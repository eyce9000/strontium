/*
 * $Id: FitData.java,v 1.2 2003/06/02 23:12:07 hwawen Exp $
 *
 * Copyright (c) 2003-2004 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.fragmentation;


/**
 * A data structure storing a set of bases that represents a particular
 * fragmentation of a symbol.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class FitData {
	/**
	 * The set of bases resulting from the fitting routine.
	 */
	private Basis[] _bases;

	/**
	 * Sum of fit errors from the bases.
	 */
	private double _totalFitError;

	/**
	 * The elements in this array correspond to _bases. Each indicates the
	 * stroke that the basis approximates.
	 */
	private int[] _strokeIndices;

	/**
	 * Number of strokes in the original symbol.
	 */
	private int _numStrokes;

	/**
	 * Two dimensional array: first dimension is the stroke indices and the
	 * second dimension contains the breakpoint indices of the stroke.
	 */
	private int[][] _strokeToBpts = null;

	/**
	 * (Not needed) Two dimensional array: first dimension is the stroke indices
	 * and the second dimension contains the fittings (LineBasis or
	 * EllipseBasis) of the stroke.
	 */
	private Basis[][] _strokeToFits = null;

	/**
	 * The fitting template. 'L' for a linear basis and 'E' for an elliptical
	 * basis.
	 */
	private String _template = null;

	/**
	 * Each basis correspond to a stroke index. There can be more than 1 basis
	 * per stroke if break points exist in the stroke. 'nstrokes' is the number
	 * of strokes in the original shape.
	 */
	public FitData(Basis[] bases, int[] strokeIndices, int nstrokes) {
		_bases = bases;
		_strokeIndices = strokeIndices;
		_totalFitError = 0;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < _bases.length; i++) {
			_totalFitError += _bases[i].getFitError();
			if (_bases[i].getType() == Basis.TYPE_LINE) {
				buf.append('L');
			} else {
				buf.append('E');
			}
		}
		_template = buf.toString();

		// store the information in a different data structure, this is
		// only necessary for painting the fits and break points.
		_numStrokes = nstrokes;
		int[] numFitsPerStroke = new int[nstrokes];
		for (int i = 0; i < nstrokes; i++) {
			numFitsPerStroke[i] = 0;
		}
		int strokeIndex = 0;
		for (int i = 0; i < getBasisCount(); i++) {
			int j = getStrokeIndex(i);
			if (j > strokeIndex) {
				strokeIndex++;
				i--;
			} else {
				numFitsPerStroke[j] = numFitsPerStroke[j] + 1;
			}
		}
		_strokeToBpts = new int[nstrokes][];
		_strokeToFits = new Basis[nstrokes][];
		for (int i = 0; i < nstrokes; i++) {
			_strokeToBpts[i] = new int[numFitsPerStroke[i] - 1];
			_strokeToFits[i] = new Basis[numFitsPerStroke[i]];
		}
		int start = 0;
		int end = 0;
		for (int i = 0; i < nstrokes; i++) {
			end = start + numFitsPerStroke[i];
			int k = 0;
			int prevn = 0;
			for (int j = start; j < end; j++) {
				_strokeToFits[i][k] = getBasis(j);
				if (j < (end - 1)) {
					Basis b = getBasis(j);
					int n = b.getNumPoints();
					if (k == 0) {// no previous fit
						_strokeToBpts[i][k] = n - 1;
						prevn = n;
					} else {
						prevn = prevn + n - 1;
						_strokeToBpts[i][k] = prevn - 1;

					}
				}
				k++;
			}
			start = end;
		}
	}

	/**
	 * Return the number of bases in this fitting.
	 */
	public int getBasisCount() {
		return _bases.length;
	}

	/**
	 * Return the basis at the specified index.
	 */
	public Basis getBasis(int i) {
		return _bases[i];
	}

	/**
	 * Return the stroke index that the basis at the specified index corresponds
	 * to.
	 */
	public int getStrokeIndex(int i) {
		return _strokeIndices[i];
	}

	/**
	 * Return the sum of the fit errors from all bases.
	 */
	public double getTotalFitError() {
		return _totalFitError;
	}

	/**
	 * Return the template of this fit. 'L' for a linear basis and 'E' for an
	 * elliptical basis.
	 */
	public String getTemplate() {
		return _template;
	}

	/**
	 * Return the number of strokes of the original shape that this fitting
	 * approximates.
	 */
	public int getNumStrokes() {
		return _numStrokes;
	}

	/**
	 * Return the breakpoints on the specified stroke (by stroke index). Null if
	 * non exists.
	 */
	public int[] getBreakpointsOnStroke(int i) {
		return _strokeToBpts[i];
	}

	/**
	 * Return the bases on the specified stroke. Null if non exists.
	 */
	public Basis[] getFitsOnStroke(int i) {
		return _strokeToFits[i];
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("Breakpoints:\n");
		for (int i = 0; i < _numStrokes; i++) {
			int[] b = _strokeToBpts[i];
			if (b != null) {
				buf.append("  stroke #" + i + ": [");
				for (int j = 0; j < b.length; j++) {
					buf.append(b[j]);
					if (j != b.length - 1) {
						buf.append(", ");
					}
				}
				buf.append("]\n");
			}
		}

		buf.append(getTemplate() + "\n");
		buf.append("Total fit error: " + getTotalFitError());
		return buf.toString();
	}
}
