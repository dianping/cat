package com.dianping.cat.system.alarm.exception;

import java.util.Date;

public class ExceptionDataEntity {

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
