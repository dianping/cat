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

public enum JspFile {
	PROJECT_ALL("/jsp/system/project/project.jsp"),

	PROJECT_ADD("/jsp/system/project/projectAdd.jsp"),

	TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE("/jsp/system/topology/topologyGraphNodeConfigAdd.jsp"),

	TOPOLOGY_GRAPH_NODE_CONFIG_LIST("/jsp/system/topology/topologyGraphNodeConfigs.jsp"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE("/jsp/system/topology/topologyGraphEdgeConfigAdd.jsp"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_LIST("/jsp/system/topology/topologyGraphEdgeConfigs.jsp"),

	TOPO_GRAPH_CONFIG_UPDATE("/jsp/system/topology/topoGraphFormatConfig.jsp"),

	STORAGE_GROUP_CONFIG_UPDATE("/jsp/system/storage/storageGroupConfig.jsp"),

	HEARTBEAT_RULE_CONFIG_LIST("/jsp/system/heartbeat/heartbeatConfigs.jsp"),

	HEARTBEAT_RULE_ADD_OR_UPDATE("/jsp/system/heartbeat/heartbeatRuleAdd.jsp"),

	HEARTBEAT_RULE_ADD_OR_UPDATE_SUBMIT("/jsp/system/heartbeat/heartbeatConfigs.jsp"),

	HEARTBEAT_RULE_DELETE("/jsp/system/heartbeat/heartbeatConfigs.jsp"),

	ALERT_DEFAULT_RECEIVERS("/jsp/system/defaultReceiver/alertDefaultReceivers.jsp"),

	ALERT_POLICY("/jsp/system/alert/policy.jsp"),

	DISPLAY_POLICY("/jsp/system/display/policy.jsp"),

	EXCEPTION_THRESHOLD("/jsp/system/exception/exceptionThreshold.jsp"),

	EXCEPTION("/jsp/system/exception/exception.jsp"),

	EXCEPTION_THRESHOLD_CONFIG("/jsp/system/exception/exceptionThresholdConfig.jsp"),

	EXCEPTION_EXCLUDE_CONFIG("/jsp/system/exception/exceptionExcludeConfig.jsp"),

	BUG_CONFIG_UPDATE("/jsp/system/bug/bugConfig.jsp"),

	UTILIZATION_CONFIG_UPDATE("/jsp/system/utilization/utilizationConfig.jsp"),

	TRANSACTION_RULE("/jsp/system/transactionRule/transactionRule.jsp"),

	TRANSACTION_RULE_UPDATE("/jsp/system/transactionRule/transactionRuleUpdate.jsp"),

	EVENT_RULE("/jsp/system/eventRule/eventRule.jsp"),

	EVENT_RULE_UPDATE("/jsp/system/eventRule/eventRuleUpdate.jsp"),

	STORAGE_RULE("/jsp/system/storageRule/storageRule.jsp"),

	STORAGE_RULE_UPDATE("/jsp/system/storageRule/storageRuleUpdate.jsp"),

	ROUTER_CONFIG_UPDATE("/jsp/system/router/routerConfig.jsp"),

	SAMPLE_CONFIG_UPDATE("/jsp/system/sample/sampleConfig.jsp"),

	SENDER_CONFIG_UPDATE("/jsp/system/sender/senderConfig.jsp"),

	DOMAIN_GROUP_CONFIG_LIST("/jsp/system/domainGroup/domainGroupConfig.jsp"),

	DOMAIN_GROUP_CONFIG_UPDATE("/jsp/system/domainGroup/domainGroupConfigUpdate.jsp"),

	ACTIVITY_CONFIG_UPDATE("/jsp/system/activity/activityConfigUpdate.jsp"),

	SERVER_FILTER_CONFIG_UPDATE("/jsp/system/server/serverFilterUpdate.jsp"),

	SERVER_CONFIG_UPDATE("/jsp/system/server/serverConfigUpdate.jsp"),

	REPORT_RELOAD_CONFIG_UPDATE("/jsp/system/reload/reportReloadConfigUpdate.jsp"),

	ALL_REPORT_CONFIG("/jsp/system/transactionRule/allReportConfig.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
