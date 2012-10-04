/*
 * $Id: ModelParser.java,v 1.1 2003/06/01 17:15:59 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.util;

import java.io.Reader;

/**
 * ModelParser is an interface that should be extended by application specific
 * model parsers. It's job is to parse data into an application specific data
 * structure.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public interface ModelParser {
	/**
	 * Parse the data in the given charater stream into a data structure and
	 * return the data structure.
	 */
	public Object parse(Reader reader) throws java.lang.Exception;
}
