package com.dianping.cat.report.task.alert;

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

	public Date getAlertTime() {
		return m_alertTime;
	}

	public String getAlertLevel() {
		return m_alertLevel;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		this.m_content = content;
	}

	public boolean isTriggered() {
		return m_isTriggered;
	}

}
