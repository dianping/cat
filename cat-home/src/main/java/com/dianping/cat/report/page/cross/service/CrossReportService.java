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
package com.dianping.cat.report.page.cross.service;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossReportMerger;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultNativeParser;
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
public class CrossReportService extends AbstractReportService<CrossReport> {

	@Override
	public CrossReport makeReport(String domain, Date start, Date end) {
		CrossReport report = new CrossReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public CrossReport queryDailyReport(String domain, Date start, Date end) {
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = CrossAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao
										.findByDomainNamePeriod(domain, name, new Date(startTime),	DailyReportEntity.READSET_FULL);
				CrossReport reportModel = queryFromDailyBinary(report.getId(), domain);

				reportModel.accept(merger);
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		CrossReport crossReport = merger.getCrossReport();

		crossReport.setStartTime(start);
		crossReport.setEndTime(end);
		return crossReport;
	}

	private CrossReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new CrossReport(domain);
		}
	}

	private CrossReport queryFromHourlyBinary(int id, Date period, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period,	HourlyReportContentEntity.READSET_CONTENT);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new CrossReport(domain);
		}
	}

	private CrossReport queryFromMonthlyBinary(int id, String domain) throws DalException {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id, MonthlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new CrossReport(domain);
		}
	}

	private CrossReport queryFromWeeklyBinary(int id, String domain) throws DalException {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id, WeeklyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new CrossReport(domain);
		}
	}

	@Override
	public CrossReport queryHourlyReport(String domain, Date start, Date end) {
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = CrossAnalyzer.ID;

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
						CrossReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);

						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		CrossReport crossReport = merger.getCrossReport();

		crossReport.setStartTime(start);
		crossReport.setEndTime(new Date(end.getTime() - 1));

		return crossReport;
	}

	@Override
	public CrossReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao
									.findReportByDomainNamePeriod(start, domain, CrossAnalyzer.ID,	MonthlyReportEntity.READSET_FULL);
			return queryFromMonthlyBinary(entity.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new CrossReport(domain);
	}

	@Override
	public CrossReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao
									.findReportByDomainNamePeriod(start, domain, CrossAnalyzer.ID,	WeeklyReportEntity.READSET_FULL);

			return queryFromWeeklyBinary(entity.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new CrossReport(domain);
	}

}
