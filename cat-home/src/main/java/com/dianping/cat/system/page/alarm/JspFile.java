package com.dianping.cat.system.page.alarm;

public enum JspFile {
	ALARM_RECORD_DETAIL("/jsp/system/alarm/alarmRecordDetail.jsp"),

	ALARM_RECORD_LIST("/jsp/system/alarm/alarmRecordList.jsp"),

	ALARM_RULE_ADD("/jsp/system/alarm/alarmRuleAdd.jsp"),

	ALARM_RULE_ADD_SUBMIT("/jsp/system/alarm/alarmOpState.jsp"),

	ALARM_RULE_UPDATE("/jsp/system/alarm/alarmRuleUpdate.jsp"),

	ALARM_RULE_UPDATE_SUBMIT("/jsp/system/alarm/alarmOpState.jsp"),

	ALARM_TEMPLATE_ADD("/jsp/system/alarm/alarmTemplateAdd.jsp"),

	ALARM_TEMPLATE_ADD_SUBMIT("/jsp/system/alarm/alarmOpState.jsp"),

	ALARM_TEMPLATE_DELETE("/jsp/system/alarm/alarmTemplateList.jsp"),

	ALARM_TEMPLATE_LIST("/jsp/system/alarm/alarmTemplateList.jsp"),

	ALARM_TEMPLATE_UPDATE("/jsp/system/alarm/alarmTemplateUpdate.jsp"),

	ALARM_TEMPLATE_UPDATE_SUBMIT("/jsp/system/alarm/alarmOpState.jsp"),

	EXCEPTION_ALARM_RULE_DELETE("/jsp/system/alarm/alarmExceptionRules.jsp"),

	EXCEPTION_ALARM_RULE_LIST("/jsp/system/alarm/alarmExceptionRules.jsp"),
	
	EXCEPTION_ALARM_RULE_SUB("/jsp/system/alarm/alarmOpState.jsp"),

	SCHEDULED_REPORT_ADD("/jsp/system/alarm/scheduledReportAdd.jsp"),

	SCHEDULED_REPORT_ADD_SUBMIT("/jsp/system/alarm/alarmOpState.jsp"),

	SCHEDULED_REPORT_DELETE("/jsp/system/alarm/scheduledReports.jsp"),
	
	SCHEDULED_REPORT_LIST("/jsp/system/alarm/scheduledReports.jsp"),

	SCHEDULED_REPORT_SUB("/jsp/system/alarm/alarmOpState.jsp"),

	SCHEDULED_REPORT_UPDATE("/jsp/system/alarm/scheduledReportUpdate.jsp"),

	SCHEDULED_REPORT_UPDATE_SUBMIT("/jsp/system/alarm/alarmOpState.jsp"),

	SERVICE_ALARM_RULE_DELETE("/jsp/system/alarm/alarmServiceRules.jsp"),

	SERVICE_ALARM_RULE_LIST("/jsp/system/alarm/alarmServiceRules.jsp"),

	SERVICE_ALARM_RULE_SUB("/jsp/system/alarm/alarmOpState.jsp")

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
