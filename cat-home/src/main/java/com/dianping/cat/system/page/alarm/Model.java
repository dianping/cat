package com.dianping.cat.system.page.alarm;

import java.util.List;

import com.dianping.cat.home.dal.alarm.AlarmRule;
import com.dianping.cat.home.dal.alarm.AlarmTemplate;
import com.dianping.cat.home.dal.alarm.MailRecord;
import com.dianping.cat.home.dal.alarm.ScheduledReport;
import com.dianping.cat.system.SystemPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<SystemPage, Action, Context> {
	private AlarmRule m_alarmRule;

	private AlarmTemplate m_alarmTemplate;

	private List<String> m_domains;

	private MailRecord m_mailRecord;

	private List<MailRecord> m_mailRecords;

	private ScheduledReport m_scheduledReport;

	private List<ScheduledReport> m_schduledReports;

	private String m_opState;

	private int m_templateIndex;

	private List<UserAlarmSubState> m_userSubStates;

	private List<UserReportSubState> m_userReportSubStates;

	public Model(Context ctx) {
		super(ctx);
	}

	public AlarmRule getAlarmRule() {
		return m_alarmRule;
	}

	public AlarmTemplate getAlarmTemplate() {
		return m_alarmTemplate;
	}

	public String getDate() {
		return "";
	}

	@Override
	public Action getDefaultAction() {
		return Action.ALARM_TEMPLATE_LIST;
	}

	public String getDomain() {
		return "";
	}

	public List<String> getDomains() {
		return m_domains;
	}

	public MailRecord getMailRecord() {
		return m_mailRecord;
	}

	public List<MailRecord> getMailRecords() {
		return m_mailRecords;
	}

	public String getOpState() {
		return m_opState;
	}

	public int getTemplateIndex() {
		return m_templateIndex;
	}

	public List<UserAlarmSubState> getUserSubStates() {
		return m_userSubStates;
	}

	public void setAlarmRule(AlarmRule alarmRule) {
		m_alarmRule = alarmRule;
	}

	public void setAlarmTemplate(AlarmTemplate alarmTemplate) {
		m_alarmTemplate = alarmTemplate;
	}

	public void setDomains(List<String> domains) {
		m_domains = domains;
	}

	public void setMailRecord(MailRecord mailRecord) {
		m_mailRecord = mailRecord;
	}

	public void setMailRecords(List<MailRecord> mailRecords) {
		m_mailRecords = mailRecords;
	}

	public void setOpState(String opState) {
		m_opState = opState;
	}

	public void setTemplateIndex(int templateIndex) {
		m_templateIndex = templateIndex;
	}

	public ScheduledReport getScheduledReport() {
		return m_scheduledReport;
	}

	public void setScheduledReport(ScheduledReport scheduledReport) {
		m_scheduledReport = scheduledReport;
	}

	public List<ScheduledReport> getSchduledReports() {
		return m_schduledReports;
	}

	public void setSchduledReports(List<ScheduledReport> schduledReports) {
		m_schduledReports = schduledReports;
	}

	public List<UserReportSubState> getUserReportSubStates() {
		return m_userReportSubStates;
	}

	public void setUserReportSubStates(List<UserReportSubState> userReportSubStates) {
		m_userReportSubStates = userReportSubStates;
	}

	public void setUserSubStates(List<UserAlarmSubState> userSubState) {
		m_userSubStates = userSubState;
	}

}
