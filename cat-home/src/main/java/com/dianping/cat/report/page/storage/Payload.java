package com.dianping.cat.report.page.storage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("operations")
	private String m_operations;

	@FieldMeta("project")
	private String m_project;

	@FieldMeta("sort")
	private String m_sort = "domain";

	public Payload() {
		super(ReportPage.STORAGE);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getOperations() {
		return m_operations;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getProject() {
		return m_project;
	}

	public String getSort() {
		return m_sort;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_DATABASE);
	}

	public void setOperations(String operations) {
		m_operations = operations;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.STORAGE);
	}

	public void setProject(String project) {
		m_project = project;
	}

	public void setSort(String sort) {
		m_sort = sort;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY_DATABASE;
		}
	}
}
