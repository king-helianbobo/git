package com.elasticsearch.application.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoulQueryMapUtil {
	public static Map<String, Object> queryStringMap(String queryStr) {
		Map<String, Object> map1 = new HashMap<String, Object>();
		List<String> array = new ArrayList<String>();
		array.add("content^1.0");
		array.add("contenttitle^3.0");
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("analyzer", "whitespace");
		map2.put("default_operator", "and");
		map2.put("fields", array);
		map2.put("query", queryStr);
		map1.put("query_string", map2);
		return map1;
	}

	public static Map<String, Object> simpleQueryStringMap(String field,
			List<String> queryStrs, String operator, String analyzer) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < queryStrs.size(); i++) {
			builder.append(queryStrs.get(i));
			builder.append(" ");
		}
		String queryStr = builder.toString();
		return _simpleQueryStringMap(field, queryStr, operator, analyzer);
	}

	public static Map<String, Object> simpleQueryStringMap(String field,
			String queryStr, String operator) {
		return _simpleQueryStringMap(field, queryStr, operator, "soul_query");
	}

	private static Map<String, Object> _simpleQueryStringMap(String field,
			String queryStr, String operator, String analyzer) {
		List<String> array = new ArrayList<String>();
		array.add(field);
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("analyzer", analyzer);
		map2.put("default_operator", operator);
		map2.put("fields", array);
		map2.put("query", queryStr);
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("simple_query_string", map2);
		return map1;
	}

	/****************** above is simple_query_string **********************************/
	/********************** below is span_near_query *********************************************************/
	public static Map<String, Object> spanNearQueryMap(String field,
			List<String> queryStrs) {
		return spanNearQueryMap(0, false, field, queryStrs);
	}

	public static Map<String, Object> spanNearQueryMap(int slop,
			boolean bOrder, String field, List<String> queryStrs) {
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("slop", slop);
		map1.put("in_order", bOrder);
		map1.put("collect_payloads", false);
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < queryStrs.size(); i++) {
			String queryStr = queryStrs.get(i);
			Map<String, Object> tmp = spanTermQueryMap(field, queryStr);
			array.add(tmp);
		}
		map1.put("clauses", array);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("span_near", map1);
		return map;
	}

	private static Map<String, Object> spanTermQueryMap(String fieldName,
			String keyword) {
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put(fieldName, keyword);
		Map<String, Object> termMap = new HashMap<String, Object>();
		termMap.put("span_term", map1);
		return termMap;
	}
}
