package com.dianping.cat.system.alarm.threshold;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ThresholdDataEntity {

	private long m_count;

	private Date m_date;

	private String m_domain;

	public long getCount() {
		return m_count;
	}

	public Date getDate() {
		return m_date;
	}

	public String getDomain() {
		return m_domain;
	}

	public void setCount(long count) {
		m_count = count;
	}

	public void setDate(Date date) {
		m_date = date;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		StringBuilder sb = new StringBuilder(100);

		sb.append("[").append("Date:").append(sdf.format(m_date)).append(";");
		sb.append("Count:").append(m_count).append(";");
		sb.append("Domain:").append(m_domain).append("]");

		return sb.toString();
	}

}
