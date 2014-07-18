package com.elasticsearch.application.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.elasticsearch.application.query.SoulQuery;
import com.elasticsearch.application.query.SoulQueryUtil;

public class LionHttpClient extends BaseHttpClient {

	private static final Log log = LogFactory.getLog(LionHttpClient.class);

	public LionHttpClient(String url, String index, String type) {
		super(url, index, type);
	}

	@SuppressWarnings("unchecked")
	public String suggestSearch(String queryStr) throws IOException {
		String json = SoulQueryUtil.suggestJson(queryStr, "suggest",
				SoulQuery.titleField, 10);
		String indexAndType = index + "/" + type;
		String firstQuery = indexAndType + "/__suggest?pretty";
		Map<String, Object> map = postQuery.post(firstQuery, json);
		List<String> suggest = (List<String>) map.get("suggestions");
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("suggestions", suggest);
		result.put("size", suggest.size());
		result.put("term", queryStr);
		String resultJson = mapper.writeValueAsString(result);
		return resultJson;
	}

	public String ifHelp() throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("result", "yes");
		String resultJson = mapper.writeValueAsString(map);
		return resultJson;
	}

	public String simpleQuerySearch(String queryStr, int from, int size,
			String tagType) {
		String tag = convertTag(tagType);
		String json = SoulQueryUtil.simpleQueryStringJson(queryStr, from, size,
				tag);
		log.info(json);
		String indexAndType = index + "/" + type;
		String query = indexAndType + "/_search?pretty=true";
		Map<String, Object> hitsMap = postQuery.post(query, json);
		return SoulQueryUtil.constructJsonResult(hitsMap, from);
	}

	@SuppressWarnings("unchecked")
	public void scanSearch(String queryStr) {
		try {
			String json = SoulQueryUtil.simpleQueryStringJson(queryStr);
			log.info(json);
			String indexAndType = index + "/" + type;
			String query1 = indexAndType
					+ "/_search?search_type=scan&scroll=4m&size=10";
			Map<String, Object> map = postQuery.post(query1, json);
			String scrollId = (String) map.get("_scroll_id");
			Map<String, Object> tmpMap = (Map<String, Object>) map.get("hits");
			int totalSize = (Integer) tmpMap.get("total");
			log.info("totalSize = " + totalSize);
			log.info("_scroll_id = " + scrollId);
			int size = 0;
			while (size < totalSize) {
				String query = "_search/scroll?scroll=4m&scroll_id=" + scrollId;
				map = postQuery.post(query);
				scrollId = (String) map.get("_scroll_id");
				map = (Map<String, Object>) map.get("hits");
				List<Map<String, Object>> tmpResult = (List<Map<String, Object>>) map
						.get("hits");
				size += tmpResult.size();
				log.info("size = " + size + ", totalSize = " + totalSize
						+ " , tmpResult.size()= " + tmpResult.size());
				if (tmpResult.size() == 0)
					break;
				for (int i = 0; i < tmpResult.size(); i++) {
					Map<String, Object> map1 = tmpResult.get(i);
					double score = (Double) map1.get("_score");
					Map<String, Object> map2 = (Map<String, Object>) map1
							.get("_source");
					log.info(score + ", " + map2.get(SoulQuery.titleField));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void scrollSearchWithSort(String queryStr) {
		try {
			String json = SoulQueryUtil.simpleQueryStringJson(queryStr);
			log.info(json);
			String indexAndType = index + "/" + type;
			String query1 = indexAndType + "/_search?scroll=4m&size=7";
			Map<String, Object> map = postQuery.post(query1, json);
			String scrollId = (String) map.get("_scroll_id");
			Map<String, Object> tmpMap = (Map<String, Object>) map.get("hits");
			List<Map<String, Object>> resut = (List<Map<String, Object>>) tmpMap
					.get("hits");
			int size = resut.size();
			for (int i = 0; i < resut.size(); i++) {
				Map<String, Object> map1 = resut.get(i);
				double score = (Double) map1.get("_score");
				Map<String, Object> map2 = (Map<String, Object>) map1
						.get("_source");
				log.info(score + ", " + map2.get(SoulQuery.titleField));
			}
			int totalSize = (Integer) tmpMap.get("total");
			log.info("totalSize = " + totalSize);
			log.info("_scroll_id = " + scrollId);
			while (size < totalSize) {
				String query = "_search/scroll?scroll=4m&scroll_id=" + scrollId;
				map = postQuery.post(query);
				scrollId = (String) map.get("_scroll_id");
				map = (Map<String, Object>) map.get("hits");
				resut = (List<Map<String, Object>>) map.get("hits");
				size += resut.size();
				log.info("size = " + size + ", totalSize = " + totalSize
						+ " , tmpResult.size()= " + resut.size());
				if (resut.size() == 0)
					break;
				for (int i = 0; i < resut.size(); i++) {
					Map<String, Object> map1 = resut.get(i);
					double score = (Double) map1.get("_score");
					Map<String, Object> map2 = (Map<String, Object>) map1
							.get("_source");
					log.info(score + ", " + map2.get(SoulQuery.titleField));
				}
				resut.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
