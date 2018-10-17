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
package com.dianping.cat.report.page.statistics.task.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.service.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.cross.display.TypeDetailInfo;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.page.statistics.service.ServiceReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

@Named(type = TaskBuilder.class, value = ServiceReportBuilder.ID)
public class ServiceReportBuilder implements TaskBuilder {

	public static final String ID = Constants.REPORT_SERVICE;

	@Inject
	protected ServiceReportService m_reportService;

	@Inject
	protected CrossReportService m_crossReportService;

	Map<String, Domain> stat = new HashMap<String, Domain>();

	@Inject
	private ServerFilterConfigManager m_configManger;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		ServiceReport serviceReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(serviceReport);

		return m_reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date start) {
		ServiceReport serviceReport = new ServiceReport(Constants.CAT);
		Date end = new Date(start.getTime() + TimeHelper.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, CrossAnalyzer.ID);

		for (String domainName : domains) {
			if (m_configManger.validateDomain(domainName)) {
				CrossReport crossReport = m_crossReportService.queryReport(domainName, start, end);
				ProjectInfo projectInfo = new ProjectInfo(TimeHelper.ONE_HOUR);

				projectInfo.setClientIp(Constants.ALL);
				projectInfo.visitCrossReport(crossReport);
				Collection<TypeDetailInfo> callInfos = projectInfo.getCallProjectsInfo();

				for (TypeDetailInfo typeInfo : callInfos) {
					if (!validataService(typeInfo)) {
						merge(serviceReport.findOrCreateDomain(typeInfo.getProjectName()), typeInfo);
					}
				}
			}
		}
		HourlyReport report = new HourlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(start);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(serviceReport);
		return m_reportService.insertHourlyReport(report, binaryContent);
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		ServiceReport serviceReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(serviceReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		ServiceReport serviceReport = queryDailyReportsByDuration(domain, period,
								new Date(period.getTime()	+ TimeHelper.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(serviceReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	public void merge(Domain domain, TypeDetailInfo info) {
		domain.setTotalCount(domain.getTotalCount() + info.getTotalCount());
		domain.setFailureCount(domain.getFailureCount() + info.getFailureCount());
		domain.setSum(domain.getSum() + info.getSum());

		if (domain.getTotalCount() > 0) {
			domain.setAvg(domain.getSum() / domain.getTotalCount());
			domain.setFailurePercent(domain.getFailureCount() * 1.0 / domain.getTotalCount());
		}
	}

	private ServiceReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		ServiceReportMerger merger = new ServiceReportMerger(new ServiceReport(domain));

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				ServiceReport reportModel = m_reportService
										.queryReport(domain, new Date(startTime), new Date(startTime	+ TimeHelper.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		ServiceReport serviceReport = merger.getServiceReport();

		serviceReport.setStartTime(start);
		serviceReport.setEndTime(end);
		return serviceReport;
	}

	private ServiceReport queryHourlyReportsByDuration(String name, String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		ServiceReportMerger merger = new ServiceReportMerger(new ServiceReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date date = new Date(startTime);
			ServiceReport reportModel = m_reportService
									.queryReport(domain, date, new Date(date.getTime()	+ TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}

		ServiceReport serviceReport = merger.getServiceReport();

		serviceReport.setStartTime(start);
		serviceReport.setEndTime(end);
		return serviceReport;
	}

	private boolean validataService(TypeDetailInfo typeInfo) {
		return typeInfo.getProjectName().equalsIgnoreCase(ProjectInfo.ALL_SERVER)	|| typeInfo.getProjectName()
								.equalsIgnoreCase("UnknownProject");
	}

}
