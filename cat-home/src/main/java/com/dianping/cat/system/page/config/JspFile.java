package com.dianping.cat.system.page.config;

public enum JspFile {
	PROJECT_ALL("/jsp/system/project.jsp"),

	PROJECT_UPATE("/jsp/system/projectUpdate.jsp"),

	AGGREGATION_ALL("/jsp/system/aggregation.jsp"),

	AGGREGATION_UPATE("/jsp/system/aggregationUpdate.jsp"),

	TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_SUBMIT("/jsp/system/topologyGraphNodeConfigAdd.jsp"),

	TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE_SUBMIT("/jsp/system/topologyGraphNodeConfigAdd.jsp"),

	TOPOLOGY_GRAPH_NODE_CONFIG_LIST("/jsp/system/topologyGraphNodeConfigs.jsp"), 
	
	TOPOLOGY_GRAPH_NODE_CONFIG_DELETE("/jsp/system/topologyGraphNodeConfigs.jsp"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE("/jsp/system/topologyGraphEdgeConfigAdd.jsp"),

	TOPOLOGY_GRAPH_EDGE_CONFIG_DELETE("/jsp/system/topologyGraphEdgeConfigs.jsp"),
	
	TOPOLOGY_GRAPH_EDGE_CONFIG_LIST("/jsp/system/topologyGraphEdgeConfigs.jsp"), 
	
	TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE_SUBMIT("/jsp/system/topologyGraphEdgeConfigAdd.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
