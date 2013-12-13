package org.ansj.util;

import org.ansj.domain.BigramEntry;
import org.ansj.domain.Term;
import org.ansj.domain.TermNature;
import org.ansj.domain.TermNatures;

public class NgramLibrary {
	private static BigramEntry[][] bigramTable = null;
	static {
		try {
			long start = System.currentTimeMillis();
			bigramTable = StaticVariable.getBigramTables();
			StaticVariable.LibraryLog.info("init bigramTable , use time :"
					+ (System.currentTimeMillis() - start) + " milliseconds");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Term from = new Term("阿", 0, new TermNatures(TermNature.NULL));
		from.getTermNatures().id = InitDictionary.getWordId(from.getName());
		Term to = new Term("全国", 0, new TermNatures(TermNature.NULL));
		to.getTermNatures().id = InitDictionary.getWordId(to.getName());
		StaticVariable.LibraryLog.info(getTwoWordFreq(from, to) + ","
				+ to.getTermNatures().id + "," + from.getTermNatures().id);
	}

	/**
	 * find frequency between one word and another word
	 */
	public static int getTwoWordFreq(Term from, Term to) {
		// id is the baseValue of Term(from) in base array
		if (from.getTermNatures().id < 0) {
			return 0;
		}
		BigramEntry[] be = bigramTable[from.getTermNatures().id];
		int index = binarySearch(be, to.getTermNatures().id);
		if (index < 0) {
			return 0;
		}
		return be[index].freq;
	}

	private static int binarySearch(BigramEntry[] be, int key) {
		int low = 0;
		int high = be.length - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			BigramEntry midVal = be[mid];
			int cmp = midVal.id - key;
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	public static void setBigramTables(BigramEntry[][] bigramTables) {
		NgramLibrary.bigramTable = bigramTables;
	}
}
