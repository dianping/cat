package com.dianping.cat.report.page.cache;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {
	@FieldMeta("op")
	private Action m_action;

	private ReportPage m_page;

	@FieldMeta("queryname")
	private String m_queryName;

	@FieldMeta("sort")
	private String m_sortBy;

	@FieldMeta("type")
	private String m_type;

	public Payload() {
		super(ReportPage.CACHE);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getQueryName() {
		return m_queryName;
	}

	public String getSortBy() {
		return m_sortBy;
	}

	public String getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_REPORT);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.CACHE);
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
	}

	public void setSortBy(String sortBy) {
		m_sortBy = sortBy;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY_REPORT;
		}
	}
}
