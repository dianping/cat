package com.dianping.cat.report.page.ip;

import java.util.List;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private IpReport m_report;

	private List<DisplayModel> m_displayModels;

	private List<String> m_domains;
	
	private String m_currentDomain;

	private String m_current;

	private String m_reportTitle;

	public Model(Context ctx) {
		super(ctx);
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

}
