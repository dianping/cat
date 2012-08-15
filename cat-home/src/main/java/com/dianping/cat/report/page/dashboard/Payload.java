package com.dianping.cat.report.page.dashboard;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("report")
	private String m_report;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("ip")
	private String m_ip;

	private ReportPage m_page;

	public static final String ALL = "All";

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.DASHBOARD);
	}

	public String getReport() {
		return m_report;
	}

	public void setReport(String report) {
		m_report = report;
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}
	
	public String getIp() {
		if (StringUtils.isEmpty(m_ip)) {
			return ALL;
		}
		return m_ip;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
