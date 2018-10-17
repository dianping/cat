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
package com.dianping.cat.report.page.problem.task;

import java.util.Date;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemReportFilter;
import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask.CurrentWeeklyMonthlyTask;

@Named(type = TaskBuilder.class, value = ProblemReportBuilder.ID)
public class ProblemReportBuilder implements TaskBuilder, Initializable {

	public static final String ID = ProblemAnalyzer.ID;

	@Inject
	protected ProblemReportService m_reportService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			ProblemReport problemReport = queryHourlyReportsByDuration(name, domain, period,	TaskHelper.tomorrowZero(period));

			DailyReport report = new DailyReport();

			report.setCreationDate(new Date());
			report.setDomain(domain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(problemReport);

			return m_reportService.insertDailyReport(report, binaryContent);
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("problem report don't support HourlyReport!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		ProblemReport problemReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));

		new ProblemReportFilter().visitProblemReport(problemReport);

		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(problemReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		ProblemReport problemReport = queryDailyReportsByDuration(domain, period,
								new Date(period.getTime()	+ TimeHelper.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(problemReport);
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

	private ProblemReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		double duration = (endTime - startTime) * 1.0 / TimeHelper.ONE_DAY;

		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(domain));
		ProblemReport problemReport = merger.getProblemReport();

		ProblemReportDailyGraphCreator creator = new ProblemReportDailyGraphCreator(problemReport, (int) duration, start);

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				ProblemReport reportModel = m_reportService
										.queryReport(domain, new Date(startTime), new Date(startTime	+ TimeHelper.ONE_DAY));

				creator.createGraph(reportModel);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		problemReport.setStartTime(start);
		problemReport.setEndTime(end);
		return problemReport;
	}

	private ProblemReport queryHourlyReportsByDuration(String name, String domain, Date start, Date endDate)
							throws DalException {
		long startTime = start.getTime();
		long endTime = endDate.getTime();

		ProblemReportMerger merger = new HistoryProblemReportMerger(new ProblemReport(domain));
		ProblemReportHourlyGraphCreator graphCreator = new ProblemReportHourlyGraphCreator(merger.getProblemReport(), 10);

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			ProblemReport report = m_reportService
									.queryReport(domain, new Date(startTime), new Date(startTime	+ TimeHelper.ONE_HOUR));

			graphCreator.createGraph(report);
			report.accept(merger);
		}

		ProblemReport dailyReport = merger.getProblemReport();
		Date date = dailyReport.getStartTime();
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);

		dailyReport.setStartTime(TaskHelper.todayZero(date));
		dailyReport.setEndTime(end);
		return dailyReport;
	}
}
