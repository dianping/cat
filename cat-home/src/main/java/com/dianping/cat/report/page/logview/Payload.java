package com.dianping.cat.report.page.logview;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;
import com.site.web.mvc.payload.annotation.PathMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("id")
	private int m_identifier;

	@PathMeta("path")
	private String[] m_path;

	public void setAction(Action action) {
		m_action = action;
	}

	public int getIdentifier() {
		return m_identifier;
	}

	public void setIdentifier(int identifier) {
		m_identifier = identifier;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String[] getPath() {
		return m_path;
	}

	public void setPath(String[] path) {
		m_path = path;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.LOGVIEW);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
