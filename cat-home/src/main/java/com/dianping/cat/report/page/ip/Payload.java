package com.dianping.cat.report.page.ip;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("domain")
	private String m_domain;

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getDomain() {
		return m_domain;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public void setAction(Action action) {
		m_action = action;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.IP);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
