package com.dianping.cat.report.page.statistics.service;

import java.util.Date;

import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.home.system.entity.SystemReport;
import com.dianping.cat.home.system.transform.DefaultNativeParser;
import com.dianping.cat.report.service.AbstractReportService;

public class SystemReportService extends AbstractReportService<SystemReport> {

	@Override
	public SystemReport makeReport(String domain, Date start, Date end) {
		throw new RuntimeException("SystemReportService do not suppot makeReport feature");
	}

	@Override
	public SystemReport queryDailyReport(String domain, Date start, Date end) {
		try {
			DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, Constants.REPORT_SYSTEM,
			      new Date(start.getTime()), DailyReportEntity.READSET_FULL);
			return queryFromDailyBinary(report.getId());
		} catch (DalException e) {
			return new SystemReport();
		}
	}

	private SystemReport queryFromDailyBinary(int id) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new SystemReport();
		}
	}

	@Override
	public SystemReport queryHourlyReport(String domain, Date start, Date end) {
		throw new RuntimeException("HighloadReportService do not suppot queryHourlyReport feature");
	}

	@Override
	public SystemReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("HighloadReportService do not suppot queryMonthlyReport feature");
	}

	@Override
	public SystemReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("HighloadReportService do not suppot queryWeeklyReport feature");
	}

}
