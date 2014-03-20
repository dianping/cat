package com.dianping.cat.report.page.alteration;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("title")
	private String m_title;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("ip")
	private String m_ip;

	@FieldMeta("user")
	private String m_user;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("url")
	private String m_url;
	
	@FieldMeta("startTime")
	private String m_startTime;
	
	@FieldMeta("endTime")
	private String m_endTime;
	
	@FieldMeta("granularity")
	private long m_granularity;

	public String getStartTime() {
		return m_startTime;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		this.m_type = type;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		this.m_title = title;
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		this.m_domain = domain;
	}

	public String getIp() {
		return m_ip;
	}

	public void setIp(String ip) {
		this.m_ip = ip;
	}

	public String getUser() {
		return m_user;
	}

	public void setUser(String user) {
		this.m_user = user;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		this.m_content = content;
	}

	public String getUrl() {
		return m_url;
	}

	public void setUrl(String url) {
		this.m_url = url;
	}
	
	public void setStartTime(String m_startTime) {
		this.m_startTime = m_startTime;
	}

	public String getEndTime() {
		return m_endTime;
	}

	public void setEndTime(String m_endTime) {
		this.m_endTime = m_endTime;
	}

	public long getGranularity() {
		return m_granularity;
	}

	public void setGranularity(long m_granularity) {
		this.m_granularity = m_granularity;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.ALTERATION);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
