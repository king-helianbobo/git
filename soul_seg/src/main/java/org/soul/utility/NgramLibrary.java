package org.soul.utility;

import org.soul.domain.BigramEntry;
import org.soul.domain.Term;
import org.soul.domain.TermNature;
import org.soul.domain.TermNatures;

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
