package com.elasticsearch.application.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

import com.elasticsearch.application.ClientStaticValue;
import com.splitword.soul.utility.SoulArrays;

public class SoulQuery {
	private static final Log log = LogFactory.getLog(SoulQuery.class);

	public static final String titleField = "contenttitle";
	public static final String contentField = "content";
	protected Map<Integer, List<Map<String, Object>>> posMaps = null;
	private Map<Integer, List<Map<String, Object>>> synonyms = new HashMap<Integer, List<Map<String, Object>>>();
	private Map<Integer, List<Map<String, Object>>> vectors = new HashMap<Integer, List<Map<String, Object>>>();
	private Map<Integer, QueryInfoPojo> terms = new HashMap<Integer, QueryInfoPojo>();
	private Set<Integer> nounWords = new HashSet<Integer>();
	protected Map<Integer, List<String>> seqMap = new HashMap<Integer, List<String>>();

	/***************************** data member ****************************************/

	public SoulQuery(Map<Integer, List<Map<String, Object>>> originalMaps) {
		this.posMaps = originalMaps;
	}

	protected List<List<String>> fillList() {
		fillBaseMap();
		fillNounWordMap();
		Map<Integer, Integer> map1 = new HashMap<Integer, Integer>();
		for (int i = 0; i < seqMap.size(); i++) {
			List<String> keywords = seqMap.get(i);
			int totalNumber = TokenFreqMap.termTotalFreq(keywords);
			map1.put(i, totalNumber);
		}
		List<List<String>> result = new LinkedList<List<String>>();
		List<Map.Entry<Integer, Integer>> list1 = SoulArrays.sortMapByValue(
				map1, -1);
		for (Map.Entry<Integer, Integer> entry : list1) {
			int seqNumber = entry.getKey();
			result.add(seqMap.get(seqNumber));
		}
		for (List<String> tmp : result)
			log.info(tmp);
		return result;
	}

	private void fillBaseMap() {
		terms.clear();
		vectors.clear();
		synonyms.clear();
		for (Integer position : posMaps.keySet()) {
			List<Map<String, Object>> tmpList = posMaps.get(position);
			for (int i = 0; i < tmpList.size(); i++) {
				Map<String, Object> tmp = tmpList.get(i);
				String type = (String) tmp.get("type");
				String token = (String) tmp.get("token");
				Integer pos = (Integer) tmp.get("position");
				Assert.assertEquals(false,
						type.equals("null") || type.equals("w"));
				Assert.assertEquals(position, pos);
				if (type.equals(ClientStaticValue.TYPE_SYNONYM)) {
					List<Map<String, Object>> synonym1 = synonyms.get(position);
					if (synonym1 == null)
						synonym1 = new LinkedList<Map<String, Object>>();
					synonym1.add(tmp);
					synonyms.put(position, synonym1);
				} else if (type.equals(ClientStaticValue.TYPE_VECTOR)) {
					List<Map<String, Object>> vector1 = vectors.get(position);
					if (vector1 == null)
						vector1 = new LinkedList<Map<String, Object>>();
					vector1.add(tmp);
					vectors.put(position, vector1);
				} else {
					QueryInfoPojo pojo = new QueryInfoPojo(token, type,
							position);
					terms.put(position, pojo);
				}
			}
		}
	}

	private void fillNounWordMap() {
		nounWords.clear();
		seqMap.clear();
		Assert.assertTrue((terms.size() > 0));
		int i = 0;
		for (Integer pos : terms.keySet()) {
			QueryInfoPojo pojo = terms.get(pos);
			pojo.seqNumber(i); // sequential number of QueryInfoPojo
			String term = pojo.getName();
			String nature = pojo.getNature();

			boolean bNature = ((nature.startsWith("n") && !nature
					.equalsIgnoreCase("null"))
					|| nature.startsWith("j")
					|| nature.startsWith("g") || nature.equals("userwuxi") || nature
					.equals("rule"));
			if (bNature && term.length() > 1)
				nounWords.add(pos); // expect larger than 1 string
			List<Map<String, Object>> synList = synonyms.get(pos);
			List<Map<String, Object>> vecList = vectors.get(pos);
			List<Map<String, Object>> resList = new LinkedList<Map<String, Object>>();
			List<String> result = new LinkedList<String>();
			result.add(term);
			if (synList != null && !synList.isEmpty())
				resList.addAll(synList);
			if (vecList != null && !vecList.isEmpty())
				resList.addAll(vecList);
			if (!resList.isEmpty())
				result.addAll(SoulQueryUtil.synonymList(resList, term, nature));
			seqMap.put(i++, result);
		}
	}

	protected QueryInfoPojo compareTermList(List<QueryInfoPojo> termList) {
		int docfreq = Integer.MAX_VALUE;
		int position = -1;
		if (termList.size() == 1)
			return termList.get(0);
		for (int i = 0; i < termList.size(); i++) {
			QueryInfoPojo pojo = termList.get(i);
			int posNumber = pojo.getPosition();
			String term = pojo.getName();
			List<Map<String, Object>> synList = synonyms.get(posNumber);
			List<Map<String, Object>> vecList = vectors.get(posNumber);
			List<Map<String, Object>> resList = new LinkedList<Map<String, Object>>();
			if (synList != null && !synList.isEmpty())
				resList.addAll(synList);
			if (vecList != null && !vecList.isEmpty())
				resList.addAll(vecList);
			List<String> result = new LinkedList<String>();
			result.add(term);
			if (!resList.isEmpty())
				result.addAll(SoulQueryUtil.synonymList(resList));
			int tmpFreq = TokenFreqMap.termTotalFreq(result);
			if (tmpFreq < docfreq) {
				docfreq = tmpFreq;
				position = i;
			}
		}
		if (position < 0)
			position = 0;
		return termList.get(position);
	}

	protected QueryInfoPojo singlePojo() {
		QueryInfoPojo pojo = null;
		if (nounWords.size() >= 2) {
			List<QueryInfoPojo> pojoList = new LinkedList<QueryInfoPojo>();
			for (Integer pos : nounWords)
				pojoList.add(terms.get(pos));
			pojo = compareTermList(pojoList);
		} else if (nounWords.size() == 0) {
			List<QueryInfoPojo> termList = new LinkedList<QueryInfoPojo>();
			List<QueryInfoPojo> vnTermList = new LinkedList<QueryInfoPojo>();
			for (Integer pos : terms.keySet()) {
				QueryInfoPojo tmpPojo = terms.get(pos);
				String nature = tmpPojo.getNature();
				String name = tmpPojo.getName();
				if (nature.equals("vn") && name.length() > 1)
					vnTermList.add(tmpPojo);
				else
					termList.add(tmpPojo);
			}
			if (vnTermList.size() == 0)
				pojo = compareTermList(termList);
			else
				pojo = compareTermList(vnTermList);
			log.info("expected String is \"" + pojo.getName() + "\"");
			return pojo;
		} else if (nounWords.size() == 1) {
			Iterator<Integer> iter = nounWords.iterator();
			if (iter.hasNext()) {
				int pos = iter.next();
				pojo = terms.get(pos);
			}
		}
		return pojo;
	}

}
