/*
 * $Id: MergeFiles.java,v 1.1 2003/10/03 02:10:37 hwawen Exp $
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package hhreco.apps;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;

import org.hhreco.recognition.MSTrainingModel;
import org.hhreco.recognition.MSTrainingParser;
import org.hhreco.recognition.MSTrainingWriter;
import org.hhreco.recognition.TimedStroke;

/**
 * Merge given SML files into one single file.
 * 
 * @author Heloise Hse (hwawen@eecs.berkeley.edu)
 */
public class MergeFiles {
	/**
	 * Usage: java hhreco.apps.MergeFiles in1 in2 ... out
	 * <p>
	 * 
	 * The first n-1 arguments (e.g. in1, in2, ...) are the files to be merged,
	 * and the last argument (e.g. out) is the name of the final output file.
	 */
	public static void main(String argv[]) {
		if (argv.length < 3) {
			System.out
					.println("Provide at least 2 files to merge, and 1 output file name");
			System.out.println("train1.sml train2.sml output.sml");
			System.exit(0);
		}

		try {
			MSTrainingModel merged_model = new MSTrainingModel();
			for (int i = 0; i < argv.length - 1; i++) {// data files to merge
														// together
				BufferedReader reader = new BufferedReader(new FileReader(
						argv[i]));
				MSTrainingParser parser = new MSTrainingParser();
				MSTrainingModel model = (MSTrainingModel) parser.parse(reader);
				for (Iterator iter = model.types(); iter.hasNext();) {
					String type = (String) iter.next();
					for (Iterator ex = model.positiveExamples(type); ex
							.hasNext();) {
						TimedStroke orig[] = (TimedStroke[]) ex.next();
						merged_model.addPositiveExample(type, orig);
					}
				}
			}
			String out_filename = argv[argv.length - 1];
			MSTrainingWriter writer = new MSTrainingWriter();
			BufferedWriter out = new BufferedWriter(
					new FileWriter(out_filename));
			writer.writeModel(merged_model, out);
			System.out.println("Merged data into " + out_filename);
			System.out.println(merged_model);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
