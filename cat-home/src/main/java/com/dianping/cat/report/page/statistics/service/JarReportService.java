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
package com.dianping.cat.report.page.statistics.service;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.jar.entity.JarReport;
import com.dianping.cat.home.jar.transform.DefaultNativeParser;
import com.dianping.cat.report.service.AbstractReportService;

@Named
public class JarReportService extends AbstractReportService<JarReport> {

	@Override
	public JarReport makeReport(String domain, Date start, Date end) {
		throw new RuntimeException("JarReportService do not suppot makeReport feature");
	}

	@Override
	public JarReport queryDailyReport(String domain, Date start, Date end) {
		throw new RuntimeException("JarReportService do not suppot queryDailyReport feature");
	}

	private JarReport queryFromHourlyBinary(int id, Date period, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao
								.findByPK(id, period,	HourlyReportContentEntity.READSET_CONTENT);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new JarReport();
		}
	}

	@Override
	public JarReport queryHourlyReport(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_JAR;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao.findAllByDomainNamePeriod(start, domain, name, HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						return queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);
					} catch (DalException e) {
						Cat.logError(e);
					}
				}
			}
		}
		return new JarReport();
	}

	@Override
	public JarReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("JarReportService do not suppot queryMonthlyReport feature");
	}

	@Override
	public JarReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("JarReportService do not suppot queryWeeklyReport feature");
	}

}
