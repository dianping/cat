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
package com.dianping.cat.report.page.state;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Date;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private StateReportService m_reportService;

	@Inject
	private StateGraphBuilder m_stateGraphs;

	@Inject
	private StateBuilder m_stateBuilder;

	@Inject(type = ModelService.class, value = StateAnalyzer.ID)
	private ModelService<StateReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	private void buildDisplayInfo(Model model, Payload payload, StateReport report) {
		StateDisplay display = new StateDisplay(payload.getIpAddress(), m_serverFilterConfigManager.getUnusedDomains());

		display.setSortType(payload.getSort());
		display.visitStateReport(report);
		model.setState(display);
		model.setReport(report);
	}

	public StateReport getHistoryReport(Payload payload) {
		String domain = Constants.CAT;
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.queryReport(domain, start, end);
	}

	private StateReport getHourlyReport(Payload payload) {
		// only for cat
		String domain = Constants.CAT;
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
								.setProperty("ip", payload.getIpAddress());

		if (m_service.isEligable(request)) {
			ModelResponse<StateReport> response = m_service.invoke(request);

			return response.getModel();
		} else {
			throw new RuntimeException("Internal error: no eligable sql service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = StateAnalyzer.ID)
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = StateAnalyzer.ID)
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(model, payload);
		String key = payload.getKey();
		StateReport report = null;
		Pair<LineChart, PieChart> pair = null;

		switch (action) {
		case HOURLY:
			report = getHourlyReport(payload);
			model.setMessage(m_stateBuilder.buildStateMessage(payload.getDate(), payload.getIpAddress()));
			buildDisplayInfo(model, payload, report);
			break;
		case HISTORY:
			report = getHistoryReport(payload);

			buildDisplayInfo(model, payload, report);
			break;
		case GRAPH:
			report = getHourlyReport(payload);
			pair = m_stateGraphs.buildGraph(payload, key, report);

			model.setGraph(new JsonBuilder().toJson(pair.getKey()));
			model.setPieChart(new JsonBuilder().toJson(pair.getValue()));
			break;
		case HISTORY_GRAPH:
			pair = m_stateGraphs.buildGraph(payload, key);

			model.setGraph(new JsonBuilder().toJson(pair.getKey()));
			model.setPieChart(new JsonBuilder().toJson(pair.getValue()));
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.STATE);
		model.setAction(payload.getAction());

		String ip = payload.getIpAddress();

		if (StringUtils.isEmpty(ip)) {
			payload.setIpAddress(Constants.ALL);
		}
		m_normalizePayload.normalize(model, payload);
	}

}
