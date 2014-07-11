package org.apache.hadoop.yarn.webapp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.yarn.webapp.view.HtmlPage;

import com.google.inject.Inject;
import com.splitword.soul.utility.IOUtil;
//import org.apache.hadoop.yarn.webapp.RequestContext;

/**
 * The embedded UI serves two pages at: <br>
 * http://localhost:9999/my and <br>
 * http://localhost:9999/my/anythingYouWant
 */
@InterfaceAudience.LimitedPrivate({ "YARN", "MapReduce" })
public class MyApp {
	private static final Log log = LogFactory.getLog(MyApp.class);

	public String readFileToString(String path, String coding) {
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = this.getClass().getResourceAsStream(path);
			log.info(path);
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

	// This is an app API
	public String anyAPI() {
		// return "anything, really!";
		String result = readFileToString("/page/index.html", "UTF-8");
		log.info(result);
		return result;
	}

	// Note this is static so it can be in any files.
	public static class MyController extends Controller {
		final MyApp app;

		// The app injection is optional
		@Inject
		MyController(MyApp app, RequestContext ctx) {
			super(ctx);
			this.app = app;
		}

		@Override
		public void index() {
			set("anything", "something");
		}

		public void anythingYouWant() {
			set("anything", app.anyAPI());
		}
	}

	// Ditto
	public static class MyView extends HtmlPage {
		// You can inject the app in views if needed.
		@Override
		public void render(Page.HTML<_> html) {
			// html.title("My App").p("#content_id_for_css_styling")
			// ._("You can have", $("anything"))._()._();
			// html.title("My App").p("#content_id_for_css_styling")
			// ._("", $("anything"))._()._();
			html.title("智能搜索-演示Demo").p("#content_id_for_css_styling")
					._("", $("anything"))._()._();
			// Note, there is no _(); (to parent element) method at root level.
			// and IDE provides instant feedback on what level you're on in
			// the auto-completion drop-downs.
		}
	}

	public static void main(String[] args) throws Exception {
		WebApps.$for(new MyApp()).at(9999).inDevMode().start().joinThread();
	}
}
