package org.ansj.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
//import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ansj.domain.BigramEntry;

import org.ansj.treeSplit.IOUtil;
import org.ansj.treeSplit.StringUtil;

public class StaticVariable {

	public static Log LibraryLog = LogFactory.getLog(StaticVariable.class);
	public static boolean allowNameRecognize = true;
	public static boolean allowNumRecognize = true;
	public static boolean allowQuantifierRecognize = true;

	// public static String userLibrary = "library/default.dic";
	// public static String ambiguityLibrary = "library/ambiguity.dic";
	public static String userLibrary;
	public static String ambiguityLibrary;

	static {
		try {
			ResourceBundle rb = ResourceBundle.getBundle("library");
			// library.properties, must locate in classPath
			if (rb.containsKey("userLibrary"))
				userLibrary = rb.getString("userLibrary");
			if (rb.containsKey("ambiguityLibrary"))
				ambiguityLibrary = rb.getString("ambiguityLibrary");
		} catch (Exception e) {
			LibraryLog.warn("can't find library.properties in classpath!");
		}

	}

	public static BufferedReader getPersonReader() {
		return DictionaryReader.getReader("person/person.dic");
	}

	public static BufferedReader getCompanyReader() {
		return DictionaryReader.getReader("company/company.data");
	}

	public static BufferedReader getNewWordReader() {
		return DictionaryReader.getReader("newWord/new_word_freq.dic");
	}

	public static BufferedReader getBaseArrayReader() {
		return DictionaryReader.getReader("arrays.dic");
	}

	public static BufferedReader getNumberReader() {
		return DictionaryReader.getReader("numberLibrary.dic");
	}

	public static BufferedReader getEnglishReader() {
		return DictionaryReader.getReader("englishLibrary.dic");
	}

	public static BufferedReader getNatureMapReader() {
		return DictionaryReader.getReader("nature/nature.map");
	}

	public static BufferedReader getNatureTableReader() {
		return DictionaryReader.getReader("nature/nature.table");
	}

	public static BufferedReader getPersonFreqReader() {
		return DictionaryReader.getReader("person/name_freq.dic");
	}

	@SuppressWarnings("unchecked")
	public static Map<String, int[][]> getPersonFreqMap() {
		InputStream inputStream = null;
		ObjectInputStream objectInputStream = null;
		Map<String, int[][]> map = new HashMap<String, int[][]>(0);
		try {
			inputStream = DictionaryReader
					.getInputStream("person/asian_name_freq.data");
			objectInputStream = new ObjectInputStream(inputStream);
			map = (Map<String, int[][]>) objectInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (objectInputStream != null)
					objectInputStream.close();
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	// frequency between one word and another word
	// if word1 and word2 no frequency ,then frequency is set to 0
	public static BigramEntry[][] getBigramTables() {
		BigramEntry[][] result = new BigramEntry[0][0];
		BufferedReader reader = null;
		try {
			reader = IOUtil.getReader(
					DictionaryReader.getInputStream("bigramdict.dic"), "UTF-8");
			String temp = null;
			String[] strs = null;
			result = new BigramEntry[InitDictionary.arrayLength][0];
			int fromId = 0;
			int toId = 0;
			int freq = 0;
			BigramEntry to = null;
			while ((temp = reader.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					continue;
				}
				strs = temp.split("\t");
				freq = Integer.parseInt(strs[1]);
				strs = strs[0].split("@");
				if ((fromId = InitDictionary.getWordId(strs[0])) <= 0) {
					fromId = 0;
				}
				if ((toId = InitDictionary.getWordId(strs[1])) <= 0) {
					toId = -1;
				}

				to = new BigramEntry(toId, freq);
				int index = Arrays.binarySearch(result[fromId], to);
				if (index > -1) { // if founded
					continue;
				} else {
					BigramEntry[] branch = new BigramEntry[result[fromId].length + 1];
					int insert = -(index + 1);
					System.arraycopy(result[fromId], 0, branch, 0, insert);
					System.arraycopy(result[fromId], insert, branch,
							insert + 1, result[fromId].length - insert);
					branch[insert] = to;
					result[fromId] = branch;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtil.close(reader);
		}
		return result;
	}
}
