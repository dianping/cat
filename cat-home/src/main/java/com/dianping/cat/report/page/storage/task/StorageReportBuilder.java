package com.dianping.cat.report.page.storage.task;

import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageReportMerger;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.storage.transform.StorageMergeHelper;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

public class StorageReportBuilder implements TaskBuilder {

	public static final String ID = StorageAnalyzer.ID;

	@Inject
	protected StorageReportService m_reportService;

	@Inject
	private StorageMergeHelper m_storageMergerHelper;

	@Override
	public boolean buildDailyTask(String name, String reportId, Date period) {
		try {
			StorageReport storageReport = queryHourlyReportsByDuration(reportId, period, TaskHelper.tomorrowZero(period));

			DailyReport report = new DailyReport();

			report.setCreationDate(new Date());
			report.setDomain(reportId);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(storageReport);
			return m_reportService.insertDailyReport(report, binaryContent);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Storage report don't support HourlyReport!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String reportId, Date period) {
		Date end = null;

		if (period.equals(TimeHelper.getCurrentMonth())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = TaskHelper.nextMonthStart(period);
		}

		StorageReport eventReport = queryDailyReportsByDuration(reportId, period, end);
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(reportId);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String reportId, Date period) {
		Date end = null;

		if (period.equals(TimeHelper.getCurrentWeek())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = new Date(period.getTime() + TimeHelper.ONE_WEEK);
		}

		StorageReport eventReport = queryDailyReportsByDuration(reportId, period, end);
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(reportId);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private StorageReport queryDailyReportsByDuration(String reportId, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		int index = reportId.lastIndexOf("-");
		String name = reportId.substring(0, index);
		String type = reportId.substring(index + 1);
		StorageReport report = new StorageReport(reportId);
		StorageReportMerger merger = new StorageReportMerger(report);

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				StorageReport reportModel = m_reportService.queryReport(reportId, new Date(startTime), new Date(startTime
				      + TimeHelper.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		StorageReport storageReport = merger.getStorageReport();

		storageReport.setName(name).setType(type);
		storageReport.setStartTime(start).setEndTime(end);
		return storageReport;
	}

	private StorageReport queryHourlyReportsByDuration(String reportId, Date start, Date end) throws DalException {
		long startTime = start.getTime();
		long endTime = end.getTime();
		int index = reportId.lastIndexOf("-");
		String name = reportId.substring(0, index);
		String type = reportId.substring(index + 1);
		StorageReport report = new StorageReport(reportId);
		HistoryStorageReportMerger merger = new HistoryStorageReportMerger(report);

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			StorageReport reportModel = m_reportService.queryReport(reportId, new Date(startTime), new Date(startTime
			      + TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		StorageReport storageReport = merger.getStorageReport();

		storageReport.setName(name).setType(type);
		storageReport.setStartTime(start).setEndTime(end);
		return storageReport;
	}

}
