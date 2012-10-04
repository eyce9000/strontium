/*
 * $Id: StrokeFilter.java,v 1.1 2003/06/01 17:15:58 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.toolbox;

import org.hhreco.recognition.TimedStroke;

/**
 * An object which filters a pen stroke in order to reduce the complexity in the
 * raw data points. Examples of filtering are point reduction, dehooking,
 * smoothing, etc.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public abstract class StrokeFilter {
	/**
	 * Apply a filtering algorithm on the specified pen stroke and return the
	 * filtered stroke.
	 */
	public abstract TimedStroke apply(TimedStroke s);
}
