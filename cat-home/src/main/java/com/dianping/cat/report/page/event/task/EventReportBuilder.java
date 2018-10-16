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
package com.dianping.cat.report.page.event.task;

import java.util.Date;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventReportCountFilter;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask.CurrentWeeklyMonthlyTask;

@Named(type = TaskBuilder.class, value = EventReportBuilder.ID)
public class EventReportBuilder implements TaskBuilder, Initializable {

	public static final String ID = EventAnalyzer.ID;

	@Inject
	protected EventReportService m_reportService;

	@Inject
	protected ServerConfigManager m_serverConfigManager;

	@Inject
	private AtomicMessageConfigManager m_atomicMessageConfigManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			EventReport eventReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));

			DailyReport report = new DailyReport();

			report.setCreationDate(new Date());
			report.setDomain(domain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
			return m_reportService.insertDailyReport(report, binaryContent);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("event report don't support HourlyReport!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		Date end = null;

		if (period.equals(TimeHelper.getCurrentMonth())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = TaskHelper.nextMonthStart(period);
		}

		EventReport eventReport = queryDailyReportsByDuration(domain, period, end);
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		Date end = null;

		if (period.equals(TimeHelper.getCurrentWeek())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = new Date(period.getTime() + TimeHelper.ONE_WEEK);
		}

		EventReport eventReport = queryDailyReportsByDuration(domain, period, end);
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	@Override
	public void initialize() throws InitializationException {
		CurrentWeeklyMonthlyReportTask.getInstance().register(new CurrentWeeklyMonthlyTask() {

			@Override
			public void buildCurrentMonthlyTask(String name, String domain, Date start) {
				buildMonthlyTask(name, domain, start);
			}

			@Override
			public void buildCurrentWeeklyTask(String name, String domain, Date start) {
				buildWeeklyTask(name, domain, start);
			}

			@Override
			public String getReportName() {
				return ID;
			}
		});
	}

	private EventReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		double duration = (endTime - startTime) * 1.0 / TimeHelper.ONE_DAY;

		HistoryEventReportMerger merger = new HistoryEventReportMerger(new EventReport(domain)).setDuration(duration);
		EventReport eventReport = merger.getEventReport();

		EventReportDailyGraphCreator creator = new EventReportDailyGraphCreator(eventReport, (int) duration, start);

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				EventReport reportModel = m_reportService
										.queryReport(domain, new Date(startTime), new Date(startTime + TimeHelper.ONE_DAY));

				creator.createGraph(reportModel);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		eventReport.setStartTime(start);
		eventReport.setEndTime(end);

		new EventReportCountFilter(m_serverConfigManager.getMaxTypeThreshold(),
								m_atomicMessageConfigManager.getMaxNameThreshold(domain), m_serverConfigManager.getTypeNameLengthLimit())
								.visitEventReport(eventReport);
		return eventReport;
	}

	private EventReport queryHourlyReportsByDuration(String name, String domain, Date start, Date endDate)
							throws DalException {
		long startTime = start.getTime();
		long endTime = endDate.getTime();
		double duration = (endTime - startTime) * 1.0 / TimeHelper.ONE_DAY;

		HistoryEventReportMerger merger = new HistoryEventReportMerger(new EventReport(domain)).setDuration(duration);
		EventReportHourlyGraphCreator graphCreator = new EventReportHourlyGraphCreator(merger.getEventReport(), 10);

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			EventReport report = m_reportService
									.queryReport(domain, new Date(startTime), new Date(startTime + TimeHelper.ONE_HOUR));

			graphCreator.createGraph(report);
			report.accept(merger);
		}

		EventReport dailyReport = merger.getEventReport();
		Date date = dailyReport.getStartTime();
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);

		dailyReport.setStartTime(TaskHelper.todayZero(date));
		dailyReport.setEndTime(end);

		new EventReportCountFilter(m_serverConfigManager.getMaxTypeThreshold(),
								m_atomicMessageConfigManager.getMaxNameThreshold(domain), m_serverConfigManager.getTypeNameLengthLimit())
								.visitEventReport(dailyReport);

		return dailyReport;
	}

}
