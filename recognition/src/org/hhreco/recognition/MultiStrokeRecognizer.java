/*
 * $Id: MultiStrokeRecognizer.java,v 1.3 2003/08/06 05:49:08 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;


/**
 * A recognizer that processes a set of strokes and return the predictions in a
 * RecognitionSet.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public interface MultiStrokeRecognizer extends StrokeRecognizer {
	/**
	 * Perform recognition on a set of strokes and return the recognition result
	 * in a ReconitionSet.
	 */
	public RecognitionSet sessionCompleted(TimedStroke strokes[]);
}
