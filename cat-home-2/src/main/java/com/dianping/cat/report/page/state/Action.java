package com.dianping.cat.report.page.state;

public enum Action implements org.unidal.web.mvc.Action {
	HOURLY("view"),

	HISTORY("history"),

	GRAPH("graph"),

	HISTORY_GRAPH("historyGraph");

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
