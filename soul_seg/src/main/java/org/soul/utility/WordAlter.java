package org.soul.utility;

public class WordAlter {

	public static final int MIN_LOWER = 65345;
	public static final int MAX_LOWER = 65370;
	public static final int LOWER_GAP = 65248;
	public static final int MIN_UPPER = 65313;
	public static final int MAX_UPPER = 65338;
	public static final int UPPER_GAP = 65216;
	public static final int MIN_UPPER_E = 65;
	public static final int MAX_UPPER_E = 90;
	public static final int UPPER_GAP_E = -32;
	public static final int MIN_UPPER_N = 65296;
	public static final int MAX_UPPER_N = 65305;
	public static final int UPPER_GAP_N = 65248;

	/**
	 * 转译全角字符和大写字母.如ｓｄｆｓｄｆ
	 */
	public static String alertAlpha(char[] chars, int start, int end) {
		for (int i = start; i < start + end; i++) {
			if (chars[i] >= MIN_LOWER && chars[i] <= MAX_LOWER) {
				chars[i] = (char) (chars[i] - LOWER_GAP);
			}
			if (chars[i] >= MIN_UPPER && chars[i] <= MAX_UPPER) {
				chars[i] = (char) (chars[i] - UPPER_GAP);
			}
			if (chars[i] >= MIN_UPPER_E && chars[i] <= MAX_UPPER_E) {
				chars[i] = (char) (chars[i] - UPPER_GAP_E);
			}
		}
		return new String(chars, start, end);
	}

	public static String alertAlpha(String temp, int start, int end) {
		char c = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < start + end; i++) {
			c = temp.charAt(i);
			if (c >= MIN_LOWER && c <= MAX_LOWER) {
				sb.append((char) (c - LOWER_GAP));
			} else if (c >= MIN_UPPER && c <= MAX_UPPER) {
				sb.append((char) (c - UPPER_GAP));
			} else if (c >= MIN_UPPER_E && c <= MAX_UPPER_E) {
				sb.append((char) (c - UPPER_GAP_E));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String alertNumber(char[] chars, int start, int end) {
		for (int i = start; i < start + end; i++) {
			if (chars[i] >= MIN_UPPER_N && chars[i] <= MAX_UPPER_N) {
				chars[i] = (char) (chars[i] - UPPER_GAP_N);
			}
		}
		return new String(chars, start, end);
	}

	public static String alertAlphaAndNumber(String temp) {
		return alertAlphaAndNumber(temp, 0, temp.length());
	}

	public static String alertAlphaAndNumber(String temp, int start, int end) {
		char c = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < start + end; i++) {
			c = temp.charAt(i);
			if (c >= MIN_LOWER && c <= MAX_LOWER) {
				sb.append((char) (c - LOWER_GAP));
			} else if (c >= MIN_UPPER && c <= MAX_UPPER) {
				sb.append((char) (c - UPPER_GAP));
			} else if (c >= MIN_UPPER_E && c <= MAX_UPPER_E) {
				sb.append((char) (c - UPPER_GAP_E));
			} else if (c >= MIN_UPPER_N && c <= MAX_UPPER_N) {
				sb.append((char) (c - UPPER_GAP_N));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String alertNumber(String temp, int start, int end) {
		char c = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < start + end; i++) {
			c = temp.charAt(i);
			if (c >= MIN_UPPER_N && c <= MAX_UPPER_N) {
				sb.append((char) (c - UPPER_GAP_N));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
