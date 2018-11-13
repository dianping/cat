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
package com.dianping.cat.report.page.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager.Department;

@ModelMeta(StorageAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private StorageReport m_originalReport;

	private StorageReport m_report;

	private Set<String> m_operations = new HashSet<String>();

	private String m_countTrend;

	private String m_avgTrend;

	private String m_errorTrend;

	private String m_longTrend;

	private int m_minute;

	private List<Integer> m_minutes;

	private int m_maxMinute;

	private Date m_reportStart;

	private Date m_reportEnd;

	private Map<String, StorageAlertInfo> m_alertInfos;

	private Map<String, Department> m_departments;

	private Map<String, Map<String, List<String>>> m_links;

	private List<Alteration> m_alterations;

	private String m_distributionChart;

	public Model(Context ctx) {
		super(ctx);
	}

	public Map<String, StorageAlertInfo> getAlertInfos() {
		return m_alertInfos;
	}

	public void setAlertInfos(Map<String, StorageAlertInfo> alertInfos) {
		m_alertInfos = alertInfos;
	}

	public List<Alteration> getAlterations() {
		return m_alterations;
	}

	public void setAlterations(List<Alteration> alterations) {
		m_alterations = alterations;
	}

	public String getAvgTrend() {
		return m_avgTrend;
	}

	public void setAvgTrend(String avgTrend) {
		m_avgTrend = avgTrend;
	}

	public String getCountTrend() {
		return m_countTrend;
	}

	public void setCountTrend(String countTrend) {
		m_countTrend = countTrend;
	}

	public List<String> getCurrentOperations() {
		if (m_report != null) {
			ArrayList<String> ops = new ArrayList<String>(m_report.getOps());

			Collections.sort(ops);
			return ops;
		} else {
			return new ArrayList<String>();
		}
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_STORAGE;
	}

	public Map<String, Department> getDepartments() {
		return m_departments;
	}

	public void setDepartments(Map<String, Department> departments) {
		m_departments = departments;
	}

	public String getDistributionChart() {
		return m_distributionChart;
	}

	public void setDistributionChart(String distributionChart) {
		m_distributionChart = distributionChart;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new HashSet<String>();
	}

	public String getErrorTrend() {
		return m_errorTrend;
	}

	public void setErrorTrend(String errorTrend) {
		m_errorTrend = errorTrend;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return SortHelper.sortIpAddress(m_report.getIps());
		}
	}

	public Map<String, Map<String, List<String>>> getLinks() {
		return m_links;
	}

	public void setLinks(Map<String, Map<String, List<String>>> links) {
		m_links = links;
	}

	public String getLongTrend() {
		return m_longTrend;
	}

	public void setLongTrend(String longTrend) {
		m_longTrend = longTrend;
	}

	public Machine getMachine() {
		Machine machine = new Machine();

		if (m_report != null) {
			Machine m = m_report.getMachines().get(getIpAddress());

			if (m != null) {
				machine = m;
			}
		}
		return machine;
	}

	public int getMaxMinute() {
		return m_maxMinute;
	}

	public void setMaxMinute(int maxMinute) {
		m_maxMinute = maxMinute;
	}

	public int getMinute() {
		return m_minute;
	}

	public void setMinute(int minute) {
		m_minute = minute;
	}

	public List<Integer> getMinutes() {
		return m_minutes;
	}

	public void setMinutes(List<Integer> minutes) {
		m_minutes = minutes;
	}

	public List<String> getOperations() {
		ArrayList<String> operations = new ArrayList<String>(m_operations);

		Collections.sort(operations);
		return operations;
	}

	public void setOperations(Set<String> operations) {
		m_operations = operations;
	}

	public StorageReport getOriginalReport() {
		return m_originalReport;
	}

	public void setOriginalReport(StorageReport originalReport) {
		m_originalReport = originalReport;
	}

	public StorageReport getReport() {
		return m_report;
	}

	public void setReport(StorageReport report) {
		m_report = report;
	}

	public Date getReportEnd() {
		return m_reportEnd;
	}

	public void setReportEnd(Date reportEnd) {
		m_reportEnd = reportEnd;
	}

	public Date getReportStart() {
		return m_reportStart;
	}

	public void setReportStart(Date reportStart) {
		m_reportStart = reportStart;
	}

}
