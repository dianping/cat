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
package com.dianping.cat.report.page.overload.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.home.dal.report.OverloadDao;
import com.dianping.cat.home.dal.report.OverloadEntity;

@Named
public class TableCapacityService extends ContainerHolder {

	@Inject
	private OverloadDao m_overloadDao;

	@Inject
	private HourlyReportDao m_hourlyReportDao;

	@Inject
	private DailyReportDao m_dailyReportDao;

	@Inject
	private WeeklyReportDao m_weeklyReportDao;

	@Inject
	private MonthlyReportDao m_monthlyReportDao;

	private OverloadReport generateOverloadReport(Object object, double reportSize, int reportType) {
		OverloadReport overloadReport = new OverloadReport();

		switch (reportType) {
		case CapacityUpdater.HOURLY_TYPE:
			overloadReport.setDomain(((HourlyReport) object).getDomain());
			overloadReport.setIp(((HourlyReport) object).getIp());
			overloadReport.setName(((HourlyReport) object).getName());
			overloadReport.setPeriod(((HourlyReport) object).getPeriod());
			overloadReport.setType(((HourlyReport) object).getType());
			break;
		case CapacityUpdater.DAILY_TYPE:
			overloadReport.setDomain(((DailyReport) object).getDomain());
			overloadReport.setIp(((DailyReport) object).getIp());
			overloadReport.setName(((DailyReport) object).getName());
			overloadReport.setPeriod(((DailyReport) object).getPeriod());
			overloadReport.setType(((DailyReport) object).getType());
			break;
		case CapacityUpdater.WEEKLY_TYPE:
			overloadReport.setDomain(((WeeklyReport) object).getDomain());
			overloadReport.setIp(((WeeklyReport) object).getIp());
			overloadReport.setName(((WeeklyReport) object).getName());
			overloadReport.setPeriod(((WeeklyReport) object).getPeriod());
			overloadReport.setType(((WeeklyReport) object).getType());
			break;
		case CapacityUpdater.MONTHLY_TYPE:
			overloadReport.setDomain(((MonthlyReport) object).getDomain());
			overloadReport.setIp(((MonthlyReport) object).getIp());
			overloadReport.setName(((MonthlyReport) object).getName());
			overloadReport.setPeriod(((MonthlyReport) object).getPeriod());
			overloadReport.setType(((MonthlyReport) object).getType());
			break;
		}
		overloadReport.setReportType(reportType);
		overloadReport.setReportLength(reportSize);

		return overloadReport;
	}

	public List<OverloadReport> queryOverloadReports(Date startTime, Date endTime) {
		List<OverloadReport> reports = new ArrayList<OverloadReport>();

		try {
			List<Overload> overloads = m_overloadDao
									.findIdAndSizeByDuration(startTime, endTime,	OverloadEntity.READSET_ID_SIZE_TYPE);

			for (Overload overload : overloads) {
				try {
					int reportId = overload.getReportId();
					int reportType = overload.getReportType();
					double reportSize = overload.getReportSize();
					Object report = null;

					switch (reportType) {
					case CapacityUpdater.HOURLY_TYPE:
						report = m_hourlyReportDao.findByPK(reportId, HourlyReportEntity.READSET_FULL);
						break;
					case CapacityUpdater.DAILY_TYPE:
						report = m_dailyReportDao.findByPK(reportId, DailyReportEntity.READSET_FULL);
						break;
					case CapacityUpdater.WEEKLY_TYPE:
						report = m_weeklyReportDao.findByPK(reportId, WeeklyReportEntity.READSET_FULL);
						break;
					case CapacityUpdater.MONTHLY_TYPE:
						report = m_monthlyReportDao.findByPK(reportId, MonthlyReportEntity.READSET_FULL);
						break;
					}
					reports.add(generateOverloadReport(report, reportSize, reportType));
				} catch (DalNotFoundException e) {
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}
		} catch (DalException e) {
			Cat.logError(e);
		}

		return reports;
	}

}
