package com.dianping.cat.system.alarm.entity;

import java.util.Date;

public class RowData {
	private Date m_date;

	private long m_count;

	public Date getDate() {
		return m_date;
	}

	public void setDate(Date date) {
		m_date = date;
	}

	public long getCount() {
		return m_count;
	}

	public void setCount(long count) {
		m_count = count;
	}

}
