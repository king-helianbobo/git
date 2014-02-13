package org.soul.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.treeSplit.StringUtil;
import org.soul.utility.MyStaticValue;

public class CompanyAttrLib {
	private static Log log = LogFactory.getLog(CompanyAttrLib.class);
	private static HashMap<String, int[]> cnMap = null;
	public CompanyAttrLib() {
	}
	public static HashMap<String, int[]> getCompanyMap() {
		if (cnMap != null) {
			return cnMap;
		}
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
			cnMap = new HashMap<String, int[]>();
		}
		return cnMap;
	}

	private static void init() throws NumberFormatException, IOException {
		BufferedReader br = null;
		try {
			cnMap = new HashMap<String, int[]>();
			br = MyStaticValue.getCompanyReader();
			String temp = null;
			String[] strs = null;
			int[] cna = null;
			while ((temp = br.readLine()) != null) {
				strs = temp.split("\t");
				cna = new int[2];
				cna[0] = Integer.parseInt(strs[1]);
				cna[1] = Integer.parseInt(strs[2]);
				cnMap.put(strs[0], cna);
			}
		} finally {
			if (br != null)
				br.close();
		}
	}

}
