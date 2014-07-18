package com.elasticsearch.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.cache.CacheBuilder;
import org.elasticsearch.common.cache.CacheLoader;
import org.elasticsearch.common.cache.LoadingCache;

import com.elasticsearch.application.client.LionHttpClient;
import com.elasticsearch.application.client.PageHttpClient;
import com.elasticsearch.application.client.SoulHttpClient;
import com.splitword.soul.utility.IOUtil;
import com.splitword.soul.utility.StringUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("all")
public class SoulHttpHandler implements HttpHandler {
	private static final Log log = LogFactory.getLog(SoulHttpHandler.class);
	private static final String ENCODING = System.getProperty("file.encoding");
	private SoulHttpClient soulClient = null;
	private LionHttpClient lionClient = null;
	private PageHttpClient pageClient = null;
	private static final String splitTag = "#";

	private final LoadingCache<String, String> resultCache;// use cache

	public SoulHttpHandler(String host, String index, String type) {
		soulClient = new SoulHttpClient(host, index, type);
		lionClient = new LionHttpClient(host, index, type);
		pageClient = new PageHttpClient(host, index, type);
		resultCache = CacheBuilder.newBuilder().build(
				new CacheLoader<String, String>() {
					@Override
					public String load(String cacheKey) throws Exception {
						String[] strs = decode(cacheKey);
						String input = strs[0];
						int start = Integer.valueOf(strs[1]);
						int len = Integer.valueOf(strs[2]);
						String tagType = strs[3];
						log.info(strs[0] + "," + strs[1] + "," + strs[2] + ","
								+ strs[3]);
						String json = soulClient.complexQuerySearch(input,
								start, len, tagType);
						return json;
					}
				});
	}

	private static enum SoulMethod {
		BASE, NLP, MYPAGE, HELP
	}

	public String processThisMap(Map<Object, Object> parameters)
			throws IOException {
		Map<String, String> result = new HashMap<String, String>();
		for (Object key : parameters.keySet()) {
			String strKey = (String) key;
			String[] values = (String[]) parameters.get(key);
			String value = values[0];
			result.put(strKey, value);
		}
		return processRequest(result);
	}

	private String code(String input, String from, String size, String tagType) {
		StringBuilder builder = new StringBuilder();
		builder.append(input);
		builder.append(splitTag);
		builder.append(from);
		builder.append(splitTag);
		builder.append(size);
		builder.append(splitTag);
		builder.append(tagType);
		return builder.toString();
	}

	private String[] decode(String cacheKey) {
		String[] strs = new String[4];
		int index = cacheKey.lastIndexOf(splitTag);
		String tagType = cacheKey.substring(index + 1);
		cacheKey = cacheKey.substring(0, index);
		index = cacheKey.lastIndexOf(splitTag);
		String size = cacheKey.substring(index + 1);
		cacheKey = cacheKey.substring(0, index);
		index = cacheKey.lastIndexOf(splitTag);
		String from = cacheKey.substring(index + 1);
		String input = cacheKey.substring(0, index);
		strs[0] = input;
		strs[1] = from;
		strs[2] = size;
		strs[3] = tagType;
		return strs;
	}

	private String processRequest(Map<String, String> parameters)
			throws IOException {
		String input = parameters.get("input");
		String method = parameters.get("method");
		String nature = parameters.get("nature");
		String json = null;
		if (StringUtil.isBlank(input) || StringUtil.isBlank(method)
				|| StringUtil.isBlank(nature))
			return null;
		else {
			SoulMethod methodType = SoulMethod.valueOf(method.toUpperCase());
			switch (methodType) {
			case NLP: {
				String from = parameters.get("from");
				String size = parameters.get("size");
				String tagType = parameters.get("tagType");
				if (from != null && size != null && tagType != null) {
					// int start = Integer.valueOf(from);
					// int len = Integer.valueOf(size);
					// log.info(from + "," + size + "," + tagType + "," +
					// input);
					// json = soulClient.complexQuerySearch(input, start, len,
					// tagType);
					String cacheKey = code(input, from, size, tagType);
					json = resultCache.getUnchecked(cacheKey);
				}
			}
				break;
			case MYPAGE: { // request from page
				String from = parameters.get("from");
				String size = parameters.get("size");
				String tagType = parameters.get("tagType");

				if (from != null && size != null && tagType != null) {
					int start = Integer.valueOf(from);
					int len = Integer.valueOf(size);
					json = pageClient.pageClientSearch(input, start, len,
							tagType);
				}
			}
				break;
			case BASE: {// request from ajax
				json = lionClient.suggestSearch(input);
				log.info(json);
			}
				break;
			case HELP: {
				String ip = parameters.get("ip");
				String ifHelp = parameters.get("ifHelp");
				if (StringUtil.isNotBlank(ifHelp))
					json = lionClient.ifHelp();
			}
				break;
			default: {
				// do nothing
				break;
			}
			}
		}
		return json;
	}

	public void handle(HttpExchange httpExchange) {
		try {
			String path = httpExchange.getRequestURI().getPath();
			if (path != null && path.startsWith("/page")) {
				writeToClient(httpExchange, readTextFile(path, "utf-8"));
				return;
			} else if (path != null && path.startsWith("/test")) {
				if (path.endsWith("png") || path.endsWith("jpg")
						|| path.endsWith("gif")) {
					byte[] bytes = readBinaryFile(path);
					httpExchange.sendResponseHeaders(200, bytes.length);
					OutputStream out = httpExchange.getResponseBody();
					out.write(bytes);
					out.flush();
					return;
				} else {
					writeToClient(httpExchange, readTextFile(path, "utf-8"));
					return;
				}
			} else if (path != null && path.startsWith("/baipi")) {
				if (path.endsWith("png") || path.endsWith("jpg")
						|| path.endsWith("gif")) {
					byte[] bytes = readBinaryFile(path);
					httpExchange.sendResponseHeaders(200, bytes.length);
					OutputStream out = httpExchange.getResponseBody();
					out.write(bytes);
					out.flush();
					return;
				} else {
					writeToClient(httpExchange, readTextFile(path, "utf-8"));
					return;
				}
			}

			else {
				Map<String, String> parameters = parseParameters(httpExchange);
				if (parameters.isEmpty() || parameters == null) {
					log.info(path);
					return;
				} else {
					String responseMsg = processRequest(parameters);
					if (StringUtil.isNotBlank(responseMsg))
						writeToClient(httpExchange, responseMsg);
					else
						return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpExchange.close();
		}
	}

	private String readTextFile(String path, String coding) {
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = this.getClass().getResourceAsStream(path);
			int length = resourceAsStream.available();
			return IOUtil.getContent(resourceAsStream, coding);
		} catch (Exception e) {
			return String.valueOf("Error: 404, File Not Found!");
		} finally {
			if (resourceAsStream != null) {
				try {
					resourceAsStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private byte[] readBinaryFile(String path) {
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = this.getClass().getResourceAsStream(path);
			int length = resourceAsStream.available();
			byte[] buffer = new byte[length * 2];
			int len = resourceAsStream.read(buffer);
			byte[] result = new byte[len];
			System.arraycopy(buffer, 0, result, 0, len);
			return result;
		} catch (Exception e) {
			return String.valueOf("Error: 404, File Not Found!").getBytes();
		} finally {
			if (resourceAsStream != null) {
				try {
					resourceAsStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void writeToClient(HttpExchange httpExchange, String responseMsg)
			throws IOException {
		byte[] bytes = responseMsg.getBytes();
		httpExchange.sendResponseHeaders(200, bytes.length);
		OutputStream out = httpExchange.getResponseBody();
		out.write(bytes);
		out.flush();
	}

	private Map<String, String> parseParameters(HttpExchange httpExchange)
			throws UnsupportedEncodingException, IOException {
		BufferedReader reader = null;
		try {
			Map<String, String> parameters = new HashMap<String, String>();
			URI requestedUri = httpExchange.getRequestURI();
			String query = requestedUri.getRawQuery();
			parseQuery(query, parameters);
			reader = IOUtil.getReader(httpExchange.getRequestBody(), ENCODING);
			query = IOUtil.getContent(reader).trim();
			parseQuery(query, parameters);
			httpExchange.setAttribute("parameters", parameters);
			return parameters;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * parse parameters from get/post request
	 * 
	 * @param query
	 * @param parameters
	 */
	private void parseQuery(String query, Map<String, String> parameters) {
		if (StringUtil.isBlank(query)) {
			return;
		}
		String splitStrs[] = query.split("\\?");
		query = splitStrs[splitStrs.length - 1];
		splitStrs = query.split("&");
		String[] param = null;
		String key = null;
		String value = null;
		for (String kv : splitStrs) {
			try {
				param = kv.split("=");
				if (param.length == 2) {
					key = URLDecoder.decode(param[0], ENCODING);
					value = URLDecoder.decode(param[1], ENCODING);
					parameters.put(key, value);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

}
