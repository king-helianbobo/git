package org.apache.hadoop.yarn.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.google.inject.Inject;
import com.splitword.soul.utility.StringUtil;

public class PageWebAppTest {
	private static final Log log = LogFactory.getLog(PageWebAppTest.class);
	static final ObjectMapper mapper = new ObjectMapper();

	static Map<String, String> parseQuery(String query) {
		Map<String, String> parameters = new HashMap<String, String>();
		if (StringUtil.isBlank(query)) {
			return null;
		}
		String splitStrs[] = query.split("\\?");
		query = splitStrs[splitStrs.length - 1];
		splitStrs = query.split("&");

		String key = null;
		String value = null;
		for (String kv : splitStrs) {
			try {
				String[] params = kv.split("=");
				if (params.length == 2) {
					key = URLDecoder.decode(params[0], "utf-8");
					value = URLDecoder.decode(params[1], "utf-8");
					parameters.put(key, value);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return parameters;
	}

	static class PageController extends Controller {
		final PageWebAppTest test;
		private static String host = "http://192.168.2.61:9200";
		private static String index = "official_mini";
		private static String type = "table";

		// private SoulHttpServlet httpServlet = new SoulHttpServlet(host,
		// index,
		// type);

		@Inject
		PageController(PageWebAppTest test) {
			this.test = test;
		}

		@Override
		public void index() {
			render(PageView.class);
		}

		public void query() throws IOException {
			HttpServletRequest request = request();
			log.info(request.getRequestURI());
			log.info(request.getMethod());
			String query = request.getQueryString();
			Map<String, String> val = parseQuery(query);
			log.info(request.getRemoteAddr());
			String resultJson2 = mapper.writeValueAsString(val);
			log.info(resultJson2);
			String input = request.getParameter("input");
			if (StringUtil.isNotBlank(input)) {
				String method = request.getParameter("method");
				String nature = request.getParameter("nature");
				String from = request.getParameter("from");
				String size = request.getParameter("size");
				String tagType = request.getParameter("tagType");
				log.info(from + "," + size + "," + tagType + "," + input);
				if (StringUtil.isNotBlank(from) && StringUtil.isNotBlank(size)
						&& StringUtil.isNotBlank(tagType)) {

					// String responseMsg = httpServlet.processRequest(input,
					// method, nature, from, size, tagType, null, null);
					// if (StringUtil.isNotBlank(responseMsg)) {
					// // LOG.info(responseMsg);
					// renderText(responseMsg);
					// }
				}

			}

		}

		public void suggest() throws IOException {
			HttpServletRequest request = request();
			log.info(request.getRequestURI());
			log.info(request.getMethod());
			String query = request.getQueryString();
			Map<String, String> val = parseQuery(query);
			log.info(request.getRemoteAddr());
			Map<Object, Object> map = request.getParameterMap();

			String resultJson1 = mapper.writeValueAsString(map);
			log.info(resultJson1);
			String resultJson2 = mapper.writeValueAsString(val);
			log.info(resultJson2);

			String input = val.get("input");
			String responseMsg = null;
			if (StringUtil.isNotBlank(input)) {
				// String method = val.get("method");
				// String nature = val.get("nature");
				// responseMsg = httpServlet.processRequest(input, method,
				// nature,
				// null, null, null, null, null);
				// if (responseMsg != null) {
				// log.info(responseMsg);
				// renderText(responseMsg);
				// }
			}

		}

		public void json() {
			Map<String, Object> val = new HashMap<String, Object>();
			List<String> list = new ArrayList<String>();
			list.add("1000");
			list.add("2000");
			list.add("3000");
			val.put("suggestions", list);
			val.put("term", "JSON");
			// renderJSON("Hello world!");
			renderJSON(val);
		}

	}

	public static void main(String[] args) throws Exception {
		// For manual controller/view testing.
		WebApps.$for("", new PageWebAppTest()).at(9999).inDevMode().start()
				.joinThread();
		// start(new WebApp() {
		// @Override public void setup() {
		// route("/:foo", FooController.class);
		// route("/foo/:foo", FooController.class);
		// route("/bar", FooController.class);
		// }
		// }).join();
	}

	static class DefaultController extends Controller {
		@Override
		public void index() {
			set("key", "default");
			render(PageView.class);
		}
	}

	String echo(String s) {
		return s;
	}

	@Test
	public void testCreate() {
		WebApp app = WebApps.$for(this).start();
		app.stop();
	}

	@Test
	public void testCreateWithPort() {
		// see if the ephemeral port is updated
		WebApp app = WebApps.$for(this).at(0).start();
		int port = app.getListenerAddress().getPort();
		assertTrue(port > 0);
		app.stop();
		// try to reuse the port
		app = WebApps.$for(this).at(port).start();
		assertEquals(port, app.getListenerAddress().getPort());
		app.stop();
	}

	@Test(expected = org.apache.hadoop.yarn.webapp.WebAppException.class)
	public void testCreateWithBindAddressNonZeroPort() {
		WebApp app = WebApps.$for(this).at("0.0.0.0:50000").start();
		int port = app.getListenerAddress().getPort();
		assertEquals(50000, port);
		// start another WebApp with same NonZero port
		WebApp app2 = WebApps.$for(this).at("0.0.0.0:50000").start();
		// An exception occurs (findPort disabled)
		app.stop();
		app2.stop();
	}

	@Test(expected = org.apache.hadoop.yarn.webapp.WebAppException.class)
	public void testCreateWithNonZeroPort() {
		WebApp app = WebApps.$for(this).at(50000).start();
		int port = app.getListenerAddress().getPort();
		assertEquals(50000, port);
		// start another WebApp with same NonZero port
		WebApp app2 = WebApps.$for(this).at(50000).start();
		// An exception occurs (findPort disabled)
		app.stop();
		app2.stop();
	}

	@Test
	public void testServePaths() {
		WebApp app = WebApps.$for("test", this).start();
		assertEquals("/test", app.getRedirectPath());
		String[] expectedPaths = { "/test", "/test/*" };
		String[] pathSpecs = app.getServePathSpecs();
		assertEquals(2, pathSpecs.length);
		for (int i = 0; i < expectedPaths.length; i++) {
			assertTrue(ArrayUtils.contains(pathSpecs, expectedPaths[i]));
		}
		app.stop();
	}

	@Test
	public void testServePathsNoName() {
		WebApp app = WebApps.$for("", this).start();
		assertEquals("/", app.getRedirectPath());
		String[] expectedPaths = { "/*" };
		String[] pathSpecs = app.getServePathSpecs();

		assertEquals(1, pathSpecs.length);
		for (int i = 0; i < expectedPaths.length; i++) {
			assertTrue(ArrayUtils.contains(pathSpecs, expectedPaths[i]));
		}
		app.stop();
	}

	@Test
	public void testDefaultRoutes() throws Exception {
		WebApp app = WebApps.$for("test", this).start();
		String baseUrl = baseUrl(app);
		try {
			assertEquals("foo", getContent(baseUrl + "test/foo").trim());
			assertEquals("foo", getContent(baseUrl + "test/foo/index").trim());
			assertEquals("bar", getContent(baseUrl + "test/foo/bar").trim());
			assertEquals("default", getContent(baseUrl + "test").trim());
			assertEquals("default", getContent(baseUrl + "test/").trim());
			assertEquals("default", getContent(baseUrl).trim());
		} finally {
			app.stop();
		}
	}

	@Test
	public void testCustomRoutes() throws Exception {
		WebApp app = WebApps.$for("test", PageWebAppTest.class, this, "ws")
				.start(new WebApp() {
					@Override
					public void setup() {
						bind(MyTestJAXBContextResolver.class);
						bind(MyTestWebService.class);

						route("/:foo", PageController.class);
						route("/bar/foo", PageController.class, "bar");
						route("/foo/:foo", DefaultController.class);
						route("/foo/bar/:foo", DefaultController.class, "index");
					}
				});
		String baseUrl = baseUrl(app);
		try {
			assertEquals("foo", getContent(baseUrl).trim());
			assertEquals("foo", getContent(baseUrl + "test").trim());
			assertEquals("foo1", getContent(baseUrl + "test/1").trim());
			assertEquals("bar", getContent(baseUrl + "test/bar/foo").trim());
			assertEquals("default", getContent(baseUrl + "test/foo/bar").trim());
			assertEquals("default1", getContent(baseUrl + "test/foo/1").trim());
			assertEquals("default2", getContent(baseUrl + "test/foo/bar/2")
					.trim());
			assertEquals(404, getResponseCode(baseUrl + "test/goo"));
			assertEquals(200, getResponseCode(baseUrl + "ws/v1/test"));
			assertTrue(getContent(baseUrl + "ws/v1/test").contains("myInfo"));
		} finally {
			app.stop();
		}
	}

	// This is to test the GuiceFilter should only be applied to webAppContext,
	// not to staticContext and logContext;
	@Test
	public void testYARNWebAppContext() throws Exception {
		// setting up the log context
		System.setProperty("hadoop.log.dir", "/Not/Existing/dir");
		WebApp app = WebApps.$for("test", this).start(new WebApp() {
			@Override
			public void setup() {
				route("/", PageController.class);
			}
		});
		String baseUrl = baseUrl(app);
		try {
			// should not redirect to foo
			assertFalse("foo".equals(getContent(baseUrl + "static").trim()));
			// Not able to access a non-existing dir, should not redirect to
			// foo.
			assertEquals(404, getResponseCode(baseUrl + "logs"));
			// should be able to redirect to foo.
			assertEquals("foo", getContent(baseUrl).trim());
		} finally {
			app.stop();
		}
	}

	static String baseUrl(WebApp app) {
		return "http://localhost:" + app.port() + "/";
	}

	static String getContent(String url) {
		try {
			StringBuilder out = new StringBuilder();
			InputStream in = new URL(url).openConnection().getInputStream();
			byte[] buffer = new byte[64 * 1024];
			int len = in.read(buffer);
			while (len > 0) {
				out.append(new String(buffer, 0, len));
				len = in.read(buffer);
			}
			return out.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static int getResponseCode(String url) {
		try {
			HttpURLConnection c = (HttpURLConnection) new URL(url)
					.openConnection();
			return c.getResponseCode();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
