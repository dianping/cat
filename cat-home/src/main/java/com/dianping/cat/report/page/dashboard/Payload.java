package com.dianping.cat.report.page.dashboard;

import org.apache.commons.lang.StringUtils;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;

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

	public String getDomain() {
		return m_domain;
	}

	public String getIp() {
		if (StringUtils.isEmpty(m_ip)) {
			return ALL;
		}
		return m_ip;
	}

	public String getName() {
		return m_name;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getReport() {
		return m_report;
	}

	public String getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.DASHBOARD);
	}

	public void setReport(String report) {
		m_report = report;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
