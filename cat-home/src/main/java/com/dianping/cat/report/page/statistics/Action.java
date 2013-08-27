package com.dianping.cat.report.page.statistics;

public enum Action implements org.unidal.web.mvc.Action {
	BUG_HISTORY_REPORT("historyBug"),

	BUG_REPORT("bug"),

	BUG_HTTP_JSON("json"),

	SERVICE_REPORT("service"),

	SERVICE_HISTORY_REPORT("historyService"),

	HEAVY_REPORT("heavy"),

	HEAVY_HISTORY_REPORT("historyHeavy"),

	UTILIZATION_REPORT("utilization"),

	UTILIZATION_HISTORY_REPORT("historyUtilization");

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
