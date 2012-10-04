/*
 * $Id: MSTrainingWriter.java,v 1.2 2003/07/28 21:13:21 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package org.hhreco.recognition;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import org.hhreco.util.ModelWriter;

/**
 * MSTrainingWriter (Multi-Stroke Training Writer) takes a MSTrainingModel and
 * outputs its content to an outputstream.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class MSTrainingWriter implements ModelWriter {

	/**
	 * Write the training model to the output stream. The caller is responsible
	 * for closing the stream.
	 */
	public void writeModel(Object m, OutputStream out) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(out));
		writeModel(m, writer);
		writer.flush();
		writer.close();
	}

	/**
	 * Write the training model to the character-output stream. The caller is
	 * responsible for closing the stream.
	 */
	public void writeModel(Object m, Writer writer) throws IOException {
		MSTrainingModel model = (MSTrainingModel) m;
		writeHeader(writer);
		writer.write("<" + MSTrainingParser.MODEL_TAG + ">\n");
		for (Iterator i = model.types(); i.hasNext();) {
			String type = (String) i.next();
			writer.write("<" + MSTrainingParser.TYPE_TAG + " "
					+ MSTrainingParser.NAME_TAG + "=\"");
			writer.write(type);
			writer.write("\">\n");
			if (model.positiveExampleCount(type) > 0) {
				for (Iterator pi = model.positiveExamples(type); pi.hasNext();) {
					TimedStroke strokes[] = (TimedStroke[]) pi.next();
					writeExample(strokes, MSTrainingModel.POSITIVE, writer);
				}
			}
			if (model.negativeExampleCount(type) > 0) {
				for (Iterator ni = model.negativeExamples(type); ni.hasNext();) {
					TimedStroke strokes[] = (TimedStroke[]) ni.next();
					writeExample(strokes, MSTrainingModel.NEGATIVE, writer);
				}
			}
			writer.write("</" + MSTrainingParser.TYPE_TAG + ">\n");
		}
		writer.write("</" + MSTrainingParser.MODEL_TAG + ">\n");
	}

	/**
	 * Write the stroke information (x, y, timestamp) and its label (indicating
	 * either positive or negative example) to the character-output stream.
	 */
	public static void writeExample(TimedStroke strokes[], boolean label,
			Writer writer) throws IOException {
		String lbl = (label) ? "+" : "-";
		int numStrokes = strokes.length;
		writer.write("<" + MSTrainingParser.EXAMPLE_TAG + " "
				+ MSTrainingParser.LABEL_TAG + "=\"");
		writer.write(lbl);
		writer.write("\" numStrokes=\"" + numStrokes + "\">\n");

		for (int i = 0; i < numStrokes; i++) {
			writer.write("\t<" + MSTrainingParser.STROKE_TAG + " "
					+ MSTrainingParser.POINTS_TAG + "=\"");
			writeStroke(strokes[i], writer);
			writer.write("\"/>\n");
		}
		writer.write("</" + MSTrainingParser.EXAMPLE_TAG + ">\n");
	}

	/**
	 * Write out the sequence of points in the stroke. This includes the x, y,
	 * and timestamp information of a point.
	 */
	public static void writeStroke(TimedStroke s, Writer writer)
			throws IOException {
		int len = s.getVertexCount();
		for (int k = 0; k < len; k++) {
			double x = s.getX(k);
			String xs = String.valueOf(x);
			writer.write(xs);
			writer.write(" ");
			double y = s.getY(k);
			String ys = String.valueOf(y);
			writer.write(ys);
			writer.write(" ");
			long t = s.getTimestamp(k);
			String ts = String.valueOf(t);
			writer.write(ts);
			if (k != (len - 1)) {
				writer.write(" ");
			}
		}
	}

	/**
	 * Write header information to the character-output stream.
	 */
	public static void writeHeader(Writer writer) throws IOException {
		writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
		writer.write("<!DOCTYPE " + MSTrainingParser.MODEL_TAG + " PUBLIC \""
				+ MSTrainingParser.PUBLIC_ID + "\"\n\t\""
				+ MSTrainingParser.DTD_URL + "\">\n\n");
	}
}
