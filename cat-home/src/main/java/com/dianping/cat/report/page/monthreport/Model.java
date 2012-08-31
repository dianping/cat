package com.dianping.cat.report.page.monthreport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dianping.cat.consumer.monthreport.model.entity.MonthReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private String m_domain;

	private MonthReport m_report;
	
	private MonthReport m_reportLast;
	
	private MonthReport m_reportLastTwo;

	private List<MonthReport> m_reports;

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
		if (m_report != null) {
			List<String> domains = m_report.getDomains();

			StringSortHelper.sortDomain(domains);
			return domains;
		}

		ArrayList<String> arrayList = new ArrayList<String>();
		
		arrayList.add(getDomain());
		return arrayList;
	}

	public MonthReport getReport() {
		return m_report;
	}

	public List<MonthReport> getReports() {
		return m_reports;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setReport(MonthReport report) {
		m_report = report;
	}

	public void setReports(List<MonthReport> reports) {
		m_reports = reports;
	}

	public MonthReport getReportLast() {
		return m_reportLast;
	}

	public void setReportLast(MonthReport reportLast) {
		m_reportLast = reportLast;
	}

	public MonthReport getReportLastTwo() {
		return m_reportLastTwo;
	}

	public void setReportLastTwo(MonthReport reportLastTwo) {
		m_reportLastTwo = reportLastTwo;
	}
	
}
