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
package com.dianping.cat.report.task.current;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.service.ProjectService;

@Named(type = TaskBuilder.class, value = CurrentReportBuilder.ID)
public class CurrentReportBuilder implements TaskBuilder {

	public static final String ID = Constants.CURRENT_REPORT;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		CurrentWeeklyMonthlyReportTask reportTask = CurrentWeeklyMonthlyReportTask.getInstance();

		try {
			List<Project> projects = m_projectService.findAll();
			List<String> domains = new ArrayList<String>();

			for (Project project : projects) {
				if (m_serverFilterConfigManager.validateDomain(project.getDomain())) {
					domains.add(project.getDomain());
				}
			}
			reportTask.setDomains(domains);

			Threads.forGroup(Constants.CAT).start(reportTask);
		} catch (DalException e) {
			Cat.logError(e);
		}
		return true;
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("current weekly monthly report builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("current weekly monthly report builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("current weekly monthly report builder don't support weekly task");
	}

}
