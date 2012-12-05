package com.dianping.cat.report.page.heatmap;

import java.util.Calendar;
import java.util.Date;

public class HeatMapReport {
	private static final int DAY = 1;

	private static final int HOUR = 0;

	private Date m_endTime = new Date();

	private Date m_startTime = new Date();

	public HeatMapReport(Date startTime, int flag) {
		m_startTime = startTime;
		if (flag == HOUR) {
			m_endTime = new Date(startTime.getTime() + 60 * 60 * 1000);
		} else if (flag == DAY) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(m_startTime);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
			m_startTime = cal.getTime();
			m_endTime = new Date(cal.getTime().getTime() + 60 * 60 * 1000 * 24);
		}
	}

	public Date getEndTime() {
		return m_endTime;
	}

	public Date getStartTime() {
		return m_startTime;
	}

	public void setEndTime(Date endTime) {
		m_endTime = endTime;
	}

	public void setStartTime(Date startTime) {
		m_startTime = startTime;
	}
}
