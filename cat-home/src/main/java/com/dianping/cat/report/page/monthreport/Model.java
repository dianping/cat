package com.dianping.cat.report.page.monthreport;

import java.util.Collection;
import java.util.Set;

import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	private ProjectReport m_report;

	private String m_domain;

	private Set<String> m_domains;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	public Collection<String> getDomains() {
		return m_domains;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setDomains(Set<String> domains) {
		m_domains = domains;
	}

	public ProjectReport getReport() {
   	return m_report;
   }

	public void setReport(ProjectReport report) {
   	m_report = report;
   }
}
