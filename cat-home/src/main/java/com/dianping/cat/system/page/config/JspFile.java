package com.dianping.cat.system.page.config;

public enum JspFile {
	PROJECT_ALL("/jsp/system/project.jsp"),

	PROJECT_UPATE("/jsp/system/projectUpdate.jsp"),

	AGGREGATION_ALL("/jsp/system/aggregation.jsp"),

	AGGREGATION_UPATE("/jsp/system/aggregationUpdate.jsp"),
	
	TOPOLOGY_GRAPH_CONFIG_ADD_OR_SUBMIT("/jsp/system/topologyGraphConfigNodeAdd.jsp"),

	TOPOLOGY_GRAPH_CONFIG_NODE_ADD_OR_UPDATE_SUBMIT("/jsp/system/topologyGraphConfigNodeAdd.jsp"),

	TOPOLOGY_GRAPH_CONFIG_DELETE("/jsp/system/topologyGraphConfigs.jsp"),

	TOPOLOGY_GRAPH_CONFIG_EDGE_ADD_OR_UPDATE("/jsp/system/topologyGraphConfigEdgeAdd.jsp"),

	TOPOLOGY_GRAPH_CONFIG_EDGE_DELETE("/jsp/system/topologyGraphConfigs.jsp"),
	
	TOPOLOGY_GRAPH_CONFIG_LIST("/jsp/system/topologyGraphConfigs.jsp"), 
	

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
