package org.soul.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.CompanyNature;
import org.soul.domain.NewWordNature;
import org.soul.domain.PersonNatureAttr;
import org.soul.domain.TermNature;
import org.soul.domain.TermNatures;
import org.soul.treeSplit.StringUtil;

public class InitDictionary {
	// private static Log log = LogFactory.getLog(InitDictionary.class);
	public static int arrayLength;
	public static final char[] IN_SYSTEM = new char[65536];

	public static int[] base = null;
	public static int[] check = null;
	public static byte[] status = null;
	public static String[] words = null;
	public static TermNatures[] termNatures = null;

	static {
		init();
	}

	private static void init() {
		long start = System.currentTimeMillis();
		try {
			initArrays();
			StaticVariable.LibraryLog.info("init core library ok use time :"
					+ (System.currentTimeMillis() - start) + " milliseconds");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对于base,check,nature,status的加载 0.代表这个字不在词典中 1.继续 2.是个词但是还可以继续 3.停止已经是个词了
	 * 
	 * @throws Exception
	 */
	public static void initArrays() throws Exception {
		BufferedReader reader = StaticVariable.getBaseArrayReader();
		initArraySize(reader);
		reader.close();
		reader = StaticVariable.getBaseArrayReader();
		initArrays(reader);
		reader.close();
	}

	private static void initArraySize(BufferedReader reader) throws IOException {
		String temp = null;
		String last = null;
		while ((temp = reader.readLine()) != null) {
			last = temp;
		}
		String[] strs = last.split("	");
		arrayLength = Integer.parseInt(strs[0]) + 1;
		base = new int[arrayLength];
		check = new int[arrayLength];
		status = new byte[arrayLength];
		words = new String[arrayLength];
		termNatures = new TermNatures[arrayLength];
	}

	private static void initArrays(BufferedReader reader) throws Exception {

		HashMap<String, PersonNatureAttr> personMap = new PersonAttrLibrary()
				.getPersonMap();
		PersonNatureAttr personAttr = null;
		HashMap<String, CompanyNature> companyMap = new CompanyAttrLib()
				.getCompanyMap();
		CompanyNature companyAttr = null;
		HashMap<String, NewWordNature> newWordMap = new NewWordAttrLib()
				.getNewWordMap();
		NewWordNature newWordAttr = null;

		String temp = null;
		int num = 0;
		while ((temp = reader.readLine()) != null) {
			String[] strs = temp.split("	");
			num = Integer.parseInt(strs[0]);
			base[num] = Integer.parseInt(strs[2]);
			check[num] = Integer.parseInt(strs[3]);
			status[num] = Byte.parseByte(strs[4]);
			if (!"null".equals(strs[5])) { // 词性不为空的词
				words[num] = strs[1];
				if (status[num] < 4) {
					for (int i = 0; i < strs[1].length(); i++) {
						IN_SYSTEM[strs[1].charAt(i)] = strs[1].charAt(i);
					}
				}
				TermNatures tn = new TermNatures(
						TermNature.setNatureStrToArray(strs[5], strs[1]), num);

				// 人名属性如果为空，则personNatureAttr为personNatureAttr.NULL
				if ((personAttr = personMap.get(strs[1])) != null) {
					tn.setPersonNatureAttr(personAttr);
				}
				// 是否地名属性
				if ((companyAttr = companyMap.get(strs[1])) != null) {
					tn.setCompanyAttr(companyAttr);
				}
				// 是否新词属性
				if ((newWordAttr = newWordMap.get(strs[1])) != null) {
					newWordAttr.updateAll(tn.allFreq);
					tn.setNewWordAttr(newWordAttr);
				}
				termNatures[num] = tn;
			}
		}
		// 人名词性补录
		Set<Entry<String, PersonNatureAttr>> entrySet = personMap.entrySet();
		char c = 0;
		TermNatures tn = null;
		for (Entry<String, PersonNatureAttr> entry : entrySet) {
			if (entry.getKey().length() == 1) {
				c = entry.getKey().charAt(0);
				if (status[c] > 1) {
					continue;
				}
				if (status[c] == 0) {
					base[c] = c;
					check[c] = -1;
					status[c] = 3;
					words[c] = entry.getKey();
				}
				if ((tn = termNatures[c]) == null) {
					tn = new TermNatures(TermNature.NR);
				}
				tn.setPersonNatureAttr(entry.getValue());
				termNatures[c] = tn;
			}
		}

		// 机构词性补录
		Set<Entry<String, CompanyNature>> cnSet = companyMap.entrySet();
		for (Entry<String, CompanyNature> entry : cnSet) {
			if (entry.getKey().length() == 1) {
				c = entry.getKey().charAt(0);
				if (status[c] > 1) {
					continue;
				}
				if (status[c] == 0) {
					base[c] = c;
					check[c] = -1;
					status[c] = 3;
					words[c] = entry.getKey();
				}

				if ((tn = termNatures[c]) == null) {
					tn = new TermNatures(TermNature.NULL);
				}
				tn.setCompanyAttr(entry.getValue());
				termNatures[c] = tn;
			}
		}
		// traditional Chinese to simplified Chinese
		BufferedReader reader2 = DictionaryReader.getReader("jianFan.dic");
		while ((temp = reader2.readLine()) != null) {
			temp = temp.trim();
			if (StringUtil.isBlank(temp)) {
				continue;
			}
			if (IN_SYSTEM[temp.charAt(0)] == 0) {
				IN_SYSTEM[temp.charAt(0)] = temp.charAt(2);
			}
		}
		reader.close();
		reader2.close();
	}

	// 判断一个词是否在词典中存在
	public static boolean isInSystemDic(String str) {
		if (StringUtil.isBlank(str)) {
			return true;
		}
		int baseValue = str.charAt(0);
		int checkValue = 0;
		for (int i = 1; i < str.length(); i++) {
			checkValue = baseValue;
			baseValue = base[baseValue] + str.charAt(i);
			if (baseValue > check.length - 1)
				return false;
			if (check[baseValue] != -1 && check[baseValue] != checkValue) {
				return false;
			}
		}
		return status[baseValue] > 1;
	}

	// word's id in array.dic
	public static int getWordId(String str) {
		if (StringUtil.isBlank(str)) {
			return 0;
		}
		int baseValue = str.charAt(0);
		int checkValue = 0;
		for (int i = 1; i < str.length(); i++) {
			checkValue = baseValue;
			baseValue = base[baseValue] + str.charAt(i);
			if (baseValue > check.length - 1)
				return 0;
			if (check[baseValue] != -1 && check[baseValue] != checkValue) {
				return 0;
			}
		}
		return baseValue;
	}

	public static char TraditionalToSimplified(char c) {
		char value = IN_SYSTEM[c];
		if (value == 0) {
			return c;
		}
		return value;
	}

}
