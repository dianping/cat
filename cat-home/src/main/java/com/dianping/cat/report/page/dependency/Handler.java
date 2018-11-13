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

import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyReportMerger;
import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.dianping.cat.home.dependency.graph.transform.DefaultJsonBuilder;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.dependency.config.TopoGraphFormatConfigManager;
import com.dianping.cat.report.page.dependency.graph.LineGraphBuilder;
import com.dianping.cat.report.page.dependency.graph.ProductLinesDashboard;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class Handler implements PageHandler<Context> {

	public static final List<String> NORMAL_URLS = Arrays.asList("/cat/r", "/cat/r/", "/cat/r/dependency");

	@Inject(type = ModelService.class, value = DependencyAnalyzer.ID)
	private ModelService<DependencyReport> m_dependencyService;

	@Inject
	private TopologyGraphManager m_graphManager;

	@Inject
	private ExternalInfoBuilder m_externalInfoBuilder;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private TopoGraphFormatConfigManager m_formatConfigManager;

	private Segment buildAllSegmentsInfo(DependencyReport report) {
		Segment result = new Segment();
		Map<Integer, Segment> segments = report.getSegments();
		DependencyReportMerger merger = new DependencyReportMerger(null);

		for (Segment segment : segments.values()) {
			Map<String, Dependency> dependencies = segment.getDependencies();
			Map<String, Index> indexs = segment.getIndexs();

			for (Index index : indexs.values()) {
				Index temp = result.findOrCreateIndex(index.getName());
				merger.mergeIndex(temp, index);
			}
			for (Dependency dependency : dependencies.values()) {
				Dependency temp = result.findOrCreateDependency(dependency.getKey());

				merger.mergeDependency(temp, dependency);
			}
		}
		return result;
	}

	private void buildDependencyDashboard(Model model, Payload payload, Date reportTime) {
		ProductLinesDashboard dashboardGraph = m_graphManager.buildDependencyDashboard(reportTime.getTime());
		Map<String, List<TopologyNode>> nodes = dashboardGraph.getNodes();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
		String minute = String.valueOf(parseQueryMinute(payload));

		for (List<TopologyNode> n : nodes.values()) {
			for (TopologyNode node : n) {
				String domain = node.getId();
				String link = String.format("?op=dependencyGraph&minute=%s&domain=%s&date=%s", minute, domain,
										sdf.format(new Date(payload.getDate())));
				node.setLink(link);
			}
		}

		model.setReportStart(new Date(payload.getDate()));
		model.setReportEnd(new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1));
		model.setDashboardGraph(dashboardGraph.toJson());
		model.setDashboardGraphData(dashboardGraph);
		model.setFormat(m_formatConfigManager.buildFormatJson());
	}

	private void buildDependencyLineChart(Model model, Payload payload, Date reportTime) {
		DependencyReport dependencyReport = queryDependencyReport(payload);
		buildHourlyReport(dependencyReport, model, payload);
		buildHourlyLineGraph(dependencyReport, model);
	}

	private void buildHourlyLineGraph(DependencyReport report, Model model) {
		LineGraphBuilder builder = new LineGraphBuilder();

		builder.visitDependencyReport(report);

		List<LineChart> index = builder.queryIndex();
		Map<String, List<LineChart>> dependencys = builder.queryDependencyGraph();

		model.setIndexGraph(buildLineChartGraph(index));
		model.setDependencyGraph(buildLineChartGraphs(dependencys));
	}

	private void buildHourlyReport(DependencyReport report, Model model, Payload payload) {
		Segment segment = report.findSegment(model.getMinute());

		model.setReport(report);
		model.setSegment(segment);

		if (payload.isAll()) {
			model.setSegment(buildAllSegmentsInfo(report));
		}
	}

	private List<String> buildLineChartGraph(List<LineChart> charts) {
		List<String> result = new ArrayList<String>();

		for (LineChart temp : charts) {
			result.add(temp.getJsonString());
		}
		return result;
	}

	private Map<String, List<String>> buildLineChartGraphs(Map<String, List<LineChart>> charts) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();

		for (Entry<String, List<LineChart>> temp : charts.entrySet()) {
			result.put(temp.getKey(), buildLineChartGraph(temp.getValue()));
		}
		return result;
	}

	private void buildProjectTopology(Model model, Payload payload, Date reportTime) {
		TopologyGraph topologyGraph = m_graphManager.buildTopologyGraph(model.getDomain(), reportTime.getTime());
		DependencyReport report = queryDependencyReport(payload);

		buildHourlyReport(report, model, payload);
		m_externalInfoBuilder.buildExceptionInfoOnGraph(payload, model, topologyGraph);
		model.setReportStart(new Date(payload.getDate()));
		model.setReportEnd(new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1));
		String build = new DefaultJsonBuilder().build(topologyGraph);

		model.setTopologyGraph(build);
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = DependencyAnalyzer.ID)
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = DependencyAnalyzer.ID)
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		if (validate(ctx)) {
			Model model = new Model(ctx);
			Payload payload = ctx.getPayload();

			normalize(model, payload);

			Action action = payload.getAction();
			long date = payload.getDate();
			Date reportTime = new Date(date + TimeHelper.ONE_MINUTE * model.getMinute());

			switch (action) {
			case LINE_CHART:
				buildDependencyLineChart(model, payload, reportTime);
				break;
			case TOPOLOGY:
				buildProjectTopology(model, payload, reportTime);
				break;
			case DEPENDENCY_DASHBOARD:
				buildDependencyDashboard(model, payload, reportTime);
				break;
			}
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.DEPENDENCY);
		model.setAction(payload.getAction());

		m_normalizePayload.normalize(model, payload);

		int minute = parseQueryMinute(payload);
		int maxMinute = 60;
		List<Integer> minutes = new ArrayList<Integer>();

		if (payload.getPeriod().isCurrent()) {
			long current = payload.getCurrentTimeMillis() / 1000 / 60;
			maxMinute = (int) (current % (60));
		}
		for (int i = 0; i < 60; i++) {
			minutes.add(i);
		}
		model.setMinute(minute);
		model.setMaxMinute(maxMinute);
		model.setMinutes(minutes);
	}

	private int parseQueryMinute(Payload payload) {
		int minute = 0;
		String min = payload.getMinute();

		if (StringUtils.isEmpty(min)) {
			long current = payload.getCurrentTimeMillis() / 1000 / 60;
			minute = (int) (current % (60));
		} else {
			minute = Integer.parseInt(min);
		}

		return minute;
	}

	private DependencyReport queryDependencyReport(Payload payload) {
		String domain = payload.getDomain();
		ModelRequest request = new ModelRequest(domain, payload.getDate());

		if (m_dependencyService.isEligable(request)) {
			ModelResponse<DependencyReport> response = m_dependencyService.invoke(request);
			DependencyReport report = response.getModel();

			if (report != null && report.getStartTime() == null) {
				report.setStartTime(new Date(payload.getDate()));
				report.setStartTime(new Date(payload.getDate() + TimeHelper.ONE_HOUR));
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable dependency service registered for " + request + "!");
		}
	}

	private boolean validate(Context ctx) {
		String url = ctx.getRequestContext().getActionUri();
		String actionUrl = url.split("\\?")[0];

		return NORMAL_URLS.contains(actionUrl);
	}

}
