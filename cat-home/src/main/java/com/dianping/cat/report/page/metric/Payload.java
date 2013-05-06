package com.dianping.cat.report.page.metric;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("op")
	private String m_group;

	@FieldMeta("channel")
	private String m_channel;

	public Payload() {
		super(ReportPage.METRIC);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getChannel() {
		return m_channel;
	}

	public String getGroup() {
		return m_group;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setChannel(String channel) {
		m_channel = channel;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.METRIC);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
