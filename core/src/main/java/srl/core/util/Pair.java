/**
 * Pair.java
 * 
 * Revision History:<br>
 * Jan 22, 2009 jbjohns - File created
 *
 * <p>
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&M University 
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
package srl.core.util;

/**
 * A simple pair of two objects, a first (of type E) and last (of type F) object
 * 
 * @param <TFIRST>
 *            Type of the first object in the pair
 * @param <TLAST>
 *            Type of the second object in the pair
 * 
 * @author jbjohns
 */
public class Pair<TFIRST, TLAST> {

	/**
	 * The first object in the pair
	 */
	private TFIRST m_first;

	/**
	 * The last object in the pair
	 */
	private TLAST m_last;

	/**
	 * Create a pair of objects
	 * 
	 * @param first
	 *            The first object in the pair, CAN be null
	 * @param last
	 *            The last object in the pair, CAN be null
	 */
	public Pair(TFIRST first, TLAST last) {
		m_first = first;
		m_last = last;
	}

	/**
	 * Get the first object in the pair
	 * 
	 * @return the first object in the pair
	 */
	public TFIRST getFirst() {
		return this.m_first;
	}

	/**
	 * Get the last object in the pair
	 * 
	 * @return the last object in the pair
	 */
	public TLAST getLast() {
		return this.m_last;
	}

}
