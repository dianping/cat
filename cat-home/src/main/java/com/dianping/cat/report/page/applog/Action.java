package com.dianping.cat.report.page.applog;

public enum Action implements org.unidal.web.mvc.Action {

	APP_LOG("appLog"),

	APP_LOG_DETAIL("appLogDetail"),

	APP_LOG_GRAPH("appLogGraph"),

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
