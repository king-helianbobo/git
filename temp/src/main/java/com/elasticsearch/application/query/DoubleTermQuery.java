package com.elasticsearch.application.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

public class DoubleTermQuery extends SoulQuery {
	private static final Log log = LogFactory.getLog(DoubleTermQuery.class);
	protected final int leastNumber = 5;
	List<String> keywords1 = null;
	List<String> keywords2 = null;

	public DoubleTermQuery(Map<Integer, List<Map<String, Object>>> tokens) {
		super(tokens);
	}

	public List<Map<String, Object>> pageQuery() {
		fillTwoList();
		List<Map<String, Object>> titleMaps = combineTermQueryForTitle(
				keywords1, keywords2, 1.5f);
		Map<String, Object> contentMap = combineTermQueryForContent(
				keywords1.get(0), keywords2.get(0), 1.2f);

		List<Map<String, Object>> finalArray = new ArrayList<Map<String, Object>>();
		finalArray.addAll(titleMaps);
		finalArray.add(contentMap);
		QueryInfoPojo pojo = singlePojo();
		if (pojo != null) {
			int seqNum = pojo.seqNumber();
			List<String> strKeys = seqMap.get(seqNum);
			log.info("strKey = " + strKeys);
			for (int i = 0; i < strKeys.size(); i++) {
				String str1 = strKeys.get(i);
				Map<String, Object> map1 = SoulQueryUtil.termQueryMap(
						titleField, str1);
				finalArray.add(map1);
			}
		}
		return finalArray;
	}

	private void fillTwoList() {
		List<List<String>> result = fillList();
		Assert.assertEquals(result.size(), 2);
		keywords1 = result.get(0);
		keywords2 = result.get(1);
		if (keywords1.size() > leastNumber)
			keywords1 = keywords1.subList(0, leastNumber);
		if (keywords2.size() > leastNumber)
			keywords2 = keywords2.subList(0, leastNumber);
		log.info(keywords1 + " / " + keywords2);
	}

	protected Map<String, Object> combineTermQueryForContent(String str1,
			String str2, float boost) {
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = SoulQueryUtil.termQueryMap(contentField,
				str1);
		Map<String, Object> map2 = SoulQueryUtil.termQueryMap(contentField,
				str2);
		array.add(map1);
		array.add(map2);
		Map<String, Object> contentMap = SoulQueryUtil.createBooleanQueryMap(
				array, 2, "all", boost);
		return contentMap;
	}

	protected List<Map<String, Object>> combineTermQueryForTitle(
			List<String> list1, List<String> list2, float boost) {
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < list1.size(); i++) {
			String str1 = list1.get(i);
			Map<String, Object> map1 = SoulQueryUtil.termQueryMap(titleField,
					str1);
			for (int j = 0; j < list2.size(); j++) {
				ArrayList<Map<String, Object>> titleArray = new ArrayList<Map<String, Object>>();
				String str2 = list2.get(j);
				Map<String, Object> map2 = SoulQueryUtil.termQueryMap(
						titleField, str2);
				titleArray.add(map1);
				titleArray.add(map2);
				Map<String, Object> titleMap = SoulQueryUtil
						.createBooleanQueryMap(titleArray, 2, "all", boost);
				array.add(titleMap);
			}
		}
		return array;
	}
}
