package com.dianping.log;

import java.util.Date;
import java.util.Map;

public class LogItem {

	private Date m_date;

	private String m_uid;

	private Map<String, String> m_tags;

	private String m_detail;

	public Date getDate() {
		return m_date;
	}

	public String getDetail() {
		return m_detail;
	}

	public Map<String, String> getTags() {
		return m_tags;
	}

	public String getUid() {
		return m_uid;
	}

	public LogItem setDate(Date date) {
		m_date = date;
		return this;
	}

	public LogItem setDetail(String detail) {
		m_detail = detail;
		return this;
	}

	public LogItem setTags(Map<String, String> tags) {
		m_tags = tags;
		return this;
	}

	public LogItem setUid(String uid) {
		m_uid = uid;
		return this;
	}

}
