package com.elasticsearch.application.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

public class ThreeTermQuery extends SoulQuery {

	private static final Log log = LogFactory.getLog(ThreeTermQuery.class);
	List<String> keywords1 = null;
	List<String> keywords2 = null;
	List<String> keywords3 = null;
	protected final int leastNumber = 4;

	public ThreeTermQuery(Map<Integer, List<Map<String, Object>>> tokens) {
		super(tokens);
	}

	private void fillThreeList() {
		Map<Integer, List<String>> resultMap = fillList();
		Assert.assertEquals(resultMap.size(), 3);
		keywords1 = resultMap.get(0);
		keywords2 = resultMap.get(1);
		keywords3 = resultMap.get(2);
		if (keywords1.size() > leastNumber)
			keywords1 = keywords1.subList(0, leastNumber);
		if (keywords2.size() > leastNumber)
			keywords2 = keywords2.subList(0, leastNumber);
		if (keywords3.size() > leastNumber)
			keywords3 = keywords3.subList(0, leastNumber);
		log.info(keywords1 + "/" + keywords2 + "/" + keywords3);
	}

	public List<Map<String, Object>> pageQuery() {
		fillThreeList();
		List<Map<String, Object>> titleMaps = combineTermQueryForTitle(
				keywords1, keywords2, keywords3, 1.0f);
		Map<String, Object> contentMap = combineTermQueryForContent(
				keywords1.get(0), keywords2.get(0), keywords3.get(0), 1.0f);
		List<Map<String, Object>> finalArray = new ArrayList<Map<String, Object>>();
		finalArray.addAll(titleMaps);
		finalArray.add(contentMap);
		QueryInfoPojo pojo = singlePojo();
		if (pojo != null) {
			int seqNum = pojo.seqNumber();
			List<String> strKeys = seqMap.get(seqNum);
			// log.info("strKey = " + strKeys);
			for (int i = 0; i < strKeys.size(); i++) {
				String str1 = strKeys.get(i);
				Map<String, Object> map1 = SoulQueryUtil.termQueryMap(
						titleField, str1);
				finalArray.add(map1);
			}
		}
		return finalArray;
	}

	protected Map<String, Object> combineTermQueryForContent(String str1,
			String str2, String str3, float boost) {
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = SoulQueryUtil.termQueryMap(contentField,
				str1, fScores.get(0));
		Map<String, Object> map2 = SoulQueryUtil.termQueryMap(contentField,
				str2, fScores.get(1));
		Map<String, Object> map3 = SoulQueryUtil.termQueryMap(contentField,
				str3, fScores.get(2));
		array.add(map1);
		array.add(map2);
		array.add(map3);
		Map<String, Object> contentMap = SoulQueryUtil.createBooleanQueryMap(
				array, 2, "all", boost);
		return contentMap;
	}

	protected List<Map<String, Object>> combineTermQueryForTitle(
			List<String> list1, List<String> list2, List<String> list3,
			float boost) {
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < list1.size(); i++) {
			String str1 = list1.get(i);
			Map<String, Object> map1 = SoulQueryUtil.termQueryMap(titleField,
					str1);
			for (int j = 0; j < list2.size(); j++) {
				String str2 = list2.get(j);
				Map<String, Object> map2 = SoulQueryUtil.termQueryMap(
						titleField, str2);
				for (int k = 0; k < list3.size(); k++) {
					String str3 = list3.get(k);
					Map<String, Object> map3 = SoulQueryUtil.termQueryMap(
							titleField, str3);
					ArrayList<Map<String, Object>> titleArray = new ArrayList<Map<String, Object>>();
					titleArray.add(map1);
					titleArray.add(map2);
					titleArray.add(map3);
					Map<String, Object> titleMap = SoulQueryUtil
							.createBooleanQueryMap(titleArray, 2, "all", boost);
					array.add(titleMap);
				}
			}
		}
		return array;
	}
}
