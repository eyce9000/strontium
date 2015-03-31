/**
 * AllPossibleGroupings.java
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

import java.util.ArrayList;
import java.util.List;

import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;


/**
 * Given a max number, creates all the possible breakdowns of that number. An
 * example '8' would produce a breakdown going from '11111111' to '8'. We can
 * put limitations on it, the max number of strokes per letter and the max
 * number of letters.
 * 
 * @author bde
 * 
 */
public class AllPossibleGroupings {

	private static int MAXNUMBEROFLETTERS = 7;

	private static int MAXNUMBEROFSTROKESPERLETTER = 5;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<ArrayList<Integer>> what = computepossibiliites(8);

		printComputePossibilites(what);

	}

	private static void printComputePossibilites(
			ArrayList<ArrayList<Integer>> possibilities) {
		for (ArrayList<Integer> aLI : possibilities) {
			for (Integer i : aLI) {
				System.out.print(i);
			}
			System.out.print("\n");
		}

	}

	public static ArrayList<ArrayList<Integer>> computepossibiliites(
			int totalStrokes) {

		ArrayList<ArrayList<Integer>> combinations = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> combination = new ArrayList<Integer>();
		combinations.add(combination);
		 return recurseCandidates(combinations, totalStrokes, MAXNUMBEROFSTROKESPERLETTER ,MAXNUMBEROFLETTERS);
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<ArrayList<Integer>> recurseCandidates(
			ArrayList<ArrayList<Integer>> combinations, int totalStrokes,
			int maxNumStrokesPerLetter, int maxNumLetters) {

		ArrayList<ArrayList<Integer>> newcombinations = new ArrayList<ArrayList<Integer>>();

		boolean changed = false;

		for (ArrayList<Integer> combination : combinations) {

			// if too many characters, stop now
			if (combination.size() > maxNumLetters) {
				continue;
			}
			// how many strokes have we used so far?
			int sum = 0;
			for (Integer slot : combination) {
				sum += slot;
			}

			// if we have used the exact right combinations, keep on list and go
			// to the next combo
			if (sum == totalStrokes) {
				newcombinations.add(combination);

				continue;
			}

			// add some strokes for the next character
			for (int numStrokes = 1; numStrokes <= maxNumStrokesPerLetter; numStrokes++) {
				// if too many strokes used, not valid
				if (sum + numStrokes > totalStrokes) {
					break;
				}

				// if exact number of characters, should have exact number of
				// strokes
				if (combination.size() == maxNumLetters) {
					if (sum + numStrokes != totalStrokes) {
						continue;
					}
				}

				ArrayList<Integer> newcombination = (ArrayList<Integer>) combination
						.clone();
				newcombination.add(numStrokes);
				changed = true;
				// for(int c : newcombination){
				// System.out.print(c + " ");
				// }
				newcombinations.add(newcombination);
			}
		}

		if (changed == false) {
			return newcombinations;
		}

		return recurseCandidates(newcombinations, totalStrokes,
				maxNumStrokesPerLetter, maxNumLetters);
	}

	public static List<List<Shape>> getGroupings(List<Stroke> strokes) {
		List<List<Shape>> groupings = new ArrayList<List<Shape>>();

		ArrayList<ArrayList<Integer>> groupsByNumber = computepossibiliites(strokes
				.size());
		for (ArrayList<Integer> groupByNumber : groupsByNumber) {
			List<Shape> shapes = new ArrayList<Shape>();
			List<Stroke> tempStrokes = new ArrayList<Stroke>();
			tempStrokes.addAll(strokes);
			for (Integer number : groupByNumber) {
				Shape shape = new Shape();
				for (int i = 0; i < number.intValue(); i++) {
					shape.add(tempStrokes.get(0));
					tempStrokes.remove(0);
				}
				shapes.add(shape);
			}
			groupings.add(shapes);
		}
		return groupings;
	}

	public static List<List<Shape>> getCombinatorialGroupings(
			List<Stroke> strokes) {
		List<List<Shape>> groups = groupStartingAt(strokes, 0,
				new ArrayList<Shape>());
		return groups;
	}

	private static List<List<Shape>> groupStartingAt(List<Stroke> strokes,
			int i, ArrayList<Shape> group) {
		List<List<Shape>> groupings = new ArrayList<List<Shape>>();
		if (i >= strokes.size()) {
			groupings.add(group);
		} else {
			ArrayList<Shape> groupPlusShape = new ArrayList<Shape>();
			groupPlusShape.addAll(group);
			Shape newShape = new Shape();
			newShape.add(strokes.get(i));
			groupPlusShape.add(newShape);
			groupings.addAll(groupStartingAt(strokes, i + 1, groupPlusShape));

			if (group.size() > 0) {
				ArrayList<Shape> groupConcatLastShape = new ArrayList<Shape>();
				for (int j = 0; j < group.size() - 1; j++)
					groupConcatLastShape.add(group.get(j));
				Shape lastShape = (Shape) group.get(group.size() - 1).clone();
				lastShape.add(strokes.get(i));
				groupConcatLastShape.add(lastShape);
				groupings.addAll(groupStartingAt(strokes, i + 1,
						groupConcatLastShape));
			}

		}
		return groupings;
	}

	public static List<List<Shape>> getCombinatorialGroupingsWithSubGroup(
			List<Stroke> strokes) {

		List<List<Shape>> groups = groupStartingAt(strokes, 0,
				new ArrayList<Shape>());
		List<Stroke> tempStrokes = new ArrayList<Stroke>();
		tempStrokes.addAll(strokes);
		tempStrokes.remove(tempStrokes.size() - 1);
		groups.addAll(groupStartingAt(tempStrokes, 0, new ArrayList<Shape>()));

		return groups;
	}

}
