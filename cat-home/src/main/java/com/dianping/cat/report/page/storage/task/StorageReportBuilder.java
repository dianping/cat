/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.page.storage.task;

import java.util.Date;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
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
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask.CurrentWeeklyMonthlyTask;

@Named(type = TaskBuilder.class, value = StorageReportBuilder.ID)
public class StorageReportBuilder implements TaskBuilder, Initializable {

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

		StorageReport storageReport = queryDailyReportsByDuration(reportId, period, end);
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(reportId);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(storageReport);
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

		StorageReport storageReport = queryDailyReportsByDuration(reportId, period, end);
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(reportId);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(storageReport);
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
				StorageReport reportModel = m_reportService
										.queryReport(reportId, new Date(startTime), new Date(startTime	+ TimeHelper.ONE_DAY));
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
			StorageReport reportModel = m_reportService
									.queryReport(reportId, new Date(startTime), new Date(startTime	+ TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		StorageReport storageReport = merger.getStorageReport();

		storageReport.setName(name).setType(type);
		storageReport.setStartTime(start).setEndTime(end);
		return storageReport;
	}

	@Override
	public void initialize() throws InitializationException {
		CurrentWeeklyMonthlyReportTask.getInstance().register(new CurrentWeeklyMonthlyTask() {

			@Override
			public void buildCurrentMonthlyTask(String name, String domain, Date start) {
				if (Constants.CAT.equals(domain)) {
					Set<String> ids = m_reportService.queryAllIds(start, TimeHelper.getCurrentDay());

					for (String id : ids) {
						buildMonthlyTask(name, id, start);
					}
				}
			}

			@Override
			public void buildCurrentWeeklyTask(String name, String domain, Date start) {
				if (Constants.CAT.equals(domain)) {
					Set<String> ids = m_reportService.queryAllIds(start, TimeHelper.getCurrentDay());

					for (String id : ids) {
						buildWeeklyTask(name, id, start);
					}
				}
			}

			@Override
			public String getReportName() {
				return ID;
			}
		});
	}

}
