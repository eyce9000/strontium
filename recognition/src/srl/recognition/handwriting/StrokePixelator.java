/**
 * StrokePixelator.java
 * 
 * Revision History:<br>
 * Jan 13, 2009 bde - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written
 *       permission.
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */

package srl.recognition.handwriting;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Point;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;



import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Take a sketch stored as a series of strokes and pixelize the sketch to a
 * bitmap representation.
 * 
 * @author beoff, awolin
 */
public class StrokePixelator {

	private static Logger log = LoggerFactory.getLogger(StrokePixelator.class);

	/**
	 * Creates an int array pixel map of a sketch.
	 * 
	 * @param sk
	 *            sketch to pixelize.
	 * @param heightWidth
	 *            square height and width to scale to.
	 * @return the pixelized version of the sketch, where pixels with a value of
	 *         1 are filled, and -1 are empty.
	 */
	public static int[][] pixelizeSketch(Sketch sketch, int heightWidth) {

		heightWidth--;

		// Find the bounding box
		BoundingBox bb = sketch.getBoundingBox();

		// Width and height of the original stroke
		double width = bb.getWidth();
		double height = bb.getHeight();

		// Transpose and scale the stroke to the top left corner
		List<Point> scaledPoints = new ArrayList<Point>();
		List<Point> points = sketch.getPoints();

		for (int i = 0; i < points.size(); i++) {

			Point point = points.get(i);

			double transposedX = point.getX() - bb.getMinX();
			double transposedY = point.getY() - bb.getMinY();

			double scaledX = (transposedX / width) * heightWidth;
			double scaledY = (transposedY / height) * heightWidth;

			scaledPoints.add(new Point(scaledX, scaledY, point.getTime()));
		}

		heightWidth++;

		// Generate pixels
		int[][] pixels = new int[heightWidth][heightWidth];

		// Have a window around the pixel if we are expanding
		int xWindow = (int) ((heightWidth / width) / 2.0);
		int yWindow = (int) ((heightWidth / height) / 2.0);

		for (Point p : scaledPoints) {

			int px = (int) p.getX();
			int py = (int) p.getY();

			for (int x = Math.max(0, px - xWindow); x <= Math.min(
					heightWidth - 1, px + xWindow); x++) {

				for (int y = Math.max(0, py - yWindow); y <= Math.min(
						heightWidth - 1, py + yWindow); y++) {

					pixels[x][y] = 1;
				}
			}
		}

		return pixels;
	}

	/**
	 * Original pixelator, as written by Eoff.
	 * 
	 * @param sk
	 *            sketch to pixelize.
	 * @param heightWidth
	 *            square height and width to scale to.
	 * @return the pixelized version of the sketch, where pixels with a value of
	 *         1 are filled, and -1 are empty.
	 */
	public static int[][] pixelizeSketch_OLD(Sketch sk, int heightWidth) {

		int[][] pixels = new int[heightWidth + 1][heightWidth + 1];

		BoundingBox bb = sk.getBoundingBox();

		double skWidthHeight = Math.max(bb.height, bb.width);

		Point centerPoint = bb.getCenterPoint();

		double shiftX = 0 - (centerPoint.getX() - skWidthHeight / 2);

		double shiftY = 0 - (centerPoint.getY() - skWidthHeight / 2);

		double normalizeWidthHeight = heightWidth / skWidthHeight;

		for (Stroke st : sk.getStrokes()) {
			Stroke dbStroke = doublePoint(st);
			dbStroke = doublePoint(dbStroke);
			dbStroke = doublePoint(dbStroke);

			for (Point pt : dbStroke.getPoints()) {
				Double xValue = (pt.getX() + shiftX) * normalizeWidthHeight;
				Double yValue = (pt.getY() + shiftY) * normalizeWidthHeight;
				pixels[xValue.intValue()][yValue.intValue()] = 1;
			}
		}

		return pixels;
	}

	/**
	 * Returns a Weka instance of a sketch (using the pixelize function))
	 * 
	 * @param sk
	 * @param heightWidth
	 * @return
	 */
	public static Instance getInstance(Sketch sk, int heightWidth,
			Instances dataSet) {

		int[][] pixels = pixelizeSketch(sk, heightWidth);

		Instance inst = new DenseInstance(dataSet.numAttributes());

		inst.setDataset(dataSet);

		int count = 0;

		for (int i = 0; i < pixels.length; i++) {
			String holder = "";
			for (int j = 0; j < pixels.length; j++) {
				if (pixels[j][i] == 1)
					inst.setValue(dataSet.attribute("Pixel" + count), 1);
				else
					inst.setValue(dataSet.attribute("Pixel" + count), -1);
				holder += String.valueOf(pixels[j][i]);
				count++;
			}
			log.debug(holder);
		}
		return inst;
	}

	/**
	 * Returns a Weka instance of a sketch for CivilSketch attributes
	 * 
	 * @param sk
	 * @param heightWidth
	 * @return
	 */
	public static Instance getInstanceCivilSketch(Sketch sk, Instances dataSet) {

		Instance inst = new DenseInstance(8);
		inst.setDataset(dataSet);

		return inst;
	}

	private static Stroke doublePoint(Stroke sk) {
		Stroke doubledStroke = new Stroke();

		for (int i = 0; i < sk.getNumPoints() - 1; i++) {
			doubledStroke.addPoint(sk.getPoint(i));

			long timeOfNewPoint = (long) ((sk.getPoint(i).getTime() + sk
					.getPoint(i + 1).getTime()) / 2.0);

			double xValue = (sk.getPoint(i).getX() + sk.getPoint(i + 1).getX()) / 2.0;

			double yValue = (sk.getPoint(i).getY() + sk.getPoint(i + 1).getY()) / 2.0;

			Point pt = new Point(xValue, yValue, timeOfNewPoint);

			doubledStroke.addPoint(pt);
		}

		doubledStroke.addPoint(sk.getLastPoint());

		return doubledStroke;
	}

}
