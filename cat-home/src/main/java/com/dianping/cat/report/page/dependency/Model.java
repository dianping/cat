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
package com.dianping.cat.report.page.dependency;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.dependency.graph.ProductLinesDashboard;

@ModelMeta(DependencyAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	public String m_message;

	@EntityMeta
	private DependencyReport m_report;

	@EntityMeta
	private List<LineChart> m_lineCharts;

	private Segment m_segment;

	private int m_minute;

	private List<Integer> m_minutes;

	private int m_maxMinute;

	private String m_topologyGraph;

	private List<String> m_indexGraph;

	private Map<String, List<String>> m_dependencyGraph;

	private String m_dashboardGraph;

	private ProductLinesDashboard m_dashboardGraphData;

	private String m_productLineGraph;

	private Date m_reportStart;

	private Date m_reportEnd;

	private String m_format;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getDashboardGraph() {
		return m_dashboardGraph;
	}

	public void setDashboardGraph(String dashboardGraph) {
		m_dashboardGraph = dashboardGraph;
	}

	public ProductLinesDashboard getDashboardGraphData() {
		return m_dashboardGraphData;
	}

	public void setDashboardGraphData(ProductLinesDashboard dashboardGraphData) {
		m_dashboardGraphData = dashboardGraphData;
	}

	@Override
	public Action getDefaultAction() {
		return Action.LINE_CHART;
	}

	public Map<String, List<String>> getDependencyGraph() {
		return m_dependencyGraph;
	}

	public void setDependencyGraph(Map<String, List<String>> dependencyGraph) {
		m_dependencyGraph = dependencyGraph;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	public String getFormat() {
		return m_format;
	}

	public void setFormat(String format) {
		m_format = format;
	}

	public List<String> getIndexGraph() {
		return m_indexGraph;
	}

	public void setIndexGraph(List<String> indexGraph) {
		m_indexGraph = indexGraph;
	}

	public List<LineChart> getLineCharts() {
		return m_lineCharts;
	}

	public void setLineCharts(List<LineChart> lineCharts) {
		m_lineCharts = lineCharts;
	}

	public int getMaxMinute() {
		return m_maxMinute;
	}

	public void setMaxMinute(int maxMinute) {
		m_maxMinute = maxMinute;
	}

	public String getMessage() {
		return m_message;
	}

	public void setMessage(String message) {
		m_message = message;
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

	public String getProductLineGraph() {
		return m_productLineGraph;
	}

	public void setProductLineGraph(String productLineGraph) {
		m_productLineGraph = productLineGraph;
	}

	public DependencyReport getReport() {
		return m_report;
	}

	public void setReport(DependencyReport report) {
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

	public Segment getSegment() {
		return m_segment;
	}

	public void setSegment(Segment segment) {
		m_segment = segment;
	}

	public String getTopologyGraph() {
		return m_topologyGraph;
	}

	public void setTopologyGraph(String topologyGraph) {
		m_topologyGraph = topologyGraph;
	}

}
