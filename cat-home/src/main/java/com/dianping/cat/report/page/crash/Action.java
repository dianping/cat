package com.dianping.cat.report.page.crash;

public enum Action implements org.unidal.web.mvc.Action {
	
	APP_CRASH_LOG("appCrashLog"),

	APP_CRASH_LOG_JSON("appCrashLogJson"),

	APP_CRASH_LOG_DETAIL("appCrashLogDetail"),

	APP_CRASH_GRAPH("appCrashGraph"),

	APP_CRASH_TREND("appCrashTrend"), 
	
	CRASH_STATISTICS("crashStatistics");

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
