package com.dianping.cat.report.page.server;

public enum JspFile {
	VIEW("/jsp/report/server/view.jsp"),

	GRAPH("/jsp/report/server/graph.jsp"),

	SCREEN("/jsp/report/server/screen.jsp"),

	AGGREGATE("/jsp/report/server/aggregate.jsp"),

	JSON("/jsp/report/server/json.jsp"),

	SCREENS("/jsp/report/server/screens.jsp"),

	SCREEN_UPDATE("/jsp/report/server/screenUpdate.jsp"),

	SCREEN_CONFIG_UPDATE("/jsp/report/server/graphUpdate.jsp"),

	INFLUX_CONFIG_UPDATE("/jsp/report/server/influxConfig.jsp"),

	SERVER_METRIC_CONFIG_UPDATE("/jsp/report/server/serverMetricConfig.jsp"),

	SERVER_ALARM_RULE("/jsp/report/server/serverRuleConfig.jsp"),

	SERVER_ALARM_RULE_UPDATE("/jsp/report/server/serverRuleUpdate.jsp"),

	NET_GRAPH_CONFIG_UPDATE("/jsp/report/server/netGraphConfig.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
