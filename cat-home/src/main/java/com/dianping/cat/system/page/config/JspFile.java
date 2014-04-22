package com.dianping.cat.system.page.config;

public enum JspFile {
	PROJECT_ALL("/jsp/system/project/project.jsp"),

	PROJECT_UPATE("/jsp/system/project/projectUpdate.jsp"),

	AGGREGATION_ALL("/jsp/system/aggregation/aggregation.jsp"),

	AGGREGATION_UPATE("/jsp/system/aggregation/aggregationUpdate.jsp"),

	TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE("/jsp/system/topology/topologyGraphNodeConfigAdd.jsp"),

	TOPOLOGY_GRAPH_NODE_CONFIG_LIST("/jsp/system/topology/topologyGraphNodeConfigs.jsp"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE("/jsp/system/topology/topologyGraphEdgeConfigAdd.jsp"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_LIST("/jsp/system/topology/topologyGraphEdgeConfigs.jsp"),

	TOPOLOGY_GRAPH_PRODUCT_LINE("/jsp/system/productLine/topologyProductLines.jsp"),

	TOPOLOGY_GRAPH_PRODUCT_ADD_OR_UPDATE("/jsp/system/productLine/topologyProductLineAdd.jsp"),

	METRIC_CONFIG_ADD_OR_UPDATE("/jsp/system/metric/metricConfigAdd.jsp"),

	METRIC_CONFIG_ADD_OR_UPDATE_SUBMIT("/jsp/system/metric/metricConfigs.jsp"),

	METRIC_CONFIG_LIST("/jsp/system/metric/metricConfigs.jsp"),

	EXCEPTION_THRESHOLD("/jsp/system/exception/exceptionThreshold.jsp"),

	EXCEPTION_THRESHOLD_CONFIG("/jsp/system/exception/exceptionThresholdConfig.jsp"),

	BUG_CONFIG_UPDATE("/jsp/system/bug/bugConfig.jsp"),

	UTILIZATION_CONFIG_UPDATE("/jsp/system/utilization/utilizationConfig.jsp"),
	
	DOMAIN_GROUP_CONFIG_UPDATE("/jsp/system/domainGroup/domainGroupConfig.jsp"),
	
	METRIC_GROUP_CONFIG_UPDATE("/jsp/system/metricGroup/metricGroupConfig.jsp"),
	
	METRIC_AGGREGATION_CONFIG_UPDATE("/jsp/system/metricAggregation/metricAggregationConfig.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
