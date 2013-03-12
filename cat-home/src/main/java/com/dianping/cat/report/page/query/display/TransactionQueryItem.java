package com.dianping.cat.report.page.query.display;

import java.util.Date;

public class TransactionQueryItem {
	private Date m_date;

	private String m_type;

	private String m_name;

	private long m_totalCount;

	private long m_failCount;

	private double m_failPercent;

	private double m_min = 86400000d;

	private double m_max = -1d;

	private double m_avg;

	private double m_tps;

	private double m_line95Value;

	public double getAvg() {
		return m_avg;
	}

	public Date getDate() {
		return m_date;
	}

	public long getFailCount() {
		return m_failCount;
	}

	public double getFailPercent() {
		return m_failPercent;
	}

	public double getLine95Value() {
		return m_line95Value;
	}

	public double getMax() {
		return m_max;
	}

	public double getMin() {
		return m_min;
	}

	public String getName() {
		return m_name;
	}

	public long getTotalCount() {
		return m_totalCount;
	}

	public double getTps() {
		return m_tps;
	}

	public String getType() {
		return m_type;
	}

	public void setAvg(double avg) {
		m_avg = avg;
	}

	public TransactionQueryItem setDate(Date date) {
		m_date = date;
		return this;
	}

	public TransactionQueryItem setFailCount(long failCount) {
		m_failCount = failCount;
		return this;
	}

	public TransactionQueryItem setFailPercent(double failPercent) {
		m_failPercent = failPercent;
		return this;
	}

	public TransactionQueryItem setLine95Value(double line95Value) {
		m_line95Value = line95Value;
		return this;
	}

	public TransactionQueryItem setMax(double max) {
		m_max = max;
		return this;
	}

	public TransactionQueryItem setMin(double min) {
		m_min = min;
		return this;
	}

	public TransactionQueryItem setName(String name) {
		m_name = name;
		return this;
	}

	public TransactionQueryItem setTotalCount(long totalCount) {
		m_totalCount = totalCount;
		return this;
	}

	public TransactionQueryItem setTps(double tps) {
		m_tps = tps;
		return this;
	}

	public TransactionQueryItem setType(String type) {
		m_type = type;
		return this;
	}
}
