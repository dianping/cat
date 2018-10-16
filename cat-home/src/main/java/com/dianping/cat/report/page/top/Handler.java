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
package com.dianping.cat.report.page.top;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.page.dependency.ExternalInfoBuilder;
import com.dianping.cat.report.page.dependency.TopExceptionExclude;
import com.dianping.cat.report.page.dependency.TopMetric;
import com.dianping.cat.report.page.state.StateBuilder;
import com.dianping.cat.report.page.top.DomainInfo.Metric;
import com.dianping.cat.report.page.top.service.TopReportService;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ExternalInfoBuilder m_externalInfoBuilder;

	@Inject
	private StateBuilder m_stateBuilder;

	@Inject(type = ModelService.class, value = TopAnalyzer.ID)
	private ModelService<TopReport> m_topService;

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_transactionService;

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_problemService;

	@Inject
	private TopReportService m_topReportService;

	@Inject
	private TransactionMergeHelper m_mergeHelper;

	@Inject
	private ExceptionRuleConfigManager m_configManager;

	@Inject
	private JsonBuilder m_builder;

	private void buildExceptionDashboard(Model model, Payload payload, long date) {
		model.setReportStart(new Date(payload.getDate()));
		model.setReportEnd(new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1));

		int minuteCount = payload.getMinuteCounts();
		int minute = model.getMinute();
		TopReport report = queryTopReport(payload);

		List<String> excludeDomains = Arrays.asList(Constants.FRONT_END);
		TopMetric topMetric = new TopMetric(minuteCount, payload.getTopCounts(), m_configManager, excludeDomains);
		Date end = new Date(payload.getDate() + TimeHelper.ONE_MINUTE * minute);
		Date start = new Date(end.getTime() - TimeHelper.ONE_MINUTE * minuteCount);

		topMetric.setStart(start).setEnd(end);
		if (minuteCount > minute) {
			Payload lastPayload = new Payload();
			Date lastHour = new Date(payload.getDate() - TimeHelper.ONE_HOUR);

			lastPayload.setDate(new SimpleDateFormat("yyyyMMddHH").format(lastHour));

			TopReport lastReport = queryTopReport(lastPayload);

			topMetric.visitTopReport(lastReport);
			model.setLastTopReport(lastReport);
		}
		topMetric.visitTopReport(report);
		model.setTopReport(report);
		model.setTopMetric(topMetric);
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "top")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "top")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		model.setAction(action);
		model.setPage(ReportPage.TOP);
		normalize(model, payload);
		long date = payload.getDate();

		if (action == Action.HEALTH) {
			DomainInfo info = buildDomainInfo(payload, model);

			ctx.getHttpServletResponse().getWriter().write(m_builder.toJson(info));
		} else {
			buildExceptionDashboard(model, payload, date);
			model.setMessage(m_stateBuilder.buildStateMessage(payload.getDate(), payload.getIpAddress()));

			if (action == Action.VIEW) {
				if (!ctx.isProcessStopped()) {
					m_jspViewer.view(ctx, model);
				}
			} else if (action == Action.API) {
				ctx.getHttpServletResponse().getWriter().write(m_builder.toJson(model.getTopMetric()));
			}
		}
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.TOP);
		model.setAction(Action.VIEW);
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

	private DomainInfo buildDomainInfo(Payload payload, Model model) {
		long date = payload.getDate();
		int minute = model.getMinute();
		int exceptedMinute = payload.getMinuteCounts();
		DomainInfo info = new DomainInfo();

		if (minute < exceptedMinute) {
			buildTransactionInfo(payload, date - TimeHelper.ONE_HOUR, info);
			buildProblemInfo(payload, date - TimeHelper.ONE_HOUR, info);
		}

		buildTransactionInfo(payload, date, info);
		buildProblemInfo(payload, date, info);

		Map<String, Metric> metrics = info.getMetrics();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		long end = date + minute * TimeHelper.ONE_MINUTE;
		long start = end - exceptedMinute * TimeHelper.ONE_MINUTE;
		Set<String> removed = new HashSet<String>();

		for (Entry<String, Metric> entry : metrics.entrySet()) {
			String key = entry.getKey();
			try {
				long d = sdf.parse(key).getTime();

				if (d <= start || d > end) {
					removed.add(key);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		for (String s : removed) {
			metrics.remove(s);
		}
		return info;
	}

	private TopReport queryTopReport(Payload payload) {
		String domain = Constants.CAT;
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
								.setProperty("date", date);

		if (m_topService.isEligable(request)) {
			ModelResponse<TopReport> response = m_topService.invoke(request);
			TopReport report = response.getModel();

			if (report == null || report.getDomains().size() == 0) {
				report = m_topReportService
										.queryReport(domain, new Date(payload.getDate()),	new Date(payload.getDate() + TimeHelper.ONE_HOUR));
			}
			report.accept(new TopExceptionExclude(m_configManager));
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligible top service registered for " + request + "!");
		}
	}

	private void buildTransactionInfo(Payload payload, long date, DomainInfo info) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();

		if (StringUtils.isEmpty(ipAddress)) {
			ipAddress = Constants.ALL;
		}

		TransactionReport urlReport = queryTransactionReport(domain, ipAddress, date, "URL");
		TransactionReport serviceReport = queryTransactionReport(domain, ipAddress, date, "PigeonService");

		new TransactionReportVisitor(ipAddress, info, "URL").visitTransactionReport(urlReport);
		new TransactionReportVisitor(ipAddress, info, "PigeonService").visitTransactionReport(serviceReport);
	}

	private void buildProblemInfo(Payload payload, long date, DomainInfo info) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();

		if (StringUtils.isEmpty(ipAddress)) {
			ipAddress = Constants.ALL;
		}

		ProblemReport report = queryProblemReport(domain, ipAddress, date, "error");

		new ProblemReportVisitor(ipAddress, info, "error").visitProblemReport(report);
	}

	private TransactionReport queryTransactionReport(String domain, String ipAddress, long date, String type) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("type", type).setProperty("name", Constants.ALL)
								.setProperty("ip", ipAddress);

		if (m_transactionService.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_transactionService.invoke(request);
			TransactionReport report = response.getModel();

			report = m_mergeHelper.mergeAllMachines(report, ipAddress);
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligible transaction service registered for " + request + "!");
		}
	}

	private ProblemReport queryProblemReport(String domain, String ipAddress, long date, String type) {
		ModelRequest request = new ModelRequest(domain, date).setProperty("type", type).setProperty("queryType",	"detail");

		if (!Constants.ALL.equals(ipAddress)) {
			request.setProperty("ip", ipAddress);
		}
		if (m_problemService.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_problemService.invoke(request);
			ProblemReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligible problem service registered for " + request + "!");
		}
	}

}
