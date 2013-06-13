package com.dianping.cat.report.page.query.display;

import java.util.Date;

public class ProblemQueryItem {
	private Date m_date;

	private String m_type;

	private String m_name;

	private long m_totalCount;

	public void addCount(long count) {
		m_totalCount += count;
	}

	public Date getDate() {
		return m_date;
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

	public ProblemQueryItem setDate(Date date) {
		m_date = date;
		return this;
	}

	public ProblemQueryItem setName(String name) {
		m_name = name;
		return this;
	}

	public ProblemQueryItem setTotalCount(long totalCount) {
		m_totalCount = totalCount;
		return this;
	}

	public ProblemQueryItem setType(String type) {
		m_type = type;
		return this;
	}
}
