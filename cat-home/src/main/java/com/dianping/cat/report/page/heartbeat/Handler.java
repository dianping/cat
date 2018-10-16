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
package com.dianping.cat.report.page.heartbeat;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private GraphBuilder m_builder;

	@Inject
	private HistoryGraphs m_historyGraphs;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private HeartbeatReportService m_reportService;

	@Inject(type = ModelService.class, value = HeartbeatAnalyzer.ID)
	private ModelService<HeartbeatReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private HeartbeatDisplayPolicyManager m_manager;

	private void buildHeartbeatGraphInfo(Model model, HeartbeatSvgGraph displayHeartbeat) {
		if (displayHeartbeat == null) {
			return;
		}
		model.setResult(displayHeartbeat);
		model.setExtensionGraph(displayHeartbeat.getExtensionGraph());
	}

	private void buildHistoryGraph(Model model, Payload payload) {
		Date start = new Date(payload.getDate() + 23 * TimeHelper.ONE_HOUR);
		Date end = new Date(payload.getDate() + 24 * TimeHelper.ONE_HOUR);
		HeartbeatReport report = m_reportService.queryReport(payload.getDomain(), start, end);
		List<String> extensionGroups = m_manager.sortGroupNames(extractExtensionGroups(report));

		model.setExtensionGroups(extensionGroups);
		model.setReport(report);
		if (StringUtils.isEmpty(payload.getIpAddress()) || Constants.ALL.equals(payload.getIpAddress())) {
			String ipAddress = getIpAddress(report, payload);

			payload.setIpAddress(ipAddress);
			payload.setRealIp(ipAddress);
		}
		m_historyGraphs.showHeartBeatGraph(model, payload);
	}

	private Set<String> extractExtensionGroups(HeartbeatReport report) {
		Set<String> groupNames = new HashSet<String>();

		for (Machine machine : report.getMachines().values()) {
			for (Period period : machine.getPeriods()) {
				Set<String> tmpGroupNames = period.getExtensions().keySet();

				groupNames.addAll(tmpGroupNames);
			}
		}
		return groupNames;
	}

	private String getIpAddress(HeartbeatReport report, Payload payload) {
		Set<String> ips = report.getIps();
		String ip = payload.getRealIp();

		if ((ip == null || ip.length() == 0) && !ips.isEmpty()) {
			ip = SortHelper.sortIpAddress(ips).get(0);
		}
		return ip;
	}

	private HeartbeatReport getReport(String domain, String ipAddress, long date, ModelPeriod period) {
		ModelRequest request = new ModelRequest(domain, date) //
								.setProperty("ip", ipAddress);

		if (m_service.isEligable(request)) {
			ModelResponse<HeartbeatReport> response = m_service.invoke(request);
			HeartbeatReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable ip service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "h")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "h")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		HeartbeatSvgGraph heartbeat = null;

		normalize(model, payload);
		switch (payload.getAction()) {
		case VIEW:
			heartbeat = showReport(model, payload);
			buildHeartbeatGraphInfo(model, heartbeat);
			break;
		case HISTORY:
			buildHistoryGraph(model, payload);
			break;
		case PART_HISTORY:
			buildHistoryGraph(model, payload);
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		String ipAddress = payload.getIpAddress();

		model.setAction(payload.getAction());
		model.setPage(ReportPage.HEARTBEAT);
		if (StringUtils.isEmpty(ipAddress) || ipAddress.equals(Constants.ALL)) {
			model.setIpAddress(Constants.ALL);
		} else {
			payload.setRealIp(payload.getIpAddress());
			model.setIpAddress(payload.getRealIp());
		}
		m_normalizePayload.normalize(model, payload);

		String reportType = payload.getReportType();
		if ("month".equals(reportType) || "week".equals(reportType)) {
			payload.setReportType("day");
		}

		String queryType = payload.getType();

		if (queryType == null || queryType.trim().length() == 0) {
			payload.setType("frameworkThread");
		}
	}

	private HeartbeatSvgGraph showReport(Model model, Payload payload) {
		try {
			HeartbeatReport report = getReport(payload.getDomain(), payload.getIpAddress(), payload.getDate(),
									payload.getPeriod());
			model.setReport(report);
			if (report != null) {
				String displayIp = getIpAddress(report, payload);

				payload.setRealIp(displayIp);
				return new HeartbeatSvgGraph(m_builder, m_manager).display(report, displayIp);
			}
		} catch (Throwable e) {
			Cat.logError(e);
			model.setException(e);
		}
		return null;
	}

	// the detail order of heartbeat is:name min max sum sum2 count_in_minutes
	public enum DetailOrder {
		NAME,
		MIN,
		MAX,
		SUM,
		SUM2,
		COUNT_IN_MINUTES
	}

}
