package org.soul.utility;

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
import org.soul.domain.PairEntry;
import org.soul.library.InitDictionary;
import org.soul.treeSplit.IOUtil;
import org.soul.treeSplit.StringUtil;

public class MyStaticValue {

	public static Log libLog = LogFactory.getLog(MyStaticValue.class);
	public static boolean allowNameRecognize = true;
	public static boolean allowNumRecognize = true;
	public static boolean allowQuantifierRecognize = true;
	public static String userLibrary;
	public static String ambiguityLibrary;
	public static String stopLibrary;
	public static String synonymLibrary;

	static {
		try {
			ResourceBundle rb = ResourceBundle.getBundle("library");
			// library.properties, must locate in classPath
			if (rb.containsKey("userLibrary"))
				userLibrary = rb.getString("userLibrary");
			else
				userLibrary = "library/default.dic";
			if (rb.containsKey("ambiguityLibrary"))
				ambiguityLibrary = rb.getString("ambiguityLibrary");
			else
				ambiguityLibrary = "library/ambiguity.dic";
			if (rb.containsKey("stopLibrary"))
				stopLibrary = rb.getString("stopLibrary");
			else
				stopLibrary = "library/stopWord.dic";
			if (rb.containsKey("synonymLibrary"))
				synonymLibrary = rb.getString("synonymLibrary");
			else
				synonymLibrary = "library/synonym.txt";
		} catch (Exception e) {
			libLog.warn("can't find library.properties in classpath!");
		}
	}

	public static BufferedReader getPersonReader() {
		return DictionaryReader.getReader("person/person.dic");
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
