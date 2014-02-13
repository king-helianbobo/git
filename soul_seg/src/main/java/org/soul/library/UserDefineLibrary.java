package org.soul.library;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.treeSplit.*;
import org.soul.utility.MyStaticValue;
import org.soul.utility.WordAlter;

import static org.soul.utility.MyStaticValue.libLog;

public class UserDefineLibrary {
	private static Log log = LogFactory.getLog(UserDefineLibrary.class);
	public static final Integer DEFAULT_FREQ = 1000;
	public static final String DEFAULT_FREQ_STR = "1000";
	public static Forest userDefineForest = null;
	public static Forest ambiguityForest = null;
	static {
		initUserLibrary();
		initAmbiguityLibrary();
	}

	/**
	 * 
	 * @author LiuBo
	 * @since 2014年1月15日
	 * @param key
	 * @param nature
	 * @param freq
	 *            void
	 */
	public static void insertWordToUserDefineLibrary(String key, String nature,
			int freq) {
		String[] paramers = new String[2];
		paramers[0] = WordAlter.alterAlphaAndNumber(nature);
		paramers[1] = String.valueOf(freq);
		TrieValue value = new TrieValue(WordAlter.alterAlphaAndNumber(key), paramers);
		LibraryToForest.insertWord(userDefineForest, value);
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
		TreeMap<Integer, String> tree = new TreeMap<Integer, String>();
		int i = 0;
		String temp = null;
		while ((temp = br.readLine()) != null) {
			String[] param = temp.split("\t");
			tree.put(i, param[0]);
			++i;
		}
		int size = i;
		for (i = 0; i < size; i++) {
			String stri = tree.get(i);
			for (int j = i + 1; j < size; j++) {
				String strj = tree.get(j);
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
		String ambiguityLibrary = MyStaticValue.ambiguityLibrary;
		if (StringUtil.isBlank(ambiguityLibrary)) {
			libLog.warn("init ambiguity  waring :" + ambiguityLibrary
					+ " because : not find that file or can not to read !");
			return;
		}
		File file = new File(ambiguityLibrary);
		if (file.isFile() && file.canRead()) {
			try {
				checkAmbiguity(file);
				// 检查文件是否合法，它不应该再引入歧义，任意两个词之间不应该有重叠
				ambiguityForest = LibraryToForest.makeForest(ambiguityLibrary);
			} catch (Exception e) {
				libLog.error("init ambiguity error :" + ambiguityLibrary
						+ " because : not find file or can not be read!");
				e.printStackTrace();
			}
		} else {
			libLog.warn("init ambiguity  waring :" + ambiguityLibrary
					+ " because : not find file or can not be read!");
		}
	}

	// 加载用户自定义词典和补充词典
	private static void initUserLibrary() {
		try {
			userDefineForest = new Forest();
			String userLibrary = MyStaticValue.userLibrary;
			loadLibrary(userDefineForest, userLibrary);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void loadFile(Forest forest, File file) {
		// 加载由单个文件构成的词典
		if (!file.canRead()) {
			libLog.warn("file in path " + file.getAbsolutePath()
					+ " can not be read!");
			return;
		}
		String temp = null;
		BufferedReader br = null;
		String[] strs = null;
		TrieValue value = null;
		try {
			br = IOUtil.getReader(new FileInputStream(file), "UTF-8");
			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					continue;
				} else {
					strs = WordAlter.alterAlphaAndNumber(temp).split("\t");
					if (strs.length != 3) {
						// "userDefine" would be default termNature
						value = new TrieValue(strs[0], "userDefine", "1000");
					} else {
						value = new TrieValue(strs[0], strs[1], strs[2]);
					}
					LibraryToForest.insertWord(forest, value);
				}
			}
			libLog.info("init userLibrary ok,path is : "
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
			libLog.warn("init userLibrary  waring :" + path
					+ " because : not find file or can not be read!");
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
			libLog.warn("init user library  error :" + path
					+ " because : not find file!");
		}
	}

	public static void removeWordInUserDefineLibrary(String word) {
		LibraryToForest.removeWord(userDefineForest, word);
	}

	public static void clearUserDefineLibrary() {
		userDefineForest.clear();
	}

}
