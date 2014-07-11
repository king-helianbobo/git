package com.elasticsearch.application.query;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

public class TokenFreqMap {
	private final static Log log = LogFactory.getLog(TokenFreqMap.class);
	public static int totalDocumentFreq = 860000;
	private static Map<String, TokenPojo> termPojoMap = new TreeMap<String, TokenPojo>();
	private static boolean loaded = false;
	private static final Lock LOCK = new ReentrantLock();

	private static void loadData() {
		if (loaded)
			return;
		LOCK.lock();
		if (loaded) {
			LOCK.unlock();
			return;
		}
		try {
			loadFreqMap();
			loaded = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			LOCK.unlock();
		}
	}

	private static void loadFreqMap() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream("library/freq.txt"), "utf-8"));
		String temp = null;
		int maxDocFreq = -1;
		while ((temp = br.readLine()) != null) {
			String result = temp.trim();
			String[] texts = result.split("\\s+");
			Assert.assertEquals(texts.length, 6);
			String name = texts[0];
			String nature = texts[1];
			int docFreq = Integer.valueOf(texts[2]);
			int totalFreq = Integer.valueOf(texts[3]);
			int titleFreq = Integer.valueOf(texts[4]);
			int contentFreq = Integer.valueOf(texts[5]);
			TokenPojo pojo = new TokenPojo(name, nature, docFreq, totalFreq,
					titleFreq, contentFreq);
			Assert.assertTrue(termPojoMap.get(name) == null);
			Assert.assertTrue(totalFreq == (titleFreq + contentFreq));
			if (docFreq > maxDocFreq)
				maxDocFreq = docFreq;
			termPojoMap.put(name, pojo);
		}
		totalDocumentFreq = maxDocFreq;
		log.info("totalDocument freq is " + totalDocumentFreq);
		br.close();
	}

	public static int termTotalFreq(List<String> vectors) {
		loadData();
		int totalNumber = 0;
		for (String term : vectors) {
			TokenPojo pojo = termPojoMap.get(term);
			if (pojo != null)
				totalNumber += pojo.getTotalFreq();
		}
		if (totalNumber == 0)
			return Integer.MAX_VALUE;
		else
			return totalNumber;
	}
}
