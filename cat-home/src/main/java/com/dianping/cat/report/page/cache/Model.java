package com.dianping.cat.report.page.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

@ModelMeta("cache")
public class Model extends AbstractReportModel<Action, Context> {
	private String m_queryName;

	@EntityMeta
	private CacheReport m_report;

	private String m_pieChart;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.HISTORY_REPORT;
	}

	@Override
	public String getDomain() {
		return m_report.getDomain();
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

	public Collection<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getIps());
		}
	}

	public String getQueryName() {
		return m_queryName;
	}

	public CacheReport getReport() {
		return m_report;
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
	}

	public void setReport(CacheReport report) {
		m_report = report;
	}

	public String getPieChart() {
		return m_pieChart;
	}

	public void setPieChart(String pieChart) {
		m_pieChart = pieChart;
	}

}
