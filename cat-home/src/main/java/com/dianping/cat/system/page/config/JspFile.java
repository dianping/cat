package com.dianping.cat.system.page.config;

public enum JspFile {
	PROJECT_ALL("/jsp/system/project/project.jsp"),

	AGGREGATION_ALL("/jsp/system/aggregation/aggregation.jsp"),

	AGGREGATION_UPATE("/jsp/system/aggregation/aggregationUpdate.jsp"),

	URL_PATTERN_ALL("/jsp/system/urlPattern/urlPattern.jsp"),

	URL_PATTERN_CONFIG_UPDATE("/jsp/system/urlPattern/urlPatternConfig.jsp"),

	URL_PATTERN_UPATE("/jsp/system/urlPattern/urlPatternUpdate.jsp"),

	TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE("/jsp/system/topology/topologyGraphNodeConfigAdd.jsp"),

	TOPOLOGY_GRAPH_NODE_CONFIG_LIST("/jsp/system/topology/topologyGraphNodeConfigs.jsp"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE("/jsp/system/topology/topologyGraphEdgeConfigAdd.jsp"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_LIST("/jsp/system/topology/topologyGraphEdgeConfigs.jsp"),

	TOPOLOGY_GRAPH_PRODUCT_LINE("/jsp/system/productLine/topologyProductLines.jsp"),

	TOPOLOGY_GRAPH_PRODUCT_ADD_OR_UPDATE("/jsp/system/productLine/topologyProductLineAdd.jsp"),

	TOPO_GRAPH_CONFIG_UPDATE("/jsp/system/topology/topoGraphFormatConfig.jsp"),

	METRIC_CONFIG_ADD_OR_UPDATE("/jsp/system/metric/metricConfigAdd.jsp"),

	METRIC_CONFIG_ADD_OR_UPDATE_SUBMIT("/jsp/system/metric/metricConfigs.jsp"),

	METRIC_RULE_ADD_OR_UPDATE("/jsp/system/metric/metricRuleAdd.jsp"),

	METRIC_RULE_ADD_OR_UPDATE_SUBMIT("/jsp/system/metric/metricConfigs.jsp"),

	METRIC_CONFIG_LIST("/jsp/system/metric/metricConfigs.jsp"),

	METRIC_RULE_CONFIG_UPDATE("/jsp/system/metric/metricRuleConfig.jsp"),

	NETWORK_RULE_CONFIG_LIST("/jsp/system/networkRule/networkConfigs.jsp"),

	NETWORK_RULE_ADD_OR_UPDATE("/jsp/system/networkRule/networkRuleAdd.jsp"),

	NETWORK_RULE_ADD_OR_UPDATE_SUBMIT("/jsp/system/networkRule/networkConfigs.jsp"),

	NETWORK_RULE_DELETE("/jsp/system/networkRule/networkConfigs.jsp"),

	DATABASE_RULE_CONFIG_LIST("/jsp/system/databaseRule/databaseConfigs.jsp"),

	DATABASE_RULE_ADD_OR_UPDATE("/jsp/system/databaseRule/databaseRuleAdd.jsp"),

	DATABASE_RULE_ADD_OR_UPDATE_SUBMIT("/jsp/system/databaseRule/databaseConfigs.jsp"),

	DATABASE_RULE_DELETE("/jsp/system/databaseRule/databaseConfigs.jsp"),

	STORAGE_GROUP_CONFIG_UPDATE("/jsp/system/storage/storageGroupConfig.jsp"),

	SYSTEM_RULE_CONFIG_LIST("/jsp/system/systemRule/systemConfigs.jsp"),

	SYSTEM_RULE_ADD_OR_UPDATE("/jsp/system/systemRule/systemRuleAdd.jsp"),

	SYSTEM_RULE_ADD_OR_UPDATE_SUBMIT("/jsp/system/systemRule/systemConfigs.jsp"),

	SYSTEM_RULE_DELETE("/jsp/system/systemRule/systemConfigs.jsp"),

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

	NET_GRAPH_CONFIG_UPDATE("/jsp/system/netGraphConfig/netGraphConfig.jsp"),

	WEB_RULE("/jsp/system/webRule/webRule.jsp"),

	WEB_RULE_UPDATE("/jsp/system/webRule/webRuleUpdate.jsp"),

	TRANSACTION_RULE("/jsp/system/transactionRule/transactionRule.jsp"),

	TRANSACTION_RULE_UPDATE("/jsp/system/transactionRule/transactionRuleUpdate.jsp"),

	STORAGE_RULE("/jsp/system/storageRule/storageRule.jsp"),

	STORAGE_RULE_UPDATE("/jsp/system/storageRule/storageRuleUpdate.jsp"),

	APP_NAME_CHECK("/jsp/system/appConfig/appNameCheck.jsp"),

	APP_LIST("/jsp/system/appConfig/appList.jsp"),

	APP_CODE_UPDATE("/jsp/system/appConfig/codeUpdate.jsp"),

	APP_SPEED_UPDATE("/jsp/system/appConfig/speedUpdate.jsp"),

	APP_UPDATE("/jsp/system/appConfig/appUpdate.jsp"),

	APP_RULE("/jsp/system/appRule/appRule.jsp"),

	APP_RULE_UPDATE("/jsp/system/appRule/appRuleUpdate.jsp"),

	APP_CONFIG_UPDATE("/jsp/system/appConfig/appConfig.jsp"),

	APP_RULE_BATCH("/jsp/system/appConfig/appConfigBatch.jsp"),

	APP_CONSTANT_UPDATE("/jsp/system/appConfig/constantUpdate.jsp"),

	APP_COMPARISON_CONFIG_UPDATE("/jsp/system/appComparison/appComparison.jsp"),

	APP_COMMAND_FORMAT_CONFIG("/jsp/system/appConfig/appCommandFormatConfig.jsp"),

	ROUTER_CONFIG_UPDATE("/jsp/system/router/routerConfig.jsp"),

	SENDER_CONFIG_UPDATE("/jsp/system/sender/senderConfig.jsp"),

	THIRD_PARTY_CONFIG_LIST("/jsp/system/thirdParty/thirdPartyConfig.jsp"),

	THIRD_PARTY_CONFIG_UPDATE("/jsp/system/thirdParty/thirdPartyConfigUpdate.jsp"),

	DOMAIN_GROUP_CONFIG_LIST("/jsp/system/domainGroup/domainGroupConfig.jsp"),

	DOMAIN_GROUP_CONFIG_UPDATE("/jsp/system/domainGroup/domainGroupConfigUpdate.jsp"),

	ACTIVITY_CONFIG_UPDATE("/jsp/system/activity/activityConfigUpdate.jsp"),

	SERVER_FILTER_CONFIG_UPDATE("/jsp/system/server/serverFilterUpdate.jsp"),

	BLACK_CONFIG_UPDATE("/jsp/system/black/blackConfigUpdate.jsp"),

	ALL_REPORT_CONFIG("/jsp/system/transactionRule/allReportConfig.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
