package com.dianping.dog.alarm.page.home;

import com.dianping.dog.alarm.AlarmPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<AlarmPage, Action> {
	private AlarmPage m_page;

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
	public AlarmPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = AlarmPage.getByName(page, AlarmPage.HOME);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
