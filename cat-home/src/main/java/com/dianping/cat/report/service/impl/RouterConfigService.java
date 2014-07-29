package com.dianping.cat.report.service.impl;

import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.home.dal.report.DailyReportContent;
import com.dianping.cat.home.dal.report.DailyReportContentEntity;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.transform.DefaultNativeParser;
import com.dianping.cat.report.service.AbstractReportService;

public class RouterConfigService extends AbstractReportService<RouterConfig> {

	@Override
	public RouterConfig makeReport(String domain, Date start, Date end) {
		RouterConfig report = new RouterConfig(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public RouterConfig queryDailyReport(String domain, Date start, Date end) {
		String name = Constants.REPORT_ROUTER;

		try {
			DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, start,
			      DailyReportEntity.READSET_FULL);
			RouterConfig config = queryFromDailyBinary(report.getId(), domain);

			return config;
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	private RouterConfig queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return null;
		}
	}

	@Override
	public RouterConfig queryHourlyReport(String domain, Date start, Date end) {
		throw new RuntimeException("router report don't support hourly report");
	}

	@Override
	public RouterConfig queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("router report don't support monthly report");
	}

	@Override
	public RouterConfig queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("router report don't support weekly report");
	}

}
