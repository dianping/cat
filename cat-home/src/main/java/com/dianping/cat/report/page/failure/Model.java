package com.dianping.cat.report.page.failure;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.view.UrlNav;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private String m_current;

	private String m_currentDomain;

	private String m_currentIp;

	private List<String> m_domains;

	private List<String> m_ips;

	private String m_jsonResult;

	private String m_reportTitle;
	
	private String m_generateTime;

	private String m_urlPrefix;

	private List<UrlNav> m_urlNavs;

	public Model(Context ctx) {
		super(ctx);
		m_urlNavs = new ArrayList<UrlNav>();
		for(UrlNav temp: UrlNav.values()){
			m_urlNavs.add(temp);
		}
	}

	public String getUrlPrefix() {
		return m_urlPrefix;
	}

	public void setUrlPrefix(String urlPrefix) {
		m_urlPrefix = urlPrefix;
	}

	public String getCurrent() {
		return m_current;
	}

	public String getCurrentDomain() {
		return m_currentDomain;
	}

	public String getCurrentIp() {
		return m_currentIp;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public List<String> getDomains() {
		return m_domains;
	}

	public List<String> getIps() {
		return m_ips;
	}

	public String getJsonResult() {
		return m_jsonResult;
	}

	public String getReportTitle() {
		return m_reportTitle;
	}

	public void setCurrent(String current) {
		this.m_current = current;
	}

	public void setCurrentDomain(String currentDomain) {
		m_currentDomain = currentDomain;
	}

	public void setCurrentIp(String currentIp) {
		m_currentIp = currentIp;
	}

	public void setDomains(List<String> domains) {
		m_domains = domains;
	}

	public void setIps(List<String> ips) {
		m_ips = ips;
	}

	public void setJsonResult(String jsonResult) {
		m_jsonResult = jsonResult;
	}

	public void setReportTitle(String reportTitle) {
		m_reportTitle = reportTitle;
	}

	public String getSimpleCurrentIp() {
		return m_currentIp.replace(".", "");
	}

	public List<UrlNav> getUrlNavs() {
   	return m_urlNavs;
   }

	public void setUrlNavs(List<UrlNav> urlNavs) {
   	this.m_urlNavs = urlNavs;
   }

	public String getGenerateTime() {
   	return m_generateTime;
   }

	public void setGenerateTime(String generateTime) {
   	m_generateTime = generateTime;
   }

}
