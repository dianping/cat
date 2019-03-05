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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageReportMerger;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.core.dal.MonthlyReportContentEntity;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.core.dal.WeeklyReportContentEntity;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.AbstractReportService;

@Named
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

	public Set<String> queryAllIds(Date start, Date end) {
		Set<String> ids = new HashSet<String>();

		for (String id : queryAllDomainNames(start, end, StorageAnalyzer.ID)) {
			ids.add(id);
		}
		return ids;
	}

	private Set<String> queryAllIds(Date start, Date end, String name, String reportId) {
		Set<String> ids = new HashSet<String>();
		String type = reportId.substring(reportId.lastIndexOf("-"));

		for (String myId : queryAllDomainNames(start, end, name)) {
			if (myId.endsWith(type)) {
				String prefix = myId.substring(0, myId.lastIndexOf("-"));

				ids.add(prefix);
			}
		}
		return ids;
	}

	@Override
	public StorageReport queryDailyReport(String id, Date start, Date end) {
		StorageReportMerger merger = new StorageReportMerger(new StorageReport(id));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = StorageAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao
										.findByDomainNamePeriod(id, name, new Date(startTime),	DailyReportEntity.READSET_FULL);
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

	private StorageReport queryFromHourlyBinary(int id, Date period, String reportId) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period,	HourlyReportContentEntity.READSET_CONTENT);

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
				reports = m_hourlyReportDao
										.findAllByDomainNamePeriod(new Date(startTime), reportId, name,	HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						StorageReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), reportId);
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
		Set<String> ids = queryAllIds(start, end, name, reportId);

		storageReport.getIds().addAll(ids);
		return storageReport;
	}

	@Override
	public StorageReport queryMonthlyReport(String reportId, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao
									.findReportByDomainNamePeriod(start, reportId, StorageAnalyzer.ID,	MonthlyReportEntity.READSET_FULL);

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
			WeeklyReport entity = m_weeklyReportDao
									.findReportByDomainNamePeriod(start, reportId, StorageAnalyzer.ID,	WeeklyReportEntity.READSET_FULL);

			return queryFromWeeklyBinary(entity.getId(), reportId);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new StorageReport(reportId);
	}

}
