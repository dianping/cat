package com.dianping.cat.report.page.app;

public enum Action implements org.unidal.web.mvc.Action {
	LINECHART("view"),

	LINECHART_JSON("linechartJson"),

	PIECHART("piechart"),

	PIECHART_JSON("piechartJson"),

	APP_ADD("appAdd"),

	APP_DELETE("appDelete"),

	APP_CONFIG_FETCH("appConfigFetch"),

	HOURLY_CRASH_LOG("crashLog"),

	HISTORY_CRASH_LOG("historyCrashLog"),

	SPEED("speed"),

	CONN_LINECHART("connLinechart"),

	CONN_PIECHART("connPiechart"),

	STATISTICS("statistics");

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
