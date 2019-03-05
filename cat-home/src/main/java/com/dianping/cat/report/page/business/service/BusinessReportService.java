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
package com.dianping.cat.report.page.business.service;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.BusinessReportMerger;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.AbstractReportService;

@Named
public class BusinessReportService extends AbstractReportService<BusinessReport> {

	@Override
	public BusinessReport makeReport(String domain, Date start, Date end) {
		BusinessReport report = new BusinessReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public BusinessReport queryDailyReport(String domain, Date start, Date end) {
		throw new UnsupportedOperationException("Business report don't support daily report");
	}

	@Override
	public BusinessReport queryHourlyReport(String domain, Date start, Date end) {
		BusinessReportMerger merger = new BusinessReportMerger(new BusinessReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = BusinessAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date period = new Date(startTime);
			List<HourlyReport> reports = null;

			try {
				reports = m_hourlyReportDao.findAllByDomainNamePeriod(period, domain, name, HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						BusinessReport reportModel = queryFromHourlyBinary(report.getId(), period, domain);
						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		BusinessReport businessReport = merger.getBusinessReport();

		businessReport.setStartTime(start);
		businessReport.setEndTime(new Date(end.getTime() - 1));

		return businessReport;
	}

	private BusinessReport queryFromHourlyBinary(int id, Date period, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period,	HourlyReportContentEntity.READSET_CONTENT);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new BusinessReport(domain);
		}
	}

	@Override
	public BusinessReport queryMonthlyReport(String domain, Date start) {
		throw new UnsupportedOperationException("Business report don't support monthly report");
	}

	@Override
	public BusinessReport queryWeeklyReport(String domain, Date start) {
		throw new UnsupportedOperationException("Business report don't support weekly report");
	}

}
