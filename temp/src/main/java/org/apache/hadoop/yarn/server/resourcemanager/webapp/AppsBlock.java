/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.server.resourcemanager.webapp;

import static org.apache.hadoop.yarn.util.StringHelper.join;
import static org.apache.hadoop.yarn.webapp.YarnWebParams.APP_STATE;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.C_PROGRESSBAR;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.C_PROGRESSBAR_VALUE;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMApp;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.AppInfo;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.TABLE;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.TBODY;
import org.apache.hadoop.yarn.webapp.util.WebAppUtils;
import org.apache.hadoop.yarn.webapp.view.HtmlBlock;

import com.google.inject.Inject;

class AppsBlock extends HtmlBlock {
	final ConcurrentMap<ApplicationId, RMApp> apps;
	private final Configuration conf;
	private static final Log log = LogFactory.getLog(AppsBlock.class);

	@Inject
	AppsBlock(RMContext rmContext, ViewContext ctx, Configuration conf) {
		super(ctx);
		apps = rmContext.getRMApps();
		this.conf = conf;
	}

	@Override
	public void render(Block html) {
		TBODY<TABLE<Hamlet>> tbody = html.table("#apps")
				.$style("text-align:center").thead().tr().th(".id", "ID")
				.th(".user", "用户").// User
				th(".name", "名称").// Name
				th(".type", "类型").// Application Type"
				th(".queue", "队列").// Queue
				th(".starttime", "开始时间").// StartTime
				th(".finishtime", "完成时间").// FinishTime
				th(".state", "状态").// State
				th(".finalstatus", "最终状态").// FinalStatus
				th(".progress", "进度").// Progress
				th(".ui", "相关界面")._()._().// Tracking UI
				tbody();
		Collection<YarnApplicationState> reqAppStates = null;
		String reqStateString = $(APP_STATE);
		if (reqStateString != null && !reqStateString.isEmpty()) {
			String[] appStateStrings = reqStateString.split(",");
			reqAppStates = new HashSet<YarnApplicationState>(
					appStateStrings.length);
			for (String stateString : appStateStrings) {
				reqAppStates.add(YarnApplicationState.valueOf(stateString));
			}
		}
		StringBuilder appsTableData = new StringBuilder("[\n");
		for (RMApp app : apps.values()) {
			if (reqAppStates != null
					&& !reqAppStates.contains(app.createApplicationState())) {
				continue;
			}
			AppInfo appInfo = new AppInfo(app, true,
					WebAppUtils.getHttpSchemePrefix(conf));
			String percent = String.format("%.1f", appInfo.getProgress());
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			/*
			 * int tmpWidth1= new Integer((int)appInfo.getProgress()); int
			 * tmpWidth2=new Integer(100); int totalWidth= tmpWidth1+tmpWidth2;
			 */
			String applocationNodeTile = "";
			String applocationType = "";
			String finalstatusFlag = "";
			if ("NEW".equals(appInfo.getState())) {
				applocationNodeTile = "新任务";
			} else if ("NEW_SAVING".equals(appInfo.getState())) {
				applocationNodeTile = "存储";
			} else if ("SUBMITTED".equals(appInfo.getState())) {
				applocationNodeTile = "提交";
			} else if ("ACCEPTED".equals(appInfo.getState())) {
				applocationNodeTile = "授权";
			} else if ("RUNNING".equals(appInfo.getState())) {
				applocationNodeTile = "运行";
			} else if ("FINISHED".equals(appInfo.getState())) {
				applocationNodeTile = "完成";
			} else if ("FAILED".equals(appInfo.getState())) {
				applocationNodeTile = "出错";
			} else if ("KILLED".equals(appInfo.getState())) {
				applocationNodeTile = "关闭";
			} else {
				applocationNodeTile = appInfo.getState();
			}
			if ("UNASSIGNED".equals(appInfo.getTrackingUI())) {
				applocationType = "未分配";
			}
			if ("UNDEFINED".equals(appInfo.getFinalStatus())) {
				finalstatusFlag = "成功";
			} else {
				finalstatusFlag = appInfo.getFinalStatus();
			}
			String finishTime = formatter.format(appInfo.getFinishTime());
			log.info("finishTime = " + finishTime + "," + finishTime.toString());
			// String name =
			// AppID numerical value parsed by parseHadoopID in
			// yarn.dt.plugins.js
			appsTableData
					.append("[\"<a href='")
					.append(url("app", appInfo.getAppId()))
					.append("'>")
					.append(appInfo.getAppId())
					.append("</a>\",\"")
					.append(StringEscapeUtils
							.escapeJavaScript(StringEscapeUtils
									.escapeHtml(appInfo.getUser())))
					.append("\",\"")
					.append(StringEscapeUtils
							.escapeJavaScript(StringEscapeUtils
									.escapeHtml(appInfo.getName().toString())))
					.append("\",\"")
					.append(StringEscapeUtils
							.escapeJavaScript(StringEscapeUtils
									.escapeHtml(appInfo.getApplicationType())))
					.append("\",\"")
					.append(StringEscapeUtils
							.escapeJavaScript(StringEscapeUtils
									.escapeHtml(appInfo.getQueue())))
					.append("\",\"")
					.append(formatter.format(appInfo.getStartTime()))
					.append("\",\"")
					.append(finishTime.toString())
					.append("\",\"")
					.append(applocationNodeTile)
					.append("\",\"")
					// appInfo.getState()
					.append(finalstatusFlag)
					.append("\",\"")
					// appInfo.getFinalStatus()
					// Progress bar
					.append("<br title='")
					.append(percent)
					.append("'> <div class='")
					.append(C_PROGRESSBAR)
					.append("' title='")
					// .append(join(percent,
					// '%')).append("'> ").append("<div class='")
					.append(join(percent, '%')).append("'> ")
					.append("<div class='").append(C_PROGRESSBAR_VALUE)
					.append("' style='").append(join("width:", percent, '%'))
					.append("'> </div> </div>").append("\",\"<a href='");

			String trackingURL = !appInfo.isTrackingUrlReady() ? "#" : appInfo
					.getTrackingUrlPretty();

			appsTableData.append(trackingURL).append("'>")
					.append(applocationType).append("</a>\"],\n");// appInfo.getTrackingUI()

		}
		if (appsTableData.charAt(appsTableData.length() - 2) == ',') {
			appsTableData.delete(appsTableData.length() - 2,
					appsTableData.length() - 1);
		}
		appsTableData.append("]");
		html.script().$type("text/javascript")
				._("var appsTableData=" + appsTableData)._();

		tbody._()._();
	}
}
