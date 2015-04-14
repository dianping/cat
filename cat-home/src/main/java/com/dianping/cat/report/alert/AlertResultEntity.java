package com.dianping.cat.report.alert;

import java.util.Date;

public class AlertResultEntity {
	private boolean m_isTriggered;

	private String m_content;

	private String m_alertLevel;

	private Date m_alertTime;

	public AlertResultEntity(boolean result, String content, String alertLevel) {
		this.m_isTriggered = result;
		this.m_content = content;
		this.m_alertLevel = alertLevel;
		this.m_alertTime = new Date();
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
		this.m_content = content;
	}

}
