package org.elasticsearch.app;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class SoulHttpClient implements Closeable {

	private static final Log log = LogFactory.getLog(SoulHttpClient.class);

	private HttpClient client;
	private ObjectMapper mapper = new ObjectMapper();

	public SoulHttpClient() {
		HttpClientParams params = new HttpClientParams();
		params.setConnectionManagerTimeout(20 * 1000);
		client = new HttpClient(params);
		HostConfiguration hostConfig = new HostConfiguration();
		String targetUrl = "http://192.168.2.212:9200";
		try {
			hostConfig.setHost(new URI(targetUrl, false));
		} catch (IOException ex) {
			throw new IllegalArgumentException("Invalid target URI "
					+ targetUrl, ex);
		}
		client.setHostConfiguration(hostConfig);
		HttpConnectionManagerParams connectionParams = client
				.getHttpConnectionManager().getParams();
		// make sure to disable Nagle's protocol
		connectionParams.setTcpNoDelay(true);
	}

	private String getSimpleQueryStringJson(String queryStr) {
		List<String> array = new ArrayList<String>();
		array.add("content^1.0");
		array.add("contenttitle^2.0");
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("analyzer", "soul_query");
		map2.put("default_operator", "and");
		map2.put("fields", array);
		map2.put("query", queryStr);
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("simple_query_string", map2);
		Map<String, Map<String, Object>> fileInfo = new HashMap<String, Map<String, Object>>();
		fileInfo.put("query", map1);
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(fileInfo);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private String getTermQueryJson(String queryStr) {
		Map<String, Object> mapmap = new HashMap<String, Object>();
		mapmap.put("contenttitle", queryStr);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("term", mapmap);
		Map<String, Object> fileInfo = new HashMap<String, Object>();
		fileInfo.put("query", map);
		List<String> array = new ArrayList<String>();
		array.add("contenttitle");
		fileInfo.put("fields", array);
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(fileInfo);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public void refresh(String index) {
		int slash = index.indexOf("/");
		String indx = (slash < 0) ? index : index.substring(0, slash);
		execute(new PostMethod(indx + "/_refresh"));
	}

	public void deleteIndex(String index) {
		execute(new DeleteMethod(index));
	}

	@SuppressWarnings("unchecked")
	public void searchPoint(String queryStr) {
		try {
			String json = this.getSimpleQueryStringJson(queryStr);
			String query = "soul_mini/table/_search";
			Map<String, Object> map = post(query, json, "hits");
			List<Map<String, Object>> tmpResult = (List<Map<String, Object>>) map
					.get("hits");
			int size = tmpResult.size();
			int totalSize = (Integer) map.get("total");
			System.out.println("totalSize = " + totalSize);
			for (int i = 0; i < tmpResult.size(); i++) {
				Map<String, Object> map1 = tmpResult.get(i);
				Map<String, Object> map2 = (Map<String, Object>) map1
						.get("_source");
				System.out.println(map2.get("contenttitle"));
			}
			while (size < totalSize) {
				map = post(query, json, "hits");
				tmpResult = (List<Map<String, Object>>) map.get("hits");
				totalSize = (Integer) map.get("total");
				System.out.println("totalSize = " + totalSize);
				size += tmpResult.size();
				for (int i = 0; i < tmpResult.size(); i++) {
					Map<String, Object> map1 = tmpResult.get(i);
					Map<String, Object> map2 = (Map<String, Object>) map1
							.get("_source");
					System.out.println(map2.get("contenttitle"));
				}
			};
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void suggestSearch(String queryStr) {
		try {
			String json = this.getTermQueryJson(queryStr);
			System.out.println(json);
			String firstQuery = "sogou_mini/table/_search?pretty=true";
			Map<String, Object> map = post(firstQuery, json, "hits");
			List<Map<String, Object>> tmpResult = (List<Map<String, Object>>) map
					.get("hits");
			int size = tmpResult.size();
			int totalSize = (Integer) map.get("total");
			System.out.println("totalSize = " + totalSize);
			for (int i = 0; i < tmpResult.size(); i++) {
				Map<String, Object> map1 = tmpResult.get(i);
				Map<String, Object> map2 = (Map<String, Object>) map1
						.get("fields");
				System.out.println(map2.get("contenttitle"));
			}
			while (size < totalSize) {
				map = post(firstQuery, json, "hits");
				tmpResult = (List<Map<String, Object>>) map.get("hits");
				totalSize = (Integer) map.get("total");
				System.out.println("totalSize = " + totalSize);
				size += tmpResult.size();
				for (int i = 0; i < tmpResult.size(); i++) {
					Map<String, Object> map1 = tmpResult.get(i);
					Map<String, Object> map2 = (Map<String, Object>) map1
							.get("fields");
					System.out.println(map2.get("contenttitle"));
				}
			};
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void scrollSearch(String queryStr) {
		try {
			String json = this.getSimpleQueryStringJson(queryStr);
			String firstQuery = "soul_mini/table/_search?search_type=scan&scroll=10m&size=10";
			PostMethod method = new PostMethod(firstQuery);
			StringRequestEntity requestEntity = new StringRequestEntity(json,
					"application/json", "UTF-8");
			method.setRequestEntity(requestEntity);
			Map<String, Object> map = parseContent(execute(method));
			String scrollId = (String) map.get("_scroll_id");
			Map<String, Object> tmpMap = (Map<String, Object>) map.get("hits");
			int totalSize = (Integer) tmpMap.get("total");
			String query = "_search/scroll?scroll=10m&size=10";
			int size = 0;
			while (size < totalSize) {
				map = post(query, scrollId, "hits");
				List<Map<String, Object>> tmpResult = (List<Map<String, Object>>) map
						.get("hits");
				size += tmpResult.size();
				for (int i = 0; i < tmpResult.size(); i++) {
					Map<String, Object> map1 = tmpResult.get(i);
					Map<String, Object> map2 = (Map<String, Object>) map1
							.get("_source");
					System.out.println(map2.get("contenttitle"));
				}
			};
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void close() {
		HttpConnectionManager manager = client.getHttpConnectionManager();
		if (manager instanceof SimpleHttpConnectionManager) {
			try {
				((SimpleHttpConnectionManager) manager).closeIdleConnections(0);
			} catch (NullPointerException npe) {
			} catch (Exception ex) {
				log.warn("Exception closing underlying HTTP manager", ex);
			}
		}
	}

	byte[] execute(HttpMethodBase method) {
		return execute(method, true);
	}

	byte[] execute(HttpMethodBase method, boolean checkStatus) {
		try {
			int status = client.executeMethod(method);
			if (checkStatus && status >= HttpStatus.SC_MULTI_STATUS) {
				String body = "";
				try {
					InputStream rstream = method.getResponseBodyAsStream();
					// Process the response from Yahoo! Web Services
					BufferedReader br = new BufferedReader(
							new InputStreamReader(rstream));
					// body = method.getResponseBodyAsString();
					String line;
					while ((line = br.readLine()) != null) {
						body += line;
					}
					br.close();
					return body.getBytes();
				} catch (IOException ex) {
					body = "";
				}
				throw new IllegalStateException(String.format(
						"[%s] on [%s] failed; server[%s] returned [%s]", method
								.getName(), method.getURI(), client
								.getHostConfiguration().getHostURL(), body));
			}
			return method.getResponseBody();

		} catch (IOException io) {
			String target;
			try {
				target = method.getURI().toString();
			} catch (IOException ex) {
				target = method.getPath();
			}
			throw new IllegalStateException(String.format(
					"Cannot get response body for [%s][%s]", method.getName(),
					target));
		} finally {
			method.releaseConnection();
		}
	}
	@SuppressWarnings("deprecation")
	private <T> T post(String query, String body, String keyStr)
			throws IOException {
		PostMethod postMethod = new PostMethod(query);
		StringRequestEntity requestEntity = new StringRequestEntity(body,
				"application/json", "UTF-8");
		postMethod.setRequestEntity(requestEntity);
		return parseContent(execute(postMethod), keyStr);
	}
	@SuppressWarnings("unchecked")
	private <T> T parseContent(byte[] content, String string)
			throws IOException {
		// create parser manually to lower Jackson requirements
		JsonParser jsonParser = mapper.getJsonFactory().createJsonParser(
				content);
		Map<String, Object> map = mapper.readValue(jsonParser, Map.class);
		for (Entry<String, Object> entry : map.entrySet()) {
			System.out.println(entry.getKey());
		}
		return (T) (string != null ? map.get(string) : map);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseContent(byte[] content) throws IOException {
		// create parser manually to lower Jackson requirements
		JsonParser jsonParser = mapper.getJsonFactory().createJsonParser(
				content);
		Map<String, Object> map = mapper.readValue(jsonParser, Map.class);
		return map;
	}
}
