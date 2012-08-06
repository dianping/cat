package com.dianping.cat.report.page.monthreport;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("domain")
	private String m_domain;

	private ReportPage m_page;

	public Payload() {
		super(ReportPage.MONTHREPORT);
	}

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

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.MONTHREPORT);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
