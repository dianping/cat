package com.dianping.cat.system.page.config;

public enum Action implements org.unidal.web.mvc.Action {
	PROJECT_ALL("projects"),

	PROJECT_UPDATE("update"),

	PROJECT_UPDATE_SUBMIT("updateSubmit"),
	
	AGGREGATION_ALL("aggregations"),
	
	AGGREGATION_UPDATE("aggregationUpdate"),

	AGGREGATION_UPDATE_SUBMIT("aggregationUpdateSubmit"),	
	
	AGGREGATION_DELETE("aggregationDelete"),

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
	
	METRIC_CONFIG_LIST("metricConfigList"),
	
	METRIC_CONFIG_ADD_OR_UPDATE("metricConfigAdd"),
	
	METRIC_CONFIG_ADD_OR_UPDATE_SUBMIT("metricConfigAddSumbit"),

	METRIC_CONFIG_DELETE("metricConfigDelete"),
	
	EXCEPTION_THRESHOLDS("exceptionThresholds"),
	
	EXCEPTION_THRESHOLD_UPDATE("exceptionThresholdUpdate"),
	
	EXCEPTION_THRESHOLD_UPDATE_SUBMIT("exceptionThresholdUpdateSubmit"),
	
	EXCEPTION_THRESHOLD_DELETE("exceptionThresholdDelete"),
	
	BUG_CONFIG_UPDATE("bugConfigUpdate"),
	;

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
