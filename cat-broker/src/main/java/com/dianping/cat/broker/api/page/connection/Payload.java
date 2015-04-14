package com.dianping.cat.broker.api.page.connection;

import com.dianping.cat.broker.api.ApiPage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ApiPage, Action> {
	private ApiPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("c")
	private String m_content;

	@FieldMeta("v")
	private String m_version;

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getContent() {
		return m_content;
	}

	@Override
	public ApiPage getPage() {
		return m_page;
	}

	public String getVersion() {
		return m_version;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setContent(String content) {
		m_content = content;
	}

	@Override
	public void setPage(String page) {
		m_page = ApiPage.getByName(page, ApiPage.CONNECTION);
	}

	public void setVersion(String version) {
		m_version = version;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
