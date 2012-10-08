package com.dianping.cat.system.page.alarm;

public enum JspFile {
	ALARM_RECORD_DETAIL("/jsp/system/alarmRecordDetail.jsp"),

	ALARM_RECORD_LIST("/jsp/system/alarmRecordList.jsp"),

	ALARM_RULE_ADD("/jsp/system/alarmRuleAdd.jsp"),

	ALARM_RULE_ADD_SUBMIT("/jsp/system/alarmOpState.jsp"),

	ALARM_RULE_UPDATE("/jsp/system/alarmRuleUpdate.jsp"),

	ALARM_RULE_UPDATE_SUBMIT("/jsp/system/alarmOpState.jsp"),

	ALARM_TEMPLATE_ADD("/jsp/system/alarmTemplateAdd.jsp"),

	ALARM_TEMPLATE_ADD_SUBMIT("/jsp/system/alarmOpState.jsp"),

	ALARM_TEMPLATE_DELETE("/jsp/system/alarmTemplateList.jsp"),

	ALARM_TEMPLATE_LIST("/jsp/system/alarmTemplateList.jsp"),

	ALARM_TEMPLATE_UPDATE("/jsp/system/alarmTemplateUpdate.jsp"),

	ALARM_TEMPLATE_UPDATE_SUBMIT("/jsp/system/alarmOpState.jsp"),

	EXCEPTION_ALARM_RULE_DELETE("/jsp/system/alarmExceptionRules.jsp"),

	EXCEPTION_ALARM_RULE_LIST("/jsp/system/alarmExceptionRules.jsp"),
	
	EXCEPTION_ALARM_RULE_SUB("/jsp/system/alarmExceptionRules.jsp"),

	SERVICE_ALARM_RULE_DELETE("/jsp/system/alarmServiceRules.jsp"),

	SERVICE_ALARM_RULE_LIST("/jsp/system/alarmServiceRules.jsp"),

	SERVICE_ALARM_RULE_SUB("/jsp/system/alarmServiceRules.jsp"),
	
	SCHEDULED_REPORT_LIST("/jsp/system/scheduledReports.jsp"),

	SCHEDULED_REPORT_ADD("/jsp/system/scheduledReportAdd.jsp"),

	SCHEDULED_REPORT_ADD_SUBMIT("/jsp/system/alarmOpState.jsp"),

	SCHEDULED_REPORT_UPDATE("/jsp/system/scheduledReportUpdate.jsp"),

	SCHEDULED_REPORT_UPDATE_SUBMIT("/jsp/system/alarmOpState.jsp"),

	SCHEDULED_REPORT_DELETE("/jsp/system/scheduledReports.jsp"),

	SCHEDULED_REPORT_SUB("/jsp/system/scheduledReports.jsp")

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
