package com.dianping.cat.report.page.heatmap;

import java.util.Date;

public class HeatMapReport {
	private Date m_startTime = new Date();

	private Date m_endTime = new Date();

	public Date getStartTime() {
		return m_startTime;
	}

	public void setStartTime(Date startTime) {
		m_startTime = startTime;
	}

	public Date getEndTime() {
		return m_endTime;
	}

	public void setEndTime(Date endTime) {
		m_endTime = endTime;
	}
}
