package com.dianping.cat.system.page.alarm;

import java.util.Comparator;

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
	
	public static class UserReportSubStateCompartor implements Comparator<UserReportSubState> {

		@Override
		public int compare(UserReportSubState o1, UserReportSubState o2) {
			int sub1 = o1.getSubscriberState();
			int sub2 = o2.getSubscriberState();
			
			if (sub1 != sub2) {
				return sub2 - sub1;
			}
			return o1.getScheduledReport().getDomain().compareTo(o2.getScheduledReport().getDomain());
		}
	}

}
