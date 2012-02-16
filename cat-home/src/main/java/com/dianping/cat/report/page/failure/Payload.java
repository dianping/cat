package com.dianping.cat.report.page.failure;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("domain")
	private String m_domain;
	
	@FieldMeta("ip")
	private String m_ip;

	@FieldMeta("current")
	private String m_current;

	@FieldMeta("method")
	private int m_method;

	public void setAction(Action action) {
		m_action = action;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.FAILURE);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		this.m_domain = domain;
	}

	public String getCurrent() {
   	return m_current;
   }

	public void setCurrent(String current) {
   	m_current = current;
   }

	public int getMethod() {
		return m_method;
	}

	public void setMethod(int method) {
		this.m_method = method;
	}

	public void setPage(ReportPage page) {
		m_page = page;
	}

	public String getIp() {
   	return m_ip;
   }

	public void setIp(String ip) {
   	m_ip = ip;
   }
}
