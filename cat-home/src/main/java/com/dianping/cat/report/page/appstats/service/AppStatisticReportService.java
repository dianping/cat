package com.dianping.cat.report.page.appstats.service;

import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.AppDailyReport;
import com.dianping.cat.app.AppDailyReportContent;
import com.dianping.cat.app.AppDailyReportContentEntity;
import com.dianping.cat.app.AppDailyReportEntity;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.home.app.transform.DefaultNativeParser;
import com.dianping.cat.report.app.AbstractAppReportService;

@Named
public class AppStatisticReportService extends AbstractAppReportService<AppReport> {

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	@Override
	public AppReport makeReport(String id, Date start, Date end) {
		AppReport report = new AppReport(id);

		report.setStartTime(start).setEndTime(end);
		return report;
	}

	@Override
	public AppReport queryDailyReport(int namespace, Date start, Date end) {
		String id = m_mobileConfigManager.getNamespace(namespace);
		AppReport reportModel = new AppReport(id);

		try {
			AppDailyReport report = m_dailyReportDao.findByAppNamePeriod(namespace, Constants.APP, start,
			      AppDailyReportEntity.READSET_FULL);
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
		AppDailyReportContent content = m_dailyReportContentDao.findByPK(id, AppDailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new AppReport(domain);
		}
	}

}
