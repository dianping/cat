package com.dianping.cat.report.page.transaction;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("type")
	private String m_type;
	
	@FieldMeta("current")
	private String m_current;
	
	@FieldMeta("method")
	private int m_method;

	@FieldMeta("op")
	private Action m_action;

	public void setAction(Action action) {
		m_action = action;
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
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
   	m_method = method;
   }

	public void setPage(ReportPage page) {
   	m_page = page;
   }

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TRANSACTION);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
