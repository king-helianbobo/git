package org.apache.hadoop.yarn.webapp;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.yarn.webapp.view.HtmlPage;

/**
 * The obligatory example. No xml/jsp/templates/config files! No proliferation
 * of strange annotations either :)
 * 
 * <p>
 * 3 in 1 example. Check results at <br>
 * http://localhost:9999/hello and <br>
 * http://localhost:9999/hello/html <br>
 * http://localhost:9999/hello/json
 */
@InterfaceAudience.LimitedPrivate({ "YARN", "MapReduce" })
public class HelloWorld {
	public static class HelloController extends Controller {
		@Override
		public void index() {
			renderText("陆逊和周瑜!");
		}

		public void html() {
			setTitle("Hello world!");
		}

		public void json() {
			renderJSON("Hello world!");
		}
	}

	public static class HelloView extends HtmlPage {
		@Override
		protected void render(Page.HTML<_> html) {
			html. // produces valid html 4.01 strict
			title($("title")).p("#hello-for-css")._($("title"))._()._();
		}
	}

	public static void main(String[] args) {
		WebApps.$for(new HelloWorld()).at(9999).inDevMode().start()
				.joinThread();
	}
}
