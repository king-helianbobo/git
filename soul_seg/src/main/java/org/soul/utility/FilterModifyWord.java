package org.soul.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.soul.domain.NatureInLib;
import org.soul.domain.Term;

public class FilterModifyWord {

	public static final String _stop = "_stop";
	private static HashMap<String, String> updateDic = new HashMap<String, String>();

	public static void setUpdateDic(HashMap<String, String> updateDic) {
		FilterModifyWord.updateDic = updateDic;
	}

	public static HashMap<String, String> getUpdateDic() {
		return updateDic;
	}

	public static List<Term> modifResult(List<Term> all) {
		return modifResult(all, updateDic);
	}

	public static List<Term> modifResult(List<Term> all,
			HashMap<String, String> updateDic) {
		List<Term> result = new ArrayList<Term>();
		try {
			String natureStr = null;
			for (Term term : all) {
				natureStr = updateDic.get(term.getName());
				if (natureStr == null) {
					result.add(term);
					continue;
				}
				if (!_stop.equals(natureStr)) {
					term.setNature(new NatureInLib(natureStr));
					result.add(term);
				}
			}
		} catch (Exception e) {
			System.err
					.println("FilterStopWord.updateDic can not be null , "
							+ "you must use set FilterStopWord.setUpdateDic(map) or use method set map");
		}
		return result;
	}
}
