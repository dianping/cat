package com.dianping.cat.report.page.health;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private HealthReport m_report;

	private HealthReport m_lastReport;

	private HealthReport m_lastTwoReport;

	private String m_historyGraph;
	
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_REPORT;
	}

	@Override
	public String getDomain() {
		if (m_report != null) {
			return m_report.getDomain();
		}
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		if (m_report == null) {
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDomain());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDomainNames();

			return StringSortHelper.sortDomain(domainNames);
		}
	}

	public String getHistoryGraph() {
		return m_historyGraph;
	}

	public HealthReport getLastReport() {
		return m_lastReport;
	}

	public HealthReport getLastTwoReport() {
		return m_lastTwoReport;
	}

	public HealthReport getReport() {
		return m_report;
	}

	public void setHistoryGraph(String historyGraph) {
		m_historyGraph = historyGraph;
	}

	public void setLastReport(HealthReport lastReport) {
		m_lastReport = lastReport;
	}

	public void setLastTwoReport(HealthReport lastTwoReport) {
		m_lastTwoReport = lastTwoReport;
	}

	public void setReport(HealthReport report) {
		m_report = report;
	}

}
