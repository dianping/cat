package com.dianping.cat.report.page.statistics;

import com.dianping.cat.Constants;

public enum Action implements org.unidal.web.mvc.Action {
	BUG_HISTORY_REPORT("historyBug"),

	BUG_REPORT(Constants.REPORT_BUG),

	BUG_HTTP_JSON("json"),

	SERVICE_REPORT(Constants.REPORT_SERVICE),

	SERVICE_HISTORY_REPORT("historyService"),

	HEAVY_REPORT(Constants.REPORT_HEAVY),

	HEAVY_HISTORY_REPORT("historyHeavy"),

	UTILIZATION_REPORT(Constants.REPORT_UTILIZATION),

	JAR_REPORT(Constants.REPORT_JAR),

	SYSTREM_REPORT(Constants.REPORT_SYSTEM),

	UTILIZATION_HISTORY_REPORT("historyUtilization"),

	ALERT_SUMMARY("summary");

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
