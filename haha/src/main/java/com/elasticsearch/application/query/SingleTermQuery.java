package com.elasticsearch.application.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

public class SingleTermQuery extends SoulQuery {
	private static final Log log = LogFactory.getLog(SingleTermQuery.class);
	List<String> keywords1 = null;
	protected final int leastNumber = 5;

	public SingleTermQuery(Map<Integer, List<Map<String, Object>>> tokens) {
		super(tokens);
	}

	protected List<Map<String, Object>> singleQueryWithTitleContent(
			List<Map<String, Object>> tokens) {
		Assert.assertTrue(tokens.size() >= 1);
		Map<String, Object> baseMap = tokens.get(0);

		String primeToken = (String) baseMap.get("token");
		String primeType = (String) baseMap.get("type");
		List<String> firstWords = new LinkedList<String>();
		firstWords.add(primeToken);
		List<Map<String, Object>> subList = tokens.subList(1, tokens.size());
		List<String> result = SoulQueryUtil.synonymList(subList, primeToken,
				primeType);
		firstWords.addAll(result);
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < firstWords.size(); i++) {
			String token = firstWords.get(i);
			float boost = 1.1f;
			if (i == 0)
				boost = 1.2f;
			Map<String, Object> map1 = SoulQueryUtil.termQueryMap(
					SoulQuery.titleField, token, boost);
			array.add(map1);
		}
		Map<String, Object> map2 = SoulQueryUtil.termQueryMap(
				SoulQuery.contentField, primeToken, 1.0f);
		array.add(map2);
		return array;
	}

	public List<Map<String, Object>> pageQuery() {
		List<Map<String, Object>> result = null;
		Set<Integer> set = posMaps.keySet();
		Iterator<Integer> iter = set.iterator();
		if (iter.hasNext()) {
			int pos = iter.next();
			result = posMaps.get(pos);
		}
		List<Map<String, Object>> array = singleQueryWithTitleContent(result);
		if (array == null || array.isEmpty())
			return null;
		else
			return array;
	}
}
