/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.system.page.config;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.system.SystemPage;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case PROJECT_ALL:
			return JspFile.PROJECT_ALL.getPath();
		case PROJECT_ADD:
			return JspFile.PROJECT_ADD.getPath();
		case PROJECT_UPDATE_SUBMIT:
			return JspFile.PROJECT_ALL.getPath();
		case PROJECT_DELETE:
			return JspFile.PROJECT_ALL.getPath();
		// Node Config
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE:
			return JspFile.TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE.getPath();
		case TOPOLOGY_GRAPH_NODE_CONFIG_DELETE:
			return JspFile.TOPOLOGY_GRAPH_NODE_CONFIG_LIST.getPath();
		case TOPOLOGY_GRAPH_NODE_CONFIG_LIST:
			return JspFile.TOPOLOGY_GRAPH_NODE_CONFIG_LIST.getPath();
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE_SUBMIT:
			return JspFile.TOPOLOGY_GRAPH_NODE_CONFIG_LIST.getPath();
		// Edge Config
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE:
			return JspFile.TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE.getPath();
		case TOPOLOGY_GRAPH_EDGE_CONFIG_DELETE:
			return JspFile.TOPOLOGY_GRAPH_EDGE_CONFIG_LIST.getPath();
		case TOPOLOGY_GRAPH_EDGE_CONFIG_LIST:
			return JspFile.TOPOLOGY_GRAPH_EDGE_CONFIG_LIST.getPath();
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE_SUBMIT:
			return JspFile.TOPOLOGY_GRAPH_EDGE_CONFIG_LIST.getPath();
		// Product Line
		case TOPO_GRAPH_FORMAT_CONFIG_UPDATE:
			return JspFile.TOPO_GRAPH_CONFIG_UPDATE.getPath();
		case STORAGE_GROUP_CONFIG_UPDATE:
			return JspFile.STORAGE_GROUP_CONFIG_UPDATE.getPath();
		case HEARTBEAT_RULE_ADD_OR_UPDATE:
			return JspFile.HEARTBEAT_RULE_ADD_OR_UPDATE.getPath();
		case HEARTBEAT_RULE_ADD_OR_UPDATE_SUBMIT:
			return JspFile.HEARTBEAT_RULE_ADD_OR_UPDATE_SUBMIT.getPath();
		case HEARTBEAT_RULE_CONFIG_LIST:
			return JspFile.HEARTBEAT_RULE_CONFIG_LIST.getPath();
		case HEARTBEAT_RULE_DELETE:
			return JspFile.HEARTBEAT_RULE_DELETE.getPath();
		case ALERT_DEFAULT_RECEIVERS:
			return JspFile.ALERT_DEFAULT_RECEIVERS.getPath();
		case ALERT_POLICY:
			return JspFile.ALERT_POLICY.getPath();
		// Excepton Config
		case EXCEPTION:
		case EXCEPTION_THRESHOLD_UPDATE_SUBMIT:
		case EXCEPTION_THRESHOLD_DELETE:
			return JspFile.EXCEPTION.getPath();
		case EXCEPTION_THRESHOLD_UPDATE:
		case EXCEPTION_THRESHOLD_ADD:
			return JspFile.EXCEPTION_THRESHOLD_CONFIG.getPath();
		// Exception Exclude Config
		case EXCEPTION_EXCLUDE_UPDATE_SUBMIT:
		case EXCEPTION_EXCLUDE_DELETE:
			return JspFile.EXCEPTION.getPath();
		case EXCEPTION_EXCLUDE_ADD:
			return JspFile.EXCEPTION_EXCLUDE_CONFIG.getPath();

		case TRANSACTION_RULE:
		case TRANSACTION_RULE_ADD_OR_UPDATE_SUBMIT:
		case TRANSACTION_RULE_DELETE:
			return JspFile.TRANSACTION_RULE.getPath();
		case TRANSACTION_RULE_ADD_OR_UPDATE:
			return JspFile.TRANSACTION_RULE_UPDATE.getPath();
		case EVENT_RULE:
		case EVENT_RULE_ADD_OR_UPDATE_SUBMIT:
		case EVENT_RULE_DELETE:
			return JspFile.EVENT_RULE.getPath();
		case EVENT_RULE_ADD_OR_UPDATE:
			return JspFile.EVENT_RULE_UPDATE.getPath();
		case STORAGE_RULE:
		case STORAGE_RULE_ADD_OR_UPDATE_SUBMIT:
		case STORAGE_RULE_DELETE:
			return JspFile.STORAGE_RULE.getPath();
		case STORAGE_RULE_ADD_OR_UPDATE:
			return JspFile.STORAGE_RULE_UPDATE.getPath();
		case ROUTER_CONFIG_UPDATE:
			return JspFile.ROUTER_CONFIG_UPDATE.getPath();
		case SAMPLE_CONFIG_UPDATE:
			return JspFile.SAMPLE_CONFIG_UPDATE.getPath();
		case ALERT_SENDER_CONFIG_UPDATE:
			return JspFile.SENDER_CONFIG_UPDATE.getPath();
		case HEARTBEAT_DISPLAY_POLICY:
			return JspFile.DISPLAY_POLICY.getPath();
		case DOMAIN_GROUP_CONFIGS:
		case DOMAIN_GROUP_CONFIG_DELETE:
		case DOMAIN_GROUP_CONFIG_SUBMIT:
			return JspFile.DOMAIN_GROUP_CONFIG_LIST.getPath();
		case DOMAIN_GROUP_CONFIG_UPDATE:
			return JspFile.DOMAIN_GROUP_CONFIG_UPDATE.getPath();
		case SERVER_FILTER_CONFIG_UPDATE:
			return JspFile.SERVER_FILTER_CONFIG_UPDATE.getPath();
		case ALL_REPORT_CONFIG:
			return JspFile.ALL_REPORT_CONFIG.getPath();
		case SERVER_CONFIG_UPDATE:
			return JspFile.SERVER_CONFIG_UPDATE.getPath();
		case REPORT_RELOAD_CONFIG_UPDATE:
			return JspFile.REPORT_RELOAD_CONFIG_UPDATE.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
