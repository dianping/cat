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
package com.dianping.cat.report.page.cross;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Date;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.cross.display.HostInfo;
import com.dianping.cat.report.page.cross.display.MethodInfo;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.HostinfoService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private CrossReportService m_reportService;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private HostinfoService m_hostinfoService;

	@Inject(type = ModelService.class, value = CrossAnalyzer.ID)
	private ModelService<CrossReport> m_service;

	private CrossReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
								.setProperty("ip", ipAddress);

		if (m_service.isEligable(request)) {
			ModelResponse<CrossReport> response = m_service.invoke(request);
			CrossReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable cross service registered for " + request + "!");
		}
	}

	private CrossReport getSummarizeReport(Payload payload) {
		String domain = payload.getDomain();

		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.queryReport(domain, start, end);
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = CrossAnalyzer.ID)
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = CrossAnalyzer.ID)
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);
		long historyTime = (payload.getHistoryEndDate().getTime() - payload.getHistoryStartDate().getTime()) / 1000;

		switch (payload.getAction()) {
		case HOURLY_PROJECT:
			CrossReport projectReport = getHourlyReport(payload);
			ProjectInfo projectInfo = new ProjectInfo(payload.getHourDuration());

			projectInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
									.setServiceSortBy(model.getServiceSort());
			projectInfo.visitCrossReport(projectReport);
			model.setProjectInfo(projectInfo);
			model.setReport(projectReport);
			break;
		case HOURLY_HOST:
			CrossReport hostReport = getHourlyReport(payload);
			HostInfo hostInfo = new HostInfo(payload.getHourDuration());

			hostInfo.setHostinfoService(m_hostinfoService);
			hostInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
									.setServiceSortBy(model.getServiceSort());
			hostInfo.setProjectName(payload.getProjectName());
			hostInfo.visitCrossReport(hostReport);
			model.setReport(hostReport);
			model.setHostInfo(hostInfo);
			break;
		case HOURLY_METHOD:
			CrossReport methodReport = getHourlyReport(payload);
			MethodInfo methodInfo = new MethodInfo(payload.getHourDuration());

			methodInfo.setHostinfoService(m_hostinfoService);
			methodInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
									.setServiceSortBy(model.getServiceSort()).setRemoteProject(payload.getProjectName());
			methodInfo.setRemoteIp(payload.getRemoteIp()).setQuery(model.getQueryName());
			methodInfo.visitCrossReport(methodReport);
			model.setReport(methodReport);
			model.setMethodInfo(methodInfo);
			break;
		case HISTORY_PROJECT:
			CrossReport historyProjectReport = getSummarizeReport(payload);
			ProjectInfo historyProjectInfo = new ProjectInfo(historyTime);

			historyProjectInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
									.setServiceSortBy(model.getServiceSort());
			historyProjectInfo.visitCrossReport(historyProjectReport);
			model.setProjectInfo(historyProjectInfo);
			model.setReport(historyProjectReport);
			break;
		case HISTORY_HOST:
			CrossReport historyHostReport = getSummarizeReport(payload);
			HostInfo historyHostInfo = new HostInfo(historyTime);

			historyHostInfo.setHostinfoService(m_hostinfoService);
			historyHostInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
									.setServiceSortBy(model.getServiceSort());
			historyHostInfo.setProjectName(payload.getProjectName());
			historyHostInfo.visitCrossReport(historyHostReport);
			model.setReport(historyHostReport);
			model.setHostInfo(historyHostInfo);
			break;
		case HISTORY_METHOD:
			CrossReport historyMethodReport = getSummarizeReport(payload);
			MethodInfo historyMethodInfo = new MethodInfo(historyTime);

			historyMethodInfo.setHostinfoService(m_hostinfoService);
			historyMethodInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
									.setServiceSortBy(model.getServiceSort()).setRemoteProject(payload.getProjectName());
			historyMethodInfo.setRemoteIp(payload.getRemoteIp()).setQuery(model.getQueryName());
			historyMethodInfo.visitCrossReport(historyMethodReport);
			model.setReport(historyMethodReport);
			model.setMethodInfo(historyMethodInfo);
			break;
		case METHOD_QUERY:
			String method = payload.getMethod();
			CrossMethodVisitor info = new CrossMethodVisitor(method);
			CrossReport queryReport = null;

			if (isHistory(payload)) {
				queryReport = getSummarizeReport(payload);
			} else {
				queryReport = getHourlyReport(payload);
			}
			info.visitCrossReport(queryReport);
			model.setReport(queryReport);
			model.setInfo(info.getInfo());
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private boolean isHistory(Payload payload) {
		String rawDate = payload.getRawDate();

		return rawDate != null && rawDate.length() == 8;
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.CROSS);
		model.setAction(payload.getAction());
		m_normalizePayload.normalize(model, payload);
		model.setCallSort(payload.getCallSort());
		model.setServiceSort(payload.getServiceSort());
		model.setQueryName(payload.getQueryName());

		if (StringUtils.isEmpty(payload.getProjectName())) {
			if (payload.getAction() == Action.HOURLY_HOST) {
				payload.setAction("view");
			}
			if (payload.getAction() == Action.HISTORY_HOST) {
				payload.setAction("history");
			}
		}
		if (StringUtils.isEmpty(payload.getRemoteIp())) {
			if (payload.getAction() == Action.HOURLY_METHOD) {
				payload.setAction("view");
			}
			if (payload.getAction() == Action.HISTORY_METHOD) {
				payload.setAction("history");
			}
		}
	}

}
