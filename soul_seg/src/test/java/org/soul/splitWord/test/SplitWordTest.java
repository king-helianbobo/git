package org.soul.splitWord.test;

import java.util.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.analysis.BasicAnalysis;
import org.soul.analysis.NlpAnalysis;
import org.soul.domain.Term;
import org.soul.treeSplit.IOUtil;
import org.junit.Test;
import org.junit.Ignore;

public class SplitWordTest {

	private static Log log = LogFactory.getLog(SplitWordTest.class);
	public static final String TAG_START_CONTENT = "<content>";
	public static final String TAG_END_CONTENT = "</content>";

	@Test
	public void test1() {
		String temp = null;
		BufferedReader reader = null;
		PrintWriter pw = null;
		try {
			reader = IOUtil.getReader("/mnt/f/tmp/content3.txt", "UTF-8");
			pw = new PrintWriter("/mnt/f/tmp/result2.txt");
			long start = System.currentTimeMillis();
			int allCount = 0;
			int termcnt = 0;
			Set<String> set = new HashSet<String>();
			while ((temp = reader.readLine()) != null) {
				temp = temp.trim();
				if (temp.startsWith(TAG_START_CONTENT)) {
					int end = temp.indexOf(TAG_END_CONTENT);
					String content = temp.substring(TAG_START_CONTENT.length(),
							end);
					if (content.length() > 0) {
						allCount += content.length();
						List<Term> result = BasicAnalysis.parse(content);
						// List<Term> result = NlpAnalysis.parse(content);
						for (Term term : result) {
							String item = term.getName().trim();
							if (item.length() > 0) {
								termcnt++;
								pw.print(item.trim() + " ");
								set.add(item);
							}
						}
						pw.println();
					}
				}
			}
			long end = System.currentTimeMillis();
			log.info("total " + termcnt + " terms, " + set.size() + " words, "
					+ allCount + " chars, processing speed: "
					+ (allCount * 1000.0 / (end - start)) + " chars per second");

			reader.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}