package com.dianping.cat.report.service.impl;

import java.util.Date;

import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.home.dal.report.DailyReportContent;
import com.dianping.cat.home.dal.report.DailyReportContentEntity;
import com.dianping.cat.home.highload.entity.HighloadReport;
import com.dianping.cat.home.highload.transform.DefaultNativeParser;
import com.dianping.cat.report.service.AbstractReportService;

public class HighloadReportService extends AbstractReportService<HighloadReport> {

	@Override
	public HighloadReport makeReport(String domain, Date start, Date end) {
		throw new RuntimeException("HighloadReportService do not suppot makeReport feature");
	}

	@Override
	public HighloadReport queryDailyReport(String domain, Date start, Date end) {
		try {
			DailyReport report = m_dailyReportDao.findByDomainNamePeriod("", Constants.HIGH_LOAD_REPORT,
			      new Date(start.getTime()), DailyReportEntity.READSET_FULL);
			return queryFromDailyBinary(report.getId());
		} catch (DalException e) {
			return new HighloadReport();
		}

	}

	private HighloadReport queryFromDailyBinary(int id) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new HighloadReport();
		}
	}

	@Override
	public HighloadReport queryHourlyReport(String domain, Date start, Date end) {
		throw new RuntimeException("HighloadReportService do not suppot queryHourlyReport feature");
	}

	@Override
	public HighloadReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("HighloadReportService do not suppot queryMonthlyReport feature");
	}

	@Override
	public HighloadReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("HighloadReportService do not suppot queryWeeklyReport feature");
	}

}
