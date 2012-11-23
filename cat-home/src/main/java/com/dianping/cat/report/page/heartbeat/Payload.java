package com.dianping.cat.report.page.heartbeat;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("ip")
	private String m_ipAddress;

	public Payload() {
		super(ReportPage.HEARTBEAT);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setIpAddress(String ip) {
		m_ipAddress = ip;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
