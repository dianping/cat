package com.dianping.cat.system.page.alarm;

public enum Action implements org.unidal.web.mvc.Action {

	ALARM_RECORD_DETAIL("alarmRecordDetail"),

	REPORT_RECORD_LIST("reportRecordList"),

	SCHEDULED_REPORT_ADD("scheduledReportAdd"),

	SCHEDULED_REPORT_ADD_SUBMIT("scheduledReportAddSubmit"),

	SCHEDULED_REPORT_DELETE("scheduledReportDelete"),

	SCHEDULED_REPORT_LIST("scheduledReports"),

	SCHEDULED_REPORT_SUB("scheduledReportSub"),

	SCHEDULED_REPORT_UPDATE("scheduledReportUpdate"),

	SCHEDULED_REPORT_UPDATE_SUBMIT("scheduledReportUpdateSubmit"),

	;

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
