package org.soul.recognition;

import java.util.*;
import org.soul.domain.NatureInLib;
import org.soul.domain.NewWord;
import org.soul.domain.ViterbiGraph;

public class RuleRecogntion {
	private static Map<String, String> ruleMap = new HashMap<String, String>();

	static {
		ruleMap.put("《", "》");
	}

	public static List<NewWord> recognition(ViterbiGraph graph) {
		return recognition(graph.convertedStr);
	}

	public static List<NewWord> recognition(String sentence) {
		String end = null;
		StringBuilder sb = null;
		String name;
		List<NewWord> result = new ArrayList<NewWord>();
		for (int i = 0; i < sentence.length(); i++) {
			name = String.valueOf(sentence.charAt(i));
			if (end == null) {
				if ((end = ruleMap.get(name)) != null) {
					sb = new StringBuilder();
				}
			} else {
				if (end.equals(name)) {
					result.add(new NewWord(sb.toString(), NatureInLib.NW, -sb
							.length()));
					sb = null;
					end = null;
				} else {
					sb.append(name);
				}
			}
		}
		return result;
	}

}
