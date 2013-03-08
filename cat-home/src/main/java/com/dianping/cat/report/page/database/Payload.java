package com.dianping.cat.report.page.database;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("sort")
	private String sortBy;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("database")
	private String m_database;

	public Payload() {
		super(ReportPage.DATABASE);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getDatabase() {
		return m_database;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_REPORT);
	}

	public void setDatabase(String database) {
		m_database = database;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.DATABASE);
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.getByName("hourly", Action.HOURLY_REPORT);
		}
	}
}
