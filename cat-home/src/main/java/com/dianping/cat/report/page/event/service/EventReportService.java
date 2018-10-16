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
package com.dianping.cat.report.page.event.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeParser;
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
public class EventReportService extends AbstractReportService<EventReport> {

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	private EventReport convert(EventReport report) {
		Date start = report.getStartTime();
		Date end = report.getEndTime();

		try {
			if (start != null && end != null && end.before(m_sdf.parse("2015-01-05"))) {
				TpsStatistics statistics = new TpsStatistics((end.getTime() - start.getTime()) / 1000.0);

				report.accept(statistics);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		// for old report, can be removed later.
		AllMachineRemover remover = new AllMachineRemover();
		report.accept(remover);

		return report;
	}

	@Override
	public EventReport makeReport(String domain, Date start, Date end) {
		EventReport report = new EventReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public EventReport queryDailyReport(String domain, Date start, Date end) {
		EventReportMerger merger = new EventReportMerger(new EventReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = EventAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao
										.findByDomainNamePeriod(domain, name, new Date(startTime),	DailyReportEntity.READSET_FULL);
				EventReport reportModel = queryFromDailyBinary(report.getId(), domain);

				reportModel.accept(merger);
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		EventReport eventReport = merger.getEventReport();

		eventReport.setStartTime(start);
		eventReport.setEndTime(end);
		return convert(eventReport);
	}

	private EventReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new EventReport(domain);
		}
	}

	private EventReport queryFromHourlyBinary(int id, Date period, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period,	HourlyReportContentEntity.READSET_CONTENT);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new EventReport(domain);
		}
	}

	private EventReport queryFromMonthlyBinary(int id, String domain) throws DalException {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id, MonthlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new EventReport(domain);
		}
	}

	private EventReport queryFromWeeklyBinary(int id, String domain) throws DalException {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id, WeeklyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new EventReport(domain);
		}
	}

	@Override
	public EventReport queryHourlyReport(String domain, Date start, Date end) {
		EventReportMerger merger = new EventReportMerger(new EventReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = EventAnalyzer.ID;

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
						EventReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);

						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		EventReport eventReport = merger.getEventReport();

		eventReport.setStartTime(start);
		eventReport.setEndTime(new Date(end.getTime() - 1));

		return convert(eventReport);
	}

	@Override
	public EventReport queryMonthlyReport(String domain, Date start) {
		EventReport eventReport = new EventReport(domain);

		try {
			MonthlyReport entity = m_monthlyReportDao
									.findReportByDomainNamePeriod(start, domain, EventAnalyzer.ID,	MonthlyReportEntity.READSET_FULL);

			eventReport = queryFromMonthlyBinary(entity.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return convert(eventReport);
	}

	@Override
	public EventReport queryWeeklyReport(String domain, Date start) {
		EventReport eventReport = new EventReport(domain);

		try {
			WeeklyReport entity = m_weeklyReportDao
									.findReportByDomainNamePeriod(start, domain, EventAnalyzer.ID,	WeeklyReportEntity.READSET_FULL);

			eventReport = queryFromWeeklyBinary(entity.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return convert(eventReport);
	}

	public class TpsStatistics extends BaseVisitor {

		public double m_duration;

		public TpsStatistics(double duration) {
			m_duration = duration;
		}

		@Override
		public void visitName(EventName name) {
			if (m_duration > 0) {
				name.setTps(name.getTotalCount() * 1.0 / m_duration);
			}
		}

		@Override
		public void visitType(EventType type) {
			if (m_duration > 0) {
				type.setTps(type.getTotalCount() * 1.0 / m_duration);
				super.visitType(type);
			}
		}
	}

	public class AllMachineRemover extends BaseVisitor {

		@Override
		public void visitEventReport(EventReport eventReport) {
			eventReport.removeMachine(Constants.ALL);
		}
	}
}