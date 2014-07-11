package com.elasticsearch.application.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LongTermQuery extends SoulQuery {

	private static final Log log = LogFactory.getLog(LongTermQuery.class);
	protected final int leastNumber = 3;
	List<String> keywords1 = null;
	List<String> keywords2 = null;
	List<String> keywords3 = null;
	List<String> keywords4 = null;
	List<String> keywords5 = null;

	public LongTermQuery(Map<Integer, List<Map<String, Object>>> tokens) {
		super(tokens);
	}

	protected List<Map<String, Object>> combineTermQueryForTitle(
			List<String> list1, List<String> list2, List<String> list3,
			List<String> list4, List<String> list5, float boost) {
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
					for (int k1 = 0; k1 < list4.size(); k1++) {
						String str4 = list4.get(k1);
						Map<String, Object> map4 = SoulQueryUtil.termQueryMap(
								titleField, str4);
						for (int k2 = 0; k2 < list5.size(); k2++) {
							String str5 = list5.get(k2);
							Map<String, Object> map5 = SoulQueryUtil
									.termQueryMap(titleField, str5);
							List<Map<String, Object>> tmpArray = new ArrayList<Map<String, Object>>();
							tmpArray.add(map1);
							tmpArray.add(map2);
							tmpArray.add(map3);
							tmpArray.add(map4);
							tmpArray.add(map5);
							Map<String, Object> titleMap = SoulQueryUtil
									.createBooleanQueryMap(tmpArray, 2, "all",
											boost);
							array.add(titleMap);
						}
					}
				}
			}
		}
		return array;
	}

	private void fillLongList() {
		List<List<String>> result = fillList();
		keywords1 = result.get(0);
		keywords2 = result.get(1);
		keywords3 = result.get(2);
		keywords4 = result.get(3);
		keywords5 = result.get(4);
		log.info(keywords1);
		log.info(keywords2);
		log.info(keywords3);
		log.info(keywords4);
		log.info(keywords5);
		if (keywords1.size() > leastNumber)
			keywords1 = keywords1.subList(0, leastNumber);
		if (keywords2.size() > leastNumber)
			keywords2 = keywords2.subList(0, leastNumber);
		if (keywords3.size() > leastNumber)
			keywords3 = keywords3.subList(0, leastNumber);
		if (keywords4.size() > leastNumber)
			keywords4 = keywords4.subList(0, leastNumber);
		if (keywords5.size() > leastNumber)
			keywords5 = keywords5.subList(0, leastNumber);
	}

	public List<Map<String, Object>> pageQuery() {
		fillLongList();
		List<Map<String, Object>> titleMaps = combineTermQueryForTitle(
				keywords1, keywords2, keywords3, keywords4, keywords5, 1.0f);
		String str1 = keywords1.get(0);
		String str2 = keywords2.get(0);
		String str3 = keywords3.get(0);
		String str4 = keywords4.get(0);
		String str5 = keywords5.get(0);
		Map<String, Object> contentMap = combineTermQueryForContent(str1, str2,
				str3, str4, str5, 1.0f);
		List<Map<String, Object>> finalArray = new ArrayList<Map<String, Object>>();
		finalArray.addAll(titleMaps);
		finalArray.add(contentMap);

		QueryInfoPojo pojo = singlePojo();
		if (pojo != null) {
			int seqNum = pojo.seqNumber();
			List<String> strKeys = seqMap.get(seqNum);
			log.info("strKey = " + strKeys);
			for (int i = 0; i < strKeys.size(); i++) {
				String strKey = strKeys.get(i);
				Map<String, Object> map1 = SoulQueryUtil.termQueryMap(
						titleField, strKey);
				finalArray.add(map1);
			}
		}
		return finalArray;
	}

	private Map<String, Object> combineTermQueryForContent(String str1,
			String str2, String str3, String str4, String str5, float boost) {
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = SoulQueryUtil.termQueryMap(contentField,
				str1);
		Map<String, Object> map2 = SoulQueryUtil.termQueryMap(contentField,
				str2);
		Map<String, Object> map3 = SoulQueryUtil.termQueryMap(contentField,
				str3);
		Map<String, Object> map4 = SoulQueryUtil.termQueryMap(contentField,
				str4);
		Map<String, Object> map5 = SoulQueryUtil.termQueryMap(contentField,
				str5);
		array.add(map1);
		array.add(map2);
		array.add(map3);
		array.add(map4);
		array.add(map5);
		Map<String, Object> contentMap = SoulQueryUtil.createBooleanQueryMap(
				array, array.size() / 2, "all", boost);
		return contentMap;
	}
}
