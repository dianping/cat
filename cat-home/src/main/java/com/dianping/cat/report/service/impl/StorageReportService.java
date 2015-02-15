package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageReportMerger;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.DailyReportContent;
import com.dianping.cat.home.dal.report.DailyReportContentEntity;
import com.dianping.cat.home.dal.report.MonthlyReportContent;
import com.dianping.cat.home.dal.report.MonthlyReportContentEntity;
import com.dianping.cat.home.dal.report.WeeklyReportContent;
import com.dianping.cat.home.dal.report.WeeklyReportContentEntity;
import com.dianping.cat.report.service.AbstractReportService;

public class StorageReportService extends AbstractReportService<StorageReport> {

	@Override
	public StorageReport makeReport(String id, Date start, Date end) {
		StorageReport report = new StorageReport(id);
		int index = id.lastIndexOf("-");
		String name = id.substring(0, index);
		String type = id.substring(index + 1);

		report.setName(name).setType(type);
		report.setStartTime(start).setEndTime(end);
		return report;
	}

	@Override
	public StorageReport queryDailyReport(String id, Date start, Date end) {
		StorageReportMerger merger = new StorageReportMerger(new StorageReport(id));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = StorageAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(id, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				StorageReport reportModel = queryFromDailyBinary(report.getId(), id);

				reportModel.accept(merger);
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		StorageReport storageReport = merger.getStorageReport();

		storageReport.setStartTime(start);
		storageReport.setEndTime(end);
		return storageReport;
	}

	private StorageReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StorageReport(domain);
		}
	}

	private StorageReport queryFromHourlyBinary(int id, String reportId) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao.findByPK(id, HourlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StorageReport(reportId);
		}
	}

	private StorageReport queryFromMonthlyBinary(int id, String reportId) throws DalException {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id, MonthlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StorageReport(reportId);
		}
	}

	private StorageReport queryFromWeeklyBinary(int id, String reportId) throws DalException {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id, WeeklyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new StorageReport(reportId);
		}
	}

	@Override
	public StorageReport queryHourlyReport(String reportId, Date start, Date end) {
		StorageReportMerger merger = new StorageReportMerger(new StorageReport(reportId));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = StorageAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao.findAllByDomainNamePeriod(new Date(startTime), reportId, name,
				      HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						StorageReport reportModel = queryFromHourlyBinary(report.getId(), reportId);
						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		StorageReport storageReport = merger.getStorageReport();

		storageReport.setStartTime(start);
		storageReport.setEndTime(new Date(end.getTime() - 1));

		return storageReport;
	}

	@Override
	public StorageReport queryMonthlyReport(String reportId, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, reportId, StorageAnalyzer.ID,
			      MonthlyReportEntity.READSET_FULL);

			return queryFromMonthlyBinary(entity.getId(), reportId);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new StorageReport(reportId);
	}

	@Override
	public StorageReport queryWeeklyReport(String reportId, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao.findReportByDomainNamePeriod(start, reportId, StorageAnalyzer.ID,
			      WeeklyReportEntity.READSET_FULL);

			return queryFromWeeklyBinary(entity.getId(), reportId);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new StorageReport(reportId);
	}

}
