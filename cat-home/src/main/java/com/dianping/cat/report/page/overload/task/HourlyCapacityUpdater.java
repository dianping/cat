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

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.home.dal.report.OverloadDao;

@Named(type = CapacityUpdater.class, value = HourlyCapacityUpdater.ID)
public class HourlyCapacityUpdater implements CapacityUpdater {

	public static final String ID = "hourly_capacity_updater";

	@Inject
	private HourlyReportContentDao m_hourlyReportContentDao;

	@Inject
	private HourlyReportDao m_hourlyReportDao;

	@Inject
	private OverloadDao m_overloadDao;

	@Inject
	private CapacityUpdateStatusManager m_manager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity() throws DalException {
		int maxId = m_manager.getHourlyStatus();

		while (true) {
			List<HourlyReportContent> reports = m_hourlyReportContentDao
									.findOverloadReport(maxId,	HourlyReportContentEntity.READSET_LENGTH);

			for (HourlyReportContent content : reports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();

					if (contentLength >= CapacityUpdater.CAPACITY) {
						Overload overload = m_overloadDao.createLocal();

						overload.setReportId(reportId);
						overload.setReportSize(contentLength);
						overload.setReportType(CapacityUpdater.HOURLY_TYPE);

						HourlyReport hourlyReport;
						try {
							hourlyReport = m_hourlyReportDao.findByPK(reportId, HourlyReportEntity.READSET_FULL);
							overload.setPeriod(hourlyReport.getPeriod());
							m_overloadDao.insert(overload);

						} catch (DalNotFoundException e) {
						} catch (Exception e) {
							Cat.logError(e);
						}
					}
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}

			int size = reports.size();
			if (size == 0) {
				break;
			} else {
				maxId = reports.get(size - 1).getReportId();
			}
		}
		m_manager.updateHourlyStatus(maxId);
	}

}
