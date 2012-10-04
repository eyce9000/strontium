/*
 * $Id: ModelWriter.java,v 1.1 2003/06/01 17:16:00 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.util;

import java.io.IOException;
import java.io.Writer;

/**
 * ModelWriter is an interface that should be extended by application specific
 * model writers to write out data structures to an output stream. For example,
 * hhreco.recognition.MSTrainingWriter extends this class and write out a
 * training set of sketched data.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public interface ModelWriter {

	/**
	 * Write the given model to the character stream.
	 */
	public void writeModel(Object model, Writer writer) throws IOException;
}
