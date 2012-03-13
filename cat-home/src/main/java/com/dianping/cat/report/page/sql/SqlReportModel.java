package com.dianping.cat.report.page.sql;

import java.text.DecimalFormat;

import com.dianping.cat.job.sql.dal.SqlReportRecord;

public class SqlReportModel {
	private SqlReportRecord m_record;

	private DecimalFormat percent = new DecimalFormat("#.##%");

	private DecimalFormat number = new DecimalFormat("#.##");

	public SqlReportModel(SqlReportRecord record) {
		m_record = record;
	}

	public String getFailurePercent() {
		double value = m_record.getFailures() / m_record.getTotalcount();
		return percent.format(value);
	}

	public String getLongPercent() {
		double value = m_record.getLongsqls() / m_record.getTotalcount();
		return percent.format(value);
	}

	public String getAvg() {
		double value = m_record.getSumvalue() / m_record.getTotalcount();
		return number.format(value);
	}

	public String getStd() {
		double sum2 = m_record.getSum2value();
		int count = m_record.getTotalcount();
		double avg = m_record.getSumvalue() / m_record.getTotalcount();
		double std = Math.sqrt(sum2 / count - avg * avg);
		return number.format(std);
	}

	public SqlReportRecord getRecord() {
		return m_record;
	}

	public void setRecord(SqlReportRecord record) {
		m_record = record;
	}

}
