package com.dianping.cat.alarm.spi.rule;

import java.util.Date;

public class DataCheckEntity {
	private boolean m_isTriggered;

	private String m_content;

	private String m_alertLevel;

	private Date m_alertTime;

	public DataCheckEntity(boolean result, String content, String alertLevel) {
		m_isTriggered = result;
		m_content = content;
		m_alertLevel = alertLevel;
		m_alertTime = new Date();
	}

	public String getAlertLevel() {
		return m_alertLevel;
	}

	public Date getAlertTime() {
		return m_alertTime;
	}

	public String getContent() {
		return m_content;
	}

	public boolean isTriggered() {
		return m_isTriggered;
	}

	public void setContent(String content) {
		m_content = content;
	}

}
