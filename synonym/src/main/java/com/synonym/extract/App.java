package com.synonym.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class App {
	private static final Log log = LogFactory.getLog(App.class);

	public static void main(String[] args) {

		String[] keywords = { "生育证", "驾驶证", "牡丹" };
		final String userAgent = "\"Mozilla/4.73 [en] (X11; U; Linux 2.2.15 i686)\"";
		try {
			for (String keyword : keywords) {
				StringBuilder builder = new StringBuilder();
				builder.append("curl -A ");
				builder.append(userAgent);
				// builder.append("  -o /tmp/index.html  "); // o必须小写
				String url = "  \"http://www.baidu.com/s?cl=3^&wd=";
				builder.append(url);
				builder.append(keyword);
				builder.append("&pn=10");
				builder.append("\";");
				String str = builder.toString();
				String bash[] = { "/bin/sh", "-c", "" };
				bash[2] = str;
				Process process = Runtime.getRuntime().exec(bash);
				log.info(str);
				// process.waitFor();
				String temp = null;
				String emStart = "<em>";
				String emEnd = "</em>";
				InputStreamReader ir = new InputStreamReader(
						process.getInputStream());
				BufferedReader reader = new BufferedReader(ir);
				while ((temp = reader.readLine()) != null) {
					if (temp.length() <= 0)
						continue;
					String filter = "百度快照";
					if (temp.indexOf(filter) >= 0)
						continue;
					int start1 = -1;
					int end1 = -1;
					StringBuilder build = new StringBuilder();
					while (true) {
						start1 = temp.indexOf(emStart);
						end1 = temp.indexOf(emEnd);
						if (start1 >= 0 && end1 >= 0) {
							// log.info(temp);
							String key = temp.substring(
									start1 + emStart.length(), end1);
							build.append(key);

							if (temp.indexOf(emStart, end1) != (end1 + emEnd
									.length())) {
								log.info(start1 + "," + end1 + ","
										+ build.toString());
								break;
							}

							else {
								temp = temp.substring(end1 + emEnd.length());
								// int end2 = temp.indexOf(emEnd);
								// String key2 = temp.substring(
								// end1 + emEnd.length(), end2);
								// build.append(key2);
							}

						} else
							break;
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
