package com.dianping.cat.report.page.transaction;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.view.UrlNav;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private String m_current;

	private String m_currentDomain;

	private List<String> m_domains;
	
	private String m_jsonResult;
	
	private String m_reportTitle;
	
	private String m_generateTime;
	
	private String m_type;
	
	private String m_urlPrefix;

	private List<UrlNav> m_urlNavs;

	public Model(Context ctx) {
		super(ctx);
		m_urlNavs = new ArrayList<UrlNav>();
		for(UrlNav temp: UrlNav.values()){
			m_urlNavs.add(temp);
		}
	}

	public String getCurrent() {
   	return m_current;
   }

	public String getUrlPrefix() {
   	return m_urlPrefix;
   }

	public void setUrlPrefix(String urlPrefix) {
   	m_urlPrefix = urlPrefix;
   }

	public List<UrlNav> getUrlNavs() {
   	return m_urlNavs;
   }

	public void setUrlNavs(List<UrlNav> urlNavs) {
   	this.m_urlNavs = urlNavs;
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

	public String getGenerateTime() {
   	return m_generateTime;
   }

	public void setGenerateTime(String generateTime) {
   	m_generateTime = generateTime;
   }
	
}
