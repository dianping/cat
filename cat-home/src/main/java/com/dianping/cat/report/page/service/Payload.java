package com.dianping.cat.report.page.service;

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

	@FieldMeta("index")
	private String m_index;
	
	@FieldMeta("model")
	private String m_model;

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}
	
	
	public String getDomain() {
   	return m_domain;
   }

	public void setDomain(String domain) {
   	m_domain = domain;
   }

	public String getIp() {
   	return m_ip;
   }

	public void setIp(String ip) {
   	m_ip = ip;
   }

	public String getIndex() {
   	return m_index;
   }

	public void setIndex(String index) {
   	m_index = index;
   }

	public String getModel() {
   	return m_model;
   }

	public void setModel(String model) {
   	m_model = model;
   }

	public void setPage(ReportPage page) {
   	m_page = page;
   }

	public void setAction(Action action) {
   	m_action = action;
   }

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.SERVICE);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
