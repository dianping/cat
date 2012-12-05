package com.dianping.cat.report.page.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private String m_queryName;

	private CacheReport m_report;

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

}
