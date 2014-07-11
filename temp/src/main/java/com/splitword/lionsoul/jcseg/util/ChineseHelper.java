package com.splitword.lionsoul.jcseg.util;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

public class ChineseHelper {
	private static final Properties CHINESETABLE = PinyinResource
			.getChineseTable();

	/**
	 * 将单个繁体字转换为简体字
	 */
	public static char toSimplifiedChinese(char c) {
		if (isTraditionalChinese(c)) {
			return CHINESETABLE.getProperty(String.valueOf(c)).charAt(0);
		}
		return c;
	}

	/**
	 * 将单个简体字转换为繁体字
	 */
	public static char toTraditionalChinese(char c) {
		String jianti = String.valueOf(c);
		if (CHINESETABLE.containsValue(jianti)) {
			Iterator<Entry<Object, Object>> itr = CHINESETABLE.entrySet()
					.iterator();
			while (itr.hasNext()) {
				Entry<Object, Object> e = itr.next();
				if (e.getValue().toString().equals(jianti)) {
					return e.getKey().toString().charAt(0);
				}
			}
		}
		return c;
	}

	/**
	 * 将繁体字符转换为简体字符
	 */
	public static String convertToSimplifiedChinese(String str) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, len = str.length(); i < len; i++) {
			char c = str.charAt(i);
			sb.append(toSimplifiedChinese(c));
		}
		return sb.toString();
	}

	/**
	 * 将简体字转换为繁体字
	 */
	public static String convertToTraditionalChinese(String str) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, len = str.length(); i < len; i++) {
			char c = str.charAt(i);
			sb.append(toTraditionalChinese(c));
		}
		return sb.toString();
	}

	/**
	 * 判断某个字符是否为繁体字
	 * 
	 * @param c
	 *            需要判断的字符
	 * @return 是繁体字返回true，否则返回false
	 */
	public static boolean isTraditionalChinese(char c) {
		return CHINESETABLE.containsKey(String.valueOf(c));
	}

	/**
	 * 判断字符是否为汉字
	 */
	public static boolean isChineseChar(char c) {
		String regex = "[\\u4e00-\\u9fa5]";
		return String.valueOf(c).matches(regex);
	}

	/**
	 * 判断字符串中是否包含汉字
	 */
	public static boolean containChineseChar(String str) {
		for (int i = 0, len = str.length(); i < len; i++) {
			char c = str.charAt(i);
			if (isChineseChar(c))
				return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否全是汉字
	 */
	public static boolean allChineseChar(String str) {
		for (int i = 0, len = str.length(); i < len; i++) {
			char c = str.charAt(i);
			if (!isChineseChar(c))
				return false;
		}
		return true;
	}
}
