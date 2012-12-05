package com.dianping.cat.system.page.alarm;

public enum Action implements org.unidal.web.mvc.Action {
	ALARM_RECORD_DETAIL("alarmRecordDetail"),
	
	ALARM_RECORD_LIST("alarmRecordList"),

	ALARM_RULE_ADD("alarmRuleAdd"),

	ALARM_RULE_ADD_SUBMIT("alarmRuleAddSubmit"),

	ALARM_RULE_UPDATE("alarmRuleUpdate"),

	ALARM_RULE_UPDATE_SUBMIT("alarmRuleUpdateSubmit"),

	ALARM_TEMPLATE_ADD("alarmTemplateAdd"),

	ALARM_TEMPLATE_ADD_SUBMIT("alarmTemplateAddSubmit"),

	ALARM_TEMPLATE_DELETE("alarmTemplateDelete"),

	ALARM_TEMPLATE_LIST("alarmTemplateList"),

	ALARM_TEMPLATE_UPDATE("alarmTemplateUpdate"),

	ALARM_TEMPLATE_UPDATE_SUBMIT("alarmTemplateUpdateSubmit"),

	EXCEPTION_ALARM_RULE_DELETE("exceptionAlarmRuleDelete"),

	EXCEPTION_ALARM_RULE_LIST("exceptionAlarmRules"),

	EXCEPTION_ALARM_RULE_SUB("exceptionAlarmRuleSub"),

	REPORT_RECORD_LIST("reportRecordList"),

	SCHEDULED_REPORT_ADD("scheduledReportAdd"),

	SCHEDULED_REPORT_ADD_SUBMIT("scheduledReportAddSubmit"),

	SCHEDULED_REPORT_DELETE("scheduledReportDelete"),

	SCHEDULED_REPORT_LIST("scheduledReports"),

	SCHEDULED_REPORT_SUB("scheduledReportSub"),

	SCHEDULED_REPORT_UPDATE("scheduledReportUpdate"),

	SCHEDULED_REPORT_UPDATE_SUBMIT("scheduledReportUpdateSubmit"),

	SERVICE_ALARM_RULE_DELETE("serviceAlarmRuleDelete"),

	SERVICE_ALARM_RULE_LIST("serviceAlarmRules"),

	SERVICE_ALARM_RULE_SUB("serviceAlarmRuleSub")

	;

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
