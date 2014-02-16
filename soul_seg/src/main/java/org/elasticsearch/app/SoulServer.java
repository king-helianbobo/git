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

import org.soul.analysis.NlpAnalysis;
import org.soul.treeSplit.IOUtil;
import org.soul.treeSplit.StringUtil;
import org.soul.utility.MyStaticValue;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

@SuppressWarnings("all")
public class SoulServer {

	private static final String FILE_ENCODING = System
			.getProperty("file.encoding");

	public void startServer(int serverPort) throws Exception {
		MyStaticValue.libLog.info("starting http server");
		HttpServerProvider provider = HttpServerProvider.provider();
		HttpServer httpserver = provider.createHttpServer(
				new InetSocketAddress(serverPort), 100);
		// 监听端口8888,能同时接受100个请求
		httpserver.createContext("/", new SoulHttpHandler());
		httpserver.setExecutor(null);
		httpserver.start();
		MyStaticValue.libLog.info("server started");
	}

	private static class SoulHttpHandler implements HttpHandler {
		public void handle(HttpExchange httpExchange) {
			try {
				String path = httpExchange.getRequestURI().getPath();
				MyStaticValue.libLog.info("path = " + path);
				if (path != null && path.startsWith("/page")) {
					writeToClient(httpExchange, readFileToString(path));
					return;
				}
				String responseMsg = "欢迎使用Soul中文分词 \n demo:http://localhost:8888/?input=中文分词&method=nlp";
				Map<String, String> paramers = parseParamers(httpExchange);
				String input = paramers.get("input");

				String method = paramers.get("method");
				String nature = paramers.get("nature");
				MyStaticValue.libLog.info("input = " + input + ",method = "
						+ method + ", nature = " + nature);
				if (StringUtil.isNotBlank(input)) {
					responseMsg = SoulServlet.processRequest(input, method,
							nature);
				}
				writeToClient(httpExchange, responseMsg);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					writeToClient(httpExchange, e.getMessage());
				} catch (IOException e1) {
					MyStaticValue.libLog.error("write to client error!");
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
				MyStaticValue.libLog.info("query = " + query);
				parseQuery(query, parameters);
				// post 请求解析
				reader = IOUtil.getReader(httpExchange.getRequestBody(),
						FILE_ENCODING);
				query = IOUtil.getContent(reader).trim();
				MyStaticValue.libLog.info("query = " + query);
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
