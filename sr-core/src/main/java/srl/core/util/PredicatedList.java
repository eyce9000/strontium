/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2011 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
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
package srl.core.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PredicatedList<E> implements List<E> {

	List<E> orig;
	Predicate<E> pred;

	/**
	 * Create a predicated list
	 * 
	 * @param orig
	 * @param pred
	 */
	public PredicatedList(List<E> orig, Predicate<E> pred) {
		this.orig = orig;
		this.pred = pred;
	}

	/**
	 * get the original list
	 * 
	 * @return
	 */
	public List<E> getOrig() {
		return orig;
	}

	@Override
	public boolean add(E e) {
		return orig.add(e);
	}

	@Override
	public void add(int index, E element) {
		orig.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return addAll(index, c);
	}

	@Override
	public void clear() {
		orig.removeAll(this);
	}

	@Override
	public boolean contains(Object o) {
		if (o == null) {
			if (!pred.apply(null))
				return false;
			for (E x : this)
				if (x == null)
					return true;
		} else {
			for (E x : this)
				if (o.equals(x))
					return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c)
			if (!contains(o))
				return false;
		return true;
	}

	@Override
	public E get(int index) {
		if (index < 0)
			return orig.get(index);
		for (E e : this) {
			if (index-- == 0)
				return e;
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public int indexOf(Object o) {
		int i = 0;
		if (o == null) {
			if (!pred.apply(null))
				return -1;
			for (E e : this) {
				if (e == null)
					return i;
				++i;
			}
		} else {
			for (E e : this) {
				if (o.equals(e))
					return i;
				++i;
			}
		}

		return -1;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o == null) {
			if (!pred.apply(null))
				return -1;
			int s = orig.size();
			int idx = 0;
			int res = -1;
			for (int i = 0; i < s; ++i) {
				E e = orig.get(i);
				if (pred.apply(e)) {
					if (e == null)
						res = idx;
					++idx;
				}
			}
			return res;
		} else {
			int s = orig.size();
			int idx = 0;
			int res = -1;
			for (int i = 0; i < s; ++i) {
				E e = orig.get(i);
				if (pred.apply(e)) {
					if (o.equals(e))
						res = idx;
					++idx;
				}
			}
			return res;
		}
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		final ListIterator<E> it = orig.listIterator();

		return new ListIterator<E>() {

			@Override
			public void add(E e) {
				it.add(e);
			}

			@Override
			public boolean hasNext() {
				return nextIndex() < orig.size();
			}

			@Override
			public boolean hasPrevious() {
				return previousIndex() >= 0;
			}

			@Override
			public E next() {
				E n = it.next();

				while (!pred.apply(n))
					n = it.next();

				return n;
			}

			@Override
			public int nextIndex() {
				int idx = it.nextIndex();
				int s = orig.size();
				while (idx < s) {
					if (pred.apply(orig.get(idx)))
						return idx;
					++idx;
				}
				return s;
			}

			@Override
			public E previous() {
				E n = it.previous();

				while (!pred.apply(n))
					n = it.previous();

				return n;
			}

			@Override
			public int previousIndex() {
				int idx = it.previousIndex();
				int s = orig.size();
				while (idx >= 0) {
					if (pred.apply(orig.get(idx)))
						return idx;
					--idx;
				}
				return s;
			}

			@Override
			public void remove() {
				it.remove();
			}

			@Override
			public void set(E e) {
				it.set(e);
			}

		};
	}

	@Override
	public boolean remove(Object o) {
		return orig.remove(o);
	}

	@Override
	public E remove(int index) {
		return orig.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return orig.remove(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return orig.retainAll(c);
	}

	@Override
	public E set(int index, E element) {
		return orig.set(index, element);
	}

	@Override
	public int size() {
		int r = 0;
		int s = orig.size();
		for (int i = 0; i < s; ++i) {
			if (pred.apply(orig.get(i)))
				++r;
		}
		return r;
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new PredicatedList<E>(orig.subList(fromIndex, toIndex), pred);
	}

	@Override
	public Object[] toArray() {
		return toArray(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		int s = size();
		Object[] aa = a;
		if (a == null || a.length < s) {
			aa = (T[]) new Object[s];
		}

		int r = 0;
		int ss = orig.size();
		for (int i = 0; i < ss; ++i) {
			E oi = orig.get(i);
			if (pred.apply(oi))
				aa[r++] = oi;
		}
		return (T[]) aa;
	}

}
