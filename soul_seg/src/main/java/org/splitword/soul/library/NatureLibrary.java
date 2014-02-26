package org.splitword.soul.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import org.splitword.soul.domain.NatureInLib;
import org.splitword.soul.domain.Term;
import org.splitword.soul.treeSplit.StringUtil;
import org.splitword.soul.utility.MyStaticValue;

public class NatureLibrary {

	private static final int YI = 1;
	private static final int FYI = -1;
	private static final HashMap<String, NatureInLib> NATUREMAP = new HashMap<String, NatureInLib>();
	private static int[][] NATURETABLE = null;

	static {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("词性列表加载失败!");
		}
	}

	private static void init() throws IOException {
		String split = "\t";
		BufferedReader reader = MyStaticValue.getNatureMapReader();
		String temp = null;
		String[] strs = null;
		int maxLength = 0;
		int p0 = 0;
		int p1 = 0;
		int p2 = 0;
		while ((temp = reader.readLine()) != null) {
			strs = temp.split(split);
			if (strs.length != 4)
				continue;
			p0 = Integer.parseInt(strs[0]);
			p1 = Integer.parseInt(strs[1]);
			p2 = Integer.parseInt(strs[3]);
			NATUREMAP.put(strs[2], new NatureInLib(strs[2], p0, p1, p2));
			maxLength = Math.max(maxLength, p1);
		}
		reader.close();

		// 加载词性关系
		NATURETABLE = new int[maxLength + 1][maxLength + 1];
		reader = MyStaticValue.getNatureTableReader();
		int j = 0;
		while ((temp = reader.readLine()) != null) {
			if (StringUtil.isBlank(temp))
				continue;
			strs = temp.split(split);
			for (int i = 0; i < strs.length; i++) {
				NATURETABLE[j][i] = Integer.parseInt(strs[i]);
			}
			j++;
		}
		reader.close();
	}

	/**
	 * 获得两个词性之间的频率
	 */
	public static int getTwoNatureFreq(NatureInLib from, NatureInLib to) {
		if (from.index < 0 || to.index < 0) {
			return 0;
		}
		return NATURETABLE[from.index][to.index];
	}

	/**
	 * 获得两个term之间的频率
	 * 
	 * @param fromTerm
	 * @param toTerm
	 * @return
	 */
	public static int getTwoTermFreq(Term fromTerm, Term toTerm) {
		NatureInLib from = fromTerm.getNatrue();
		NatureInLib to = toTerm.getNatrue();
		if (from.index < 0 || to.index < 0) {
			return 0;
		}
		return NATURETABLE[from.index][to.index];
	}

	/**
	 * 根据字符串获得词性
	 * 
	 * @param natureStr
	 * @return
	 */
	public static NatureInLib getNature(String natureStr) {
		NatureInLib nature = NATUREMAP.get(natureStr);
		if (nature == null) {
			nature = new NatureInLib(natureStr, FYI, FYI, YI);
			NATUREMAP.put(natureStr, nature);
			return nature;
		}
		return nature;
	}

}
