package org.soul.utility;

import com.google.common.base.Charsets;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpClient {

	private final URL baseUrl;

	public HttpClient(TransportAddress transportAddress) {
		InetSocketAddress address = ((InetSocketTransportAddress) transportAddress)
				.address();
		try {
			baseUrl = new URL("http", address.getAddress().getHostAddress(),
					address.getPort(), "/");
		} catch (MalformedURLException e) {
			throw new ElasticSearchException("", e);
		}
	}

	public HttpClient(String url) {
		try {
			baseUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new ElasticSearchException("", e);
		}
	}

	public HttpClient(URL url) {
		baseUrl = url;
	}

	public HttpClientResponse request(String path) {
		return request("GET", path);
	}

	public HttpClientResponse request(String method, String path) {
		return request(method, path, null);
	}

	public HttpClientResponse request(String method, String path,
			Map<String, String> headers) {
		URL url;
		try {
			url = new URL(baseUrl, path);
		} catch (MalformedURLException e) {
			throw new ElasticSearchException("Cannot parse " + path, e);
		}

		HttpURLConnection urlConnection;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod(method);
			if (headers != null) {
				for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
					urlConnection.setRequestProperty(headerEntry.getKey(),
							headerEntry.getValue());
				}
			}
			urlConnection.connect();
		} catch (IOException e) {
			throw new ElasticSearchException("", e);
		}

		int errorCode = -1;
		Map<String, List<String>> respHeaders = null;
		try {
			errorCode = urlConnection.getResponseCode();
			respHeaders = urlConnection.getHeaderFields();
			InputStream inputStream = urlConnection.getInputStream();
			String body = null;
			try {
				body = Streams.copyToString(new InputStreamReader(inputStream,
						Charsets.UTF_8));
			} catch (IOException e1) {
				throw new ElasticSearchException(
						"problem reading error stream", e1);
			}
			return new HttpClientResponse(body, errorCode, respHeaders, null);
		} catch (IOException e) {
			InputStream errStream = urlConnection.getErrorStream();
			String body = null;
			if (errStream != null) {
				try {
					body = Streams.copyToString(new InputStreamReader(
							errStream, Charsets.UTF_8));
				} catch (IOException e1) {
					throw new ElasticSearchException(
							"problem reading error stream", e1);
				}
			}
			return new HttpClientResponse(body, errorCode, respHeaders, e);
		} finally {
			urlConnection.disconnect();
		}
	}
}
