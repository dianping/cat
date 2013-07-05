package com.dianping.cat.report.task.spi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.WeeklyReportDao;

public abstract class AbstractReportBuilder {

	@Inject
	protected DailyReportDao m_dailyReportDao;
	
	@Inject
	protected WeeklyReportDao m_weeklyReportDao;
	
	@Inject
	protected MonthlyReportDao m_monthlyReportDao;

	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected HourlyReportDao m_reportDao;

	@Inject
	protected DailyGraphDao m_dailyGraphDao;

	protected Set<String> getDomainsFromHourlyReport(Date start, Date end) {
		List<HourlyReport> domainNames = new ArrayList<HourlyReport>();
		Set<String> result = new HashSet<String>();
		
		try {
			domainNames = m_reportDao
			      .findAllByDomainNameDuration(start, end, null, null, HourlyReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			Cat.logError(e);
		}
		if (domainNames != null) {
			for (HourlyReport domainName : domainNames) {
				result.add(domainName.getDomain());
			}
		}
		return result;
	}
}
