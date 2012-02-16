package com.dianping.cat.report.page.service;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private String m_xmlData;
	
	private String m_domains;
	
	private String m_ips;
	
	public String getXmlData() {
   	return m_xmlData;
   }

	public void setXmlData(String xmlData) {
   	m_xmlData = xmlData;
   }

	public Model(Context ctx) {
		super(ctx);
	}

	public String getDomains() {
   	return m_domains;
   }

	public void setDomains(String domains) {
   	m_domains = domains;
   }

	public String getIps() {
   	return m_ips;
   }

	public void setIps(String ips) {
   	m_ips = ips;
   }
	
	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
