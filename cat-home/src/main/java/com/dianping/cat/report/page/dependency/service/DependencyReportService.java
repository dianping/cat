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
package com.dianping.cat.report.page.dependency.service;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyReportMerger;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.AbstractReportService;

@Named
public class DependencyReportService extends AbstractReportService<DependencyReport> {

	@Override
	public DependencyReport makeReport(String domain, Date start, Date end) {
		DependencyReport report = new DependencyReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public DependencyReport queryDailyReport(String domain, Date start, Date end) {
		throw new UnsupportedOperationException("Dependency report don't support daily report");
	}

	private DependencyReport queryFromHourlyBinary(int id, Date period, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period,	HourlyReportContentEntity.READSET_CONTENT);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new DependencyReport(domain);
		}
	}

	@Override
	public DependencyReport queryHourlyReport(String domain, Date start, Date end) {
		DependencyReportMerger merger = new DependencyReportMerger(new DependencyReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = DependencyAnalyzer.ID;

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
						DependencyReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);
						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		DependencyReport dependencyReport = merger.getDependencyReport();

		dependencyReport.setStartTime(start);
		dependencyReport.setEndTime(new Date(end.getTime() - 1));

		return dependencyReport;
	}

	@Override
	public DependencyReport queryMonthlyReport(String domain, Date start) {
		throw new UnsupportedOperationException("Dependency report don't support monthly report");
	}

	@Override
	public DependencyReport queryWeeklyReport(String domain, Date start) {
		throw new UnsupportedOperationException("Dependency report don't support weekly report");
	}

}
