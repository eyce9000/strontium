/*
 * $Id: NullIterator.java,v 1.1 2003/06/01 17:16:00 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.util;

import java.util.NoSuchElementException;

/**
 * An iterator over nothing.
 * 
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 */
public class NullIterator extends IteratorAdapter {
	public boolean hasNext() {
		return false;
	}

	public Object next() {
		throw new NoSuchElementException("No more elements");
	}
}
