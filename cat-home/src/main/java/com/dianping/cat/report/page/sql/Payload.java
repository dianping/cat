package com.dianping.cat.report.page.sql;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("sort")
	private String m_sortBy;

	@FieldMeta("database")
	private String m_database;

	public Payload() {
		super(ReportPage.SQL);
	}

	@Override
	public Action getAction() {
		return m_action == null ? Action.HOURLY_REPORT : m_action;
	}

	public String getDatabase() {
		return m_database;
	}

	public String getSortBy() {
		return m_sortBy;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_REPORT);
	}

	public void setDatabase(String database) {
		m_database = database;
	}

	public void setSortBy(String sortBy) {
		m_sortBy = sortBy;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY_REPORT;
		}
	}
}
