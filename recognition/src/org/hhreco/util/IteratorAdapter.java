/*
 * $Id: IteratorAdapter.java,v 1.1 2003/06/01 17:15:59 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.util;

import java.util.Iterator;

/**
 * An iterator that implements the Iterator, intended for subclassing so that
 * you don't have to provide the remove() method all the time....
 * 
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 */
public class IteratorAdapter implements Iterator {
	public boolean hasNext() {
		throw new UnsupportedOperationException(
				"This method must be overridden");
	}

	public Object next() {
		throw new UnsupportedOperationException(
				"This method must be overridden");
	}

	public void remove() {
		throw new UnsupportedOperationException("Can't remove element");
	}
}
