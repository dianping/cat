package com.dianping.cat.report.page.ip;

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

	@FieldMeta("ip")
	private String m_ip;

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

	public String getIp() {
   	return m_ip;
   }

	public void setIp(String ip) {
   	m_ip = ip;
   }
}
