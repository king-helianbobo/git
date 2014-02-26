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

	private String spellCheckJson(String queryStr) {
		Map<String, String> fileInfo = new HashMap<String, String>();
		fileInfo.put("term", queryStr);
		fileInfo.put("field", "content");
		fileInfo.put("similarity", "-0.8");
		fileInfo.put("type", "soul");
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(fileInfo);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	private String simpleQueryStringJson(String queryStr) {
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
		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		map.put("query", map1);
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(map);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	private String termQueryJson(String queryStr) {
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
			return null;
		}
	}
	@SuppressWarnings("unchecked")
	public void anotherSuggestSearch(String queryStr) {
		try {
			String json = spellCheckJson(queryStr);
			log.info(json);
			String firstQuery = "sogou_spellcheck/table/__suggest?pretty=true";
			PostMethod method = new PostMethod(firstQuery);
			StringRequestEntity requestEntity = new StringRequestEntity(json,
					"application/json", "UTF-8");
			method.setRequestEntity(requestEntity);
			Map<String, Object> map = parseContent(execute(method));
			List<String> result = (List<String>) map.get("suggestions");
			for (int i = 0; i < result.size(); i++)
				log.info(result.get(i));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void simpleQuerySearch(String queryStr) {
		try {
			String json = this.simpleQueryStringJson(queryStr);
			String query = "soul_mini/table/_search";
			Map<String, Object> map = post(query, json);
			map = (Map<String, Object>) map.get("hits");
			List<Map<String, Object>> tmpResult = (List<Map<String, Object>>) map
					.get("hits");
			int size = tmpResult.size();
			int totalSize = (Integer) map.get("total");
			log.info("totalSize = " + totalSize);
			for (int i = 0; i < tmpResult.size(); i++) {
				Map<String, Object> map1 = tmpResult.get(i);
				Map<String, Object> map2 = (Map<String, Object>) map1
						.get("_source");
				log.info(map2.get("contenttitle"));
			}
			while (size < totalSize) {
				map = post(query, json);
				map = (Map<String, Object>) map.get("hits");
				tmpResult = (List<Map<String, Object>>) map.get("hits");
				totalSize = (Integer) map.get("total");
				size += tmpResult.size();
				for (int i = 0; i < tmpResult.size(); i++) {
					Map<String, Object> map1 = tmpResult.get(i);
					Map<String, Object> map2 = (Map<String, Object>) map1
							.get("_source");
					log.info(map2.get("contenttitle"));
				}
			};
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	public void suggestSearch(String queryStr) {
		try {
			String json = termQueryJson(queryStr);
			log.info(json);
			String firstQuery = "sogou_mini/table/_search?pretty=true";
			Map<String, Object> map = post(firstQuery, json);
			map = (Map<String, Object>) map.get("hits");
			List<Map<String, Object>> tmpResult = (List<Map<String, Object>>) map
					.get("hits");
			// int size = tmpResult.size();
			int totalSize = (Integer) map.get("total");
			log.info("totalSize = " + totalSize);
			for (int i = 0; i < tmpResult.size(); i++) {
				Map<String, Object> map1 = tmpResult.get(i);
				Map<String, Object> map2 = (Map<String, Object>) map1
						.get("fields");
				log.info(map2.get("contenttitle"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void scrollSearch(String queryStr) {
		try {
			String json = this.simpleQueryStringJson(queryStr);
			log.info(json);
			String firstQuery = "soul_mini/table/_search?search_type=scan&scroll=10m&size=10";
			PostMethod method = new PostMethod(firstQuery);
			StringRequestEntity requestEntity = new StringRequestEntity(json,
					"application/json", "UTF-8");
			method.setRequestEntity(requestEntity);
			Map<String, Object> map = parseContent(execute(method));
			String scrollId = (String) map.get("_scroll_id");
			Map<String, Object> tmpMap = (Map<String, Object>) map.get("hits");
			int totalSize = (Integer) tmpMap.get("total");
			log.info("totalSize = " + totalSize);
			String query = "_search/scroll?scroll=10m&size=10";
			int size = 0;
			while (size < totalSize) {
				map = post(query, scrollId);
				map = (Map<String, Object>) map.get("hits");
				List<Map<String, Object>> tmpResult = (List<Map<String, Object>>) map
						.get("hits");
				size += tmpResult.size();
				for (int i = 0; i < tmpResult.size(); i++) {
					Map<String, Object> map1 = tmpResult.get(i);
					Map<String, Object> map2 = (Map<String, Object>) map1
							.get("_source");
					log.info(map2.get("contenttitle"));
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

	private byte[] execute(HttpMethodBase method) {
		return execute(method, true);
	}
	private byte[] execute(HttpMethodBase method, boolean checkStatus) {
		try {
			int status = client.executeMethod(method);
			if (checkStatus && status >= HttpStatus.SC_MULTI_STATUS) {
				String body = "";
				InputStream rstream = method.getResponseBodyAsStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						rstream));
				String line;
				while ((line = br.readLine()) != null) {
					body += line;
				}
				br.close();
				return body.getBytes();
			} else
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

	// byte[] execute(HttpMethodBase method, boolean checkStatus) {
	// try {
	// int status = client.executeMethod(method);
	// if (checkStatus && status >= HttpStatus.SC_MULTI_STATUS) {
	// String body = "";
	// try {
	// InputStream rstream = method.getResponseBodyAsStream();
	// BufferedReader br = new BufferedReader(
	// new InputStreamReader(rstream));
	// String line;
	// while ((line = br.readLine()) != null) {
	// body += line;
	// }
	// br.close();
	// return body.getBytes();
	// } catch (IOException ex) {
	// body = "";
	// }
	// throw new IllegalStateException(String.format(
	// "[%s] on [%s] failed; server[%s] returned [%s]", method
	// .getName(), method.getURI(), client
	// .getHostConfiguration().getHostURL(), body));
	// }
	// return method.getResponseBody();
	// } catch (IOException io) {
	// String target;
	// try {
	// target = method.getURI().toString();
	// } catch (IOException ex) {
	// target = method.getPath();
	// }
	// throw new IllegalStateException(String.format(
	// "Cannot get response body for [%s][%s]", method.getName(),
	// target));
	// } finally {
	// method.releaseConnection();
	// }
	// }
	@SuppressWarnings("deprecation")
	private Map<String, Object> post(String query, String json)
			throws IOException {
		PostMethod postMethod = new PostMethod(query);
		StringRequestEntity requestEntity = new StringRequestEntity(json,
				"application/json", "UTF-8");
		postMethod.setRequestEntity(requestEntity);
		return parseContent(execute(postMethod));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseContent(byte[] content) throws IOException {
		// create parser manually to lower Jackson requirements
		JsonParser jsonParser = mapper.getJsonFactory().createJsonParser(
				content);
		Map<String, Object> map = mapper.readValue(jsonParser, Map.class);
		return map;
	}

	public void deleteIndex(String index) {
		execute(new DeleteMethod(index));
	}
}
