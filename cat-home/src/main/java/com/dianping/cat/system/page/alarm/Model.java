package com.dianping.cat.system.page.alarm;

import java.util.List;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.home.dal.alarm.MailRecord;
import com.dianping.cat.home.dal.alarm.ScheduledReport;
import com.dianping.cat.system.SystemPage;

public class Model extends ViewModel<SystemPage, Action, Context> {

	private List<String> m_domains;

	private MailRecord m_mailRecord;

	private List<MailRecord> m_mailRecords;

	private String m_opState;

	private List<ScheduledReport> m_schduledReports;

	private ScheduledReport m_scheduledReport;

	private int m_templateIndex;

	private List<UserReportSubState> m_userReportSubStates;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getDate() {
		return "";
	}
	
	@Override
	public Action getDefaultAction() {
		return Action.SCHEDULED_REPORT_LIST;
	}

	public String getDomain() {
		return "";
	}

	public List<String> getDomains() {
		return m_domains;
	}

	public String getIpAddress(){
		return "";
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

	public List<ScheduledReport> getSchduledReports() {
		return m_schduledReports;
	}

	public ScheduledReport getScheduledReport() {
		return m_scheduledReport;
	}

	public int getTemplateIndex() {
		return m_templateIndex;
	}

	public List<UserReportSubState> getUserReportSubStates() {
		return m_userReportSubStates;
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

	public void setSchduledReports(List<ScheduledReport> schduledReports) {
		m_schduledReports = schduledReports;
	}

	public void setScheduledReport(ScheduledReport scheduledReport) {
		m_scheduledReport = scheduledReport;
	}

	public void setTemplateIndex(int templateIndex) {
		m_templateIndex = templateIndex;
	}

	public void setUserReportSubStates(List<UserReportSubState> userReportSubStates) {
		m_userReportSubStates = userReportSubStates;
	}

}
