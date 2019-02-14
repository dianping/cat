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
package com.dianping.cat.report.page.transaction;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.transaction.DisplayNames.TransactionNameModel;
import com.dianping.cat.report.page.transaction.GraphPayload.AverageTimePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.DurationPayload;
import com.dianping.cat.report.page.transaction.GraphPayload.FailurePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.HitPayload;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.DistributionDetailVisitor;
import com.dianping.cat.report.page.transaction.transform.PieGraphChartVisitor;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.page.transaction.transform.TransactionTrendGraphBuilder;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {

	@Inject
	private GraphBuilder m_builder;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private XmlViewer m_xmlViewer;

	@Inject
	private TransactionReportService m_reportService;

	@Inject
	private TransactionMergeHelper m_mergeHelper;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private DomainGroupConfigManager m_configManager;

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_service;

	private void buildDistributionInfo(Model model, String type, String name, TransactionReport report) {
		PieGraphChartVisitor chartVisitor = new PieGraphChartVisitor(type, name);
		DistributionDetailVisitor detailVisitor = new DistributionDetailVisitor(type, name);

		chartVisitor.visitTransactionReport(report);
		detailVisitor.visitTransactionReport(report);
		model.setDistributionChart(chartVisitor.getPieChart().getJsonString());
		model.setDistributionDetails(detailVisitor.getDetails());
	}

	private void buildTransactionMetaInfo(Model model, Payload payload, TransactionReport report) {
		String type = payload.getType();
		String sorted = payload.getSortBy();
		String queryName = payload.getQueryName();
		String ip = payload.getIpAddress();

		if (!StringUtils.isEmpty(type)) {
			DisplayNames displayNames = new DisplayNames();

			model.setDisplayNameReport(displayNames.display(sorted, type, ip, report, queryName));
			buildTransactionNamePieChart(displayNames.getResults(), model);
		} else {
			model.setDisplayTypeReport(new DisplayTypes().display(sorted, ip, report));
		}
	}

	private void buildTransactionNameGraph(Model model, TransactionReport report, String type, String name, String ip) {
		TransactionType t = report.findOrCreateMachine(ip).findOrCreateType(type);
		TransactionName transactionName = t.findOrCreateName(name);

		if (transactionName != null) {
			String graph1 = m_builder
									.build(new DurationPayload("Duration Distribution", "Duration (ms)", "Count",	transactionName));
			String graph2 = m_builder.build(new HitPayload("Hits Over Time", "Time (min)", "Count", transactionName));
			String graph3 = m_builder.build(
									new AverageTimePayload("Average Duration Over Time", "Time (min)",	"Average Duration (ms)", transactionName));
			String graph4 = m_builder.build(new FailurePayload("Failures Over Time", "Time (min)", "Count",	transactionName));

			model.setGraph1(graph1);
			model.setGraph2(graph2);
			model.setGraph3(graph3);
			model.setGraph4(graph4);
		}
	}

	private void buildTransactionNamePieChart(List<TransactionNameModel> names, Model model) {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();

		for (int i = 1; i < names.size(); i++) {
			TransactionNameModel name = names.get(i);
			Item item = new Item();
			TransactionName transaction = name.getDetail();
			item.setNumber(transaction.getTotalCount()).setTitle(transaction.getId());
			items.add(item);
		}

		chart.addItems(items);
		model.setPieChart(new JsonBuilder().toJson(chart));
	}

	private TransactionReport filterReportByGroup(TransactionReport report, String domain, String group) {
		List<String> ips = m_configManager.queryIpByDomainAndGroup(domain, group);
		List<String> removes = new ArrayList<String>();

		for (Machine machine : report.getMachines().values()) {
			String ip = machine.getIp();

			if (!ips.contains(ip)) {
				removes.add(ip);
			}
		}
		for (String ip : removes) {
			report.getMachines().remove(ip);
		}
		return report;
	}

	private TransactionReport getHourlyGraphReport(Model model, Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		String name = payload.getName();

		if (name == null || name.length() == 0) {
			name = "*";
		}

		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
								.setProperty("type", payload.getType()) //
								.setProperty("name", name)//
								.setProperty("ip", ipAddress);

		ModelResponse<TransactionReport> response = m_service.invoke(request);
		TransactionReport report = response.getModel();
		return report;
	}

	private TransactionReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		ModelRequest request = new ModelRequest(domain, payload.getDate()).setProperty("type", payload.getType())
								.setProperty("ip", ipAddress);

		if (m_service.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_service.invoke(request);
			TransactionReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "t")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "t")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Cat.logMetricForCount("http-request-transaction");
		
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);
		String domain = payload.getDomain();
		Action action = payload.getAction();
		String ipAddress = payload.getIpAddress();
		String group = payload.getGroup();
		String type = payload.getType();
		String name = payload.getName();
		String ip = payload.getIpAddress();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		if (StringUtils.isEmpty(group)) {
			group = m_configManager.queryDefaultGroup(domain);
			payload.setGroup(group);
		}
		model.setGroupIps(m_configManager.queryIpByDomainAndGroup(domain, group));
		model.setGroups(m_configManager.queryDomainGroup(payload.getDomain()));

		switch (action) {
		case HOURLY_REPORT:
			TransactionReport report = getHourlyReport(payload);
			report = m_mergeHelper.mergeAllMachines(report, ipAddress);

			if (report != null) {
				model.setReport(report);
				buildTransactionMetaInfo(model, payload, report);
			}
			break;
		case HISTORY_REPORT:
			report = m_reportService.queryReport(domain, payload.getHistoryStartDate(), payload.getHistoryEndDate());
			report = m_mergeHelper.mergeAllMachines(report, ipAddress);

			if (report != null) {
				model.setReport(report);
				buildTransactionMetaInfo(model, payload, report);
			}
			break;
		case HISTORY_GRAPH:
			report = m_reportService.queryReport(domain, start, end);

			if (Constants.ALL.equalsIgnoreCase(ip)) {
				buildDistributionInfo(model, type, name, report);
			}

			report = m_mergeHelper.mergeAllMachines(report, ip);
			new TransactionTrendGraphBuilder().buildTrendGraph(model, payload, report);
			break;
		case GRAPHS:
			report = getHourlyGraphReport(model, payload);

			if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
				buildDistributionInfo(model, type, name, report);
			}
			if (name == null || name.length() == 0) {
				name = Constants.ALL;
			}

			report = m_mergeHelper.mergeAllNames(report, ip, name);

			model.setReport(report);
			buildTransactionNameGraph(model, report, type, name, ip);
			break;
		case HOURLY_GROUP_REPORT:
			report = getHourlyReport(payload);
			report = filterReportByGroup(report, domain, group);
			report = m_mergeHelper.mergeAllMachines(report, ipAddress);

			if (report != null) {
				model.setReport(report);

				buildTransactionMetaInfo(model, payload, report);
			}
			break;
		case HISTORY_GROUP_REPORT:
			report = m_reportService.queryReport(domain, payload.getHistoryStartDate(), payload.getHistoryEndDate());
			report = filterReportByGroup(report, domain, group);
			report = m_mergeHelper.mergeAllMachines(report, ipAddress);

			if (report != null) {
				model.setReport(report);
				buildTransactionMetaInfo(model, payload, report);
			}
			break;
		case GROUP_GRAPHS:
			report = getHourlyGraphReport(model, payload);
			report = filterReportByGroup(report, domain, group);
			buildDistributionInfo(model, type, name, report);

			if (name == null || name.length() == 0) {
				name = Constants.ALL;
			}
			report = m_mergeHelper.mergeAllNames(report, ip, name);

			model.setReport(report);
			buildTransactionNameGraph(model, report, type, name, ip);
			break;
		case HISTORY_GROUP_GRAPH:
			report = m_reportService.queryReport(domain, start, end);
			report = filterReportByGroup(report, domain, group);

			buildDistributionInfo(model, type, name, report);

			report = m_mergeHelper.mergeAllMachines(report, ip);
			new TransactionTrendGraphBuilder().buildTrendGraph(model, payload, report);
			break;
		}

		if (payload.isXml()) {
			m_xmlViewer.view(ctx, model);
		} else {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Model model, Payload payload) {
		m_normalizePayload.normalize(model, payload);
		model.setPage(ReportPage.TRANSACTION);
		model.setAction(payload.getAction());

		if (StringUtils.isEmpty(payload.getType())) {
			payload.setType(null);
		}

		String queryName = payload.getQueryName();

		if (queryName != null) {
			model.setQueryName(queryName);
		} else {
			payload.setQueryName(null);
		}
	}

	public enum DetailOrder {
		TYPE,
		NAME,
		TOTAL_COUNT,
		FAILURE_COUNT,
		MIN,
		MAX,
		SUM,
		SUM2
	}

	public enum SummaryOrder {
		TYPE,
		TOTAL_COUNT,
		FAILURE_COUNT,
		MIN,
		MAX,
		SUM,
		SUM2
	}

}
