package org.apache.hadoop.yarn.webapp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.yarn.webapp.view.TextView;

import com.google.inject.Inject;
import com.splitword.soul.utility.IOUtil;

public class PageView extends TextView {
	@Inject
	protected PageView(ViewContext ctx, String contentType) {
		super(ctx, MimeType.HTML);
	}

	public String readFileToString(String path, String coding) {
		InputStream resourceAsStream = null;
		try {
			LOG.info(path + "," + root_url(path));
			resourceAsStream = new FileInputStream(root_url(path));
			resourceAsStream.available();
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

	@Override
	public void render() {
		// context().rendered = true;
		// context().response.setContentType(MimeType.HTML);
		String result = readFileToString("/tmp/index.html", "UTF-8");
		// LOG.info(result);
		try {
			writer().write(result);
			writer().println();
		} catch (Exception e) {
			throw new WebAppException(e);
		}
		// html._(result);
		// html.link(root_url("static/jquery/themes-1.9.1/base/jquery-ui.css"))
		// .link(root_url("static/dt-1.9.4/css/jui-dt.css"))
		// .script(root_url("static/jquery/jquery-1.8.2.min.js"))
		// .script(root_url("static/jquery/jquery-ui-1.9.1.custom.min.js"))
		// .script(root_url("static/dt-1.9.4/js/jquery.dataTables.min.js"))
		// .script(root_url("static/yarn.dt.plugins.js"))
		// .style("#jsnotice { padding: 0.2em; text-align: center; }",
		// ".ui-progressbar { height: 1em; min-width: 5em }"); // required
		// html.head()
		// .link(root_url("css/bootstrap.min.css"))
		// .link(root_url("css/jquery.autocomplete.css"))
		// .script(root_url("static/jquery/jquery-1.8.2.min.js"))
		// .script(root_url("static/jquery/jquery-ui-1.9.1.custom.min.js"))
		// .script(root_url("static/dt-1.9.4/js/jquery.dataTables.min.js"))
		// .script(root_url("static/jquery.autocomplete.min.js"))
		// .script(root_url("static/yarn.dt.plugins.js"))
		// .style("body { padding-top: 295px;}",
		// ".starter-template { padding: 10px 5px;text-align: center;}")
		// .title("智能搜索-演示Demo")._();
		// html.body().div("navbar navbar-inverse navbar-fixed-top")
		// .div("container").div("navbar-header").button()
		// .$type(ButtonType.button).$class("navbar-toggle")
		// .span("sr-only", "Toggle navigation").span("icon-bar")._()
		// .a("navbar-brand", "#", "智能搜索-Demo")._()._()._()._();
		// html.body().div("container").form().$method(Method.post)
		// .$name("form1").$id("form1").div("starter-template")
		// .input().$type(InputType.text)
		// .$style("width: 65%; height:28px;")
		// .$onchange("OnInput(event)").$name("input").$id("input")
		// ._().input().$type(InputType.button)
		// .$class("btn btn-primary").$id("segButton").$value("搜索")
		// ._()._()
		//
		// ._().div("starter-template")._()
		//
		// ._()._();
		// html._();

		// html.div("container").div("navbar-header").button()
		// .$type(ButtonType.button).$class("navbar-toggle")
		// .span("sr-only", "Toggle navigation").span("icon-bar")._()
		// .a("navbar-brand", "#", "智能搜索-Demo")._()._()._()._();
	}
}