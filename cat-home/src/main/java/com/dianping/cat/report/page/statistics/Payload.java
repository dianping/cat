package com.dianping.cat.report.page.statistics;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("sort")
	private String m_sortBy = "avg";

	@FieldMeta("tab")
	private String m_tab = "tab1";

	public String getTab() {
		return m_tab;
	}

	public void setTab(String tab) {
		m_tab = tab;
	}

	public Payload() {
		super(ReportPage.STATISTICS);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getSortBy() {
		return m_sortBy;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.BUG_REPORT);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.STATISTICS);
	}

	public void setSortBy(String sortBy) {
		m_sortBy = sortBy;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.BUG_REPORT;
		}
	}
}
