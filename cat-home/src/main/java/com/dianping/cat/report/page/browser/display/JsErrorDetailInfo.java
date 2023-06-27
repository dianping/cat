package com.dianping.cat.report.page.browser.display;

import java.util.Date;

public class JsErrorDetailInfo {

	private String m_detail;

	private Date m_errorTime;

	private String m_level;

	private String m_module;

	private String m_agent;

	private String m_dpid;

	public String getAgent() {
		return m_agent;
	}

	public String getDetail() {
		return m_detail;
	}

	public String getDpid() {
		return m_dpid;
	}

	public Date getErrorTime() {
		return m_errorTime;
	}

	public String getLevel() {
		return m_level;
	}

	public String getModule() {
		return m_module;
	}

	public void setAgent(String agent) {
		m_agent = agent;
	}

	public void setDetail(String detail) {
		m_detail = detail;
	}

	public void setDpid(String dpid) {
		m_dpid = dpid;
	}

	public void setErrorTime(Date errorTime) {
		m_errorTime = errorTime;
	}

	public void setLevel(String level) {
		m_level = level;
	}

	public void setModule(String module) {
		m_module = module;
	}

}
