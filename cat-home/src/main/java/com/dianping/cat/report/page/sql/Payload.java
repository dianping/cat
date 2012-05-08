package com.dianping.cat.report.page.sql;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("id")
	private int id;

	@FieldMeta("sort")
	private String m_sortBy;

	public Payload() {
		super(ReportPage.SQL);
	}

	public String getSortBy() {
		return m_sortBy;
	}

	public void setSortBy(String sortBy) {
		m_sortBy = sortBy;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public Action getAction() {
		return m_action == null ? Action.VIEW : m_action;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
