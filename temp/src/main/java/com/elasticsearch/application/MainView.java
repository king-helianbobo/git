package com.elasticsearch.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.webapp.MimeType;
import org.apache.hadoop.yarn.webapp.WebAppException;
import org.apache.hadoop.yarn.webapp.view.TextView;

import com.google.inject.Inject;

public class MainView extends TextView {
	private static final Log log = LogFactory.getLog(MainView.class);

	@Inject
	protected MainView(ViewContext ctx, String contentType) {
		super(ctx, MimeType.HTML);
	}

	@Override
	public void render() {
		String result = PageView.readFileToString("library/pageHaha.html",
				"UTF-8");
		log.info(result);
		try {
			writer().write(result);
			writer().println();
		} catch (Exception e) {
			throw new WebAppException(e);
		}
	}
}
