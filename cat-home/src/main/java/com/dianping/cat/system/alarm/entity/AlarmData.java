package com.dianping.cat.system.alarm.entity;

import java.util.Date;

public class AlarmData {

	private String m_type;

	private Date m_date;

	private long m_count;

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

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
