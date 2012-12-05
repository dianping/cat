package com.dianping.cat.report.page.home;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	public Payload() {
		super(ReportPage.HOME);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
