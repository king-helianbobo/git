package com.elasticsearch.application;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.yarn.webapp.MimeType;
import org.apache.hadoop.yarn.webapp.WebAppException;
import org.apache.hadoop.yarn.webapp.view.TextView;

import com.google.inject.Inject;

public class PageView extends TextView {
	@Inject
	protected PageView(ViewContext ctx, String contentType) {
		super(ctx, MimeType.HTML);
	}

	static String html = null;

	static {
		html = readFileToString("library/pageIndex.html", "UTF-8");
	}

	public static String readFileToString(String path, String coding) {
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = new FileInputStream(path);
			resourceAsStream.available();
			return com.splitword.soul.utility.IOUtil.getContent(
					resourceAsStream, coding);
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
		try {
			writer().write(html);
			// String result = readFileToString("library/pageIndex.html",
			// "UTF-8");
			// writer().write(result);
			writer().println();
		} catch (Exception e) {
			throw new WebAppException(e);
		}
	}
}
