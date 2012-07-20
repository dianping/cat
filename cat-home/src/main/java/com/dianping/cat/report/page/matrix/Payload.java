package com.dianping.cat.report.page.matrix;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("sort")
	private String sortBy;
	
	public Payload() {
		super(ReportPage.MATRIX);
	}

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_REPORT);
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
		m_page = ReportPage.getByName(page, ReportPage.MATRIX);
	}
	

	public String getSortBy() {
   	return sortBy;
   }

	public void setSortBy(String sortBy) {
   	this.sortBy = sortBy;
   }

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY_REPORT;
		}
	}
}
