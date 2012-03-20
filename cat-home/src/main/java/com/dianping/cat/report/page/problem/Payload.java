package com.dianping.cat.report.page.problem;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("ip")
	private String m_ipAddress;
	
	@FieldMeta("thread")
	private String m_threadId;

	public Payload() {
		super(ReportPage.PROBLEM);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public String getThreadId() {
   	return m_threadId;
   }

	public void setAction(Action action) {
		m_action = action;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public void setThreadId(String threadId) {
   	m_threadId = threadId;
   }
	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
