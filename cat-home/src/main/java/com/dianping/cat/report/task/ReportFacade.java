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
package com.dianping.cat.report.task;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.task.TaskManager;

@Named
public class ReportFacade extends ContainerHolder implements LogEnabled, Initializable {

	private Logger m_logger;

	private Map<String, TaskBuilder> m_reportBuilders = new HashMap<String, TaskBuilder>();

	public boolean builderReport(Task task) {
		try {
			if (task == null) {
				return false;
			}
			int type = task.getTaskType();
			String reportName = task.getReportName();
			String reportDomain = task.getReportDomain();
			Date reportPeriod = task.getReportPeriod();
			TaskBuilder reportBuilder = getReportBuilder(reportName);

			if (reportBuilder == null) {
				Cat.logError(new RuntimeException("no report builder for type:" + " " + reportName));
				return false;
			} else {
				boolean result = false;

				if (type == TaskManager.REPORT_HOUR) {
					result = reportBuilder.buildHourlyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TaskManager.REPORT_DAILY) {
					result = reportBuilder.buildDailyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TaskManager.REPORT_WEEK) {
					result = reportBuilder.buildWeeklyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TaskManager.REPORT_MONTH) {
					result = reportBuilder.buildMonthlyTask(reportName, reportDomain, reportPeriod);
				}
				if (result) {
					return result;
				} else {
					m_logger.error(task.toString());
				}
			}
		} catch (Exception e) {
			m_logger.error("Error when building report," + e.getMessage(), e);
			Cat.logError(e);
			return false;
		}
		return false;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private TaskBuilder getReportBuilder(String reportName) {
		return m_reportBuilders.get(reportName);
	}

	@Override
	public void initialize() throws InitializationException {
		m_reportBuilders = lookupMap(TaskBuilder.class);
	}

}
