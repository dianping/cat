package com.dianping.cat.report.page.health;

public enum Action implements org.unidal.web.mvc.Action {
	HOURLY_REPORT("hourly"),

	HISTORY_REPORT("history"),
	
	HISTORY_GRAPH("historyGraph");

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
