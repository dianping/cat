package com.dianping.cat.report.page.crash.service;

import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.AppDailyReport;
import com.dianping.cat.app.AppDailyReportContent;
import com.dianping.cat.app.AppDailyReportContentEntity;
import com.dianping.cat.app.AppDailyReportEntity;
import com.dianping.cat.home.crash.entity.CrashReport;
import com.dianping.cat.home.crash.transform.DefaultNativeParser;
import com.dianping.cat.report.app.AbstractAppReportService;

@Named
public class CrashStatisticReportService extends AbstractAppReportService<CrashReport> {

	@Override
	public CrashReport makeReport(String domain, Date start, Date end) {
		CrashReport report = new CrashReport(domain);

		report.setStartTime(start).setEndTime(end);
		return report;
	}

	@Override
	public CrashReport queryDailyReport(int appId, Date start, Date end) {
		CrashReport reportModel = new CrashReport(String.valueOf(appId));

		try {
			AppDailyReport report = m_dailyReportDao.findByAppNamePeriod(appId, Constants.CRASH, start,
			      AppDailyReportEntity.READSET_FULL);
			reportModel = queryFromDailyBinary(report.getId(), String.valueOf(appId));
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			Cat.logError(e);
		}

		reportModel.setStartTime(start).setEndTime(end);
		return reportModel;
	}

	private CrashReport queryFromDailyBinary(int id, String domain) throws DalException {
		AppDailyReportContent content = m_dailyReportContentDao.findByPK(id, AppDailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new CrashReport(domain);
		}
	}
}
