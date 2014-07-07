package com.dianping.cat.report.task.alert;

import java.util.Date;

public class AlertResultEntity {
	private boolean m_isTriggered;

	private String m_content;

	private String m_alertType;
	
	private Date m_alertTime;
	
	public AlertResultEntity(){
		this.m_isTriggered = false;
		this.m_content = "";
		this.m_alertType = "";
		this.m_alertTime = new Date();
	}
	
	public AlertResultEntity(boolean result, String content, String alertType){
		this.m_isTriggered = result;
		this.m_content = content;
		this.m_alertType = alertType;
		this.m_alertTime = new Date();
	}

	public Date getAlertTime() {
		return m_alertTime;
	}

	public String getAlertType() {
		return m_alertType;
	}

	public String getContent() {
		return m_content;
	}

	public boolean isTriggered() {
		return m_isTriggered;
	}

}
