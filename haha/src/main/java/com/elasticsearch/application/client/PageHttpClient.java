package com.elasticsearch.application.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.elasticsearch.application.query.DoubleTermQuery;
import com.elasticsearch.application.query.FourTermQuery;
import com.elasticsearch.application.query.FiveTermQuery;
import com.elasticsearch.application.query.SingleTermQuery;
import com.elasticsearch.application.query.SoulQueryUtil;
import com.elasticsearch.application.query.ThreeTermQuery;

public class PageHttpClient extends BaseHttpClient {
	private static final Log log = LogFactory.getLog(PageHttpClient.class);

	public PageHttpClient(String url, String index, String type) {
		super(url, index, type);
	}

	public String pageClientSearch(String queryStr, int from, int size,
			String tagType) {
		boolean bTraditional = false;
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
		} else if (tokens.size() == 5) {
			FiveTermQuery longTermQuery = new FiveTermQuery(tokens);
			List<Map<String, Object>> array1 = longTermQuery.pageQuery();
			queryMap = SoulQueryUtil.createBooleanQueryMap(array1, 1, tag);
		} else { // larger than 5
			Map<Integer, List<Map<String, Object>>> branchs = null;
			List<Map<String, Object>> queryMaps = new LinkedList<Map<String, Object>>();
			for (Integer pos : tokens.keySet()) {
				if (branchs == null) {
					branchs = new HashMap<Integer, List<Map<String, Object>>>();
				}
				List<Map<String, Object>> list = tokens.get(pos);
				branchs.put(pos, list);
				if (branchs.size() == 2) {
					DoubleTermQuery twoQuery = new DoubleTermQuery(branchs);
					List<Map<String, Object>> array1 = twoQuery.pageQuery();
					branchs = null;
					queryMaps.add(SoulQueryUtil.createBooleanQueryMap(array1,
							1, "all", 100.0f));
				}
			}
			if (branchs != null && branchs.size() > 0) {
				if (branchs.size() == 1) { // single word
					SingleTermQuery singleQuery = new SingleTermQuery(branchs);
					List<Map<String, Object>> array1 = singleQuery.pageQuery();
					queryMaps.add(SoulQueryUtil.createBooleanQueryMap(array1,
							1, "all", 50.0f));
				}
			}
			queryMap = SoulQueryUtil.createBooleanQueryMap(queryMaps,
					queryMaps.size() * 3 / 4, tag);
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

	@Override
	public void close() {
		postQuery.close();
	}
}
