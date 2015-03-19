package com.dianping.cat.report.page.transaction;

public enum Action implements org.unidal.web.mvc.Action {
	GRAPHS("graphs"),

	HISTORY_GRAPH("historyGraph"),

	HISTORY_REPORT("history"),

	HOURLY_REPORT("view"),

	GROUP_GRAPHS("groupGraphs"),

	HISTORY_GROUP_GRAPH("historyGroupGraph"),

	HISTORY_GROUP_REPORT("historyGroupReport"),

	HOURLY_GROUP_REPORT("groupReport");

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
