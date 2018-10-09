package com.dianping.cat.report.task.reload;

import com.dianping.cat.core.dal.HourlyReport;

public class ReportReloadEntity {

	private HourlyReport m_report;

	private byte[] m_reportContent;

	public ReportReloadEntity(HourlyReport report, byte[] reportContent) {
		m_report = report;
		m_reportContent = reportContent;
	}

	public HourlyReport getReport() {
		return m_report;
	}

	public void setReport(HourlyReport report) {
		m_report = report;
	}

	public byte[] getReportContent() {
		return m_reportContent;
	}

	public void setReportContent(byte[] reportContent) {
		m_reportContent = reportContent;
	}

}
