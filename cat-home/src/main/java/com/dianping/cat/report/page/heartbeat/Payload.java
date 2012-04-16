package com.dianping.cat.report.page.heartbeat;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
	public Payload() {
		super(ReportPage.IP);
	}

	@FieldMeta("op")
	private Action m_action;

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(Action action) {
		m_action = action;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
