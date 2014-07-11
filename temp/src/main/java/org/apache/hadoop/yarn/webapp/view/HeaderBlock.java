package org.apache.hadoop.yarn.webapp.view;

import org.apache.hadoop.classification.InterfaceAudience;

@InterfaceAudience.LimitedPrivate({ "YARN", "MapReduce" })
public class HeaderBlock extends HtmlBlock {

	@Override
	protected void render(Block html) {
		String loggedIn = "";
		if (request().getRemoteUser() != null) {
			loggedIn = "当前用户: " + request().getRemoteUser();// Logged in as
		}
		String logTitle = "";
		if ("All Applications".equals($(TITLE))) {
			logTitle = "应用程序";
		} else if ("NEW Applications".equals($(TITLE))) {
			logTitle = "新程序";
		} else if ("NEW_SAVING Applications ".equals($(TITLE))) {
			logTitle = "保存的程序";
		} else if ("SUBMITTED Applications".equals($(TITLE))) {
			logTitle = "提交程序";
		} else if ("ACCEPTED Applications ".equals($(TITLE))) {
			logTitle = "授权的程序";
		} else if ("RUNNING Applications ".equals($(TITLE))) {
			logTitle = "运行的程序";
		} else if ("FINISHED Applications ".equals($(TITLE))) {
			logTitle = "完成的程序";
		} else if ("FAILED Applications".equals($(TITLE))) {
			logTitle = "失败的程序";
		} else if ("KILLED Applications ".equals($(TITLE))) {
			logTitle = "关闭的程序";
		} else if ("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING Applications"
				.equals($(TITLE))) {
			logTitle = "程序调度";
		}
		html.div("#header.ui-widget").div("#user")._(loggedIn)._().div("#logo")
				.img("/static/hadoop-st.png")._().h1(logTitle)._();// $(TITLE)
	}
}
