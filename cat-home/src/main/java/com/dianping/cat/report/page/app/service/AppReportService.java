package com.dianping.cat.report.page.app.service;

import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.home.app.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.report.service.AbstractReportService;

public class AppReportService extends AbstractReportService<AppReport> {

	@Override
	public AppReport makeReport(String id, Date start, Date end) {
		AppReport report = new AppReport(id);

		report.setStartTime(start).setEndTime(end);
		return report;
	}

	@Override
	public AppReport queryDailyReport(String id, Date start, Date end) {
		AppReport reportModel = new AppReport(id);

		try {
			DailyReport report = m_dailyReportDao.findByDomainNamePeriod(id, Constants.APP, start,
			      DailyReportEntity.READSET_FULL);
			reportModel = queryFromDailyBinary(report.getId(), id);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			Cat.logError(e);
		}

		reportModel.setStartTime(start).setEndTime(end);
		return reportModel;
	}

	private AppReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new AppReport(domain);
		}
	}

	@Override
	public AppReport queryHourlyReport(String id, Date start, Date end) {
		throw new RuntimeException("Top report don't support hourly report");
	}

	@Override
	public AppReport queryMonthlyReport(String id, Date start) {
		throw new RuntimeException("Top report don't support monthly report");
	}

	@Override
	public AppReport queryWeeklyReport(String id, Date start) {
		throw new RuntimeException("Top report don't support weekly report");
	}

}
