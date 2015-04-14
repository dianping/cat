package com.dianping.cat.report.page.heartbeat;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("extensionType")
	private String m_extensionType;

	private String m_realIp;

	public Payload() {
		super(ReportPage.HEARTBEAT);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getExtensionType() {
		return m_extensionType;
	}

	public String getRealIp() {
		return m_realIp;
	}

	public String getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setExtensionType(String extensionType) {
		m_extensionType = extensionType;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.HEARTBEAT);
	}

	public void setRealIp(String realIp) {
		m_realIp = realIp;
	}

	public void setType(String type) {
		m_type = type;
	}
	
	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
