package com.elasticsearch.application.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import com.elasticsearch.application.query.PostQuery;

public class BaseHttpClient implements Closeable {

	protected String url = null;
	protected String index = null;
	protected String type = null;
	protected ObjectMapper mapper = new ObjectMapper();
	protected PostQuery postQuery = null;
	private static final Log log = LogFactory.getLog(BaseHttpClient.class);

	public BaseHttpClient(String url, String index, String type) {
		HttpClientParams params = new HttpClientParams();
		params.setConnectionManagerTimeout(20 * 1000);
		HttpClient client = new HttpClient(params);
		HostConfiguration hostConfig = new HostConfiguration();
		this.url = url;
		this.index = index;
		this.type = type;
		try {
			hostConfig.setHost(new URI(this.url, false));
		} catch (IOException ex) {
			throw new IllegalArgumentException("Invalid target URI " + url, ex);
		}
		client.setHostConfiguration(hostConfig);
		HttpConnectionManagerParams connectionParams = client
				.getHttpConnectionManager().getParams();
		// make sure to disable Nagle's protocol
		connectionParams.setTcpNoDelay(true);
		this.postQuery = new PostQuery(client);
	}

	protected List<Map<String, Object>> primeTokenList(String queryStr) {
		String query = index + "/_analyze?analyzer=soul_query_nature&pretty";
		Map<String, Object> map = postQuery.post(query, queryStr);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> tokenMaps = (List<Map<String, Object>>) map
				.get("tokens");
		return tokenMaps;
	}

	protected Map<Integer, List<Map<String, Object>>> secondTokenList(
			List<Map<String, Object>> tokenMaps) {
		Map<Integer, List<Map<String, Object>>> posMaps = new HashMap<Integer, List<Map<String, Object>>>();
		for (int i = 0; i < tokenMaps.size(); i++) {
			Map<String, Object> tmp = tokenMaps.get(i);
			String type = (String) tmp.get("type");
			int position = (Integer) tmp.get("position");
			if (type.equals("null") || type.equals("w"))
				continue;
			List<Map<String, Object>> mapList = posMaps.get(position);
			if (mapList == null)
				mapList = new LinkedList<Map<String, Object>>();
			mapList.add(tmp);
			posMaps.put(position, mapList);
		}
		if (posMaps.isEmpty())
			return null;
		else {
			Map<Integer, List<Map<String, Object>>> result = new HashMap<Integer, List<Map<String, Object>>>();
			Set<String> set = new HashSet<String>();
			for (Integer pos : posMaps.keySet()) {
				List<Map<String, Object>> list = posMaps.get(pos);
				String key = (String) list.get(0).get("token");
				if (!set.contains(key)) {
					set.add(key);
					result.put(pos, list);
				}
			}
			// log.info(ExtractUtil.setToString(set, false));
			return result;
		}
	}

	@Override
	public void close() {
		postQuery.close();
	}

	protected static String convertTag(String tagType) {
		final String[] tags = { "魅力锡城", "锡城资讯", "信息公开", "公共服务", "行政服务", "政民互动",
				"领导之窗" };
		String tag = "all";
		if (tagType.equalsIgnoreCase("tag1"))
			tag = tags[0];
		else if (tagType.equalsIgnoreCase("tag2"))
			tag = tags[1];
		else if (tagType.equalsIgnoreCase("tag3"))
			tag = tags[2];
		else if (tagType.equalsIgnoreCase("tag4"))
			tag = tags[3];
		else if (tagType.equalsIgnoreCase("tag5"))
			tag = tags[4];
		else if (tagType.equalsIgnoreCase("tag6"))
			tag = tags[5];
		else if (tagType.equalsIgnoreCase("tag7"))
			tag = tags[6];
		else if (tagType.equalsIgnoreCase("first"))
			tag = "first";

		return tag;
	}

}
