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
package com.dianping.cat.system.page.router.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.transform.DefaultNativeParser;
import com.dianping.cat.report.service.AbstractReportService;
import com.dianping.cat.system.page.router.config.RouterConfigManager;

@Named
public class RouterConfigService extends AbstractReportService<RouterConfig> {

	@Inject
	private RouterConfigManager m_routerConfigManager;

	@Override
	public RouterConfig makeReport(String domain, Date start, Date end) {
		return null;
	}

	@Override
	public RouterConfig queryDailyReport(String domain, Date start, Date end) {
		long time = start.getTime();
		Map<Long, Pair<RouterConfig, Long>> routerConfigs = m_routerConfigManager.getRouterConfigs();
		Pair<RouterConfig, Long> pair = routerConfigs.get(time);

		if (pair == null) {
			String name = Constants.REPORT_ROUTER;

			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, start, DailyReportEntity.READSET_FULL);
				RouterConfig config = queryFromDailyBinary(report.getId());

				routerConfigs.put(time, new Pair<RouterConfig, Long>(config, report.getCreationDate().getTime()));
				return config;
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
			return null;
		} else {
			return pair.getKey();
		}
	}

	private RouterConfig queryFromDailyBinary(int id) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return null;
		}
	}

	@Override
	public RouterConfig queryHourlyReport(String domain, Date start, Date end) {
		throw new RuntimeException("router report don't support hourly report");
	}

	public RouterConfig queryLastReport(String domain) {
		try {
			List<DailyReport> reports = m_dailyReportDao
									.queryLatestReportsByDomainName(domain, Constants.REPORT_ROUTER, 1, DailyReportEntity.READSET_FULL);

			if (reports.size() == 0) {
				return null;
			}

			DailyReport report = reports.get(0);
			RouterConfig config = queryFromDailyBinary(report.getId());

			return config;
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	@Override
	public RouterConfig queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("router report don't support monthly report");
	}

	@Override
	public RouterConfig queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("router report don't support weekly report");
	}

}
