package com.dianping.cat.system.page.config;

public enum Action implements org.unidal.web.mvc.Action {
	PROJECT_ALL("projects"),

	PROJECT_UPDATE_SUBMIT("updateSubmit"),

	PROJECT_DELETE("projectDelete"),

	AGGREGATION_ALL("aggregations"),

	AGGREGATION_UPDATE("aggregationUpdate"),

	AGGREGATION_UPDATE_SUBMIT("aggregationUpdateSubmit"),

	AGGREGATION_DELETE("aggregationDelete"),

	URL_PATTERN_ALL("urlPatterns"),

	URL_PATTERN_CONFIG_UPDATE("urlPatternConfigUpdate"),

	URL_PATTERN_UPDATE("urlPatternUpdate"),

	URL_PATTERN_UPDATE_SUBMIT("urlPatternUpdateSubmit"),

	URL_PATTERN_DELETE("urlPatternDelete"),

	TOPOLOGY_GRAPH_NODE_CONFIG_LIST("topologyGraphNodeConfigList"),

	TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE("topologyGraphNodeConfigAdd"),

	TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE_SUBMIT("topologyGraphNodeConfigAddSumbit"),

	TOPOLOGY_GRAPH_NODE_CONFIG_DELETE("topologyGraphNodeConfigDelete"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE("topologyGraphEdgeConfigAdd"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE_SUBMIT("topologyGraphEdgeConfigAddSumbit"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_DELETE("topologyGraphEdgeConfigDelete"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_LIST("topologyGraphEdgeConfigList"),

	TOPOLOGY_GRAPH_PRODUCT_LINE("topologyProductLines"),

	TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE("topologyProductLineAdd"),

	TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE_SUBMIT("topologyProductLineAddSubmit"),

	TOPOLOGY_GRAPH_PRODUCT_LINE_DELETE("topologyProductLineDelete"),

	TOPO_GRAPH_FORMAT_CONFIG_UPDATE("topoGraphFormatUpdate"),

	METRIC_CONFIG_LIST("metricConfigList"),

	METRIC_CONFIG_ADD_OR_UPDATE("metricConfigAdd"),

	METRIC_CONFIG_ADD_OR_UPDATE_SUBMIT("metricConfigAddSubmit"),

	METRIC_RULE_ADD_OR_UPDATE("metricRuleAdd"),

	METRIC_RULE_ADD_OR_UPDATE_SUBMIT("metricRuleAddSubmit"),

	METRIC_CONFIG_DELETE("metricConfigDelete"),

	METRIC_RULE_CONFIG_UPDATE("metricRuleConfigUpdate"),

	NETWORK_RULE_CONFIG_LIST("networkRuleConfigList"),

	NETWORK_RULE_ADD_OR_UPDATE("networkRuleUpdate"),

	NETWORK_RULE_ADD_OR_UPDATE_SUBMIT("networkRuleSubmit"),

	NETWORK_RULE_DELETE("networkRulDelete"),

	DATABASE_RULE_CONFIG_LIST("databaseRuleConfigList"),

	DATABASE_RULE_ADD_OR_UPDATE("databaseRuleUpdate"),

	DATABASE_RULE_ADD_OR_UPDATE_SUBMIT("databaseRuleSubmit"),

	DATABASE_RULE_DELETE("databaseRulDelete"),

	SYSTEM_RULE_CONFIG_LIST("systemRuleConfigList"),

	SYSTEM_RULE_ADD_OR_UPDATE("systemRuleUpdate"),

	SYSTEM_RULE_ADD_OR_UPDATE_SUBMIT("systemRuleSubmit"),

	SYSTEM_RULE_DELETE("systemRulDelete"),

	HEARTBEAT_RULE_CONFIG_LIST("heartbeatRuleConfigList"),

	HEARTBEAT_RULE_ADD_OR_UPDATE("heartbeatRuleUpdate"),

	HEARTBEAT_RULE_ADD_OR_UPDATE_SUBMIT("heartbeatRuleSubmit"),

	HEARTBEAT_RULE_DELETE("heartbeatRulDelete"),

	ALERT_DEFAULT_RECEIVERS("alertDefaultReceivers"),

	ALERT_POLICY("alertPolicy"),

	DISPLAY_POLICY("displayPolicy"),

	EXCEPTION("exception"),

	EXCEPTION_THRESHOLD_UPDATE("exceptionThresholdUpdate"),

	EXCEPTION_THRESHOLD_ADD("exceptionThresholdAdd"),

	EXCEPTION_THRESHOLD_UPDATE_SUBMIT("exceptionThresholdUpdateSubmit"),

	EXCEPTION_THRESHOLD_DELETE("exceptionThresholdDelete"),

	EXCEPTION_EXCLUDE_ADD("exceptionExcludeAdd"),

	EXCEPTION_EXCLUDE_UPDATE_SUBMIT("exceptionExcludeUpdateSubmit"),

	EXCEPTION_EXCLUDE_DELETE("exceptionExcludeDelete"),

	BUG_CONFIG_UPDATE("bugConfigUpdate"),

	NET_GRAPH_CONFIG_UPDATE("netGraphConfigUpdate"),

	WEB_RULE("webRule"),

	WEB_RULE_ADD_OR_UPDATE("webRuleUpdate"),

	WEB_RULE_ADD_OR_UPDATE_SUBMIT("webRuleSubmit"),

	WEB_RULE_DELETE("webRuleDelete"),

	APP_NAME_CHECK("appNameCheck"),

	APP_LIST("appList"),

	APP_COMMMAND_UPDATE("appUpdate"),

	APP_COMMAND_SUBMIT("appSubmit"),

	APP_COMMAND_DELETE("appPageDelete"),

	APP_CODE_UPDATE("appCodeUpdate"),

	APP_CODE_SUBMIT("appCodeSubmit"),

	APP_CODE_ADD("appCodeAdd"),

	APP_CODE_DELETE("appCodeDelete"),

	APP_SPEED_UPDATE("appSpeedUpdate"),

	APP_SPEED_SUBMIT("appSpeedSubmit"),

	APP_SPEED_ADD("appSpeedAdd"),

	APP_SPEED_DELETE("appSpeedDelete"),

	APP_CONSTANT_ADD("appConstantAdd"),

	APP_CONSTANT_UPDATE("appConstantUpdate"),

	APP_CONSTATN_DELETE("appConstantDelete"),

	APP_CONSTATN_SUBMIT("appConstantSubmit"),

	APP_RULE("appRule"),

	APP_RULE_ADD_OR_UPDATE("appRuleUpdate"),

	APP_RULE_ADD_OR_UPDATE_SUBMIT("appRuleSubmit"),

	APP_RULE_DELETE("appRuleDelete"),

	APP_RULE_BATCH_UPDATE("appRuleBatchUpdate"),

	APP_COMMAND_FORMAT_CONFIG("appCommandFormatConfig"),

	TRANSACTION_RULE("transactionRule"),

	TRANSACTION_RULE_ADD_OR_UPDATE("transactionRuleUpdate"),

	TRANSACTION_RULE_ADD_OR_UPDATE_SUBMIT("transactionRuleSubmit"),

	TRANSACTION_RULE_DELETE("transactionRuleDelete"),

	STORAGE_RULE("storageRule"),

	STORAGE_RULE_ADD_OR_UPDATE("storageRuleUpdate"),

	STORAGE_RULE_ADD_OR_UPDATE_SUBMIT("storageRuleSubmit"),

	STORAGE_RULE_DELETE("storageRuleDelete"),

	STORAGE_GROUP_CONFIG_UPDATE("storageGroupConfigUpdate"),

	APP_CONFIG_UPDATE("appConfigUpdate"),

	APP_COMPARISON_CONFIG_UPDATE("appComparisonConfigUpdate"),

	THIRD_PARTY_RULE_CONFIGS("thirdPartyRuleConfigs"),

	THIRD_PARTY_RULE_UPDATE("thirdPartyRuleUpdate"),

	THIRD_PARTY_RULE_SUBMIT("thirdPartyRuleSubmit"),

	THIRD_PARTY_RULE_DELETE("thirdPartyRuleDelete"),

	DOMAIN_GROUP_CONFIGS("domainGroupConfigs"),

	DOMAIN_GROUP_CONFIG_UPDATE("domainGroupConfigUpdate"),

	DOMAIN_GROUP_CONFIG_SUBMIT("domainGroupConfigSubmit"),

	DOMAIN_GROUP_CONFIG_DELETE("domainGroupConfigDelete"),

	ROUTER_CONFIG_UPDATE("routerConfigUpdate"),

	ALERT_SENDER_CONFIG_UPDATE("alertSenderConfigUpdate"),

	ACTIVITY_CONFIG_UPDATE("activityConfigUpdate"),

	SERVER_FILTER_CONFIG_UPDATE("serverFilterConfigUpdate"),

	ALL_REPORT_CONFIG("allReportConfig"),

	BLACK_CONFIG_UPDATE("blackConfigUpdate");

	private String m_name;

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	private Action(String name) {
		m_name = name;
	}

	@Override
	public String getName() {
		return m_name;
	}
}
