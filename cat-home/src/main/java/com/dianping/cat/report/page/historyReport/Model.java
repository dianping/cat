package com.dianping.cat.report.page.historyReport;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
	
	public String m_domain;
	public List<String> m_domains;
	public String getDomain() {
   	return m_domain;
   }

	public void setDomain(String domain) {
   	m_domain = domain;
   }

	public List<String> getDomains() {
		List<String> result = new ArrayList<String>();
		result.add("MobileApi");
		result.add("Cat");
   	return result;
   }

	public void setDomains(List<String> domains) {
   	m_domains = domains;
   }
	public String getDate(){
		return "";
	}
	
}
