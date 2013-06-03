package com.dianping.cat.system.page.config;

public enum JspFile {
	PROJECT_ALL("/jsp/system/project.jsp"),

	PROJECT_UPATE("/jsp/system/projectUpdate.jsp"),

	AGGREGATION_ALL("/jsp/system/aggregation.jsp"),

	AGGREGATION_UPATE("/jsp/system/aggregationUpdate.jsp"),

	TOPOLOGY_GRAPH_CONFIG_ADD("/jsp/system/topologyGraphConfigNodeAdd.jsp"),

	TOPOLOGY_GRAPH_CONFIG_DELETE("/jsp/system/topologyGraphConfigNodeDelete.jsp"),

	TOPOLOGY_GRAPH_CONFIG_EDGE_ADD("/jsp/system/topologyGraphConfigEdgeAdd.jsp"),

	TOPOLOGY_GRAPH_CONFIG_EDGE_DELETE("/jsp/system/topologyGraphConfigEdgeDelete.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
