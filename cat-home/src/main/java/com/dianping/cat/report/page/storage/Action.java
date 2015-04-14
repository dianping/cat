package com.dianping.cat.report.page.storage;

public enum Action implements org.unidal.web.mvc.Action {
	HOURLY_STORAGE("view"),

	HOURLY_STORAGE_GRAPH("hourlyGraph"),

	HISTORY_STORAGE("history"),

	DASHBOARD("dashboard");

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
