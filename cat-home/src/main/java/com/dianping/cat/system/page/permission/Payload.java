package com.dianping.cat.system.page.permission;

import com.dianping.cat.system.SystemPage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("content")
	private String m_content;

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.USER);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.PERMISSION);
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		m_content = content;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.USER;
		}
	}

	public String getReportType() {
		return "";
	}

}
