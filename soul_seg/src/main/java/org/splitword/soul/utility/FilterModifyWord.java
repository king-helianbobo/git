package org.splitword.soul.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.splitword.soul.domain.NatureInLib;
import org.splitword.soul.domain.Term;

public class FilterModifyWord {
	private static Log log = LogFactory.getLog(FilterModifyWord.class);
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
				} else if (!_stop.equals(natureStr)) {
					term.setNature(new NatureInLib(natureStr));
					result.add(term);
				}
			}
		} catch (Exception e) {
			log.error("FilterStopWord.updateDic can not be null!");
		}
		return result;
	}
}
