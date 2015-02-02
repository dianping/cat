package com.dianping.cat.system.page.plugin;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.PathMeta;

import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@PathMeta("parts")
	private String[] m_parts;

	@FieldMeta("source")
	private boolean m_downloadSource;

	@FieldMeta("mapping")
	private boolean m_downloadMapping;

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public String getType() {
		if (m_parts != null && m_parts.length > 0) {
			return m_parts[0];
		} else {
			return null;
		}
	}

	public boolean isDownloadMapping() {
		return m_downloadMapping;
	}

	public boolean isDownloadSource() {
		return m_downloadSource;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.PLUGIN);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
