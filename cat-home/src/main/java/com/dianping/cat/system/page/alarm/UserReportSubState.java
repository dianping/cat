package com.dianping.cat.system.page.alarm;

import com.dianping.cat.home.dal.alarm.ScheduledReport;

public class UserReportSubState {

	private ScheduledReport m_scheduledReport;

	private int m_subscriberState;

	public UserReportSubState(ScheduledReport scheduledReport) {
		m_scheduledReport = scheduledReport;
	}

	public ScheduledReport getScheduledReport() {
		return m_scheduledReport;
	}

	public void setScheduledReport(ScheduledReport scheduledReport) {
		m_scheduledReport = scheduledReport;
	}

	public int getSubscriberState() {
		return m_subscriberState;
	}

	public void setSubscriberState(int subscriberState) {
		m_subscriberState = subscriberState;
	}

}
