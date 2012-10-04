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