package com.elasticsearch.application.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.splitword.soul.utility.StringUtil;

public class ExtractUtil {
	private static final Log log = LogFactory.getLog(ExtractUtil.class);
	final static String emStart = "<em>";
	final static String emEnd = "</em>";

	public static List<String> extractEmphas(String content) {
		List<String> result = new LinkedList<String>();
		while (content.indexOf(emStart) >= 0 && content.indexOf(emEnd) >= 0) {
			String word = extractEmpha(content);
			String filter = "...";
			if (!word.equals(filter))
				result.add(word);
			int end = content.indexOf(emEnd);
			end = end + emEnd.length();
			content = content.substring(end);
		}
		if (result.size() > 0)
			return result;
		else
			return null;
	}

	private static String extractEmpha(String content) {
		int start = -1;
		int end = -1;
		StringBuilder builder = new StringBuilder();
		while (true) {
			start = content.indexOf(emStart);
			end = content.indexOf(emEnd, start);
			if (start >= 0 && end >= 0) {
				String key = content.substring(start + emStart.length(), end);
				builder.append(key);
				if (content.indexOf(emStart, end) != (end + emEnd.length())) {
					// log.info(start1 + "," + end1 + "," + builder.toString());
					break;
				} else {
					content = content.substring(end + emEnd.length());
				}
			} else
				break;
		}
		return builder.toString();
	}

	public static void advance(BufferedReader br, final int startNumber) {
		int i = 0;
		try {
			while (br.readLine() != null) {
				if (i < startNumber) {
					i++;
					continue;
				} else
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static public String curlCommnad(String keyword, String cookieFile,
			String userAgent, int number) {
		StringBuilder builder = new StringBuilder();
		builder.append("curl --cookie " + cookieFile + " -A \"" + userAgent
				+ "\"");
		String url = "  \"http://www.baidu.com/s?^&wd=";
		builder.append(url);
		builder.append(keyword);
		builder.append("&pn=");
		builder.append(number);
		builder.append("\";");
		return builder.toString();
	}

	public static String setToString(Set<String> set) {
		return setToString(set, true);
	}

	public static String setToString(Set<String> set, boolean append) {
		Iterator<String> it = set.iterator();
		StringBuilder builder = new StringBuilder();
		Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
		boolean firstWord = true;
		while (it.hasNext()) {
			String str = it.next();
			int length = str.length();
			List<String> list = map.get(length);
			if (list == null)
				list = new LinkedList<String>();
			list.add(str);
			map.put(length, list);
		}
		for (Integer key : map.keySet()) {
			List<String> list = map.get(key);
			for (String str : list) {
				if (!firstWord)
					builder.append(",");
				else
					firstWord = false;
				builder.append(str);
			}
		}
		if (append)
			builder.append("\n");
		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	public static List<String> readNextData(BufferedReader br, final int number)
			throws IOException {
		List<String> list = new LinkedList<String>();
		String lineStr = null;
		while ((lineStr = br.readLine()) != null) {
			lineStr = lineStr.trim();
			String[] strs = lineStr.split("\t");
			if (StringUtil.isBlank(lineStr))
				continue;
			if (lineStr.length() <= 1)
				continue;
			list.add(strs[0]);
			if (list.size() >= number)
				break;
		}
		if (lineStr != null)
			return list;
		else if (lineStr == null && list.size() > 1)
			return list;
		else
			return null;
	}
}
