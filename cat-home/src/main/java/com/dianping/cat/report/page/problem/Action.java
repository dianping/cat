package com.dianping.cat.report.page.problem;

public enum Action implements org.unidal.web.mvc.Action {
	DETAIL("detail"),

	GROUP("group"),

	THREAD("thread"),

	HOUR_GRAPH("hourlyGraph"),

	HISTORY_REPORT("history"),

	HISTORY_GRAPH("historyGraph"),

	HOULY_REPORT("view"),

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
