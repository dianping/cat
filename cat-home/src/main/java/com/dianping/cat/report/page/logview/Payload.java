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

	@FieldMeta("header")
	private boolean m_showHeader = true;

	@FieldMeta("id")
	private int m_identifier;

	@PathMeta("path")
	private String[] m_path;

	@Override
	public Action getAction() {
		return m_action;
	}

	public int getIdentifier() {
		return m_identifier;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String[] getPath() {
		return m_path;
	}

	public boolean isShowHeader() {
		return m_showHeader;
	}

	public void setAction(Action action) {
		m_action = action;
	}

	public void setIdentifier(int identifier) {
		m_identifier = identifier;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.LOGVIEW);
	}

	public void setPath(String[] path) {
		m_path = path;
	}

	public void setShowHeader(String showHeader) {
		m_showHeader = !"no".equals(showHeader);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
