package com.dianping.cat.system.page.config;

public enum Action implements org.unidal.web.mvc.Action {
	PROJECT_ALL("projects"),

	PROJECT_UPDATE("update"),

	PROJECT_UPDATE_SUBMIT("updateSubmit"),
	
	AGGREGATION_ALL("aggregations"),
	
	AGGREGATION_UPDATE("aggregationUpdate"),

	AGGREGATION_UPDATE_SUBMIT("aggregationUpdateSubmit"),	
	
	AGGREGATION_DELETE("aggregationDelete"),
	
	TOPOLOGY_GRAPH_CONFIG_NODE_ADD("topologyGraphConfigNodeAdd"),
	
	TOPOLOGY_GRAPH_CONFIG_NODE_DELETE("topologyGraphConfigNodeDelete"),
	
	TOPOLOGY_GRAPH_CONFIG_EDGE_ADD("topologyGraphConfigEdgeAdd"),
	
	TOPOLOGY_GRAPH_CONFIG_EDGE_DELETE("topologyGraphConfigEdgeDelete");

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	private String m_name;

	private Action(String name) {
		m_name = name;
	}

	@Override
	public String getName() {
		return m_name;
	}
}
