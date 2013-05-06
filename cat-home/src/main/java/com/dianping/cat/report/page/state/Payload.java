package com.dianping.cat.report.page.state;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;
	
	@FieldMeta("key")
	private String m_key;

	public Payload() {
		super(ReportPage.STATE);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getKey() {
		return m_key;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY);
	}
	
	public void setKey(String key) {
		m_key = key;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.STATE);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY;
		}
	}
}
