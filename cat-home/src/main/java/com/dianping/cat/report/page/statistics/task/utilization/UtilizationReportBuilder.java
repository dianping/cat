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
package com.dianping.cat.report.page.statistics.task.utilization;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.utilization.entity.ApplicationState;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.home.utilization.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.cross.display.TypeDetailInfo;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.page.statistics.service.UtilizationReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

@Named(type = TaskBuilder.class, value = UtilizationReportBuilder.ID)
public class UtilizationReportBuilder implements TaskBuilder {

	public static final String ID = Constants.REPORT_UTILIZATION;

	@Inject
	protected UtilizationReportService m_reportService;

	@Inject
	protected TransactionReportService m_transactionReportService;

	@Inject
	protected HeartbeatReportService m_heartbeatReportService;

	@Inject
	protected CrossReportService m_crossReportService;

	@Inject
	private TransactionMergeHelper m_mergeHelper;

	@Inject
	private ServerFilterConfigManager m_configManger;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		UtilizationReport utilizationReport = queryHourlyReportsByDuration(name, domain, period,
								TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(utilizationReport);

		return m_reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date start) {
		UtilizationReport utilizationReport = new UtilizationReport(Constants.CAT);
		Date end = new Date(start.getTime() + TimeHelper.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, TransactionAnalyzer.ID);
		TransactionReportVisitor transactionVisitor = new TransactionReportVisitor().setUtilizationReport(utilizationReport);
		HeartbeatReportVisitor heartbeatVisitor = new HeartbeatReportVisitor().setUtilizationReport(utilizationReport);

		for (String domainName : domains) {
			if (m_configManger.validateDomain(domainName)) {
				TransactionReport transactionReport = m_transactionReportService.queryReport(domainName, start, end);
				int size = transactionReport.getMachines().size();

				utilizationReport.findOrCreateDomain(domainName).setMachineNumber(size);
				transactionReport = m_mergeHelper.mergeAllMachines(transactionReport, Constants.ALL);
				transactionVisitor.visitTransactionReport(transactionReport);
			}
		}

		for (String domainName : domains) {
			if (m_configManger.validateDomain(domainName)) {
				HeartbeatReport heartbeatReport = m_heartbeatReportService.queryReport(domainName, start, end);

				heartbeatVisitor.visitHeartbeatReport(heartbeatReport);
			}
		}

		for (String domainName : domains) {
			if (m_configManger.validateDomain(domainName)) {
				CrossReport crossReport = m_crossReportService.queryReport(domainName, start, end);
				ProjectInfo projectInfo = new ProjectInfo(TimeHelper.ONE_HOUR);

				projectInfo.setClientIp(Constants.ALL);
				projectInfo.visitCrossReport(crossReport);
				Collection<TypeDetailInfo> callInfos = projectInfo.getCallProjectsInfo();

				for (TypeDetailInfo typeInfo : callInfos) {
					String project = typeInfo.getProjectName();

					if (!validataService(project)) {
						long failure = typeInfo.getFailureCount();
						Domain d = utilizationReport.findOrCreateDomain(project);
						ApplicationState service = d.findApplicationState("PigeonService");

						if (service != null) {
							service.setFailureCount(service.getFailureCount() + failure);

							long count = service.getCount();
							if (count > 0) {
								service.setFailurePercent(service.getFailureCount() * 1.0 / count);
							}
						}
					}
				}
			}
		}

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(end);

		HourlyReport report = new HourlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(start);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(utilizationReport);

		return m_reportService.insertHourlyReport(report, binaryContent);
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		UtilizationReport utilizationReport = queryDailyReportsByDuration(domain, period,	TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		byte[] binaryContent = DefaultNativeBuilder.build(utilizationReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		UtilizationReport utilizationReport = queryDailyReportsByDuration(domain, period,
								new Date(period.getTime()	+ TimeHelper.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(utilizationReport);

		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private UtilizationReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(domain));

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				UtilizationReport reportModel = m_reportService
										.queryReport(domain, new Date(startTime), new Date(startTime	+ TimeHelper.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		UtilizationReport utilizationReport = merger.getUtilizationReport();

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(end);
		return utilizationReport;
	}

	private UtilizationReport queryHourlyReportsByDuration(String name, String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date date = new Date(startTime);
			UtilizationReport reportModel = m_reportService
									.queryReport(domain, date, new Date(date.getTime()	+ TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		UtilizationReport utilizationReport = merger.getUtilizationReport();

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(end);
		return utilizationReport;
	}

	private boolean validataService(String projectName) {
		return projectName.equalsIgnoreCase(ProjectInfo.ALL_SERVER) || projectName.equalsIgnoreCase("UnknownProject");
	}

}
