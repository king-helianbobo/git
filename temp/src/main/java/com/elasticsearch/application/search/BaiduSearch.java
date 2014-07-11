package com.elasticsearch.application.search;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.elasticsearch.application.query.SoulFileWriter;

public class BaiduSearch {
	private static final Log log = LogFactory.getLog(BaiduSearch.class);
	static String writePath = "/tmp/baidu.txt";
	static SoulFileWriter writer = new SoulFileWriter(writePath);
	private static final String filter = "百度快照";

	private static void extractResult(String lines) {
		// List<String> result = new LinkedList<String>();
		Set<String> set = new TreeSet<String>();
		List<String> empha = ExtractUtil.extractEmphas(lines);
		if (empha != null && !empha.isEmpty()) {
			for (String str : empha) {
				set.add(str);
			}
			log.info(ExtractUtil.setToString(set, false));
		}

	}

	private static String executeCurl(String curlStr)
			throws InterruptedException {
		try {
			String result = null;
			String bash[] = { "/bin/sh", "-c", " " };
			bash[2] = curlStr;
			loop: while (true) {
				Process process = Runtime.getRuntime().exec(bash);
				InputStreamReader ir = new InputStreamReader(
						process.getInputStream());
				BufferedReader reader = new BufferedReader(ir);
				String temp = null;
				if ((temp = reader.readLine()) == null) {
					log.info("百度屏蔽了这次请求，请手工调整:" + curlStr);
					Thread.sleep(100 * 1000);
					continue loop;
				} else {
					StringBuilder builder = new StringBuilder();
					builder.append(temp + "\n");
					long start = System.currentTimeMillis();
					while ((temp = reader.readLine()) != null) {
						long end = System.currentTimeMillis();
						// log.info(temp);
						// log.info((float) (end - start) / 1000.0);
						if (temp.length() <= 0)
							continue;
						else {
							builder.append(temp + "\n");
						}
					}
					result = builder.toString();
					writer.writeStr(result);
					return result;
				}
			}
		} catch (IOException e) {
			log.info("error happened!");
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		Thread t = Thread.currentThread();
		t.setName("curl");
		String srcPath = "library/newword.txt";
		try {
			InputStream in = new FileInputStream(srcPath);
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"utf-8"));
			int startNumber = 3064;
			ExtractUtil.advance(br, startNumber);// 跳转startNumber行之后
			String[] cookies = { "/tmp/cook1.txt", "/tmp/cook2.txt",
					"/tmp/cook3.txt", "/tmp/cook4.txt", "/tmp/cook5.txt",
					"/tmp/cook6.txt", "/tmp/cook7.txt", "/tmp/cook8.txt",
					"/tmp/cook9.txt", "/tmp/cook10.txt" };

			String[] agents = {
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)",
					"Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 6.0)",
					"Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; el-GR)",
					"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.3; Trident/7.0)",
					"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)",
					"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)",
					"Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)" };
			int i = 0;
			while (true) {
				List<String> keys = ExtractUtil.readNextData(br, 1);
				if (keys == null)
					break;
				else {
					String cookie = cookies[i % 10];
					String agent = agents[i % 8];
					i++;
					String keyword = keys.get(0);
					log.info("Begin search " + keyword + " from baidu!");
					String str1 = ExtractUtil.curlCommnad(keyword, cookie,
							agent, 0);
					String str2 = ExtractUtil.curlCommnad(keyword, cookie,
							agent, 10);
					log.info(str1);
					String result1 = executeCurl(str1);
					String result2 = executeCurl(str2);
					extractResult(result1 + result2);
					writer.flush();
					Thread.sleep(3 * 1000);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
