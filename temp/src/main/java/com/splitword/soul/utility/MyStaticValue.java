package com.splitword.soul.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MyStaticValue {

	public static Log libLog = LogFactory.getLog(MyStaticValue.class);
	public static boolean allowNameRecognize = true;
	public static boolean allowNumRecognize = true;
	public static boolean allowQuantifierRecognize = true;

	public static BufferedReader getPersonReader() {
		return DictionaryReader.getReader("person/person.dic");
	}

	public static BufferedReader userDefineLibrary1Reader() {
		return DictionaryReader.getReader("dictionary/default.dic");
	}

	public static BufferedReader userDefineLibrary2Reader() {
		return DictionaryReader.getReader("dictionary/wuxi.dic");
	}

	public static BufferedReader userDefineLibrary3Reader() {
		return DictionaryReader.getReader("dictionary/newword.dic");
	}

	public static BufferedReader ambiguityLibraryReader() {
		return DictionaryReader.getReader("dictionary/ambiguity.dic");
	}

	public static BufferedReader stopWordReader() {
		return DictionaryReader.getReader("dictionary/stopWord.dic");
	}

	public static BufferedReader getCompanyReader() {
		return DictionaryReader.getReader("company/company.data");
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

}
