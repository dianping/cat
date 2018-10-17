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
package com.dianping.cat.report.page.event;

import java.util.ArrayList;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.event.transform.DistributionDetailVisitor.DistributionDetail;

@ModelMeta(EventAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private DisplayNames m_displayNameReport;

	private DisplayTypes m_displayTypeReport;

	private List<String> m_groups;

	private List<String> m_groupIps;

	private String m_failureTrend;

	private String m_graph1;

	private String m_graph2;

	private String m_graph3;

	private String m_graph4;

	private String m_hitTrend;

	private String m_mobileResponse;

	private String m_distributionChart;

	@EntityMeta
	private EventReport m_report;

	private String m_type;

	private String m_pieChart;

	private List<DistributionDetail> m_distributionDetails;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_REPORT;
	}

	public DisplayNames getDisplayNameReport() {
		return m_displayNameReport;
	}

	public void setDisplayNameReport(DisplayNames displayNameReport) {
		m_displayNameReport = displayNameReport;
	}

	public DisplayTypes getDisplayTypeReport() {
		return m_displayTypeReport;
	}

	public void setDisplayTypeReport(DisplayTypes displayTypeReport) {
		m_displayTypeReport = displayTypeReport;
	}

	public String getDistributionChart() {
		return m_distributionChart;
	}

	public void setDistributionChart(String distributionChart) {
		m_distributionChart = distributionChart;
	}

	public List<DistributionDetail> getDistributionDetails() {
		return m_distributionDetails;
	}

	public void setDistributionDetails(List<DistributionDetail> distributionDetails) {
		m_distributionDetails = distributionDetails;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
	}

	public String getFailureTrend() {
		return m_failureTrend;
	}

	public void setFailureTrend(String failureTrend) {
		m_failureTrend = failureTrend;
	}

	public String getGraph1() {
		return m_graph1;
	}

	public void setGraph1(String graph1) {
		m_graph1 = graph1;
	}

	public String getGraph2() {
		return m_graph2;
	}

	public void setGraph2(String graph2) {
		m_graph2 = graph2;
	}

	public String getGraph3() {
		return m_graph3;
	}

	public void setGraph3(String graph3) {
		m_graph3 = graph3;
	}

	public String getGraph4() {
		return m_graph4;
	}

	public void setGraph4(String graph4) {
		m_graph4 = graph4;
	}

	public List<String> getGroupIps() {
		return m_groupIps;
	}

	public void setGroupIps(List<String> groupIps) {
		m_groupIps = groupIps;
	}

	public List<String> getGroups() {
		return m_groups;
	}

	public void setGroups(List<String> groups) {
		m_groups = groups;
	}

	public String getHitTrend() {
		return m_hitTrend;
	}

	public void setHitTrend(String hitTrend) {
		m_hitTrend = hitTrend;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return SortHelper.sortIpAddress(m_report.getIps());
		}
	}

	public String getMobileResponse() {
		return m_mobileResponse;
	}

	public void setMobileResponse(String mobileResponse) {
		m_mobileResponse = mobileResponse;
	}

	public String getPieChart() {
		return m_pieChart;
	}

	public void setPieChart(String pieChart) {
		m_pieChart = pieChart;
	}

	public EventReport getReport() {
		return m_report;
	}

	public void setReport(EventReport report) {
		m_report = report;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

}
