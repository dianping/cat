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
package com.dianping.cat.report.task.reload;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;

public abstract class AbstractReportReloader implements ReportReloader {

	@Inject
	protected HourlyReportDao m_hourlyReportDao;

	@Inject
	protected HourlyReportContentDao m_hourlyReportContentDao;

	@Inject
	protected ServerConfigManager m_serverConfigManager;

	protected int getAnalyzerCount() {
		return m_serverConfigManager.getThreadsOfRealtimeAnalyzer(getId());
	}

	public boolean insertHourlyReport(ReportReloadEntity entity) {
		try {
			HourlyReport report = entity.getReport();
			m_hourlyReportDao.insert(report);

			int id = report.getId();
			HourlyReportContent proto = m_hourlyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(entity.getReportContent());
			proto.setPeriod(report.getPeriod());
			m_hourlyReportContentDao.insert(proto);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean reload(long time) {
		try {
			List<ReportReloadEntity> reports = loadReport(time);

			for (ReportReloadEntity entity : reports) {
				insertHourlyReport(entity);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return true;
	}

}
