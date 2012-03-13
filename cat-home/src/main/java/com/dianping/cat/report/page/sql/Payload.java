package com.dianping.cat.report.page.sql;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.dianping.cat.report.page.transaction.Action;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	public Payload() {
		super(ReportPage.SQL);
	}

	public void setAction(Action action) {
		m_action = action;
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
		m_page = ReportPage.getByName(page, ReportPage.SQL);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
