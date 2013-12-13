package org.soul.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import org.soul.domain.NewWordNature;

//新词词典
public class NewWordAttrLib {
	private HashMap<String, NewWordNature> nwMap = null;

	public NewWordAttrLib() {
	}

	public HashMap<String, NewWordNature> getNewWordMap()
			throws NumberFormatException, IOException {
		if (nwMap != null) {
			return nwMap;
		}
		init();
		return nwMap;
	}

	// company_freq

	private void init() throws NumberFormatException, IOException {
		BufferedReader br = null;
		try {
			nwMap = new HashMap<String, NewWordNature>();
			br = StaticVariable.getNewWordReader();
			String temp = null;
			String[] strs = null;
			NewWordNature nna = null;

			int b = 0;
			int m = 0;
			int e = 0;

			while ((temp = br.readLine()) != null) {
				strs = temp.split("\t");
				b = Integer.parseInt(strs[1]);
				m = Integer.parseInt(strs[2]);
				e = Integer.parseInt(strs[3]);
				nna = new NewWordNature(b, m, e);
				nwMap.put(strs[0], nna);
			}
		} finally {
			if (br != null)
				br.close();
		}
	}
}
