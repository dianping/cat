package com.dianping.cat.report.page.ip;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.view.UrlNav;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private IpReport m_report;

	private List<DisplayModel> m_displayModels;

	private List<String> m_domains;
	
	private String m_currentDomain;

	private String m_current;

	private String m_reportTitle;
	
	private String m_generateTime;
	
	private String m_urlPrefix;

	private List<UrlNav> m_urlNavs;

	public Model(Context ctx) {
		super(ctx);
		m_urlNavs = new ArrayList<UrlNav>();
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public List<DisplayModel> getDisplayModels() {
		return m_displayModels;
	}

	public List<String> getDomains() {
		return m_domains;
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
   	m_urlNavs = urlNavs;
   }

	public String getCurrentDomain() {
   	return m_currentDomain;
   }

	public void setCurrentDomain(String currentDomain) {
   	m_currentDomain = currentDomain;
   }

	public IpReport getReport() {
		return m_report;
	}

	public String getReportInJson() {
		return String.format(IpReport.JSON, m_report);
	}

	public void setDisplayModels(List<DisplayModel> models) {
		m_displayModels = models;
	}

	public void setDomains(List<String> domains) {
		m_domains = domains;
	}

	public void setReport(IpReport report) {
		m_report = report;
	}

	public String getCurrent() {
		return m_current;
	}

	public void setCurrent(String current) {
		m_current = current;
	}

	public String getReportTitle() {
		return m_reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		m_reportTitle = reportTitle;
	}

	public String getGenerateTime() {
   	return m_generateTime;
   }

	public void setGenerateTime(String generateTime) {
   	m_generateTime = generateTime;
   }

}
