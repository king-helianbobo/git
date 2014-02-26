package org.lionsoul.elasticsearch.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.MapWritable;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.hadoop.util.WritableUtils;
import org.splitword.soul.treeSplit.StringUtil;
public class SogouDataReader {

	private static Log log = LogFactory.getLog(SogouDataReader.class);
	private InputStream in = null;
	private BufferedReader br = null;
	List<String> paths; // file path list
	private String CHAR_CODING = "gbk";
	// private LinkedList<Map<String, String>> elements = new
	// LinkedList<Map<String, String>>();

	public SogouDataReader(List<String> paths) {
		this.paths = paths;
	}

	public SogouDataReader(String directory) {
		paths = new LinkedList<String>();
		File file = new File(directory);
		if (file.isFile()) {
			paths.add(directory);
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().trim().endsWith(".txt")
						&& files[i].isFile()) {
					paths.add(files[i].getAbsolutePath());
					log.info(files[i].getAbsolutePath());
				}
			}
		} else {
			log.error(directory + " is not legal!");
		}
	}

	public void convertToHdfsFormat(String outputDir) throws IOException {
		if (outputDir.endsWith("/"))
			outputDir = outputDir.substring(0, outputDir.length() - 1);
		File dir = new File(outputDir);
		if (!dir.exists())
			dir.mkdir();
		else {
			dir.delete();
			dir.mkdir();
		}
		FileWriter fw = null;
		BufferedWriter bw = null;
		long totalDocNumber = 0;
		HashSet<String> set = new HashSet<String>();
		while (true) {
			if (br == null) {
				if (paths.isEmpty())
					break;
				else {
					String path = paths.get(0);
					int lastIndex = path.lastIndexOf("/");
					String fileName = path.substring(lastIndex);
					log.info(path + " " + (outputDir + fileName));
					paths.remove(0);
					in = new FileInputStream(path);
					br = new BufferedReader(new InputStreamReader(in,
							CHAR_CODING));
					fw = new FileWriter(outputDir + fileName, true);
					bw = new BufferedWriter(fw);
				}
			}
			while (br != null) {
				final int number = 10;
				List<HashMap<String, String>> products = generateDataMap(number);
				if (products != null)
					totalDocNumber += products.size();
				for (int i = 0; i < products.size(); i++) {
					HashMap<String, String> map = products.get(i);
					StringBuilder builder = new StringBuilder();
					String tag = "$$$$";
					builder.append(map.get("url") + tag);
					builder.append(map.get("docno") + tag);
					String str = map.get("docno").trim();
					if (!set.contains(str))
						set.add(str);
					builder.append(map.get("postTime") + tag);
					builder.append(map.get("contenttitle") + tag);
					builder.append(map.get("content"));
					bw.write(builder.toString());
					bw.newLine();
				}
			}
			bw.close();
			fw.close();
			if (paths.size() > 0) {
				String path = paths.get(0);
				int lastIndex = path.lastIndexOf("/");
				String fileName = path.substring(lastIndex);
				log.info(path + " " + (outputDir + fileName));
				paths.remove(0);
				in = new FileInputStream(path);
				br = new BufferedReader(new InputStreamReader(in, CHAR_CODING));
				fw = new FileWriter(outputDir + fileName, true);
				bw = new BufferedWriter(fw);
			} else {
				break;
			}
		}
		log.info("total Document number is " + totalDocNumber);
		log.info("total unique Document number is " + set.size());
	}
	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> next() throws IOException {
		if (br == null) {
			if (paths.isEmpty())
				return null; // no more data
			else {
				String path = paths.get(0);
				log.info(path);
				paths.remove(0);
				in = new FileInputStream(path);
				br = new BufferedReader(new InputStreamReader(in, CHAR_CODING));
			}
		}
		final int number = 10;
		List<HashMap<String, String>> products = generateDataMap(number);
		while (products.size() < number) {
			int size = products.size();
			if (paths.size() > 0) {
				String path = paths.get(0);
				log.info(path);
				paths.remove(0);
				in = new FileInputStream(path);
				br = new BufferedReader(new InputStreamReader(in, CHAR_CODING));
				products.addAll(generateDataMap(number - size));
			} else {
				if (products.size() > 0)
					return products;
				else
					return null;
			}
		}
		return products;
	}
	private List<HashMap<String, String>> generateDataMap(int number)
			throws IOException {
		List<HashMap<String, String>> products = Lists.newArrayList();
		String temp = null;
		boolean isFirstElement = true;
		int n = 0;
		HashMap<String, String> tmpMap = new HashMap<String, String>();
		while ((temp = br.readLine()) != null) {
			temp = temp.trim();
			if (StringUtil.isBlank(temp))
				continue;
			if (isFirstElement) {
				tmpMap.clear();
				if (temp.startsWith("<doc>"))
					isFirstElement = false;
				continue;
			} else if (temp.startsWith("<contenttitle>")) {
				int end = temp.lastIndexOf("</contenttitle>");
				if (end >= 0) {
					String content = temp.substring("<contenttitle>".length(),
							end);
					if (content.length() > 0)
						tmpMap.put("contenttitle", content);
					else
						tmpMap.put("contenttitle", "null");
				}
			} else if (temp.startsWith("<content>")) {
				int end = temp.lastIndexOf("</content>");
				if (end >= 0) {
					String content = temp.substring("<content>".length(), end);
					if (content.length() > 0)
						tmpMap.put("content", content);
					else
						tmpMap.put("content", "null");
				}
			} else if (temp.startsWith("<url>")) {
				int end = temp.lastIndexOf("</url>");
				if (end >= 0) {
					String content = temp.substring("<url>".length(), end);
					if (content.length() > 0)
						tmpMap.put("url", content);
					else
						tmpMap.put("url", "null");
				}
			} else if (temp.startsWith("<docno>")) {
				int end = temp.lastIndexOf("</docno>");
				if (end >= 0) {
					String content = temp.substring("<docno>".length(), end);
					if (content.length() > 0)
						tmpMap.put("docno", content);
					else
						tmpMap.put("docno", "null");
				}
			} else { // (temp.startsWith("</doc>"))
				HashMap<String, String> result = addTimeToMap(tmpMap);
				if (result != null) {
					products.add(result);
					n++;
				}
				isFirstElement = true;
				if (n == number)
					break;
			}
		}
		if (temp == null)
			br = null;
		return products;
	}
	private HashMap<String, String> addTimeToMap(HashMap<String, String> docMap) {
		for (String str : docMap.values()) {
			if (str.equals("null"))
				return null;
		}
		HashMap<String, String> element = new HashMap<String, String>();
		element.put("url", docMap.get("url"));
		element.put("docno", docMap.get("docno"));
		element.put("contenttitle", docMap.get("contenttitle"));
		element.put("content", docMap.get("content"));
		element.put("postTime", generateRandomDate());
		return element;
	}

	private static String generateRandomDate() {
		StringBuilder builder = new StringBuilder();
		builder.append("2013-");
		// get month
		String str1 = RandomStringUtils.random(1, "01");
		if (str1.equals("1"))
			builder.append(str1 + RandomStringUtils.random(1, "012"));
		else
			builder.append(str1 + RandomStringUtils.random(1, "123456789"));
		// get day
		builder.append("-");
		str1 = RandomStringUtils.random(1, "012");
		if (str1.equals("0"))
			builder.append(str1 + RandomStringUtils.random(1, "123456789"));
		else
			builder.append(str1 + RandomStringUtils.random(1, "0123456789"));
		builder.append(" ");
		// get hours
		str1 = RandomStringUtils.random(1, "012");
		if (str1.equals("2"))
			builder.append(str1 + RandomStringUtils.random(1, "0123"));
		else
			builder.append(str1 + RandomStringUtils.random(1, "0123456789"));
		builder.append(":");
		// get minutes
		str1 = RandomStringUtils.random(1, "012345");
		builder.append(str1 + RandomStringUtils.random(1, "0123456789"));
		builder.append(":");
		// get seconds
		str1 = RandomStringUtils.random(1, "012345");
		builder.append(str1 + RandomStringUtils.random(1, "0123456789"));
		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	public static List<String> readOneLine(BufferedReader reader, int size)
			throws IOException {
		String temp = null;
		List<String> entry = new LinkedList<String>();
		int n = 0;
		while ((temp = reader.readLine()) != null) {
			temp = temp.trim();
			entry.add(temp);
			n++;
			if (n >= size)
				break;
		}
		if (entry.size() > 0)
			return entry;
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> getTestData(BufferedReader reader)
			throws IOException {
		List<Map<String, String>> products = Lists.newArrayList();
		String temp = null;
		boolean isTitle = true;
		Map<String, String> entry = new HashMap<String, String>();
		while ((temp = reader.readLine()) != null) {
			temp = temp.trim();
			if (isTitle) {
				entry.clear();
				if (temp.startsWith("<contenttitle>")) {
					int end = temp.lastIndexOf("</contenttitle>");
					if (end >= 0) {
						String content = temp.substring(
								"<contenttitle>".length(), end);
						if (content.length() > 0)
							entry.put("title", content);
					}
				}
			} else {
				if (temp.startsWith("<content>")) {
					int end = temp.lastIndexOf("</content>");
					if (end >= 0) {
						String content = temp.substring("<content>".length(),
								end);
						if (content.length() > 0)
							entry.put("content", content);
					}
				}
			}
			if (!isTitle) {
				products.add((Map<String, String>) ((HashMap<String, String>) entry)
						.clone());
			}
			isTitle = (!isTitle);
		}
		List<Map<String, String>> entryList = Lists.newArrayList();
		for (int i = 0; i < products.size(); i++) {
			Map<String, String> element = new LinkedHashMap<String, String>();
			element.put("number", String.valueOf(i + 1)); // id start from 1
			element.put("cardid", RandomStringUtils.randomAlphabetic(10));
			element.put("date", generateRandomDate());
			element.put("title", products.get(i).get("title"));
			element.put("content", products.get(i).get("content"));
			entryList.add(element);
		}
		return entryList;
	}
}
