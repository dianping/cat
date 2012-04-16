package com.dianping.cat.report.heartbeat.heartbeat.page.heartbeat;

import com.dianping.cat.report.heartbeat.heartbeat.HeartbeatPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<HeartbeatPage, Action> {
	private HeartbeatPage m_page;

	@FieldMeta("op")
	private Action m_action;

	public void setAction(Action action) {
		m_action = action;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public HeartbeatPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = HeartbeatPage.getByName(page, HeartbeatPage.HEARTBEAT);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
