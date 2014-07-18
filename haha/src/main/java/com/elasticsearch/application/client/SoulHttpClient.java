package com.elasticsearch.application.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParser;

import com.elasticsearch.application.query.DoubleTermQuery;
import com.elasticsearch.application.query.FiveTermQuery;
import com.elasticsearch.application.query.FourTermQuery;
import com.elasticsearch.application.query.SingleTermQuery;
import com.elasticsearch.application.query.SoulQueryUtil;
import com.elasticsearch.application.query.ThreeTermQuery;
import com.keyword.compare.TokenFreqMap;

public class SoulHttpClient extends BaseHttpClient {
	private static final Log log = LogFactory.getLog(SoulHttpClient.class);

	public SoulHttpClient(String url, String index, String type) {
		super(url, index, type);
	}

	public String complexQuerySearch(String queryStr, int from, int size,
			String tagType) throws IOException {
		String tag = convertTag(tagType);
		log.info(queryStr + "," + from + "," + size + "," + tagType);
		if (tag.equals("first")) {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			String[] tags = { "tag1", "tag2", "tag3", "tag4", "tag5", "tag6",
					"tag7" };
			for (String tagKey : tags) {
				String json = complexQuerySearch2(queryStr, from, size, tagKey);
				JsonParser jsonParser = mapper.getJsonFactory()
						.createJsonParser(json);
				@SuppressWarnings("unchecked")
				Map<String, Object> map = mapper.readValue(jsonParser,
						Map.class);
				Integer totalSize = (Integer) map.get("count");
				if (totalSize > 0)
					resultMap.put(tagKey, map);
			}
			if (TokenFreqMap.isLeader(queryStr)) {
				Map<String, String> leaderInfo = TokenFreqMap
						.leaderInfo(queryStr);
				if (leaderInfo != null && !leaderInfo.isEmpty())
					resultMap.put("tag8", leaderInfo);
			}
			String result = mapper.writeValueAsString(resultMap);
			log.info(result);
			return result;
		} else
			return complexQuerySearch2(queryStr, from, size, tagType);
	}

	private String complexQuerySearch2(String queryStr, int from, int size,
			String tagType) {
		boolean bTraditional = false;
		String tag = null;
		StringBuilder builder = new StringBuilder();
		builder.append(index + "/" + type + "/_search?pretty=true");
		if (bTraditional)
			tag = convertTag(tagType);
		else
			tag = tagType;
		log.info("tag = " + tag);
		List<Map<String, Object>> primeTokens = primeTokenList(queryStr);
		Map<Integer, List<Map<String, Object>>> tokens = secondTokenList(primeTokens);
		if (tokens == null || tokens.isEmpty())
			return null;
		Map<String, Object> queryMap = null;
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
			FiveTermQuery longTermQuery = new FiveTermQuery(tokens);
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
