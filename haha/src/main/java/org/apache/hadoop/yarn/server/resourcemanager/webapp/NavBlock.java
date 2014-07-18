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

import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.DIV;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.LI;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.UL;
import org.apache.hadoop.yarn.webapp.view.HtmlBlock;

public class NavBlock extends HtmlBlock {

	@Override
	public void render(Block html) {
		UL<DIV<Hamlet>> mainList = html.div("#nav").h3("集群").// Cluster
				ul().li().a(url("cluster"), "关于")._().// About
				li().a(url("nodes"), "")._();// Nodes
		UL<LI<UL<DIV<Hamlet>>>> subAppsList = mainList.li()
				.a(url("apps"), "应用程序").// Applications
				ul();
		subAppsList.li()._();
		String stateTitle = "";
		for (YarnApplicationState state : YarnApplicationState.values()) {
			if ("NEW".equals(state.toString())) {
				stateTitle = "新程序";
			} else if ("NEW_SAVING".equals(state.toString())) {
				stateTitle = "存储的程序";
			} else if ("SUBMITTED".equals(state.toString())) {
				stateTitle = "提交的程序";
			} else if ("ACCEPTED".equals(state.toString())) {
				stateTitle = "授权的程序";
			} else if ("RUNNING".equals(state.toString())) {
				stateTitle = "运行的程序";
			} else if ("FINISHED".equals(state.toString())) {
				stateTitle = "完成的程序";
			} else if ("FAILED".equals(state.toString())) {
				stateTitle = "失败的程序";
			} else if ("KILLED".equals(state.toString())) {
				stateTitle = "关闭的程序";
			}
			subAppsList.li().a(url("apps", state.toString()), stateTitle)._();// state.toString()
		}
		subAppsList._()._();
		mainList.li().a(url("scheduler"), "程序调度")._()._().// Scheduler
				h3("工具").// Tools
				ul().li().a("/conf", "配置")._().// Configuration
				li().a("/logs", "本地日志")._().// Local log
				li().a("/stacks", "服务器集群")._().// Server stacks
				li().a("/metrics", "服务器数据记录")._()._()._();// Server metrics
	}
}
