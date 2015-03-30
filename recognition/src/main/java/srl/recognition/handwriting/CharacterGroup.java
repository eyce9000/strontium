package srl.recognition.handwriting;

import java.util.ArrayList;

public class CharacterGroup {

	ArrayList<Character> m_characters = new ArrayList<Character>();

	ArrayList<Integer> m_groupings = null;

	public void setGrouping(ArrayList<Integer> groupings) {
		m_groupings = groupings;
	}

	public void add(Character c) {
		m_characters.add(c);
	}

	public void printBest() {
		int sum = 0;
		for (Character ch : m_characters) {
			System.out.print(ch.getBestResult() + "("
					+ ch.getHighestConfidence() + ")");
			sum += ch.getHighestConfidence();
		}
		System.out.println(" sum = " + sum);
	}

	
	public double getConfidence(String s) {

		// FOR CIVIL ONLY
		if (s.equals("alpha") || s.equals("beta") || s.equals("gamma") || s.equals("theta") || s.equals("equals") || s.equals("period")) {
			return m_characters.get(0).getConfidence(s);
		}
		
		if (s.length() != m_characters.size()) {
			return 0;
		}

		int count = 0;
		double conf = 0;
		for (char c : s.toCharArray()) {
			conf += m_characters.get(count).getConfidence(c);
			count++;
		}

		return conf / count;
	}

}
