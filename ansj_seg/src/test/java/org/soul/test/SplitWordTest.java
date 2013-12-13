package org.soul.test;

import java.util.*;
import java.io.*;

import org.ansj.splitWord.NlpAnalysis;
import org.ansj.splitWord.BasicAnalysis;
import org.ansj.treeSplit.IOUtil;
import org.ansj.domain.Term;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SplitWordTest {

	private static Log log = LogFactory.getLog(SplitWordTest.class);
	public static final String TAG_START_CONTENT = "<content>";
	public static final String TAG_END_CONTENT = "</content>";

	public static void main(String[] args) {
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
						// List<Term> result = BasicAnalysis.parse(content);
						List<Term> result = NlpAnalysis.parse(content);
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
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != pw) {
				pw.close();
			}
		}
	}
}