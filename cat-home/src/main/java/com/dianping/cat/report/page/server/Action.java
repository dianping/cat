package com.dianping.cat.report.page.server;

public enum Action implements org.unidal.web.mvc.Action {

	VIEW("view"),

	VIEW_JSON("viewJson"),

	GRAPH("graph"),

	GRAPH_JSON("graphJson"),

	AGGREGATE("aggregate"),

	ENDPOINT("endPoint"),

	MEASUREMTN("measurement"),

	BUILDVIEW("buildview"),

	SCREEN("screen"),

	SCREEN_JSON("screenJson"),

	SCREENS("screens"),

	SCREEN_UPDATE("screenUpdate"),

	SCREEN_DELETE("screenDelete"),

	SCREEN_SUBMIT("screenSubmit"),

	GRAPH_UPDATE("graphUpdate"),

	GRAPH_SUBMIT("graphSubmit"),

	INFLUX_CONFIG_UPDATE("influxConfigUpdate"),

	SERVER_METRIC_CONFIG_UPDATE("serverMetricConfigUpdate"),

	SERVER_ALARM_RULE("serverAlarmRule"),

	SERVER_ALARM_RULE_UPDATE("serverAlarmRuleUpdate"),

	SERVER_ALARM_RULE_SUBMIT("serverAlarmRuleSubmit"),

	SERVER_ALARM_RULE_DELETE("serverAlarmRuleDelete"),

	NET_GRAPH_CONFIG_UPDATE("netGraphConfigUpdate"),

	;

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
