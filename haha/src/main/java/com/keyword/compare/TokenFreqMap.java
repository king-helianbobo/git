package com.keyword.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;

import com.splitword.lionsoul.jcseg.util.ChineseHelper;
import com.splitword.soul.utility.StringUtil;

public class TokenFreqMap {
	private final static Log log = LogFactory.getLog(TokenFreqMap.class);
	private static int totalDocumentFreq = 860000;
	private static Map<String, TokenPojo> termPojoMap = new TreeMap<String, TokenPojo>();
	private static Set<String> stopWords = new HashSet<String>();
	private static Map<String, Map<String, String>> leaderMap = new HashMap<String, Map<String, String>>();
	public static Map<String, List<String>> leaderTree = null;
	private static ObjectMapper mapper = new ObjectMapper();
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
			loadStopWords();
			loadLeaderTree();
			loadLeaderInfo();
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

	private static void loadStopWords() throws IOException {
		BufferedReader br = DictionaryReader.getReader("stopWord.dic");
		String temp = null;
		while ((temp = br.readLine()) != null) {
			if (StringUtil.isBlank(temp))
				continue;

			String result = temp.trim();
			// log.info(result);
			stopWords.add(result);
		}
		log.info("stop word's size is " + stopWords.size());
		br.close();
	}

	private static void loadLeaderInfo() throws IOException {
		BufferedReader br = DictionaryReader.getReader("baipi/leader/info.txt");
		String temp = null;
		while ((temp = br.readLine()) != null) {
			if (StringUtil.isBlank(temp))
				continue;
			String json = temp.trim();
			JsonParser jsonParser = mapper.getJsonFactory().createJsonParser(
					json);
			@SuppressWarnings("unchecked")
			Map<String, String> tmp = mapper.readValue(jsonParser, Map.class);
			String name = (String) tmp.get("name");
			String[] nameStrs = name.split("\\s+");
			StringBuilder builder = new StringBuilder();
			if (nameStrs.length > 1) {
				for (String nameStr : nameStrs)
					builder.append(nameStr);
				name = builder.toString();
			}

			Map<String, String> map2 = new HashMap<String, String>();
			for (String key : tmp.keySet()) {
				if (key.equals("name"))
					continue;
				if (key.equals("img"))
					map2.put(key, "leader/" + tmp.get(key));
				else
					map2.put(key, tmp.get(key));
			}
			leaderMap.put(name, map2);
		}
		for (String key : leaderMap.keySet())
			log.info(key);
		log.info("stop word's size is " + stopWords.size());
		br.close();
	}

	public static Map<String, String> leaderInfo(String queryStr)
			throws JsonGenerationException, JsonMappingException, IOException {
		loadData();
		String name = queryStr;
		Map<String, String> tmpMap = leaderMap.get(name);
		if (tmpMap == null) {
			List<String> synonymList = leaderTree.get(name);
			for (String str : synonymList) {
				if (leaderMap.containsKey(str)) {
					name = str;
					tmpMap = leaderMap.get(name);
					break;
				}
			}
			if (tmpMap == null)
				return null;
		}
		String infoMd5 = tmpMap.get("infoMD5");
		String info = tmpMap.get("info");
		String expectedMd5 = MD5Utility.calculateStrMD5(info);
		Map<String, String> resultMap = new HashMap<String, String>();

		if (expectedMd5.equals(infoMd5)) {
			resultMap.put("info", info);
		}
		String imgPath = tmpMap.get("img");
		String imgMD5 = tmpMap.get("imgMD5");
		String expectedImgMd5 = MD5Utility.calculateFileMD5(imgPath);
		if (expectedImgMd5.equals(imgMD5)) {
			resultMap.put("img", imgPath);
		}
		return resultMap;
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

	public static int termTotalFreq(String str) {
		loadData();
		TokenPojo pojo = termPojoMap.get(str);
		if (pojo != null)
			return pojo.getTotalFreq();
		else
			return -1;
	}

	public static boolean isStopWord(String str) {
		loadData();
		if (stopWords.contains(str) && str.length() == 1
				&& ChineseHelper.allChineseChar(str))
			return true;
		else if (stopWords.contains(str)
				&& !ChineseHelper.containChineseChar(str))
			return true;
		else
			return false;
	}

	public static boolean isLeader(String str) {
		loadData();
		return leaderTree.containsKey(str);
	}

	private static void loadLeaderTree() throws IOException {
		File synonymLibrary = new File("library/leader.txt");
		if (!synonymLibrary.isFile() || !synonymLibrary.canRead()) {
			log.info("Can't find file!");
			leaderTree = null;
			return;
		} else {
			leaderTree = new TreeMap<String, List<String>>();
			InputStream in = new FileInputStream(synonymLibrary);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, "UTF-8"));
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				String[] strs = temp.split(",");
				List<String> list = new LinkedList<String>();
				for (int i = 0; i < strs.length; i++) {
					list.add(strs[i].trim());
				}
				for (int i = 0; i < strs.length; i++) {
					List<String> newList = new LinkedList<String>(list);
					newList.remove(i);
					leaderTree.put(strs[i], newList);
				}
			}
			log.info("synonym dictionary loaded!");
			reader.close();
			in.close();
		}
	}
}
