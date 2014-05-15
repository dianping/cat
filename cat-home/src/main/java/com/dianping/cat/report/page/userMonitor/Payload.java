package com.dianping.cat.report.page.userMonitor;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("url")
	private String m_url;

	@FieldMeta("city")
	private String m_city;

	@FieldMeta("channel")
	private String m_channel;
	
	@FieldMeta("type")
	private String m_type;

	public Payload() {
		super(ReportPage.USERMONITOR);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.USERMONITOR);
	}
	
	public String getUrl() {
   	return m_url;
   }

	public void setUrl(String url) {
   	m_url = url;
   }

	public String getCity() {
   	return m_city;
   }

	public void setCity(String city) {
   	m_city = city;
   }

	public String getChannel() {
   	return m_channel;
   }

	public void setChannel(String channel) {
   	m_channel = channel;
   }

	public String getType() {
   	return m_type;
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
