package org.soul.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.CompanyNature;
import org.soul.treeSplit.StringUtil;

public class CompanyAttrLib {

	private static Log log = LogFactory.getLog(CompanyAttrLib.class);
	private HashMap<String, CompanyNature> cnMap = null;

	public CompanyAttrLib() {
	}

	public HashMap<String, CompanyNature> getCompanyMap()
			throws NumberFormatException, IOException {
		if (cnMap != null) {
			return cnMap;
		}
		init();
		return cnMap;
	}

	// company_freq
	private void init() throws NumberFormatException, IOException {
		BufferedReader br = null;
		try {
			cnMap = new HashMap<String, CompanyNature>();
			br = StaticVariable.getCompanyReader(); // company/company.data
			String temp = null;
			String[] strs = null;
			CompanyNature cna = null;

			int p = 0;
			int b = 0;
			int m = 0;
			int e = 0;
			int s = 0;
			int allFreq = 0;

			while ((temp = br.readLine()) != null) {
				strs = temp.split("\t");
				p = Integer.parseInt(strs[1]);
				b = Integer.parseInt(strs[2]);
				m = Integer.parseInt(strs[3]);
				e = Integer.parseInt(strs[4]);
				s = Integer.parseInt(strs[5]);
				allFreq = Integer.parseInt(strs[6]);
				cna = new CompanyNature(p, b, m, e, s, allFreq);
				cnMap.put(strs[0], cna);
			}
		} finally {
			if (br != null)
				br.close();
		}
	}

	public static double[] loadFactory() {
		BufferedReader reader = DictionaryReader.getReader("company/company.map");
		String temp = null;
		double[] factory = new double[51];
		String[] strs = null;
		int index = 0;
		float fac = 0;
		try {
			while ((temp = reader.readLine()) != null) {
				if (StringUtil.isBlank(temp = temp.trim()))
					continue;
				strs = temp.split("\t");
				index = Integer.parseInt(strs[0]);
				fac = Float.parseFloat(strs[2]);
				if (index > 50) {
					index = 50;
				}
				factory[index] += fac; // 机构名长度不要超过50
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return factory;
	}
}
