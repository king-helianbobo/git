package com.elasticsearch.application.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

public class PostQuery {
	private ObjectMapper mapper = new ObjectMapper();
	private HttpClient client;
	private static final Log log = LogFactory.getLog(PostQuery.class);

	public PostQuery(HttpClient client) {
		this.client = client;
	}

	public Map<String, Object> post(String query, String json) {
		try {
			PostMethod postMethod = new PostMethod(query);
			if (json != null) {
				StringRequestEntity requestEntity = new StringRequestEntity(
						json, "application/json", "UTF-8");
				postMethod.setRequestEntity(requestEntity);
			}
			return parseContent(execute(postMethod));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, Object> post(String query) throws IOException {
		return post(query, null);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseContent(byte[] content) throws IOException {
		// create parser manually to lower Jackson requirements
		JsonParser jsonParser = mapper.getJsonFactory().createJsonParser(
				content);
		Map<String, Object> map = mapper.readValue(jsonParser, Map.class);
		return map;
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

	public void close() {
		HttpConnectionManager manager = client.getHttpConnectionManager();
		if (manager instanceof SimpleHttpConnectionManager) {
			try {
				((SimpleHttpConnectionManager) manager).closeIdleConnections(0);
			} catch (Exception ex) {
				log.warn("Exception closing underlying HTTP manager", ex);
			}
		}
	}

	public void deleteIndex(String index) {
		execute(new DeleteMethod(index));
	}
}
