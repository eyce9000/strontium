/**
 * HandwritingInterpretations.java
 * 
 * Revision History:<br>
 * Feb 2, 2009 bde - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
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
 * 
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

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Shape;


/**
 * HandwritingInterpretations functions as the dictionary of possible words in
 * our domain. It also allows us to represent certain shapes (filled-triangle)
 * as text. Since text recognition is a vision based recognizer certain shapes
 * that we have difficulty recognizing with our traditional approach.
 * 
 * @author bde
 * 
 */
public class HandwritingInterpretations {

	/**
	 * 
	 */
	HashMap<String, Double> characterRecognitionMap = new HashMap<String, Double>();

	private static HashMap<String, Integer> s_numStrokesPerChar = new HashMap<String, Integer>();

	/**
	 * Dictionary, set of all the text words used in the domain
	 */
	Set<String> m_dictionary = new TreeSet<String>();

	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory.getLogger(HandwritingInterpretations.class);

	public HandwritingInterpretations(HWRType type) {
		switch (type) {
		case INNER:
			makeDictionary();
			break;
		case ECHELON:
			makeEchelonDictionary();
			break;
		case DECISIONGRAPHIC:
			makeDecisionGraphicDictionary();
			break;
		case UNIQUEDESIGNATOR:
			makeUniqueDesignatorDictionary();
			break;
		case ARROW:
			makeArrowDictionary();
			break;
		case CIVIL:
			makeCivilDictionary();
			makeCivilNumStrokesPerChar();
			break;
		}
	}

	/**
	 * Builds the dictionary for decision graphics
	 */
	private void makeDecisionGraphicDictionary() {

		m_dictionary.clear();

		m_dictionary.add("@");

		m_dictionary.add("^");

		m_dictionary.add("#");

		m_dictionary.add("&");

	}

	/**
	 * Builds the dictionary for the echelons
	 */
	private void makeEchelonDictionary() {
		log.debug("Outside Directory Being Made");

		m_dictionary.clear();

		// Company Modifier
		m_dictionary.add("1");

		// Batallion Modifier
		m_dictionary.add("11");

		// Brigade Modifier
		m_dictionary.add("X");

		// Platoon Modifier
		m_dictionary.add("***");

		// Reinforced Modifier
		m_dictionary.add("+");

		// Reduced Modifier
		m_dictionary.add("-");

		// We are having trouble grouping the platoon
		m_dictionary.add("*");

	}

	private void makeUniqueDesignatorDictionary() {

		m_dictionary.clear();

		m_dictionary.add("1");
		m_dictionary.add("1-1");
		m_dictionary.add("1-2");
		m_dictionary.add("1-28");
		m_dictionary.add("1-3");
		m_dictionary.add("1-4");
		m_dictionary.add("1-5");
		m_dictionary.add("2");
		m_dictionary.add("2-1");
		m_dictionary.add("2-2");
		m_dictionary.add("2-28");
		m_dictionary.add("2-3");
		m_dictionary.add("2-4");
		m_dictionary.add("3");
		m_dictionary.add("3-1");
		m_dictionary.add("3-2");
		m_dictionary.add("3-3");
		m_dictionary.add("4");
		m_dictionary.add("7");
		m_dictionary.add("7-8");
		m_dictionary.add("8-1");
		m_dictionary.add("8-2");
		m_dictionary.add("8-3");
		m_dictionary.add("90");

	}

	/**
	 * A dictionary of CivilSketch words.
	 */
	private void makeCivilDictionary() {
		m_dictionary.clear();

		// Roman
		m_dictionary.add("F");
		m_dictionary.add("M");
		m_dictionary.add("X");
		m_dictionary.add("Y");

		// Greek letters
		m_dictionary.add("alpha"); // α
		m_dictionary.add("beta"); // β
		m_dictionary.add("gamma"); // γ
		m_dictionary.add("theta"); // θ

		// Numbers
		m_dictionary.add("0");
		m_dictionary.add("1");
		m_dictionary.add("2");
		m_dictionary.add("3");
		m_dictionary.add("4");
		m_dictionary.add("5");
		m_dictionary.add("6");
		m_dictionary.add("7");
		m_dictionary.add("8");
		m_dictionary.add("9");

		// Symbols
		m_dictionary.add("period"); // .
		m_dictionary.add("equals"); // =
	}

	/**
	 * A dictionary of CivilSketch words.
	 */
	public static int approximateStrokes(String character) {

		if (s_numStrokesPerChar.containsKey(character)) {
			return s_numStrokesPerChar.get(character);
		} else {
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * A dictionary for COA arrows.
	 */
	private void makeArrowDictionary() {

		m_dictionary.clear();
		log.debug("Arrow Dictionary Being Made");

		m_dictionary.add("CATK");

		m_dictionary.add("S");
		m_dictionary.add("C");
		m_dictionary.add("G");
		m_dictionary.add("W");
	}

	private void makeDictionary() {

		m_dictionary.clear();

		log.debug("Inside Directory Being Made");
		m_dictionary.add("MAIN");
		m_dictionary.add("BDOC");
		m_dictionary.add("CTCP");
		m_dictionary.add("ASLT");
		m_dictionary.add("BLUE");
		m_dictionary.add("NAI");
		m_dictionary.add("TAI");
		m_dictionary.add("OBJ");
		m_dictionary.add("ATK");
		m_dictionary.add("CKP");
		m_dictionary.add("JTF");
		m_dictionary.add("TAC");
		m_dictionary.add("TOC");
		m_dictionary.add("FSB");
		m_dictionary.add("CAV");
		m_dictionary.add("ACP");
		m_dictionary.add("CCP");
		m_dictionary.add("RFA");
		m_dictionary.add("ACA");
		m_dictionary.add("NFA");
		m_dictionary.add("PSN");
		m_dictionary.add("F");
		m_dictionary.add("L");
		m_dictionary.add("H");
		m_dictionary.add("U");
		m_dictionary.add("A");
		m_dictionary.add("B");
		m_dictionary.add("M");
		m_dictionary.add("X");
		m_dictionary.add("EA");
		m_dictionary.add("AA");
		m_dictionary.add("SP");
		m_dictionary.add("RP");
		m_dictionary.add("PP");
		m_dictionary.add("PD");
		m_dictionary.add("CA");
		m_dictionary.add("SF");
		m_dictionary.add("MP");
		m_dictionary.add("MI");
		m_dictionary.add("LZ");
		m_dictionary.add("DZ");
		m_dictionary.add("AG");
		m_dictionary.add("PL");
		m_dictionary.add("ALEX");
		m_dictionary.add("BLACK");
		m_dictionary.add("BORIS");
		m_dictionary.add("BOXER");
		m_dictionary.add("BROTHER");
		m_dictionary.add("BULLDOG");
		m_dictionary.add("CHIEF");
		m_dictionary.add("CHIP");
		m_dictionary.add("CHOW");
		m_dictionary.add("CHUCK");
		m_dictionary.add("CURT");
		m_dictionary.add("CZAR");
		m_dictionary.add("DIRK");
		m_dictionary.add("DOM");
		m_dictionary.add("DOUG");
		m_dictionary.add("GARY");
		m_dictionary.add("GOLD");
		m_dictionary.add("GREEN");
		m_dictionary.add("HOUND");
		m_dictionary.add("IGOR");
		m_dictionary.add("IVAN");
		m_dictionary.add("JIM");
		m_dictionary.add("JOHN");
		m_dictionary.add("JOHN2");
		m_dictionary.add("KARL");
		m_dictionary.add("MARG");
		m_dictionary.add("MARY");
		m_dictionary.add("MAX");
		m_dictionary.add("MIKE");
		m_dictionary.add("MOE");
		m_dictionary.add("MUTT");
		m_dictionary.add("NATASHA");
		m_dictionary.add("ODIN");
		m_dictionary.add("OLAF");
		m_dictionary.add("OLEG");
		m_dictionary.add("OSCAR");
		m_dictionary.add("OWEN");
		m_dictionary.add("PEKE");
		m_dictionary.add("PETE");
		m_dictionary.add("PETE2");
		m_dictionary.add("PETER");
		m_dictionary.add("PIT");
		m_dictionary.add("PUG");
		m_dictionary.add("RED");
		m_dictionary.add("SABER");
		m_dictionary.add("SADDLE");
		m_dictionary.add("SCARF");
		m_dictionary.add("SPUR");
		m_dictionary.add("SUSAN");
		m_dictionary.add("TANK");
		m_dictionary.add("TODD");
		m_dictionary.add("WHITE");
		m_dictionary.add("YELLOW");

		m_dictionary.add("FPF");
		m_dictionary.add("FSCL");
		m_dictionary.add("CFL");
		m_dictionary.add("LOA");

		m_dictionary.add("LOC");

		m_dictionary.add("FINALCL");
		m_dictionary.add("FINAL");
		m_dictionary.add("CL");
		m_dictionary.add("LDLC");

		m_dictionary.add("0");
		m_dictionary.add("1");
		m_dictionary.add("2");
		m_dictionary.add("3");
		m_dictionary.add("4");
		m_dictionary.add("5");
		m_dictionary.add("6");
		m_dictionary.add("7");
		m_dictionary.add("8");
		m_dictionary.add("9");
		m_dictionary.add("10");
		m_dictionary.add("101");

		m_dictionary.add("CATK");
		m_dictionary.add("S");
		m_dictionary.add("C");
		m_dictionary.add("G");
		m_dictionary.add("W");
	}

	/**
	 * Create a map storing the maximum number of strokes a character could be
	 * drawn in. This is based on observation/intuition.
	 */
	private void makeCivilNumStrokesPerChar() {

		s_numStrokesPerChar = new HashMap<String, Integer>();

		// Roman
		s_numStrokesPerChar.put("F", 3);
		s_numStrokesPerChar.put("M", 5);
		s_numStrokesPerChar.put("X", 2);
		s_numStrokesPerChar.put("Y", 3);

		// Greek letters
		s_numStrokesPerChar.put("alpha", 1); // α
		s_numStrokesPerChar.put("beta", 2); // β
		s_numStrokesPerChar.put("gamma", 1); // γ
		s_numStrokesPerChar.put("theta", 1); // θ

		// Numbers
		s_numStrokesPerChar.put("0", 1);
		s_numStrokesPerChar.put("1", 3);
		s_numStrokesPerChar.put("2", 2);
		s_numStrokesPerChar.put("3", 1);
		s_numStrokesPerChar.put("4", 2);
		s_numStrokesPerChar.put("5", 2);
		s_numStrokesPerChar.put("6", 2);
		s_numStrokesPerChar.put("7", 3);
		s_numStrokesPerChar.put("8", 1);
		s_numStrokesPerChar.put("9", 2);

		// Symbols
		s_numStrokesPerChar.put("period", 1); // .
		s_numStrokesPerChar.put("equals", 2); // =

	}

	/**
	 * Submit the character to the interpretation.
	 * 
	 * @param characters
	 *            characters to add to the interpretation.
	 */
	public void submitCharacters(CharacterGroup characters) {
		for (String word : m_dictionary) {

			// Simplify the word
			double newconf = characters.getConfidence(simplifyWord(word));
			Double d = characterRecognitionMap.get(word);
			if (d == null || d < newconf) {
				characterRecognitionMap.put(word, newconf);
			}
		}
	}

	/**
	 * This method takes into consideration the fact that 0 and 1 are often
	 * misrecognized as O and I, respectively. This method thus simplifies the
	 * word. Our model, as is, does not contain the letter 'O' or "I".
	 * 
	 * @param word
	 * @return
	 */
	private String simplifyWord(String word) {

		if (word.indexOf('I') != -1 || word.indexOf('O') != -1) {

			char[] characters = word.toCharArray();

			String outgoingWord = "";

			for (int i = 0; i < characters.length; i++) {

				if (characters[i] == 'I') {
					outgoingWord += "1";
				} else if (characters[i] == 'O') {
					outgoingWord += "0";

				} else {
					outgoingWord += String.valueOf(characters[i]);
				}

			}

			log.debug("Translating " + word + " to " + outgoingWord);

			return outgoingWord;
		}

		else {
			return word;
		}
	}

	public void printBestOverall() {
		String bestword = null;
		double confidence = -1;
		for (String word : m_dictionary) {
			if (confidence < characterRecognitionMap.get(word)) {
				confidence = characterRecognitionMap.get(word);
				bestword = word;
			}
		}
		System.out.println("best = " + bestword + " at " + confidence);
		m_bestWord = bestword;
	}

	@SuppressWarnings("unused")
	private String m_bestWord = "";

	public void printBest() {
		for (String word : m_dictionary) {
			System.out.println("best " + word + " = "
					+ characterRecognitionMap.get(word));
		}
	}

	public void setAttributes(Shape shape) {
		String bestword = null;
		double confidence = -1;
		for (String word : m_dictionary) {
			shape.setAttribute(word, String.valueOf(characterRecognitionMap
					.get(word)));

			if (confidence < characterRecognitionMap.get(word)) {
				confidence = characterRecognitionMap.get(word);
				bestword = word;
			}
		}

		log.debug("Best Word " + bestword + " Confidence "
				+ characterRecognitionMap.get(bestword));

		// Translate the decision graphics to a more easily readable string.

		if (bestword.startsWith("@") || bestword.startsWith("^")
				|| bestword.startsWith("#") || bestword.startsWith("&")) {
			log
					.debug("Should I return filled-square or filled-triangle instead?");
			if (bestword.startsWith("@")) {
				shape.setAttribute("TEXT_BEST", "filled_square");
				shape.setAttribute("filled_square", shape.getAttribute("@"));
			} else if (bestword.startsWith("^")) {
				shape.setAttribute("TEXT_BEST", "filled_triangle");
				shape.setAttribute("filled_triangle", shape.getAttribute("^"));
			} else if (bestword.startsWith("#")) {
				shape.setAttribute("TEXT_BEST", "unfilled_square");
				shape.setAttribute("unfilled_square", shape.getAttribute("#"));
			} else if (bestword.startsWith("&")) {
				shape.setAttribute("TEXT_BEST", "unfilled_triangle");
				shape
						.setAttribute("unfilled_triangle", shape
								.getAttribute("&"));
			}

		} else {
			shape.setAttribute("TEXT_BEST", bestword);
		}
	}

}
