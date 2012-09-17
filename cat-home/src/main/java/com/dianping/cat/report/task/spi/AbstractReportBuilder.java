package com.dianping.cat.report.task.spi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.hadoop.dal.GraphDao;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public abstract class AbstractReportBuilder {

	@Inject
	protected DailyreportDao m_dailyReportDao;

	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected ReportDao m_reportDao;

	protected void clearDailyReport(Dailyreport report) throws DalException {
		m_dailyReportDao.deleteByDomainNamePeriod(report);
	}

	protected void clearHourlyGraphs(List<Graph> graphs) throws DalException {
		for (Graph graph : graphs) {
			m_graphDao.deleteByDomainNamePeriodIp(graph);
		}
	}

	protected Set<String> getDomains(Date start, Date end) {
		List<Report> domainNames = new ArrayList<Report>();
		Set<String> result = new HashSet<String>();
		try {
			domainNames = m_reportDao
			      .findAllByDomainNameDuration(start, end, null, null, ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			Cat.logError(e);
		}
		if (domainNames == null || domainNames.size() == 0) {
			return result;
		}
		for (Report domainName : domainNames) {
			result.add(domainName.getDomain());
		}
		return result;
	}

	protected Set<String> getDatabases(Date start, Date end) {
		List<Report> databaseNames = new ArrayList<Report>();
		Set<String> result = new HashSet<String>();

		try {
			databaseNames = m_reportDao.findAllByDomainNameDuration(start, end, null, "database",
			      ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			Cat.logError(e);
		}
		if (databaseNames == null || databaseNames.size() == 0) {
			return result;
		}
		for (Report domainName : databaseNames) {
			result.add(domainName.getDomain());
		}
		return result;
	}
}
