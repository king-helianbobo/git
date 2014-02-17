package org.soul.library;

import java.io.BufferedReader;
import java.util.Arrays;

import org.soul.domain.PairEntry;
import org.soul.domain.Term;
import org.soul.treeSplit.IOUtil;
import org.soul.treeSplit.StringUtil;
import org.soul.utility.DictionaryReader;
import org.soul.utility.MyStaticValue;

public class BiGramLibrary {
	private static PairEntry[][] bigramTable = null;
	static {
		try {
			long start = System.currentTimeMillis();
			bigramTable = getBigramTables();
			MyStaticValue.libLog.info("init bigramTable , use time :"
					+ (System.currentTimeMillis() - start) + " milliseconds");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * find frequency between one word and another word
	 */
	public static int getTwoWordFreq(Term from, Term to) {
		// id is the baseValue of Term(from) in base array
		if (from.getTermNatures().id < 0) {
			return 0;
		}
		PairEntry[] be = bigramTable[from.getTermNatures().id];
		int index = binarySearch(be, to.getTermNatures().id);
		if (index < 0) {
			return 0;
		}
		return be[index].freq;
	}

	// frequency between one word and another word
	// if word1 and word2 no frequency ,then frequency is set to 0
	public static PairEntry[][] getBigramTables() {
		PairEntry[][] result = new PairEntry[InitDictionary.arrayLength][0];
		BufferedReader reader = null;
		try {
			reader = IOUtil.getReader(
					DictionaryReader.getInputStream("bigramdict.dic"), "UTF-8");
			String temp = null;
			String[] strs = null;
			int fromId = 0;
			int toId = 0;
			int freq = 0;
			PairEntry toEntry = null;
			while ((temp = reader.readLine()) != null) {
				if (StringUtil.isBlank(temp))
					continue;
				strs = temp.split("\t");
				freq = Integer.parseInt(strs[1]);
				strs = strs[0].split("@");
				if ((fromId = InitDictionary.getWordId(strs[0])) <= 0) {
					fromId = 0;
				}
				if ((toId = InitDictionary.getWordId(strs[1])) <= 0) {
					toId = -1;
				}
				toEntry = new PairEntry(toId, freq);
				int index = Arrays.binarySearch(result[fromId], toEntry);
				if (index > -1) { // if founded
					continue;
				} else {
					PairEntry[] branch = new PairEntry[result[fromId].length + 1];
					int insPos = -(index + 1);
					System.arraycopy(result[fromId], 0, branch, 0, insPos);
					System.arraycopy(result[fromId], insPos, branch,
							insPos + 1, result[fromId].length - insPos);
					branch[insPos] = toEntry;
					result[fromId] = branch;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtil.close(reader);
		}
		return result;
	}

	private static int binarySearch(PairEntry[] be, int key) {
		int low = 0;
		int high = be.length - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			PairEntry midVal = be[mid];
			int cmp = midVal.id - key;
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found
	}

}
