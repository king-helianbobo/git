package com.elasticsearch.application;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.webapp.Controller;
import org.apache.hadoop.yarn.webapp.view.TextPage;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.inject.Inject;
import com.splitword.soul.utility.StringUtil;

public class PageHttpMvc {

	private static final Log log = LogFactory.getLog(PageHttpMvc.class);
	static final ObjectMapper mapper = new ObjectMapper();
	public String index = "official_mini";
	public String type = "table";
	public String host = null;

	public PageHttpMvc(String host, String index, String type) {
		this.host = host;
		this.index = index;
		this.type = type;
	}

	static class FooController extends Controller {
		private SoulHttpHandler soulHandler = null;

		@Inject
		FooController(PageHttpMvc handler) {
			soulHandler = new SoulHttpHandler(handler.host, handler.index,
					handler.type);
		}

		@Override
		public void index() {
			HttpServletRequest request = request();
			log.info(request.getRequestURI());
			log.info(request.getMethod());
			log.info(request.getRemoteAddr());
			String query = request.getQueryString();
			log.info(query);
			Map<String, Object> val = new HashMap<String, Object>();
			List<String> list = new ArrayList<String>();
			list.add("1000");
			list.add("2000");
			list.add("3000");
			val.put("suggestions", list);
			val.put("term", "JSON");
			try {
				String resultJson = mapper.writeValueAsString(val);
				renderText(resultJson);
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void query() throws IOException {
			HttpServletRequest request = request();
			String input = request.getParameter("input");
			String method = request.getParameter("method");
			String nature = request.getParameter("nature");
			if (StringUtil.isNotBlank(input) && StringUtil.isNotBlank(method)
					&& StringUtil.isNotBlank(nature)) {
				String from = request.getParameter("from");
				String size = request.getParameter("size");
				String tagType = request.getParameter("tagType");
				if (StringUtil.isNotBlank(from) && StringUtil.isNotBlank(size)
						&& StringUtil.isNotBlank(tagType)) {
					@SuppressWarnings("unchecked")
					String responseMsg = soulHandler.processThisMap(request
							.getParameterMap());
					if (StringUtil.isNotBlank(responseMsg)) {
						renderText(responseMsg);
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public void suggest() throws IOException {
			HttpServletRequest request = request();
			log.info(request.getRequestURI());
			log.info(request.getMethod());
			String query = request.getQueryString();
			log.info(query);
			log.info(request.getRemoteAddr());
			String input = request.getParameter("input");
			String time = request.getParameter("_");
			Date date = new Date(Long.valueOf(time));
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			log.info(df.format(date));
			String responseMsg = null;
			if (StringUtil.isNotBlank(input)) {
				responseMsg = soulHandler.processThisMap(request
						.getParameterMap());
				if (responseMsg != null) {
					renderText(responseMsg);
				}
			}
		}

		public void tables() {
			render(PageView.class);
		}
	}

	static class FooView extends TextPage {
		@Override
		public void render() {
			puts($("key"), $("foo"));
		}
	}
}
