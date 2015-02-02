package com.dianping.cat.report.task.overload;

import com.dianping.cat.core.dal.DailyReport;

public class OverloadReport extends DailyReport {

	private int m_reportType;

	private double m_reportLength;

	public int getReportType() {
		return m_reportType;
	}

	public void setReportType(int reportType) {
		m_reportType = reportType;
	}

	public double getReportLength() {
		return m_reportLength;
	}

	public void setReportLength(double reportLength) {
		m_reportLength = reportLength;
	}

}
