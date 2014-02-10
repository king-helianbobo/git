package org.soul.domain;

import org.soul.utility.NatureLibrary;
import org.soul.utility.StaticVarForSegment;

public class TermNature {

	public static final TermNature NULL = new TermNature("null", 1);
	public static final TermNature NB = new TermNature("m", 1);
	public static final TermNature EN = new TermNature("en", 1);
	public static final TermNature BEGIN = new TermNature("始##始", 1);
	public static final TermNature END = new TermNature("末##末", 1);
	public static final TermNature USER_DEFINE = new TermNature("userDefine", 1);
	public static final TermNature NR = new TermNature("nr", 1);
	public static final TermNature NT = new TermNature("nt", 1);
	public static final TermNature NW = new TermNature("nw", 1); // new word

	public Nature nature;
	public int frequency;

	public TermNature(String natureStr, int frequency) {
		this.nature = NatureLibrary.getNature(natureStr);
		this.frequency = frequency;
	}

	public static TermNature[] setNatureStrToArray(String natureStr, String word) {
		natureStr = natureStr.substring(1, natureStr.length() - 1);
		String[] split = natureStr.split(",");
		String[] strs = null;
		Integer frequency = null;
		TermNature[] all = new TermNature[split.length];
		for (int i = 0; i < split.length; i++) {
			strs = split[i].split("=");
			frequency = Integer.parseInt(strs[1]);
			// if (strs[0].trim().equals("f") && (frequency > 0)) {
			// StaticVariable.LibraryLog.info("frequency = " + frequency
			// + ",word = " + word);
			// }
			all[i] = new TermNature(strs[0].trim(), frequency);
		}
		return all;
	}

	@Override
	public String toString() {
		return this.nature.natureStr + "/" + frequency;
	}
}
