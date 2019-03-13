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
package com.dianping.cat.report.page.heartbeat.service;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.BaseVisitor;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.AbstractReportService;

@Named
public class HeartbeatReportService extends AbstractReportService<HeartbeatReport> {

	@Override
	public HeartbeatReport makeReport(String domain, Date start, Date end) {
		HeartbeatReport report = new HeartbeatReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public HeartbeatReport queryDailyReport(String domain, Date start, Date end) {
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = HeartbeatAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao
										.findByDomainNamePeriod(domain, name, new Date(startTime),	DailyReportEntity.READSET_FULL);
				HeartbeatReport reportModel = queryFromDailyBinary(report.getId(), domain);

				reportModel.accept(merger);
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();

		heartbeatReport.setStartTime(start);
		heartbeatReport.setEndTime(new Date(end.getTime() - 1));

		new HeartbeatConvertor().visitHeartbeatReport(heartbeatReport);
		return heartbeatReport;
	}

	private HeartbeatReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new HeartbeatReport(domain);
		}
	}

	private HeartbeatReport queryFromHourlyBinary(int id, Date period, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period,	HourlyReportContentEntity.READSET_CONTENT);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new HeartbeatReport(domain);
		}
	}

	@Override
	public HeartbeatReport queryHourlyReport(String domain, Date start, Date end) {
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = HeartbeatAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao
										.findAllByDomainNamePeriod(new Date(startTime), domain, name,	HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						HeartbeatReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);
						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();

		heartbeatReport.setStartTime(start);
		heartbeatReport.setEndTime(new Date(end.getTime() - 1));

		new HeartbeatConvertor().visitHeartbeatReport(heartbeatReport);
		return heartbeatReport;
	}

	@Override
	public HeartbeatReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("Heartbeat report don't support monthly report");
	}

	@Override
	public HeartbeatReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("Heartbeat report don't support weekly report");
	}

	public static class HeartbeatConvertor extends BaseVisitor {

		@Override
		public void visitPeriod(Period period) {
			Extension catExtension = period.findOrCreateExtension("CatUsage");

			if (period.getCatMessageProduced() > 0 || period.getCatMessageSize() > 0) {
				catExtension.findOrCreateDetail("Produced").setValue(period.getCatMessageProduced());
				catExtension.findOrCreateDetail("Overflowed").setValue(period.getCatMessageOverflow());
				catExtension.findOrCreateDetail("Bytes").setValue(period.getCatMessageSize());

				Extension system = period.findOrCreateExtension("System");

				system.findOrCreateDetail("LoadAverage").setValue(period.getSystemLoadAverage());

				Extension gc = period.findOrCreateExtension("GC");
				gc.findOrCreateDetail("ParNewCount").setValue(period.getNewGcCount());
				gc.findOrCreateDetail("ConcurrentMarkSweepCount").setValue(period.getOldGcCount());

				Extension thread = period.findOrCreateExtension("FrameworkThread");

				thread.findOrCreateDetail("HttpThread").setValue(period.getHttpThreadCount());
				thread.findOrCreateDetail("CatThread").setValue(period.getCatThreadCount());
				thread.findOrCreateDetail("PigeonThread").setValue(period.getPigeonThreadCount());
				thread.findOrCreateDetail("ActiveThread").setValue(period.getThreadCount());
				thread.findOrCreateDetail("StartedThread").setValue(period.getTotalStartedCount());

				Extension disk = period.findOrCreateExtension("Disk");
				List<Disk> disks = period.getDisks();

				for (Disk vinfo : disks) {
					disk.findOrCreateDetail(vinfo.getPath() + " Free").setValue(vinfo.getFree());
				}
			}
			super.visitPeriod(period);
		}
	}

}
