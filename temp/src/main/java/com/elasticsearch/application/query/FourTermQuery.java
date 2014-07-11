package com.elasticsearch.application.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

public class FourTermQuery extends SoulQuery {

	private static final Log log = LogFactory.getLog(FourTermQuery.class);
	protected final int leastNumber = 3;
	List<String> keywords1 = null;
	List<String> keywords2 = null;
	List<String> keywords3 = null;
	List<String> keywords4 = null;

	public FourTermQuery(Map<Integer, List<Map<String, Object>>> tokens) {
		super(tokens);
	}

	private void fillFourList() {
		List<List<String>> result = fillList();
		Assert.assertEquals(result.size(), 4);
		keywords1 = result.get(0);
		keywords2 = result.get(1);
		keywords3 = result.get(2);
		keywords4 = result.get(3);
		if (keywords1.size() > leastNumber)
			keywords1 = keywords1.subList(0, leastNumber);
		if (keywords2.size() > leastNumber)
			keywords2 = keywords2.subList(0, leastNumber);
		if (keywords3.size() > leastNumber)
			keywords3 = keywords3.subList(0, leastNumber);
		if (keywords4.size() > leastNumber)
			keywords4 = keywords4.subList(0, leastNumber);
	}

	private List<Map<String, Object>> combineTermQueryForTitle(
			List<String> list1, List<String> list2, List<String> list3,
			List<String> list4, float boost) {
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
					for (int l = 0; l < list4.size(); l++) {
						String str4 = list4.get(l);
						Map<String, Object> map4 = SoulQueryUtil.termQueryMap(
								titleField, str4);
						List<Map<String, Object>> tmpArray = new ArrayList<Map<String, Object>>();
						tmpArray.add(map1);
						tmpArray.add(map2);
						tmpArray.add(map3);
						tmpArray.add(map4);
						Map<String, Object> titleMap = SoulQueryUtil
								.createBooleanQueryMap(tmpArray, 2, "all",
										boost);
						array.add(titleMap);
					}

				}
			}
		}
		return array;
	}

	public List<Map<String, Object>> pageQuery() {
		fillFourList();
		List<Map<String, Object>> titleArray = combineTermQueryForTitle(
				keywords1, keywords2, keywords3, keywords4, 1.0f);
		Map<String, Object> contentMap = combineTermQueryForContent(
				keywords1.get(0), keywords2.get(0), keywords3.get(0),
				keywords3.get(0), 1.0f);
		List<Map<String, Object>> finalArray = new ArrayList<Map<String, Object>>();
		finalArray.addAll(titleArray);
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

	protected Map<String, Object> combineTermQueryForContent(String str1,
			String str2, String str3, String str4, float boost) {
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = SoulQueryUtil.termQueryMap(contentField,
				str1);
		Map<String, Object> map2 = SoulQueryUtil.termQueryMap(contentField,
				str2);
		Map<String, Object> map3 = SoulQueryUtil.termQueryMap(contentField,
				str3);
		Map<String, Object> map4 = SoulQueryUtil.termQueryMap(contentField,
				str4);
		array.add(map1);
		array.add(map2);
		array.add(map3);
		array.add(map4);
		Map<String, Object> contentMap = SoulQueryUtil.createBooleanQueryMap(
				array, array.size() / 2, "all", boost);
		return contentMap;
	}
}
