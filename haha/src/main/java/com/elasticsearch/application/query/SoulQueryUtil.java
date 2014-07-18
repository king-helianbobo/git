package com.elasticsearch.application.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import com.elasticsearch.application.ClientStaticValue;
import com.elasticsearch.application.WordAlter;
import com.splitword.soul.utility.StringUtil;

public class SoulQueryUtil {
	private static Log log = LogFactory.getLog(SoulQueryUtil.class);
	public static final String preTag = "<tag1 style=\"color:red\">";
	public static final String postTag = "</tag1>";
	public static final String urlField = "url";
	public static final String tagField = "tag";
	public static final String timeField = "time";

	public static final int fragmentSize = 250;
	private static ObjectMapper mapper = new ObjectMapper();

	public static String termListJson(String term) {
		List<String> array = new ArrayList<String>();
		array.add(SoulQuery.titleField);
		array.add(SoulQuery.contentField);
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("fields", array);
		map2.put("size", 0);
		map2.put("action", "termlist");
		map2.put("term", term);
		try {
			String json = mapper.writeValueAsString(map2);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String suggestJson(String queryStr, String type,
			String field, int size) {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("term", queryStr);
		tmpMap.put("field", field);
		tmpMap.put("size", String.valueOf(size));
		tmpMap.put("type", type);
		try {
			String json = mapper.writeValueAsString(tmpMap);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Map<String, Object> termQueryMap(String fieldName,
			String keyword) {
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put(fieldName, keyword);
		Map<String, Object> termMap = new HashMap<String, Object>();
		termMap.put("term", map1);
		return termMap;
	}

	public static Map<String, Object> termQueryMap(String fieldName,
			String keyword, float boost) {
		Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("value", keyword);
		map2.put("boost", boost);
		map1.put(fieldName, map2);
		Map<String, Object> termMap = new HashMap<String, Object>();
		termMap.put("term", map1);
		return termMap;
	}

	/*******************************************************************************/
	public static Map<String, Object> createBooleanQueryMap(
			List<Map<String, Object>> shouldList, int minimumMatch, String tag) {
		return createBooleanQueryMap(shouldList, minimumMatch, tag, 1.0f);
	}

	public static Map<String, Object> createBooleanQueryMap(
			List<Map<String, Object>> shouldList, int minimumMatch) {
		return createBooleanQueryMap(shouldList, minimumMatch, "all", 1.0f);
	}

	public static Map<String, Object> createBooleanQueryMap(
			List<Map<String, Object>> shouldList, int minimumMatch, String tag,
			float boost) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> map3 = new HashMap<String, Object>();
		map3.put("should", shouldList);
		if (tag.equals("all")) {
			// do nothing
		} else {
			List<Object> array = new ArrayList<Object>();
			array.add(termQueryMap("tag", tag));
			map3.put("must", array);
		}
		map3.put("minimum_should_match", minimumMatch);
		// map3.put("boost", boost);
		map.put("bool", map3);
		return map;
	}

	public static List<String> synonymList(
			List<Map<String, Object>> synonymTokens) {
		List<String> firstWords = new LinkedList<String>();
		for (int i = 0; i < synonymTokens.size(); i++) {
			Map<String, Object> tokenMap = synonymTokens.get(i);
			String type = (String) tokenMap.get("type");
			String token = (String) tokenMap.get("token");
			if (type.equals(ClientStaticValue.TYPE_SYNONYM)
					&& !firstWords.contains(token)) {
				firstWords.add(token);
			} else if (type.equals(ClientStaticValue.TYPE_VECTOR)
					&& (!firstWords.contains(token))) {
				firstWords.add(token);
			} else
				continue;
		}
		return firstWords;
	}

	public static List<String> synonymList(
			List<Map<String, Object>> synonymTokens, String baseToken,
			String baseType) {
		List<String> firstWords = new LinkedList<String>();
		List<String> secondWords = new LinkedList<String>();
		for (int i = 0; i < synonymTokens.size(); i++) {
			Map<String, Object> tokenMap = synonymTokens.get(i);
			String type = (String) tokenMap.get("type");
			String token = (String) tokenMap.get("token");
			if (token.length() == 1 && baseToken.length() > 1)
				continue;
			if (type.equals(ClientStaticValue.TYPE_SYNONYM)) {
				firstWords.add(token);
			} else if (type.equals(ClientStaticValue.TYPE_VECTOR)
					&& (!firstWords.contains(token))) {

				String common = WordAlter.commonSet(baseToken, token);
				if (baseToken.equals(common) || token.equals(common))
					firstWords.add(token);
				// if (baseType.startsWith("n")) {
				// if (WordAlter.nounIntersection(baseToken, token, baseType))
				// secondWords.add(token);
				// } else if (baseType.startsWith("j")) {
				// String common = WordAlter.commonSet(baseToken, token);
				// if (baseToken.equals(common))
				// firstWords.add(token);
				// } else if (WordAlter.bIntersection(baseToken, token)) {
				// secondWords.add(token);
				// }
			} else
				continue;
		}
		if (!secondWords.isEmpty()) {
			firstWords.addAll(secondWords);
		}
		return firstWords;
	}

	/******************************************************************************************/

	public static String complexQueryJson(Map<String, Object> queryMap,
			int from, int size) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("query", queryMap);
		result.put(
				"highlight",
				createHighLigthQuery(SoulQuery.titleField,
						SoulQuery.contentField));
		result.put("from", from);
		result.put("size", size);
		try {
			String json = mapper.writeValueAsString(result);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Map<String, Object> scriptQuery(Map<String, Object> queryMap) {
		Map<String, Object> scriptQuery1 = new HashMap<String, Object>();
		Map<String, Object> scriptQuery2 = new HashMap<String, Object>();
		// Map<String, Object> scriptQuery3a = new HashMap<String, Object>();
		Map<String, Object> scriptQuery3b = new HashMap<String, Object>();
		// Map<String, Object> scriptQuery3aa = new HashMap<String, Object>();

		// scriptQuery3aa.put("field", "postTime");
		// scriptQuery3a.put("exists", scriptQuery3aa);
		scriptQuery3b.put("lang", "native");
		scriptQuery3b.put("script", "boostDateScript");
		Map<String, Object> temp = new HashMap<String, Object>();
		temp.put("field", "postTime");
		temp.put("time", "1404295969046");
		scriptQuery3b.put("params", temp);
		scriptQuery2.put("script_score", scriptQuery3b);
		// scriptQuery2.put("filter", scriptQuery3a);

		List<Object> array = new ArrayList<Object>();
		scriptQuery1.put("boost_mode", "replace");
		scriptQuery1.put("query", queryMap);
		array.add(scriptQuery2);
		scriptQuery1.put("functions", array);
		return scriptQuery1;
	}

	public static String scriptQueryJson(Map<String, Object> queryMap,
			int from, int size) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> scriptQuery1 = new HashMap<String, Object>();
		Map<String, Object> scriptQuery2 = scriptQuery(queryMap);
		scriptQuery1.put("function_score", scriptQuery2);
		result.put("query", scriptQuery1);
		result.put("explain", true);
		result.put(
				"highlight",
				createHighLigthQuery(SoulQuery.titleField,
						SoulQuery.contentField));
		result.put("from", from);
		result.put("size", size);
		try {
			String json = mapper.writeValueAsString(result);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/******************************************************************************************/
	public static String simpleQueryStringJson(String queryStr, int from,
			int size, String tag) {
		Map<String, Object> map1 = SoulQueryMapUtil.simpleQueryStringMap(
				SoulQuery.contentField, queryStr, "and");
		Map<String, Object> map2 = SoulQueryMapUtil.simpleQueryStringMap(
				SoulQuery.titleField, queryStr, "or");
		List<Map<String, Object>> array1 = new ArrayList<Map<String, Object>>();
		array1.add(map1);
		array1.add(map2);
		array1.add(termQueryMap("contenttitle.untouched", queryStr, 4.0f));
		Map<String, Object> queryMap = createBooleanQueryMap(array1, 1, tag);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("query", queryMap);
		result.put(
				"highlight",
				createHighLigthQuery(SoulQuery.titleField,
						SoulQuery.contentField));
		result.put("size", size);
		result.put("from", from);
		try {
			String json = mapper.writeValueAsString(result);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String spanNearQueryJson(String queryStr, String tag,
			int from, int size, List<String> queryStrs) {
		Map<String, Object> map1 = SoulQueryMapUtil.spanNearQueryMap(
				SoulQuery.contentField, queryStrs);
		Map<String, Object> map2 = SoulQueryMapUtil.spanNearQueryMap(
				SoulQuery.titleField, queryStrs);
		List<Map<String, Object>> array1 = new ArrayList<Map<String, Object>>();
		array1.add(map1);
		array1.add(map2);
		array1.add(termQueryMap("contenttitle.untouched", queryStr, 4.0f));
		Map<String, Object> queryMap = createBooleanQueryMap(array1, 1, tag);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("query", queryMap);
		result.put(
				"highlight",
				createHighLigthQuery(SoulQuery.titleField,
						SoulQuery.contentField));
		result.put("size", size);
		result.put("from", from);
		try {
			String json = mapper.writeValueAsString(result);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String queryStringJson(String queryStr, String tag, int from,
			int size, String... queryStrs) {
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < queryStrs.length; i++) {
			Map<String, Object> map = SoulQueryMapUtil
					.queryStringMap(queryStrs[i]);
			array.add(map);
		}
		array.add(termQueryMap("contenttitle.untouched", queryStr, 4.0f));
		Map<String, Object> queryMap = createBooleanQueryMap(array, 1, tag);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("query", queryMap);
		result.put(
				"highlight",
				createHighLigthQuery(SoulQuery.titleField,
						SoulQuery.contentField));
		result.put("size", size);
		result.put("from", from);
		try {
			String json = mapper.writeValueAsString(result);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String simpleQueryStringJson(String queryStr) {
		return simpleQueryStringJson(queryStr, 0, 10, "all");
	}

	public static Map<String, Object> createHighLigthQuery(String... fields) {
		Map<String, Object> tagMap = new HashMap<String, Object>();
		List<String> tag1 = new ArrayList<String>();
		tag1.add(preTag);
		List<String> tag2 = new ArrayList<String>();
		tag2.add(postTag);
		tagMap.put("pre_tags", tag1);
		tagMap.put("post_tags", tag2);
		Map<String, Object> map3 = new HashMap<String, Object>();
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			Map<String, Object> tmp = new HashMap<String, Object>();
			if (field.equals(SoulQuery.titleField)) {
				tmp.put("number_of_fragments", 0);
			} else if (field.equals(SoulQuery.contentField)) {
				tmp.put("number_of_fragments", 3);
				tmp.put("fragment_size", fragmentSize);
			} else {
			}
			map3.put(field, tmp);
		}
		tagMap.put("fields", map3);
		tagMap.put("require_field_match", true);
		return tagMap;
	}

	public static String formatHighLightContent(String content) {
		int startPos1 = 0;
		int endPos1 = content.indexOf(SoulQueryUtil.preTag);
		int startPos2 = content.lastIndexOf(SoulQueryUtil.postTag)
				+ SoulQueryUtil.postTag.length();
		if ((endPos1 < 0) || (startPos2 < 0)) {
			endPos1 = content.length();
			startPos2 = 0;
		}
		int endPos2 = content.length() - 1;
		for (int i = 0; i < endPos1; i++) {
			char c = content.charAt(i);
			if (!Character.isLetterOrDigit(c))
				startPos1++;
			else
				break;
		}
		for (int i = content.length() - 1; i >= startPos2; i--) {
			char c = content.charAt(i);
			if (!Character.isLetterOrDigit(c))
				endPos2--;
			else
				break;
		}
		String result = "..." + content.substring(startPos1, endPos2 + 1)
				+ "...";
		return result;
	}

	private static List<Object> keywordsArray(String keywords) {
		List<Object> array = new ArrayList<Object>();
		if (StringUtil.isBlank(keywords) || keywords.equalsIgnoreCase("null"))
			return array;
		String[] strs = keywords.split("\t");
		for (String str : strs) {
			array.add(str);
		}
		return array;
	}

	private static List<Map<String, Object>> sourceArray(String source) {
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		if (StringUtil.isBlank(source) || source.equalsIgnoreCase("null"))
			return array;
		String[] strs = source.split("\t");
		for (String str : strs) {
			int begin = str.indexOf('(');
			int end = str.lastIndexOf(')');
			Map<String, Object> tmp = new HashMap<String, Object>();
			if (end >= 0 && begin >= 0) {
				String url = str.substring(begin + 1, end);
				String tag = str.substring(0, begin);
				tmp.put("title", tag);
				tmp.put("url", "http://" + url);
				array.add(tmp);
			} else {
				// do nothing ,ignore empty url
			}
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	public static String constructJsonResult(Map<String, Object> map, int from) {
		try {
			map = (Map<String, Object>) map.get("hits");
			if (map == null)
				return null;
			List<Map<String, Object>> hits = (List<Map<String, Object>>) map
					.get("hits");
			int totalSize = (Integer) map.get("total");
			List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < hits.size(); i++) {
				Map<String, Object> map1 = hits.get(i);
				double score = (Double) map1.get("_score");
				Map<String, Object> sourceMap = (Map<String, Object>) map1
						.get("_source");
				Map<String, Object> lightMap = (Map<String, Object>) map1
						.get("highlight");
				String url = (String) sourceMap.get(urlField);
				String titleStr = (String) sourceMap.get(SoulQuery.titleField);
				String contentStr = null;
				String srcContent = (String) sourceMap
						.get(SoulQuery.contentField);
				if (lightMap != null) {
					List<String> titles = (List<String>) lightMap
							.get(SoulQuery.titleField);
					if (titles != null)
						titleStr = titles.get(0);
					List<String> contents = (List<String>) lightMap
							.get(SoulQuery.contentField);
					if (contents != null)
						contentStr = contents.get(0);
				}
				if (contentStr == null) {
					int size = (srcContent.length() < SoulQueryUtil.fragmentSize) ? srcContent
							.length() : SoulQueryUtil.fragmentSize;
					contentStr = srcContent.substring(0, size);
				}
				Map<String, Object> result2 = new HashMap<String, Object>();
				String tmpContent = SoulQueryUtil
						.formatHighLightContent(contentStr);
				result2.put(SoulQuery.titleField, titleStr);
				result2.put(SoulQuery.contentField, tmpContent);
				result2.put(urlField, url);
				result2.put("score", score);
				String source = (String) sourceMap.get("source");
				result2.put("source", sourceArray(source));
				String keywords = (String) sourceMap.get("keywords");
				result2.put("keywords", keywordsArray(keywords));
				result2.put(tagField, (String) sourceMap.get(tagField));
				result2.put(timeField, (String) sourceMap.get("postTime"));
				array.add(result2);
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("count", totalSize);
			result.put("from", from);
			result.put("size", hits.size());
			result.put("pageList", array);
			String resultJson = mapper.writeValueAsString(result);
			log.info(resultJson);
			// log.info("result: " + totalSize + "/" + from + "/" +
			// hits.size());
			return resultJson;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
