package com.dianping.cat.report.task.spi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.DailygraphDao;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.Graph;
import com.dianping.cat.home.dal.report.GraphDao;
import com.dianping.cat.home.dal.report.MonthreportDao;
import com.dianping.cat.home.dal.report.WeeklyreportDao;

public abstract class AbstractReportBuilder {

	@Inject
	protected DailyreportDao m_dailyReportDao;
	
	@Inject
	protected WeeklyreportDao m_weeklyreportDao;
	
	@Inject
	protected MonthreportDao m_monthreportDao;

	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected ReportDao m_reportDao;

	@Inject
	protected DailygraphDao m_dailygraphDao;
	
	protected void clearDailyReport(Dailyreport report) throws DalException {
		m_dailyReportDao.deleteByDomainNamePeriod(report);
	}

	protected void clearHourlyGraphs(List<Graph> graphs) throws DalException {
		for (Graph graph : graphs) {
			m_graphDao.deleteByDomainNamePeriodIp(graph);
		}
	}

	protected Set<String> getDomainsFromHourlyReport(Date start, Date end) {
		List<Report> domainNames = new ArrayList<Report>();
		Set<String> result = new HashSet<String>();
		
		try {
			domainNames = m_reportDao
			      .findAllByDomainNameDuration(start, end, null, null, ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			Cat.logError(e);
		}
		if (domainNames != null) {
			for (Report domainName : domainNames) {
				result.add(domainName.getDomain());
			}
		}
		return result;
	}

	protected Set<String> getDatabasesFromHoulyReport(Date start, Date end) {
		List<Report> databaseNames = new ArrayList<Report>();
		Set<String> result = new HashSet<String>();

		try {
			databaseNames = m_reportDao.findDatabaseAllByDomainNameDuration(start, end, null, "database",
			      ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			Cat.logError(e);
		}
		if (databaseNames != null) {
			for (Report domainName : databaseNames) {
				result.add(domainName.getDomain());
			}
		}
		return result;
	}
}
