package com.elasticsearch.application.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class YiwuSearch {
	private static final Log log = LogFactory.getLog(YiwuSearch.class);
	public static final ArrayList<String> hasSearch = new ArrayList<String>();

	private static String extractResult4Yiwu(String lines) {
		Map<String, Integer> maps = new HashMap<String, Integer>();
		List<String> empha = ExtractUtil4Yiwu.extractEmphas4Yiwu(lines);
		if (empha != null && !empha.isEmpty()) {

			String result = wordFrequency(empha, maps);
			return result;
		}
		return "";
	}

	private static String wordFrequency(List<String> list,
			Map<String, Integer> maps) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		boolean firstWord = true;
		for (String l : list) {
			if (maps.containsKey(l)) {
				int freq = maps.get(l);
				freq++;
				maps.put(l, freq);
			} else
				maps.put(l, 1);
		}
		List<Map.Entry<String, Integer>> mappingList = null;
		mappingList = new ArrayList<Map.Entry<String, Integer>>(maps.entrySet());
		Collections.sort(mappingList,
				new Comparator<Map.Entry<String, Integer>>() {
					public int compare(Map.Entry<String, Integer> o1,
							Map.Entry<String, Integer> o2) {
						return (o2.getValue() - o1.getValue());
					}
				});

		for (Entry<String, Integer> entry : maps.entrySet()) {
			if (!firstWord)
				builder.append("\t");
			else
				firstWord = false;
			builder.append(entry.getKey() + "\t" + entry.getValue());
		}

		return builder.toString();
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
					log.info("义乌屏蔽了这次请求，请手工调整:" + curlStr);
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
		String srcPath = "library/freq.txt";
		String writePath = "library/result5.txt";
		int count = 0;
		try {

			InputStream in = new FileInputStream(srcPath);
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"utf-8"));

			OutputStream out = new FileOutputStream(writePath);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out,
					"utf-8"));

			ArrayList<FreqEntity> freqEntities = ReadFreqFile.readFile(srcPath);

			FreqComparator comp = new FreqComparator();
			Collections.sort(freqEntities, comp);
			Collections.reverse(freqEntities);
			
			System.out.println(freqEntities.size());
			
			for (int i = 0; i < freqEntities.size(); i++) {
				String keyword = freqEntities.get(i).getWord();
				log.info("Begin search " + keyword + " from yiwu!");
				String str1 = ExtractUtil4Yiwu.curlCommnad4Yiwu(keyword);
				log.info(str1);
				if (!ifHasSearch(keyword, hasSearch)) {
					String result1 = executeCurl(str1);
					String searchResult = extractResult4Yiwu(result1);
					hasSearch.add(keyword);
					searchResult = execludeWord(keyword, searchResult);
					log.info("result : " + searchResult.toString());
					if (!"".equals(searchResult) && null != searchResult) {

						bw.append(keyword + "\t" + searchResult + "\n");
						bw.flush();

						/*
						 * if(count >= 1000){ log.info("count is equal 1000");
						 * break; }
						 */
						count++;
						log.info("current : " + i);
					}
				}
				// Thread.sleep(1 * 1000);
			}
			bw.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static boolean ifHasSearch(String keyword,
			ArrayList<String> hassearch2) {
		// TODO Auto-generated method stub

		if (null == hassearch2 || hassearch2.size() <= 0)
			return false;
		for (String search : hassearch2) {
			if (keyword.trim().equals(search.trim()))
				return true;
			else
				continue;
		}
		return false;
	}

	private static String execludeWord(String keyword, String searchResult) {
		String[] searchResults = searchResult.split("[,]");
		List<String> list = new ArrayList<String>(Arrays.asList(searchResults));
		boolean firstWord = true;
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			String str = iter.next();
			if (str.trim().equals(keyword.trim())) {
				iter.remove();
			}
		}

		StringBuilder sb = new StringBuilder();
		for (String str : list) {
			if (!firstWord)
				sb.append(",");
			else
				firstWord = false;
			sb.append(str);
		}
		// return Arrays.toString(searchResults);
		return sb.toString();
	}

	public static void delArrayElement(String[] arrayElemen, int index) {
		String[] ary = new String[arrayElemen.length - 1];
		System.arraycopy(arrayElemen, 0, ary, 0, index);
		System.arraycopy(arrayElemen, index + 1, ary, index, ary.length - index);
	}

}
