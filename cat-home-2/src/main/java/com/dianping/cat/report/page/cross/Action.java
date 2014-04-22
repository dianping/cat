package com.dianping.cat.report.page.cross;

public enum Action implements org.unidal.web.mvc.Action {
	HOURLY_HOST("host"),

	HOURLY_METHOD("method"),

	HOURLY_PROJECT("view"),

	HISTORY_HOST("historyHost"),

	HISTORY_METHOD("historyMethod"),

	HISTORY_PROJECT("history"),

	METHOD_QUERY("query");

	private String m_name;

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	private Action(String name) {
		m_name = name;
	}

	@Override
	public String getName() {
		return m_name;
	}
}
