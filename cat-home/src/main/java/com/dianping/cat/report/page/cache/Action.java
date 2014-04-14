package com.dianping.cat.report.page.cache;

public enum Action implements org.unidal.web.mvc.Action {
	HISTORY_REPORT("history"),

	HOURLY_REPORT("view");

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
