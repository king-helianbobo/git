package org.soul.domain;

import static org.soul.library.InitDictionary.base;
import static org.soul.library.InitDictionary.check;
import static org.soul.library.InitDictionary.status;
import static org.soul.library.InitDictionary.termNatures;
import static org.soul.library.InitDictionary.words;

import org.soul.utility.WordAlter;

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
		this.i = 0;
		charsLength = chars.length();
	}

	int charsLength = 0;
	private String chars;
	private int start = 0;
	private int baseValue = 0;
	private int tmpBaseValue = 0;
	private int i = 0;
	private String str = null;

	public String fetchOneWord() {
		for (; i < charsLength; i++) {
			int charHashCode = WordAlter.TraditionalToSimplified(chars
					.charAt(i));
			switch (getStatement(charHashCode)) {
				case 0 : // not in dictionary
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
						baseValue = 0;
						break; // break current switch
					}
				case 2 : // continue
					i++;
					offe = start;
					tmpBaseValue = baseValue;
					return words[tmpBaseValue];
				case 3 : // form one word,break
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
			return fetchOneWord();
		}
		start = 0;
		baseValue = 0;
		i = 0;
		return null;
	}

	/**
	 * 获得单词的状态. 0.代表单词不在词典中 1.继续 2.单词是个词但是可以继续 3.已经是个词了,stop
	 * 
	 * @author LiuBo
	 * @since 2014年1月14日
	 * @param charHashCode
	 * @return int 单词编码
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
	/**
	 * get current word's natures, Term have multiple natures
	 * 
	 * @author LiuBo
	 * @since 2014年1月14日
	 * @return TermNatures
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