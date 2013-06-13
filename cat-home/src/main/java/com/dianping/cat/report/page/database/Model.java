package com.dianping.cat.report.page.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private DatabaseReport m_report;

	private DisplayDatabase m_displayDatabase;

	private String m_ipAddress;

	private String m_domain;

	private String m_database;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getDatabase() {
		return m_database;
	}

	public Collection<String> getDatabases() {
		if (m_report == null) {
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDatabase());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDatabaseNames();

			return StringSortHelper.sortDomain(domainNames);
		}
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_REPORT;
	}

	public DisplayDatabase getDisplayDatabase() {
		return m_displayDatabase;
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	public List<String> getDomains() {
		if (m_report == null) {
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDomain());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDomainNames();

			return StringSortHelper.sortDomain(domainNames);
		}
	}

	@Override
	public String getIpAddress() {
		return m_ipAddress;
	}

	public List<String> getIps() {
		if (m_report == null) {
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDomain());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDomainNames();

			domainNames.add(getDomain());
			return StringSortHelper.sortDomain(domainNames);
		}
	}

	public DatabaseReport getReport() {
		return m_report;
	}

	public void setDatabase(String database) {
		m_database = database;
	}

	public void setDisplayDatabase(DisplayDatabase displayDatabase) {
		m_displayDatabase = displayDatabase;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	@Override
	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public void setReport(DatabaseReport report) {
		m_report = report;
	}

}
