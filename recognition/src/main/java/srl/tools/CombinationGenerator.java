/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2012 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&M University 
 *        nor the names of its contributors may be used to endorse or promote 
 *        products derived from this software without specific prior written 
 *        permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *  
 *******************************************************************************/
package srl.tools;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Generate combinations of the sequence [0,1,2,...,n-1] in sorted order
 *
 */
public class CombinationGenerator implements Iterator<int[]>, Iterable<int[]> {
	private int[] a = null;
	private int n;
	private int r;

	/**
	 * N is the size of the set, R is the size of the combinations to find
	 * @param n
	 * @param r
	 */
	public CombinationGenerator(int n, int r) {
		if (r > n) {
			throw new IllegalArgumentException("can't choose more elements than there are, you might want multichoose...");
		}
		if (n < 1) {
			throw new IllegalArgumentException("can't take combinations of an empty set");
		}
		if (r < 1) {
			throw new IllegalArgumentException("if you want an empty list, look elsewhere");
		}
		this.n = n;
		this.r = r;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("this just doesn't make sense");
	}
	
	public void initialize() {
		a = new int[r];
		for (int i = 0; i < r; i++) {
			a[i] = i;
		}
	}
	
	/**
	 * Check if the last combination generated was [n-r, n-r-1, ..., n-1]
	 */
	@Override
	public boolean hasNext()
	{
		if (a == null) return true;
		int index = 0;
		for (int i = n-r; i < n; ++i, ++index)
		{
			if (a[index] != i) return true;
		}
		return false;
	}

	/**
	 * Generate and return the next combination
	 */
	@Override
	public int[] next() {
		if (a == null) {
			initialize();
			return a;
		}
		
		// find the first index that isn't as large as it could be
		int i = r - 1;
		while (a[i] == n - r + i) {
			i--;
		}
		
		// make it larger
		a[i] = a[i] + 1;
		
		// make everything after it larger to match
		for (int j = i + 1; j < r; j++) {
			a[j] = a[i] + j - i;
		}
		
		return a;
	}
	
	public static void main(String[] args)
	{
		for (int[] i: new CombinationGenerator(10, 2))
		{
			System.out.println(Arrays.toString(i));
		}
	}

	@Override
	public Iterator<int[]> iterator() {
		return this;
	}
}