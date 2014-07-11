package com.elasticsearch.application;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.splitword.lionsoul.jcseg.util.ChineseHelper;
import com.splitword.soul.utility.StringUtil;

public class WordAlter {
	private static Log log = LogFactory.getLog(WordAlter.class);
	public static final int MinQuanJiaoAlpha = 65345;// ａ
	public static final int MaxQuanJiaoAlpha = 65370;// ｚ
	public static final int MinAsciiAlpha = 65;// A
	public static final int MaxAsciiAlpha = 90;// Z
	public static final int MinQuanJiaoAlpha_UPPER = 65313;// Ａ
	public static final int MaxQuanJiaoAlpha_UPPER = 65338;// Ｚ
	public static final int MinQuanJiaoNumber = 65296;// ０
	public static final int MaxQuanJiaoNumber = 65305;// ９

	static final int LOWER_GAP = 65248;
	static final int UPPER_GAP = 65216;
	static final int UPPER_GAP_E = -32;
	static final int UPPER_GAP_N = 65248;
	private static final char[] CHARCOVER = new char[65536];

	static {
		for (int i = 0; i < CHARCOVER.length; i++) {
			if (i >= MinQuanJiaoAlpha && i <= MaxQuanJiaoAlpha) {
				CHARCOVER[i] = (char) (i - LOWER_GAP);
			} else if (i >= MinQuanJiaoAlpha_UPPER
					&& i <= MaxQuanJiaoAlpha_UPPER) {
				CHARCOVER[i] = (char) (i - UPPER_GAP);
			} else if (i >= MinAsciiAlpha && i <= MaxAsciiAlpha) {
				CHARCOVER[i] = (char) (i - UPPER_GAP_E);
			} else if (i >= MinQuanJiaoNumber && i <= MaxQuanJiaoNumber) {
				CHARCOVER[i] = (char) (i - UPPER_GAP_N);
			} else {
				CHARCOVER[i] = 0;
			}
		}
		/*********************************************/
		CHARCOVER['！'] = '。';
		CHARCOVER['＇'] = '。';
		CHARCOVER['，'] = '。';
		CHARCOVER['︰'] = '﹕';
		CHARCOVER['#'] = '。';
		CHARCOVER['!'] = '。';
		CHARCOVER['︳'] = '。';
		CHARCOVER['&'] = '。';
		CHARCOVER['︴'] = '。';
		CHARCOVER['︵'] = '《';
		CHARCOVER['︶'] = '》';
		CHARCOVER['︷'] = '《';
		CHARCOVER['*'] = '。';
		CHARCOVER['︸'] = '》';
		CHARCOVER['︹'] = '《';
		CHARCOVER['︺'] = '》';
		CHARCOVER['︻'] = '《';
		CHARCOVER['︼'] = '》';
		CHARCOVER['︽'] = '《';
		CHARCOVER[','] = '。';
		CHARCOVER['︾'] = '》';
		CHARCOVER['-'] = '。';
		CHARCOVER['︿'] = '《';
		CHARCOVER[';'] = '。';
		CHARCOVER['?'] = '。';
		CHARCOVER['>'] = '》';
		CHARCOVER['<'] = '《';
		CHARCOVER['﹖'] = '。';
		CHARCOVER['﹔'] = '。';
		CHARCOVER['﹐'] = '。';
		CHARCOVER['﹑'] = '。';
		CHARCOVER['﹞'] = '》';
		CHARCOVER['﹟'] = '。';
		CHARCOVER['﹝'] = '《';
		CHARCOVER['﹄'] = '》';
		CHARCOVER['﹃'] = '《';
		CHARCOVER['‖'] = '。';
		CHARCOVER['﹂'] = '》';
		CHARCOVER['―'] = '。';
		CHARCOVER['﹁'] = '《';
		CHARCOVER['—'] = '。';
		CHARCOVER['﹀'] = '》';
		CHARCOVER[']'] = '》';
		CHARCOVER['﹏'] = '。';
		CHARCOVER['\\'] = '。';
		CHARCOVER['﹎'] = '。';
		CHARCOVER['_'] = '。';
		CHARCOVER['﹍'] = '。';
		CHARCOVER['^'] = '。';
		CHARCOVER['﹌'] = '。';
		CHARCOVER['﹋'] = '。';
		CHARCOVER['﹊'] = '。';
		CHARCOVER['”'] = '"';
		CHARCOVER['['] = '《';
		CHARCOVER['﹉'] = '。';
		CHARCOVER['“'] = '"';
		CHARCOVER['•'] = '·';
		CHARCOVER['…'] = '。';
		CHARCOVER['`'] = '。';
		CHARCOVER['〉'] = '》';
		CHARCOVER['〈'] = '《';
		CHARCOVER['』'] = '》';
		CHARCOVER['『'] = '《';
		CHARCOVER['」'] = '》';
		CHARCOVER['「'] = '《';
		CHARCOVER['﹤'] = '《';
		CHARCOVER['∕'] = '。';
		CHARCOVER['【'] = '《';
		CHARCOVER['﹦'] = '=';
		CHARCOVER['】'] = '》';
		CHARCOVER['〖'] = '《';
		CHARCOVER['﹡'] = '。';
		CHARCOVER['〗'] = '"';
		CHARCOVER['﹠'] = '&';
		CHARCOVER['〔'] = '《';
		CHARCOVER['〕'] = '》';
		CHARCOVER['﹢'] = '+';
		CHARCOVER['‹'] = '《';
		CHARCOVER['~'] = '。';
		CHARCOVER['}'] = '》';
		CHARCOVER['›'] = '》';
		CHARCOVER['|'] = '。';

		CHARCOVER['{'] = '《';
		CHARCOVER['〞'] = '"';
		CHARCOVER['﹨'] = '。';

		CHARCOVER['〝'] = '"';
		CHARCOVER['«'] = '《';
		CHARCOVER['¡'] = '。';
		CHARCOVER['¦'] = '。';
		CHARCOVER['»'] = '》';
		CHARCOVER['¸'] = '。';
		CHARCOVER['¿'] = '。';
		CHARCOVER['´'] = '。';
		CHARCOVER['ˉ'] = '。';
		CHARCOVER['ˋ'] = '。';
		CHARCOVER['ˊ'] = '。';
		CHARCOVER['ˇ'] = '。';
		CHARCOVER['˜'] = '。';
		CHARCOVER['．'] = '。';
	}

	public static int chineseCharNumbers(String s) {
		int count = 0;
		Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(s);
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	private static char alterChar(char c) {
		char result = 0;
		if (c >= MinQuanJiaoAlpha && c <= MaxQuanJiaoAlpha) {
			result = (char) (c - LOWER_GAP);
		} else if (c >= MinQuanJiaoAlpha_UPPER && c <= MaxQuanJiaoAlpha_UPPER) {
			result = (char) (c - UPPER_GAP);
		} else if (c >= MinAsciiAlpha && c <= MaxAsciiAlpha) {
			result = (char) (c - UPPER_GAP_E);
		} else if (c >= MinQuanJiaoNumber && c <= MaxQuanJiaoNumber) {
			result = (char) (c - UPPER_GAP_N);
		} else {
			result = c;
		}
		return result;
		// return traditionalToSimplified(result);
	}

	public static String alterAlphaAndNumber(String temp) {
		return alterAlphaAndNumber(temp, 0, temp.length());
	}

	public static String alterAlphaAndNumber(String temp, int start, int end) {
		char c = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < start + end; i++) {
			c = temp.charAt(i);
			sb.append(alterChar(c));
		}
		return sb.toString();
	}

	public static char[] alterStr(String str) {
		char[] chars = new char[str.length()];
		char c = 0;
		for (int i = 0; i < chars.length; i++) {
			c = CHARCOVER[str.charAt(i)];
			if (c > 0) {
				chars[i] = c;
			} else {
				chars[i] = str.charAt(i);
			}
		}
		return chars;
	}

	public static boolean isRuleWord(String word) {
		if (CHARCOVER[word.charAt(0)] > 0
				|| CHARCOVER[word.charAt(word.length() - 1)] > 0) {
			return true;
		}
		return false;
	}

	public static char traditionalToSimplified(char c) {

		return c;
	}

	public static String traditionalToSimplified(String str) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			char value = traditionalToSimplified(c);
			builder.append(value);
		}
		return builder.toString();
	}

	public static boolean isLetterOrDigit(char t) {
		char c = alterChar(t);
		if (Character.isLetterOrDigit(c)) {
			return true;
		} else
			return false;
	}

	static public boolean bIntersection(String str1, String str2) {
		if (!ChineseHelper.allChineseChar(str1)
				|| !ChineseHelper.allChineseChar(str2))
			return false;
		String commonPart = commonSet(str1, str2);
		if (commonPart == null)
			return false;
		else
			return true;
	}

	static public String commonSet(String str1, String str2) {
		if (StringUtil.isBlank(str1) || StringUtil.isBlank(str2))
			return null;
		else {
			StringBuilder builder = new StringBuilder();
			String string1 = str1;
			String string2 = str2;
			if (str1.length() > str2.length()) {
				string1 = str2;
				string2 = str1;
			}
			int numStr = 0;
			for (int i = 0; i < string1.length(); i++) {
				char c = string1.charAt(i);
				if (string2.contains(String.valueOf(c))) {
					builder.append(c);
					numStr++;
				}
			}
			if (numStr <= 0)
				return null;
			else
				return builder.toString();
		}
	}

	static public boolean nounIntersection(String primeToken,
			String vectorToken, String primeType) {
		String commonPart = commonSet(primeToken, vectorToken);
		if (commonPart == null)
			return false;
		else {
			int len1 = primeToken.length();
			int len2 = vectorToken.length();
			String string1 = null;
			String string2 = null;
			if (len1 > len2) {
				string1 = vectorToken;
				string2 = primeToken;
			} else {
				string1 = primeToken;
				string2 = vectorToken;
			}
			if (commonPart.equals(string1))
				return true; // 最好的情况，比如“中纪委”和“中央纪委”
			else if (vectorToken.endsWith(commonPart))
				return false; // 比如“妇科”和“内科”，“大学”和“小学”
			else if (primeType.equals("nt"))// 如果是机构，返回false
				return false;
			else
				return true;
		}
	}
}
