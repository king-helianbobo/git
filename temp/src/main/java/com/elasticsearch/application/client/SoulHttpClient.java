package com.elasticsearch.application.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParser;

import com.elasticsearch.application.ClientStaticValue;
import com.elasticsearch.application.query.DoubleTermQuery;
import com.elasticsearch.application.query.FourTermQuery;
import com.elasticsearch.application.query.LongTermQuery;
import com.elasticsearch.application.query.SingleTermQuery;
import com.elasticsearch.application.query.SoulQueryMapUtil;
import com.elasticsearch.application.query.SoulQueryUtil;
import com.elasticsearch.application.query.ThreeTermQuery;

public class SoulHttpClient extends BaseHttpClient {
	private static final Log log = LogFactory.getLog(SoulHttpClient.class);

	public SoulHttpClient(String url, String index, String type) {
		super(url, index, type);
	}

	@SuppressWarnings("unchecked")
	private List<List<String>> tokenFragments(List<Map<String, Object>> tokens) {
		ArrayList<String> keys = new ArrayList<String>();
		List<List<String>> fragments = new ArrayList<List<String>>();
		int lastPosition = 0;
		String lastType = null;
		log.info(tokens.size());
		for (int i = 0; i < tokens.size(); i++) {
			Map<String, Object> tmp = tokens.get(i);
			String type = (String) tmp.get("type");
			String token = (String) tmp.get("token");
			int position = (Integer) tmp.get("position");
			log.info(type + "," + token + "," + position);
			if (type.equals(ClientStaticValue.TYPE_PINYIN))
				continue;
			if (i == 0) {
				if (type.equals(ClientStaticValue.TYPE_HANZI))
					keys.add("hanzi");
				else if (type.equals(ClientStaticValue.TYPE_WORD))
					keys.add("word");
				keys.add(token);
				lastPosition = position;
				lastType = type;
			} else {
				if (lastType.equals(type)) {
					if ((lastPosition + 1) != position) {
						fragments.add((List<String>) keys.clone());
						keys.clear();
						if (type.equals(ClientStaticValue.TYPE_HANZI))
							keys.add("hanzi");
						else if (type.equals(ClientStaticValue.TYPE_WORD))
							keys.add("word");
					}
					keys.add(token);
					lastPosition = position;
				} else {
					fragments.add((List<String>) keys.clone());
					keys.clear();
					if (type.equals(ClientStaticValue.TYPE_HANZI))
						keys.add("hanzi");
					else if (type.equals(ClientStaticValue.TYPE_WORD))
						keys.add("word");
					keys.add(token);
					lastPosition = position;
					lastType = type;
				}
			}
		}
		if (keys.size() > 1)
			fragments.add(keys);
		return fragments;
	}

	private List<String> convertQueryStr(List<String> keys) {
		String tag = keys.get(0);
		Set<String> filter = ClientStaticValue.stopWordsSet;
		List<String> result = new ArrayList<String>();
		if (tag.equals("word")) {
			for (int i = 1; i < keys.size(); i++) {
				String key = keys.get(i);
				if (filter != null && !filter.contains(key))
					result.add(key);
			}
			if (result.isEmpty())
				return null;
			else
				return result;
		} else if (tag.equals("hanzi")) {
			keys.remove(0);
			if (keys.isEmpty())
				return null;
			else
				return keys;
		}
		return null;
	}

	private List<List<String>> constructFragments(
			List<Map<String, Object>> fragments) {
		List<List<String>> tokens = tokenFragments(fragments);
		List<List<String>> result = new ArrayList<List<String>>();
		for (int j = 0; j < tokens.size(); j++) {
			List<String> keys = tokens.get(j);
			List<String> list = convertQueryStr(keys);
			if (list != null) {
				if (list.size() > 6) {
					int number = list.size() / 2;
					List<String> list1 = new LinkedList<String>();
					List<String> list2 = new LinkedList<String>();
					for (int i = 0; i < list.size(); i++) {
						if (i < number)
							list1.add(list.get(i));
						else
							list2.add(list.get(i));
					}
					result.add(list1);
					result.add(list2);
				} else
					result.add(list);
			}
		}
		if (!result.isEmpty())
			return result;
		else
			return null;
	}

	private Map<String, Object> longQueryStr(List<Map<String, Object>> tokens,
			String tag) {
		List<List<String>> fragments = constructFragments(tokens);
		if (fragments != null) {
			List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < fragments.size(); i++) {
				List<Map<String, Object>> array1 = new ArrayList<Map<String, Object>>();
				List<String> split = fragments.get(i);
				log.info("thisSplit = " + split);
				if (split.size() == 1) {
					String str = split.get(0);
					Map<String, Object> map1 = SoulQueryUtil.termQueryMap(
							"content", str);
					Map<String, Object> map2 = SoulQueryUtil.termQueryMap(
							"contenttitle", str);
					array1.add(map1);
					array1.add(map2);
				} else {
					Map<String, Object> map1 = SoulQueryMapUtil
							.spanNearQueryMap("content", split);
					List<Map<String, Object>> array2 = new ArrayList<Map<String, Object>>();
					for (int j = 0; j < split.size(); j++) {
						Map<String, Object> tmpMap = SoulQueryUtil
								.termQueryMap("contenttitle", split.get(j));
						array2.add(tmpMap);
						log.info(split.get(j));
					}
					int minimum = split.size() / 2 + 1;
					Map<String, Object> map2 = SoulQueryUtil
							.createBooleanQueryMap(array2, minimum);
					array1.add(map1);
					array1.add(map2);
				}
				Map<String, Object> map3 = SoulQueryUtil.createBooleanQueryMap(
						array1, 1);
				array.add(map3);
			}
			int number = fragments.size() / 2;
			Map<String, Object> map3 = SoulQueryUtil.createBooleanQueryMap(
					array, number, tag);
			return map3;
		} else
			return null;
	}

	public String complexQuerySearch(String queryStr, int from, int size,
			String tagType) throws IOException {
		String tag = convertTag(tagType);
		if (tag.equals("first")) {
			// ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> resultMap = new HashMap<String, Object>();
			String[] tags = { "tag1", "tag2", "tag3", "tag4", "tag5", "tag6",
					"tag7" };
			for (String str : tags) {
				String json = complexQuerySearch2(queryStr, from, size, str);
				JsonParser jsonParser = mapper.getJsonFactory()
						.createJsonParser(json);
				@SuppressWarnings("unchecked")
				Map<String, Object> map = mapper.readValue(jsonParser,
						Map.class);
				Integer totalSize = (Integer) map.get("count");
				if (totalSize <= 0)
					resultMap.put(str, totalSize);
				else
					resultMap.put(str, map);
			}
			String result = mapper.writeValueAsString(resultMap);
			return result;
		} else
			return complexQuerySearch2(queryStr, from, size, tagType);
	}

	private String complexQuerySearch2(String queryStr, int from, int size,
			String tagType) {
		boolean bTraditional = true;
		String tag = null;
		StringBuilder builder = new StringBuilder();
		builder.append(index + "/" + type + "/_search?pretty=true");
		if (bTraditional)
			tag = convertTag(tagType);
		else
			tag = tagType;
		List<Map<String, Object>> primeTokens = primeTokenList(queryStr);
		Map<Integer, List<Map<String, Object>>> tokens = secondTokenList(primeTokens);
		if (tokens == null || tokens.isEmpty())
			return null;
		Map<String, Object> queryMap = null;
		// log.info("tokens.size() = " + tokens.size());
		if (tokens.size() == 1) { // single word
			SingleTermQuery singleQuery = new SingleTermQuery(tokens);
			List<Map<String, Object>> array = singleQuery.pageQuery();
			queryMap = SoulQueryUtil.createBooleanQueryMap(array, 1, tag);
		} else if (tokens.size() == 2) {
			DoubleTermQuery twotermQuery = new DoubleTermQuery(tokens);
			List<Map<String, Object>> array1 = twotermQuery.pageQuery();
			queryMap = SoulQueryUtil.createBooleanQueryMap(array1, 1, tag);
		} else if (tokens.size() == 3) {
			ThreeTermQuery threeTermQuery = new ThreeTermQuery(tokens);
			List<Map<String, Object>> array1 = threeTermQuery.pageQuery();
			queryMap = SoulQueryUtil.createBooleanQueryMap(array1, 1, tag);
		} else if (tokens.size() == 4) {
			FourTermQuery fourTermQuery = new FourTermQuery(tokens);
			List<Map<String, Object>> array1 = fourTermQuery.pageQuery();
			queryMap = SoulQueryUtil.createBooleanQueryMap(array1, 1, tag);
		} else if (tokens.size() > 4) {
			LongTermQuery longTermQuery = new LongTermQuery(tokens);
			List<Map<String, Object>> array1 = longTermQuery.pageQuery();
			queryMap = SoulQueryUtil.createBooleanQueryMap(array1, 1, tag);
		}
		// String json = SoulQueryUtil.complexQueryJson(queryMap, from, size);
		String json = SoulQueryUtil.scriptQueryJson(queryMap, from, size);
		if (!bTraditional)
			builder.append("&routing=" + tagType
					+ "&search_type=query_and_fetch");
		String url = builder.toString();
		log.info(json);
		log.info(url);
		Map<String, Object> map = postQuery.post(url, json);
		return SoulQueryUtil.constructJsonResult(map, from);
	}

}
