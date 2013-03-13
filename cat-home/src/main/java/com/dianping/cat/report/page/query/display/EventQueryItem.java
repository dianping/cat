package com.dianping.cat.report.page.query.display;

import java.util.Date;

public class EventQueryItem {
	private Date m_date;

	private String m_type;

	private String m_name;

	private long m_totalCount;

	private long m_failCount;

	private double m_failPercent;

	public Date getDate() {
		return m_date;
	}

	public long getFailCount() {
		return m_failCount;
	}

	public double getFailPercent() {
		return m_failPercent;
	}

	public String getName() {
		return m_name;
	}

	public long getTotalCount() {
		return m_totalCount;
	}

	public String getType() {
		return m_type;
	}

	public EventQueryItem setDate(Date date) {
		m_date = date;
		return this;
	}

	public EventQueryItem setFailCount(long failCount) {
		m_failCount = failCount;
		return this;
	}

	public EventQueryItem setFailPercent(double failPercent) {
		m_failPercent = failPercent;
		return this;
	}


	public EventQueryItem setName(String name) {
		m_name = name;
		return this;
	}

	public EventQueryItem setTotalCount(long totalCount) {
		m_totalCount = totalCount;
		return this;
	}

	public EventQueryItem setType(String type) {
		m_type = type;
		return this;
	}
}
