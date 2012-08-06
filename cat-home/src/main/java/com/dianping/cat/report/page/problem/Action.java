package com.dianping.cat.report.page.problem;

public enum Action implements com.site.web.mvc.Action {
	DETAIL("detail"),

	GROUP("group"),

	HISTORY("history"),

	HISTORY_GRAPH("historyGraph"),

	MOBILE("mobile"),

	THREAD("thread"),

	VIEW("view");

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
