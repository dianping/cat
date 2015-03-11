package com.dianping.cat.report.page.storage;

public enum Action implements org.unidal.web.mvc.Action {
	HOURLY_DATABASE("database"),

	HOURLY_CACHE("cache"),

	HOURLY_DATABASE_GRAPH("hourlyDatabaseGraph"),

	HOURLY_CACHE_GRAPH("hourlyCacheGraph"),

	HISTORY_DATABASE("historyDatabase"),

	HISTORY_CACHE("historyCache"),

	DASHBOARD("dashboard");

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
