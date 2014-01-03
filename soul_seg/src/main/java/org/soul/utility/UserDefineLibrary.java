package org.soul.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.treeSplit.*;

import static org.soul.utility.StaticVariable.LibraryLog;

public class UserDefineLibrary {

	// public static final String DEFAULT_NATURE = ;
	private static Log log = LogFactory.getLog(UserDefineLibrary.class);
	public static final Integer DEFAULT_FREQ = 1000;

	public static final String DEFAULT_FREQ_STR = "1000";

	public static Forest FOREST = null;

	public static Forest ambiguityForest = null;

	static {
		initUserLibrary();
		initAmbiguityLibrary();
	}

	/**
	 * 关键词增加
	 * 
	 * @param keyWord
	 *            需要增加的关键词
	 * @param nature
	 *            关键词的词性
	 * @param freq
	 *            关键词的词频
	 */
	public static void insertWord(String keyword, String nature, int freq) {
		String[] paramers = new String[2];
		paramers[0] = nature;
		paramers[1] = String.valueOf(freq);
		Value value = new Value(keyword, paramers);
		Library.insertWord(FOREST, value);
	}

	private static boolean isOverlap(String str1, String str2) {
		int len1 = str1.length();
		int len2 = str2.length();
		int len = Math.min(len1, len2);
		for (int i = 1; i <= len; i++) {
			if (str1.substring(0, i).equals(str2.substring(len2 - i)))
				return true;
			if (str2.substring(0, i).equals(str1.substring(len1 - i)))
				return true;
		}
		return false;
	}

	private static void checkAmbiguity(File file) throws Exception {
		FileInputStream fs = new FileInputStream(file);
		BufferedReader br = IOUtil.getReader(fs, "UTF-8");
		TreeMap<Integer, String> array = new TreeMap<Integer, String>();
		int i = 0;
		String temp = null;
		while ((temp = br.readLine()) != null) {
			String[] param = temp.split("\t");
			array.put(i, param[0]);
			++i;
		}
		int size = i;
		for (i = 0; i < size; i++) {
			String stri = array.get(i);
			for (int j = i + 1; j < size; j++) {
				String strj = array.get(j);
				if (isOverlap(stri, strj)) {
					log.error("word " + stri + " conflict with " + strj);
				}
			}
		}
	}

	/**
	 * load ambiguity sentence library
	 */
	private static void initAmbiguityLibrary() {
		String ambiguityLibrary = StaticVariable.ambiguityLibrary;
		if (StringUtil.isBlank(ambiguityLibrary)) {
			LibraryLog.warn("init ambiguity  waring :" + ambiguityLibrary
					+ " because : not find that file or can not to read !");
			return;
		}
		File file = new File(ambiguityLibrary);
		if (file.isFile() && file.canRead()) {
			try {
				checkAmbiguity(file);// 检查文件是否合法，它不应该再引入歧义
				ambiguityForest = Library.makeForest(ambiguityLibrary);
			} catch (Exception e) {
				LibraryLog.error("init ambiguity  error :" + ambiguityLibrary
						+ " because : not find that file or can not to read !");
				e.printStackTrace();
			}
		} else {
			LibraryLog.warn("init ambiguity  waring :" + ambiguityLibrary
					+ " because : not find that file or can not to read !");
		}
	}

	// 加载用户自定义词典和补充词典
	private static void initUserLibrary() {
		try {
			FOREST = new Forest();
			String userLibrary = StaticVariable.userLibrary;
			loadLibrary(FOREST, userLibrary);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 加载由单个文件构成的词典
	private static void loadFile(Forest forest, File file) {
		if (!file.canRead()) {
			LibraryLog.warn("file in path " + file.getAbsolutePath()
					+ " can not to read!");
			return;
		}
		String temp = null;
		BufferedReader br = null;
		String[] strs = null;
		Value value = null;
		try {
			br = IOUtil.getReader(new FileInputStream(file), "UTF-8");
			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					continue;
				} else {
					strs = temp.split("\t");
					if (strs.length != 3) {
						value = new Value(strs[0], "userDefine", "1000");
					} else {
						value = new Value(strs[0], strs[1], strs[2]);
					}
					Library.insertWord(forest, value);
				}
			}
			LibraryLog.info("init user userLibrary ok path is : "
					+ file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtil.close(br);
			br = null;
		}
	}

	// 加载用户自定义词典
	public static void loadLibrary(Forest forest, String path) {
		File file = new File(path);
		if (!file.canRead() || file.isHidden()) {
			LibraryLog.warn("init userLibrary  waring :" + path
					+ " because : not find that file or can not to read !");
			return;
		}
		if (file.isFile()) {
			loadFile(forest, file);
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().trim().endsWith(".dic")) {
					loadFile(forest, files[i]);
				}
			}
		} else {
			LibraryLog.warn("init user library  error :" + path
					+ " because : not find that file !");
		}
	}

	// remove key word
	public static void removeWord(String word) {
		Library.removeWord(FOREST, word);
	}

	public static void clear() {
		FOREST.clear();
	}

}
