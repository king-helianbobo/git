package com.synonym.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class App {
	private static final Log log = LogFactory.getLog(App.class);
	final static String emStart = "<em>";
	final static String emEnd = "</em>";
	private static List<String> extractEmphas(String content) {
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
		int start1 = -1;
		int end1 = -1;
		StringBuilder builder = new StringBuilder();
		while (true) {
			start1 = content.indexOf(emStart);
			end1 = content.indexOf(emEnd, start1);
			if (start1 >= 0 && end1 >= 0) {
				// log.info(content);
				String key = content.substring(start1 + emStart.length(), end1);
				builder.append(key);
				if (content.indexOf(emStart, end1) != (end1 + emEnd.length())) {
					// log.info(start1 + "," + end1 + "," + builder.toString());
					break;
				} else {
					content = content.substring(end1 + emEnd.length());
				}
			} else
				break;
		}
		return builder.toString();
	}

	static private String curlCommnad(String keyword, int number) {
		StringBuilder builder = new StringBuilder();
		final String userAgent = "\"Mozilla/4.73 [en] (X11; U; Linux 2.2.15 i686)\"";
		builder.append("curl -A ");
		builder.append(userAgent);
		// builder.append("  -o /tmp/index.html  "); // o必须小写
		String url = "  \"http://www.baidu.com/s?cl=3^&wd=";
		builder.append(url);
		builder.append(keyword);
		builder.append("&pn=");
		builder.append(number);
		builder.append("\";");
		return builder.toString();
	}
	private static String setToString(Set<String> set, boolean append) {
		Iterator<String> it = set.iterator();
		StringBuilder builder = new StringBuilder();
		boolean firstWord = true;
		while (it.hasNext()) {
			if (!firstWord)
				builder.append(",");
			else
				firstWord = false;
			String str = it.next();
			builder.append(str);
		}
		if (append)
			builder.append("\n");
		return builder.toString();
	}

	private static void executeCurl(SoulFileWriter writer, String... keywords) {
		final String filter = "百度快照";
		try {
			for (String keyword : keywords) {
				String bash[] = {"/bin/sh", "-c", " "};
				List<String> result = new LinkedList<String>();
				for (int i = 0; i < 2; i++) {
					String str = curlCommnad(keyword, i * 10);
					bash[2] = str;
					Process process = Runtime.getRuntime().exec(bash);
					log.info(str);
					try {
						process.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// log.info(process.getErrorStream());
					InputStreamReader ir = new InputStreamReader(
							process.getInputStream());
					InputStreamReader ir1 = new InputStreamReader(
							process.getErrorStream());
					BufferedReader reader = new BufferedReader(ir);
					String temp = null;
					while ((temp = reader.readLine()) != null) {
						log.info(temp);
						if (temp.length() <= 0)
							continue;
						if (temp.indexOf(filter) >= 0)
							continue;
						// log.info(temp);
						List<String> list = extractEmphas(temp);
						if (list != null) {
							result.addAll(list);
						}
					}
				}
				if (result.size() > 0) {
					Set<String> set = new HashSet<String>();
					for (String str : result) {
						if (str.length() > 1)
							set.add(str);
					}
					log.info(set);
					String line = setToString(set, true);
					writer.writeString(line);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public static void main(String[] args) {
		String[] keywords = {"高唱", "省视", "无终", "纪委", "旅舍", "短浅"};

		Thread t = Thread.currentThread();
		t.setName("curl");
		int i = 0;
		String path = "/tmp/" + String.valueOf(1) + ".txt";
		SoulFileWriter writer = new SoulFileWriter(path);
		try {
			while (true) {
				executeCurl(writer, keywords);
				i++;
				t.sleep(10 * 1000);
				writer.flush();
			}
		} catch (InterruptedException e) {
			writer.close();
			log.error("This thread has interrupted!");
		}
	}

}
