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
package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.problem.transform.DetailStatistics;
import com.dianping.cat.report.page.problem.transform.ProblemStatistics;

@ModelMeta(ProblemAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private ProblemStatistics m_allStatistics;

	private int m_currentMinute; // for navigation

	private String m_defaultSqlThreshold;

	private String m_defaultThreshold;

	@EntityMeta
	private DetailStatistics m_detailStatistics;

	private String m_errorsTrend;

	@EntityMeta
	private GroupLevelInfo m_groupLevelInfo;

	private String m_groupName;

	private int m_hour;

	private int m_lastMinute; // last minute of current hour

	@EntityMeta
	private ProblemReport m_report;

	private String m_threadId;

	@EntityMeta
	private ThreadLevelInfo m_threadLevelInfo;

	private List<String> m_groups;

	private List<String> m_groupIps;

	private String m_distributionChart;

	public Model(Context ctx) {
		super(ctx);
	}

	public ProblemStatistics getAllStatistics() {
		return m_allStatistics;
	}

	public void setAllStatistics(ProblemStatistics allStatistics) {
		m_allStatistics = allStatistics;
	}

	public int getCurrentMinute() {
		return m_currentMinute;
	}

	public void setCurrentMinute(int currentMinute) {
		m_currentMinute = currentMinute;
	}

	@Override
	public Action getDefaultAction() {
		return Action.GROUP;
	}

	public String getDefaultSqlThreshold() {
		return m_defaultSqlThreshold;
	}

	public void setDefaultSqlThreshold(String defaultSqlThreshold) {
		m_defaultSqlThreshold = defaultSqlThreshold;
	}

	public String getDefaultThreshold() {
		return m_defaultThreshold;
	}

	public void setDefaultThreshold(String defaultThreshold) {
		m_defaultThreshold = defaultThreshold;
	}

	public DetailStatistics getDetailStatistics() {
		return m_detailStatistics;
	}

	public void setDetailStatistics(DetailStatistics detailStatistics) {
		m_detailStatistics = detailStatistics;
	}

	public String getDistributionChart() {
		return m_distributionChart;
	}

	public void setDistributionChart(String distributionChart) {
		m_distributionChart = distributionChart;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
	}

	public String getErrorsTrend() {
		return m_errorsTrend;
	}

	public void setErrorsTrend(String errorsTrend) {
		m_errorsTrend = errorsTrend;
	}

	public List<String> getGroupIps() {
		return m_groupIps;
	}

	public void setGroupIps(List<String> groupIps) {
		m_groupIps = groupIps;
	}

	public GroupLevelInfo getGroupLevelInfo() {
		return m_groupLevelInfo;
	}

	public void setGroupLevelInfo(GroupLevelInfo groupLevelInfo) {
		m_groupLevelInfo = groupLevelInfo;
	}

	public String getGroupName() {
		return m_groupName;
	}

	public void setGroupName(String groupName) {
		m_groupName = groupName;
	}

	public List<String> getGroups() {
		return m_groups;
	}

	public void setGroups(List<String> groups) {
		m_groups = groups;
	}

	public int getHour() {
		return m_hour;
	}

	public void setHour(int hour) {
		m_hour = hour;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return SortHelper.sortIpAddress(m_report.getIps());
		}
	}

	public int getLastMinute() {
		return m_lastMinute;
	}

	public void setLastMinute(int lastMinute) {
		m_lastMinute = lastMinute;
	}

	public int getMinuteLast() {
		if (m_currentMinute == 0) {
			return 0;
		}
		return m_currentMinute - 1;
	}

	public int getMinuteNext() {
		if (m_currentMinute == 59) {
			return 59;
		}
		return m_currentMinute + 1;
	}

	public ProblemReport getReport() {
		return m_report;
	}

	public void setReport(ProblemReport report) {
		m_report = report;
	}

	public String getThreadId() {
		return m_threadId;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public ThreadLevelInfo getThreadLevelInfo() {
		return m_threadLevelInfo;
	}

	public void setThreadLevelInfo(ThreadLevelInfo threadLevelInfo) {
		m_threadLevelInfo = threadLevelInfo;
	}

}
