package org.elasticsearch.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.splitword.soul.analysis.NlpAnalysis;
import org.splitword.soul.treeSplit.IOUtil;
import org.splitword.soul.treeSplit.StringUtil;
import org.splitword.soul.utility.MyStaticValue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

@SuppressWarnings("all")
public class SoulServer {

	private static final String FILE_ENCODING = System
			.getProperty("file.encoding");
	public static Log libLog = LogFactory.getLog(SoulServer.class);

	public void startServer(int serverPort) throws Exception {
		libLog.info("starting http server");
		HttpServerProvider provider = HttpServerProvider.provider();
		HttpServer httpserver = provider.createHttpServer(
				new InetSocketAddress(serverPort), 100);
		// 监听端口8888,能同时接受100个请求
		httpserver.createContext("/", new SoulHttpHandler());
		httpserver.setExecutor(null);
		httpserver.start();
		libLog.info("server started");
	}

	private static class SoulHttpHandler implements HttpHandler {
		public void handle(HttpExchange httpExchange) {
			try {
				String path = httpExchange.getRequestURI().getPath();
				if (path != null && path.startsWith("/page")) {
					libLog.info("path = " + path);
					writeToClient(httpExchange, readFileToString(path));
					return;
				} else if (path != null && path.endsWith("favicon.ico")) {
					return;
				} else {
					libLog.info("path = " + path);
					Map<String, String> paramers = parseParamers(httpExchange);
					String input = paramers.get("input");
					String method = paramers.get("method");
					String nature = paramers.get("nature");
					libLog.info("input = " + input + ",method = " + method
							+ ", nature = " + nature);
					if (StringUtil.isNotBlank(input)) {
						String responseMsg = SoulServlet.processRequest(input,
								method, nature);
						writeToClient(httpExchange, responseMsg);
					} else {
						writeToClient(httpExchange, "");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					writeToClient(httpExchange, e.getMessage());
				} catch (IOException e1) {
					libLog.error("write to client error!");
				}
			} finally {
				httpExchange.close();
			}
		}

		private String readFileToString(String path) {
			InputStream resourceAsStream = null;
			try {
				resourceAsStream = this.getClass().getResourceAsStream(path);
				resourceAsStream.available();
				return IOUtil.getContent(resourceAsStream, IOUtil.UTF8);
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

		private void writeToClient(HttpExchange httpExchange, String responseMsg)
				throws IOException {
			byte[] bytes = responseMsg.getBytes();
			httpExchange.sendResponseHeaders(200, bytes.length); // 设置响应头属性及响应信息的长度
			OutputStream out = httpExchange.getResponseBody(); // 获得输出流
			out.write(bytes);
			out.flush();
		}

		private Map<String, String> parseParamers(HttpExchange httpExchange)
				throws UnsupportedEncodingException, IOException {
			BufferedReader reader = null;
			try {
				Map<String, String> parameters = new HashMap<String, String>();
				URI requestedUri = httpExchange.getRequestURI();
				String query = requestedUri.getRawQuery();
				libLog.info("RawQuery = [" + query + "]");
				parseQuery(query, parameters);
				reader = IOUtil.getReader(httpExchange.getRequestBody(),
						FILE_ENCODING);
				query = IOUtil.getContent(reader).trim();
				libLog.info("RequestBody = " + query);
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
		 * 从get请求中解析参数
		 * 
		 * @param query
		 * @param parameters
		 */
		private void parseQuery(String query, Map<String, String> parameters) {
			if (StringUtil.isBlank(query)) {
				return;
			}
			libLog.info(query);
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
						key = URLDecoder.decode(param[0], FILE_ENCODING);
						value = URLDecoder.decode(param[1], FILE_ENCODING);
						parameters.put(key, value);
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
