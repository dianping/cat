package com.dianping.cat.report.page.top;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;
	
	@FieldMeta("count")
	private int m_count;

	public Payload() {
		super(ReportPage.TOP);
	}

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
		m_page = ReportPage.getByName(page, ReportPage.TOP);
	}

	public int getCount() {
		return m_count;
	}

	public void setCount(int count) {
		m_count = count;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
