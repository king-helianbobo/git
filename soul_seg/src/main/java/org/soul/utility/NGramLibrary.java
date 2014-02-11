package org.soul.utility;

import org.soul.domain.PairEntry;
import org.soul.domain.Term;
import org.soul.domain.TermNature;
import org.soul.domain.TermNatures;

public class NGramLibrary {
	private static PairEntry[][] bigramTable = null;
	static {
		try {
			long start = System.currentTimeMillis();
			bigramTable = MyStaticValue.getBigramTables();
			MyStaticValue.LibraryLog.info("init bigramTable , use time :"
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
		return -(low + 1); // key not found.
	}

	public static void setBigramTables(PairEntry[][] bigramTables) {
		NGramLibrary.bigramTable = bigramTables;
	}
}
