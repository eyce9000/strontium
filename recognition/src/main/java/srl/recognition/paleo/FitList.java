/**
 * FitList.java
 * 
 * Revision History:<br>
 * Aug 20, 2008 bpaulson - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
package srl.recognition.paleo;

import java.util.ArrayList;

/**
 * "Smarter" list to use for adding fits
 * 
 * @author bpaulson
 */
public class FitList extends ArrayList<Fit> {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 8742679044072746039L;

	/**
	 * Index indicating where in the hierarchy the top fit was added
	 */
	private int m_indexAdded = -1;

	/**
	 * Add a fit to the list and set the hierarchy index
	 * 
	 * @param f
	 *            fit to add
	 * @param hierarchyIndex
	 *            index indicating where in the hierarchy the fit was added
	 */
	public void add(Fit f, int hierarchyIndex) {
		if (m_indexAdded == -1)
			m_indexAdded = hierarchyIndex;
		add(f);
	}

	/**
	 * Add a fit to the list (at a specific index) and set the hierarchy index
	 * 
	 * @param index
	 *            index to add fit to
	 * @param f
	 *            fit
	 * @param hierarchyIndex
	 *            hierarchy index
	 */
	public void add(int index, Fit f, int hierarchyIndex) {
		if (m_indexAdded == -1 || index == 0)
			m_indexAdded = hierarchyIndex;
		add(index, f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	@Override
	public boolean add(Fit f) {
		// dont add null fits
		if (f instanceof NullFit)
			return false;

		// check to make sure fit isnt an empty polyline fit;
		// if it is then return a regular line fit
		if (f instanceof PolylineFit)
			if (((PolylineFit) f).getNumSubStrokes() <= 0)
				return add(new LineFit(f.m_features, true));

		// check to make sure we dont already have an instance of the fit
		if (!contains(f))
			return super.add(f);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, Fit f) {
		// dont add null fits
		if (f instanceof NullFit)
			return;

		// check to make sure fit isnt an empty polyline fit;
		// if it is then return a regular line fit
		if (f instanceof PolylineFit)
			if (((PolylineFit) f).getNumSubStrokes() <= 0)
				add(index, new LineFit(f.m_features, true));

		// check to make sure we dont already have an instance of the fit
		if (!contains(f))
			super.add(index, f);
		return;
	}

	/**
	 * Get the index indicating where in the hierarchy the top fit was added
	 * 
	 * @return index
	 */
	public int getHierarchyIndex() {
		return m_indexAdded;
	}
}
