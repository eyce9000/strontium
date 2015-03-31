/**
 * BuildTargetAttribute.java
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

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

/**
 * This class is needed due to our use of WEKA. It builds the dataset
 * description, and the target attribute.
 * 
 * @author bde
 * 
 */
public class BuildTargetAttribute {

	/**
	 * Number of pixels in the standardized square's width and length
	 */
	private static int PIXEL_COUNT = 11;

	/**
	 * Builds the uppercase target attribute, used by handwriting recognizer
	 * 
	 * @return
	 */

	public static Attribute buildDecisionGraphicAttribute() {
		FastVector fw = new FastVector();

		fw.addElement("@");
		fw.addElement("^");
		fw.addElement("#");
		fw.addElement("&");

		Attribute dg = new Attribute("CapitalLetter", fw);

		return dg;
	}

	public static Instances createDecisionGraphicDataSet() {
		FastVector classValues = new FastVector();

		classValues.addElement("@");
		classValues.addElement("^");
		classValues.addElement("#");
		classValues.addElement("&");

		Attribute ClassAttribute = new Attribute("letter", classValues);

		FastVector fvInstancesAttribute = new FastVector(53);

		Attribute attributeCount = new Attribute("StrokeCount");

		fvInstancesAttribute.addElement(attributeCount);

		Attribute bbRatio = new Attribute("BoundingBoxRatio");

		fvInstancesAttribute.addElement(bbRatio);

		for (int i = 0; i <= Math.pow((PIXEL_COUNT + 1), 2) - 1; i++) {
			fvInstancesAttribute.addElement(new Attribute("Pixel"
					+ String.valueOf(i)));
		}

		fvInstancesAttribute.addElement(ClassAttribute);

		Instances dataSet = new Instances("data", fvInstancesAttribute, 10);

		dataSet.setClassIndex(dataSet.numAttributes() - 1);

		return dataSet;
	}

	public static Attribute buildEchelonAttribute() {
		// I will have to adjust this, and remove letters.
		// Maybe I can make this read from a file? Not change by hand?

		FastVector fw = new FastVector();

		fw.addElement("X");
		fw.addElement("1");
		fw.addElement("-");
		fw.addElement("+");
		fw.addElement("*");

		Attribute echelon = new Attribute("CapitalLetter", fw);

		return echelon;

	}

	public static Instances createEchelonDataSet() {
		FastVector classValues = new FastVector(26);

		classValues.addElement("X");
		classValues.addElement("1");
		classValues.addElement("-");
		classValues.addElement("+");
		classValues.addElement("*");

		Attribute ClassAttribute = new Attribute("letter", classValues);

		FastVector fvInstancesAttribute = new FastVector(53);

		Attribute attributeCount = new Attribute("StrokeCount");

		fvInstancesAttribute.addElement(attributeCount);

		Attribute bbRatio = new Attribute("BoundingBoxRatio");

		fvInstancesAttribute.addElement(bbRatio);

		for (int i = 0; i <= Math.pow((PIXEL_COUNT + 1), 2) - 1; i++) {
			fvInstancesAttribute.addElement(new Attribute("Pixel"
					+ String.valueOf(i)));
		}

		fvInstancesAttribute.addElement(ClassAttribute);

		Instances dataSet = new Instances("data", fvInstancesAttribute, 10);

		dataSet.setClassIndex(dataSet.numAttributes() - 1);

		return dataSet;
	}

	public static Attribute buildUppercaseLetterAttribute() {

		// I will have to adjust this, and remove letters.
		// Maybe I can make this read from a file? Not change by hand?

		FastVector fw = new FastVector();
		fw.addElement("A");
		fw.addElement("B");
		fw.addElement("C");
		fw.addElement("D");
		fw.addElement("E");
		fw.addElement("F");
		fw.addElement("G");
		fw.addElement("H");

		// There is no longer and 'I'
		// fw.addElement("I");

		fw.addElement("J");
		fw.addElement("K");
		fw.addElement("L");
		fw.addElement("M");
		fw.addElement("N");

		// There is no longer an 'O'
		// fw.addElement("O");

		fw.addElement("P");
		fw.addElement("Q");
		fw.addElement("R");
		fw.addElement("S");
		fw.addElement("T");
		fw.addElement("U");
		fw.addElement("V");
		fw.addElement("W");
		fw.addElement("X");
		fw.addElement("Y");
		fw.addElement("Z");

		fw.addElement("0");
		fw.addElement("1");
		fw.addElement("2");
		fw.addElement("3");
		fw.addElement("4");
		fw.addElement("5");
		fw.addElement("6");
		fw.addElement("7");
		fw.addElement("8");
		fw.addElement("9");

		fw.addElement(".");

		fw.addElement("@");
		fw.addElement("^");
		fw.addElement("#");
		fw.addElement("&");

		fw.addElement("+");
		fw.addElement("-");

		Attribute letter = new Attribute("CapitalLetter", fw);

		return letter;

	}

	/**
	 * Creates the Instances, which is needed to give an instance a frame of
	 * reference
	 * 
	 * @return
	 */
	public static Instances createInstancesDataSet() {

		FastVector classValues = new FastVector(26);
		classValues.addElement("A");
		classValues.addElement("B");
		classValues.addElement("C");
		classValues.addElement("D");
		classValues.addElement("E");
		classValues.addElement("F");
		classValues.addElement("G");
		classValues.addElement("H");

		// There is no longer an 'I'
		// classValues.addElement("I");

		classValues.addElement("J");
		classValues.addElement("K");
		classValues.addElement("L");
		classValues.addElement("M");
		classValues.addElement("N");

		// There is no longer an 'O'
		// classValues.addElement("O");

		classValues.addElement("P");
		classValues.addElement("Q");
		classValues.addElement("R");
		classValues.addElement("S");
		classValues.addElement("T");
		classValues.addElement("U");
		classValues.addElement("V");
		classValues.addElement("W");
		classValues.addElement("X");
		classValues.addElement("Y");
		classValues.addElement("Z");

		classValues.addElement("0");
		classValues.addElement("1");
		classValues.addElement("2");
		classValues.addElement("3");
		classValues.addElement("4");
		classValues.addElement("5");
		classValues.addElement("6");
		classValues.addElement("7");
		classValues.addElement("8");
		classValues.addElement("9");

		classValues.addElement(".");

		classValues.addElement("@");
		classValues.addElement("^");
		classValues.addElement("#");
		classValues.addElement("&");

		classValues.addElement("+");
		classValues.addElement("-");

		Attribute ClassAttribute = new Attribute("letter", classValues);

		FastVector fvInstancesAttribute = new FastVector(53);

		Attribute attributeCount = new Attribute("StrokeCount");

		fvInstancesAttribute.addElement(attributeCount);

		Attribute bbRatio = new Attribute("BoundingBoxRatio");

		fvInstancesAttribute.addElement(bbRatio);

		for (int i = 0; i <= Math.pow((PIXEL_COUNT + 1), 2) - 1; i++) {
			fvInstancesAttribute.addElement(new Attribute("Pixel"
					+ String.valueOf(i)));
		}

		fvInstancesAttribute.addElement(ClassAttribute);

		Instances dataSet = new Instances("data", fvInstancesAttribute, 10);

		dataSet.setClassIndex(dataSet.numAttributes() - 1);

		return dataSet;

	}

	/**
	 * ??
	 * 
	 * @return
	 */
	public static Attribute buildCivilAttribute() {
		// I will have to adjust this, and remove letters.
		// Maybe I can make this read from a file? Not change by hand?

		FastVector fw = new FastVector();

		// Roman
		fw.addElement("F");
		fw.addElement("M");
		fw.addElement("X");
		fw.addElement("Y");

		// Greek
		fw.addElement("alpha"); // alpha, α
		fw.addElement("beta"); // beta, β
		fw.addElement("gamma"); // gamma, γ
		fw.addElement("theta"); // theta, θ

		// Numbers
		fw.addElement("0");
		fw.addElement("1");
		fw.addElement("2");
		fw.addElement("3");
		fw.addElement("4");
		fw.addElement("5");
		fw.addElement("6");
		fw.addElement("7");
		fw.addElement("8");
		fw.addElement("9");

		// Symbols
		fw.addElement("period"); // period, .
		fw.addElement("equals"); // equals, =

		Attribute civil = new Attribute("CapitalLetter", fw);

		return civil;
	}

	/**
	 * Creates the Instances, which is needed to give an instance a frame of
	 * reference
	 * 
	 * @return
	 */
	public static Instances createCivilInstancesDataSet() {

		FastVector classValues = new FastVector();

		// Roman
		classValues.addElement("F");
		classValues.addElement("M");
		classValues.addElement("X");
		classValues.addElement("Y");

		// Greek
		classValues.addElement("alpha"); // alpha, α
		classValues.addElement("beta"); // beta, β
		classValues.addElement("gamma"); // gamma, γ
		classValues.addElement("theta"); // theta, θ

		// Numbers
		classValues.addElement("0");
		classValues.addElement("1");
		classValues.addElement("2");
		classValues.addElement("3");
		classValues.addElement("4");
		classValues.addElement("5");
		classValues.addElement("6");
		classValues.addElement("7");
		classValues.addElement("8");
		classValues.addElement("9");

		// Symbols
		classValues.addElement("period"); // period, .
		classValues.addElement("equals"); // equals, =

		Attribute classAttribute = new Attribute("letter", classValues);

		// Additional attributes
		FastVector fvInstancesAttribute = new FastVector();

		boolean newAttrs = true;
		boolean hybridAttrs = true;

		if (newAttrs || hybridAttrs) {

			// Smoothness
			Attribute smoothnessAttr = new Attribute("Smoothness");
			fvInstancesAttribute.addElement(smoothnessAttr);

			// First-last point attrs
			Attribute firstLastDistAttr = new Attribute("FirstLastDist");
			fvInstancesAttribute.addElement(firstLastDistAttr);

			Attribute firstLastCosineAttr = new Attribute("FirstLastCosine");
			fvInstancesAttribute.addElement(firstLastCosineAttr);

			Attribute firstLastSineAttr = new Attribute("FirstLastSine");
			fvInstancesAttribute.addElement(firstLastSineAttr);

			// Number of strokes
			Attribute strokeCount = new Attribute("StrokeCount");
			fvInstancesAttribute.addElement(strokeCount);

			// Bounding box ratio
			Attribute bbRatio = new Attribute("BoundingBoxRatio");
			fvInstancesAttribute.addElement(bbRatio);

			// % of pixels in left half
			Attribute leftPixels = new Attribute("LeftPixels");
			fvInstancesAttribute.addElement(leftPixels);

			// % of pixels in right half
			Attribute rightPixels = new Attribute("RightPixels");
			fvInstancesAttribute.addElement(rightPixels);

			// % of pixels in top half
			Attribute topPixels = new Attribute("TopPixels");
			fvInstancesAttribute.addElement(topPixels);

			// % of pixels in bottom half
			Attribute bottomPixels = new Attribute("BottomPixels");
			fvInstancesAttribute.addElement(bottomPixels);

			// Density
			Attribute densityAttr = new Attribute("Density");
			fvInstancesAttribute.addElement(densityAttr);

			if (hybridAttrs) {
				// Pixels
				for (int i = 0; i <= Math.pow(CivilAttributes.S_PIXELSIZE, 2) - 1; i++) {
					fvInstancesAttribute.addElement(new Attribute("Pixel"
							+ String.valueOf(i)));
				}
			}
		} else {
			// Number of strokes
			Attribute strokeCount = new Attribute("StrokeCount");
			fvInstancesAttribute.addElement(strokeCount);

			// Bounding box ratio
			Attribute bbRatio = new Attribute("BoundingBoxRatio");
			fvInstancesAttribute.addElement(bbRatio);

			// Pixels
			for (int i = 0; i <= Math.pow(CivilAttributes.S_PIXELSIZE, 2) - 1; i++) {
				fvInstancesAttribute.addElement(new Attribute("Pixel"
						+ String.valueOf(i)));
			}
		}

		// Letters, numbers, and symbols
		fvInstancesAttribute.addElement(classAttribute);

		// Create the dataset
		Instances dataSet = new Instances("data", fvInstancesAttribute, 10);
		dataSet.setClassIndex(dataSet.numAttributes() - 1);

		return dataSet;

	}

	public static FastVector buildTargetFastVector() {
		return null;
	}

}
