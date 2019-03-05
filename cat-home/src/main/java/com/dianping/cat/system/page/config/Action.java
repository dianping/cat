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

public enum Action implements org.unidal.web.mvc.Action {
	PROJECT_ALL("projects"),

	PROJECT_ADD("projectAdd"),

	PROJECT_UPDATE_SUBMIT("updateSubmit"),

	PROJECT_DELETE("projectDelete"),

	TOPOLOGY_GRAPH_NODE_CONFIG_LIST("topologyGraphNodeConfigList"),

	TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE("topologyGraphNodeConfigAdd"),

	TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE_SUBMIT("topologyGraphNodeConfigAddSumbit"),

	TOPOLOGY_GRAPH_NODE_CONFIG_DELETE("topologyGraphNodeConfigDelete"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE("topologyGraphEdgeConfigAdd"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE_SUBMIT("topologyGraphEdgeConfigAddSumbit"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_DELETE("topologyGraphEdgeConfigDelete"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_LIST("topologyGraphEdgeConfigList"),

	TOPO_GRAPH_FORMAT_CONFIG_UPDATE("topoGraphFormatUpdate"),

	HEARTBEAT_RULE_CONFIG_LIST("heartbeatRuleConfigList"),

	HEARTBEAT_RULE_ADD_OR_UPDATE("heartbeatRuleUpdate"),

	HEARTBEAT_RULE_ADD_OR_UPDATE_SUBMIT("heartbeatRuleSubmit"),

	HEARTBEAT_RULE_DELETE("heartbeatRulDelete"),

	ALERT_DEFAULT_RECEIVERS("alertDefaultReceivers"),

	ALERT_POLICY("alertPolicy"),

	HEARTBEAT_DISPLAY_POLICY("displayPolicy"),

	EXCEPTION("exception"),

	EXCEPTION_THRESHOLD_UPDATE("exceptionThresholdUpdate"),

	EXCEPTION_THRESHOLD_ADD("exceptionThresholdAdd"),

	EXCEPTION_THRESHOLD_UPDATE_SUBMIT("exceptionThresholdUpdateSubmit"),

	EXCEPTION_THRESHOLD_DELETE("exceptionThresholdDelete"),

	EXCEPTION_EXCLUDE_ADD("exceptionExcludeAdd"),

	EXCEPTION_EXCLUDE_UPDATE_SUBMIT("exceptionExcludeUpdateSubmit"),

	EXCEPTION_EXCLUDE_DELETE("exceptionExcludeDelete"),

	TRANSACTION_RULE("transactionRule"),

	TRANSACTION_RULE_ADD_OR_UPDATE("transactionRuleUpdate"),

	TRANSACTION_RULE_ADD_OR_UPDATE_SUBMIT("transactionRuleSubmit"),

	TRANSACTION_RULE_DELETE("transactionRuleDelete"),

	EVENT_RULE("eventRule"),

	EVENT_RULE_ADD_OR_UPDATE("eventRuleUpdate"),

	EVENT_RULE_ADD_OR_UPDATE_SUBMIT("eventRuleSubmit"),

	EVENT_RULE_DELETE("eventRuleDelete"),

	STORAGE_RULE("storageRule"),

	STORAGE_RULE_ADD_OR_UPDATE("storageRuleUpdate"),

	STORAGE_RULE_ADD_OR_UPDATE_SUBMIT("storageRuleSubmit"),

	STORAGE_RULE_DELETE("storageRuleDelete"),

	STORAGE_GROUP_CONFIG_UPDATE("storageGroupConfigUpdate"),

	DOMAIN_GROUP_CONFIGS("domainGroupConfigs"),

	DOMAIN_GROUP_CONFIG_UPDATE("domainGroupConfigUpdate"),

	DOMAIN_GROUP_CONFIG_SUBMIT("domainGroupConfigSubmit"),

	DOMAIN_GROUP_CONFIG_DELETE("domainGroupConfigDelete"),

	ROUTER_CONFIG_UPDATE("routerConfigUpdate"),

	SAMPLE_CONFIG_UPDATE("sampleConfigUpdate"),

	ALERT_SENDER_CONFIG_UPDATE("alertSenderConfigUpdate"),

	SERVER_FILTER_CONFIG_UPDATE("serverFilterConfigUpdate"),

	SERVER_CONFIG_UPDATE("serverConfigUpdate"),

	REPORT_RELOAD_CONFIG_UPDATE("reportReloadConfigUpdate"),

	ALL_REPORT_CONFIG("allReportConfig");

	private String m_name;

	private Action(String name) {
		m_name = name;
	}

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	@Override
	public String getName() {
		return m_name;
	}
}
