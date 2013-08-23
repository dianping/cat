package com.dianping.cat.report.page.bug;

public enum Action implements org.unidal.web.mvc.Action {
	HISTORY_REPORT("historyBug"),

	HOURLY_REPORT("view"),

	HTTP_JSON("json"),

	SERVICE_REPORT("service"),

	SERVICE_HISTORY_REPORT("historyService");

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
