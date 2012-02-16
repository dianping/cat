package com.dianping.cat.report.page.transaction;

import java.util.List;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private String m_current;

	private String m_currentDomain;

	private List<String> m_domains;
	
	private String m_jsonResult;
	
	private String m_reportTitle;
	
	private String m_type;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getCurrent() {
   	return m_current;
   }

	
	public String getCurrentDomain() {
   	return m_currentDomain;
   }

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public List<String> getDomains() {
		return m_domains;
	}

	public String getJsonResult() {
   	return m_jsonResult;
   }

	public String getReportTitle() {
   	return m_reportTitle;
   }

	public String getType() {
		return m_type;
	}

	public void setCurrent(String current) {
   	m_current = current;
   }

	public void setCurrentDomain(String currentDomain) {
   	m_currentDomain = currentDomain;
   }

	public void setDomains(List<String> domains) {
		this.m_domains = domains;
	}

	public void setJsonResult(String jsonResult) {
   	m_jsonResult = jsonResult;
   }

	public void setReportTitle(String reportTitle) {
   	m_reportTitle = reportTitle;
   }

	public void setType(String type) {
		this.m_type = type;
	}
	
}
