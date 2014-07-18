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

import static org.apache.hadoop.yarn.webapp.YarnWebParams.NODE_STATE;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.DATATABLES;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.DATATABLES_ID;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.initID;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.tableInit;

import java.util.Collection;

import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.ResourceManager;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.ResourceScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.NodeInfo;
import org.apache.hadoop.yarn.util.Times;
import org.apache.hadoop.yarn.webapp.SubView;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.TABLE;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.TBODY;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.TR;
import org.apache.hadoop.yarn.webapp.view.HtmlBlock;

import com.google.inject.Inject;

class NodesPage extends RmView {

	static class NodesBlock extends HtmlBlock {
		final RMContext rmContext;
		final ResourceManager rm;
		private static final long BYTES_IN_MB = 1024 * 1024;

		@Inject
		NodesBlock(RMContext context, ResourceManager rm, ViewContext ctx) {
			super(ctx);
			this.rmContext = context;
			this.rm = rm;
		}

		@Override
		protected void render(Block html) {
			html._(MetricsOverviewTable.class);

			ResourceScheduler sched = rm.getResourceScheduler();
			String type = $(NODE_STATE);
			TBODY<TABLE<Hamlet>> tbody = html.table("#nodes")
					.$style("text-align:center").thead().tr()
					.th(".rack", "Rack").th(".state", "节点状态").// Node State
					th(".nodeaddress", "节点地址").// Node Address
					th(".nodehttpaddress", "节点IP").// Node HTTP Address
					th(".lastHealthUpdate", "最后更新的").// Last health-update
					th(".healthReport", "正常报告").// Health-report
					th(".containers", "分区").// Containers
					th(".mem", "已使用内存").// Mem Used
					th(".mem", "可用内存").// Mem Avail
					th(".nodeManagerVersion", "版本").// Version
					_()._().tbody();
			NodeState stateFilter = null;
			if (type != null && !type.isEmpty()) {
				stateFilter = NodeState.valueOf(type.toUpperCase());
			}
			Collection<RMNode> rmNodes = this.rmContext.getRMNodes().values();
			boolean isInactive = false;
			if (stateFilter != null) {
				switch (stateFilter) {
				case DECOMMISSIONED:
				case LOST:
				case REBOOTED:
					rmNodes = this.rmContext.getInactiveRMNodes().values();
					isInactive = true;
					break;
				}
			}
			for (RMNode ni : rmNodes) {
				if (stateFilter != null) {
					NodeState state = ni.getState();
					if (!stateFilter.equals(state)) {
						continue;
					}
				} else {
					// No filter. User is asking for all nodes. Make sure you
					// skip the
					// unhealthy nodes.
					if (ni.getState() == NodeState.UNHEALTHY) {
						continue;
					}
				}
				NodeInfo info = new NodeInfo(ni, sched);
				int usedMemory = (int) info.getUsedMemory();
				int availableMemory = (int) info.getAvailableMemory();
				String state = "";
				if ("RUNNING".equals(info.getState())) {
					state = "运行中";
				} else if ("REBOOTED".equals(info.getState())) {
					state = "已重启";
				} else if ("UNHEALTHY".equals(info.getState())) {
					state = "异常";
				} else if ("LOST".equals(info.getState())) {
					state = "丢失";
				} else if ("DECOMMISSIONED".equals(info.getState())) {
					state = "停止";
				} else {
					state = info.getState();
				}
				TR<TBODY<TABLE<Hamlet>>> row = tbody.tr().td(info.getRack())
						.td(state).// info.getState()
						td(info.getNodeId());
				if (isInactive) {
					row.td()._("N/A")._();
				} else {
					String httpAddress = info.getNodeHTTPAddress();
					row.td().a("//" + httpAddress, httpAddress)._();
				}
				row.td().br()
						.$title(String.valueOf(info.getLastHealthUpdate()))._()
						._(Times.format(info.getLastHealthUpdate()))._()
						.td(info.getHealthReport())
						.td(String.valueOf(info.getNumContainers())).td().br()
						.$title(String.valueOf(usedMemory))._()
						._(StringUtils.byteDesc(usedMemory * BYTES_IN_MB))._()
						.td().br().$title(String.valueOf(usedMemory))._()
						._(StringUtils.byteDesc(availableMemory * BYTES_IN_MB))
						._().td(ni.getNodeManagerVersion())._();
			}
			tbody._()._();
		}
	}

	@Override
	protected void preHead(Page.HTML<_> html) {
		commonPreHead(html);
		String type = $(NODE_STATE);
		String typeFlag = "";
		if ("rebooted".equals(type)) {
			typeFlag = "重启";
		} else if ("unhealthy".equals(type)) {
			typeFlag = "异常";
		} else if ("lost".equals(type)) {
			typeFlag = "丢失";
		} else if ("decommissioned".equals(type)) {
			typeFlag = "停止";
		}
		String title = "群集的节点";// Nodes of the cluster
		if (type != null && !type.isEmpty()) {
			title = title + " (" + typeFlag + ")";// type
		}
		setTitle(title);
		set(DATATABLES_ID, "nodes");
		set(initID(DATATABLES, "nodes"), nodesTableInit());
		setTableStyles(html, "nodes", ".healthStatus {width:10em}",
				".healthReport {width:10em}");
	}

	@Override
	protected Class<? extends SubView> content() {
		return NodesBlock.class;
	}

	private String nodesTableInit() {
		StringBuilder b = tableInit().append(", aoColumnDefs: [");
		b.append("{'bSearchable': false, 'aTargets': [ 6 ]}");
		b.append(", {'sType': 'title-numeric', 'bSearchable': false, "
				+ "'aTargets': [ 7, 8 ] }");
		b.append(", {'sType': 'title-numeric', 'aTargets': [ 4 ]}");
		b.append("]");
		b.append(
				", oLanguage : { \"sSearch\": \"搜索\",\"sInfo\": \"从 _START_ 到 _END_ /共 _TOTAL_ 条数据\",\"sLengthMenu\": \"每页显示 _MENU_ 条记录\",\"sZeroRecords\": \"抱歉， 没有找到\",\"sInfoEmpty\": \"没有数据\",\"oPaginate\":{\"sFirst\":\"首页\",\"sPrevious\":\"前一页\",\"sNext\":\"下一页\",\"sLast\":\"尾页\"},\"sZeroRecords\": \"没有检索到数据\"}}")
				.toString();
		return b.toString();
	}
}
