/**
 * RecognitionResult.java
 * 
 * Revision History:<br>
 * Oct 3, 2008 jbjohns - File created
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
package srl.recognition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import srl.core.sketch.Shape;


/**
 * Implementation of the interface.
 * 
 * @author jbjohns
 */
public class RecognitionResult implements IRecognitionResult,
		Comparable<IRecognitionResult> {

	/**
	 * The identifier for this recognition result.
	 */
	private UUID m_id;

	/**
	 * N-Best list for results of a grouping of strokes. For multiple groupings,
	 * you might consider using multiple recognition result instances, one per
	 * grouping.
	 */
	private List<Shape> m_nBestList;

	/**
	 * Comparator to compare shape confidence values.
	 */
	private ConfidenceComparator m_confComp = new ConfidenceComparator();

	/**
	 * Create an empty recognition result with a new, random UUID and empty
	 * n-best list.
	 */
	public RecognitionResult() {
		m_id = UUID.randomUUID();
		m_nBestList = new ArrayList<Shape>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionResult#getID()
	 */
	public UUID getID() {
		return m_id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionResult#getNBestList()
	 */
	public List<Shape> getNBestList() {
		return m_nBestList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionResult#setNBestList(java.util.List)
	 */
	public void setNBestList(List<Shape> nBestList) {
		if (nBestList != null) {
			m_nBestList = nBestList;
		} else {
			m_nBestList = new ArrayList<Shape>();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionResult#getNumInterpretations()
	 */
	public int getNumInterpretations() {
		if (m_nBestList != null) {
			return m_nBestList.size();
		} else {
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionResult#addShapeToNBestList(org.ladder
	 * .core.sketch.Shape)
	 */
	public void addShapeToNBestList(Shape shape) {
		if (m_nBestList == null) {
			m_nBestList = new ArrayList<Shape>();
		}
		m_nBestList.add(shape);
	}

	/**
	 * This method makes a linear search every call and thus runs in O(n). We do
	 * this because we can't ensure that no shapes or the n-best list have not
	 * been changed externally to this class. Additionally, we don't believe
	 * that enough interpretations will be put onto the list to make a linear
	 * lookup noticeable enough to warrant the trouble of sorting the
	 * list/caching the best shape.
	 * 
	 * @see ConfidenceComparator
	 * @return The shape with the highest confidence value.
	 */
	public Shape getBestShape() {
		Shape bestShape = null;

		for (Shape shape : m_nBestList) {
			// this the first shape we've seen? then it's automatically the best
			// so far
			// OR
			// compare this confidence to the bestShape confidence
			// > 0 if shape has a higher confidence than bestShape
			if (bestShape == null || m_confComp.compare(shape, bestShape) > 0) {
				bestShape = shape;
			}
		}

		return bestShape;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionResult#sortNBestList()
	 */
	public void sortNBestList() {
		Collections.sort(m_nBestList, m_confComp);
		// m_nbest is now in ascending, so reverse it so that the first element
		// is highest and the list is descending order
		Collections.reverse(m_nBestList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;

		if (obj instanceof IRecognitionResult) {
			if (this == obj) {
				ret = true;
			} else {
				IRecognitionResult otherRes = (IRecognitionResult) obj;
				ret = this.getID().equals(otherRes.getID());
			}
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getID().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(IRecognitionResult o) {
		int ret = 1;

		// if o is null, then ret == 1 and we're always greater than null
		if (o != null) {
			ret = this.getID().compareTo(o.getID());
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Recognition results:: id=" + m_id + " , nbest="
				+ m_nBestList.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionResult#trimToNInterpretations(int)
	 */
	public void trimToNInterpretations(int n) {
		sortNBestList();
		// we only need to trim if there are more elements in the n-best list
		// than requested.
		if (m_nBestList != null && m_nBestList.size() > n) {
			m_nBestList = m_nBestList.subList(0, n);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionResult#normalizeConfidences()
	 */
	public void normalizeConfidences() {
		double confidenceSum = 0;
		// loop once to sum
		for (Shape shape : m_nBestList) {
			Double conf = shape.getInterpretation().confidence;
			if (conf != null && conf > 0) {
				confidenceSum += conf.doubleValue();
			} else {
				shape.getInterpretation().confidence = 0.0;
			}
		}

		// loop again to set the new confidence values
		// only set if there were confidences to begin with, else we'll get
		// divide by zeroes.
		if (confidenceSum > 0) {
			for (Shape shape : m_nBestList) {
				double newConf = shape.getInterpretation().confidence
						/ confidenceSum;
				shape.getInterpretation().confidence = newConf;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionResult#getLockedShape()
	 */
	public Shape getLockedShape() {

		for (Shape shape : m_nBestList) {
			if (shape.getAttribute("locked") == "true") {
				return shape;
			}
		}

		return null;
	}
}
