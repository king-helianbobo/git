package org.soul.splitWord;

import static org.soul.utility.InitDictionary.TraditionalToSimplified;
import static org.soul.utility.InitDictionary.base;
import static org.soul.utility.InitDictionary.check;
import static org.soul.utility.InitDictionary.status;
import static org.soul.utility.InitDictionary.termNatures;
import static org.soul.utility.InitDictionary.words;

import org.soul.domain.TermNatures;

public class GetWords {

	public int offe;

	// just split Chinese words
	public GetWords(String str) {
		setStr(str);
	}

	public GetWords() {
	}

	public void setStr(String chars) {
		this.chars = chars;
		charsLength = chars.length();
	}

	int charsLength = 0;
	private String chars;
	private int start = 0;
	private int baseValue = 0;
	private int tmpBaseValue = 0;
	public int i = 0;
	private String str = null;

	public String allWords() {
		for (; i < charsLength; i++) {
			int charHashCode = TraditionalToSimplified(chars.charAt(i));// 简体转繁体
			switch (getStatement(charHashCode)) {
			case 0: // 不在词典中
				if (baseValue == chars.charAt(i)) {
					str = String.valueOf(chars.charAt(i));
					offe = i;
					start = ++i;
					baseValue = 0;
					tmpBaseValue = baseValue;
					return str;
				} else {
					i = start;
					start++;
					// System.out.println("start = " + start + ", i = " + i);
					baseValue = 0;
					break; // break current switch
				}
			case 2: // 是个词，但能继续成词
				i++;
				offe = start;
				tmpBaseValue = baseValue;
				return words[tmpBaseValue];
			case 3: // 已经是一个词了
				offe = start;
				start++;
				i = start;
				tmpBaseValue = baseValue;
				baseValue = 0;
				return words[tmpBaseValue];
			}
		}
		if (start++ != charsLength) {
			i = start;
			baseValue = 0;
			return allWords();
		}
		start = 0;
		baseValue = 0;
		i = 0;
		return null;
	}

	/**
	 * 根据用户传入的c得到单词的状态. 0.代表这个字不在词典中 1.继续 2.是个词但是还可以继续 3.停止已经是个词了
	 * 
	 * @param c
	 * @return
	 */
	private int getStatement(int charHashCode) {
		int tmp = baseValue;
		baseValue = base[baseValue] + charHashCode;
		if (check[baseValue] == tmp || check[baseValue] == -1) {
			return status[baseValue];
		}
		return 0;
	}

	public byte getStatus() {
		return status[tmpBaseValue];
	}

	/**
	 * 获得当前词的词性
	 * 
	 * @return
	 */
	public TermNatures getTermNatures() {
		TermNatures tns = termNatures[tmpBaseValue];
		if (tns == null) {
			return TermNatures.NULL;
		}
		return tns;
	}

	public int getOffe() {
		return offe;
	}

}