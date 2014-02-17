package org.soul.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.PersonNatureAttr;
import org.soul.recognition.AsianNameRecognition;
import org.soul.utility.CompactHashMap;
import org.soul.utility.MyStaticValue;

public class PersonAttrLibrary {

	private static Log log = LogFactory.getLog(PersonAttrLibrary.class);
	private CompactHashMap<String, PersonNatureAttr> pnMap = null;

	public PersonAttrLibrary() {
	}

	public CompactHashMap<String, PersonNatureAttr> getPersonMap()
			throws NumberFormatException, IOException {
		if (pnMap != null) {
			return pnMap;
		}
		init1();
		init2();
		return pnMap;
	}

	private void logPersonFreqMap(Map<String, int[][]> map) {
		Set<Entry<String, int[][]>> entrySet = map.entrySet();
		for (Entry<String, int[][]> entry : entrySet) {
			StringBuilder str = new StringBuilder();
			str.append(entry.getKey() + "[ ");
			int ints[][] = entry.getValue();
			for (int i = 0; i < ints.length; i++) {
				str.append("(");
				for (int j = 0; j < ints[i].length; j++)
					str.append(ints[i][j] + " ");
				str.append(")");
			}
			str.append("]");
			log.info(str.toString());
		}
	}
	// "person/asian_name_freq.data"
	private void init2() throws NumberFormatException, IOException {
		Map<String, int[][]> personFreqMap = MyStaticValue.getPersonFreqMap();
		// logPersonFreqMap(personFreqMap);
		Set<Entry<String, int[][]>> entrySet = personFreqMap.entrySet();
		PersonNatureAttr pna = null;
		for (Entry<String, int[][]> entry : entrySet) {
			String key = entry.getKey().trim();
			pna = pnMap.get(key);
			if (pna == null) {
				pna = new PersonNatureAttr();
				pna.setlocFreq(entry.getValue());
				pnMap.put(key, pna);
			} else {
				pna.setlocFreq(entry.getValue());
			}
		}
		personFreqMap = null;
	}

	// person.dic
	private void init1() throws NumberFormatException, IOException {
		BufferedReader br = null;
		try {
			pnMap = new CompactHashMap<String, PersonNatureAttr>();
			br = MyStaticValue.getPersonReader(); // "person/person.dic"
			String temp = null;
			PersonNatureAttr pna = null;
			while ((temp = br.readLine()) != null) {
				String[] strs = temp.split("\t");
				String str = strs[0].trim();
				pna = pnMap.get(str);
				if (pna == null) {
					pna = new PersonNatureAttr();
				}
				pna.addFreq(Integer.parseInt(strs[1]),
						Integer.parseInt(strs[2]));

				pnMap.put(str, pna);
			}
		} finally {
			if (br != null)
				br.close();
		}
	}
}
