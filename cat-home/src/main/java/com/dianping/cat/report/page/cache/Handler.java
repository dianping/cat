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
package com.dianping.cat.report.page.cache;

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

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.cache.CacheReport.CacheNameItem;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.AllMachineMerger;
import com.dianping.cat.report.page.transaction.transform.AllNameMerger;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {

	@Inject(type = ModelService.class, value = EventAnalyzer.ID)
	private ModelService<EventReport> m_eventService;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private TransactionReportService m_transactionReportService;

	@Inject
	private EventReportService m_eventReportService;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_transactionService;

	private CacheReport buildCacheReport(TransactionReport transactionReport, EventReport eventReport, Payload payload) {
		String type = payload.getType();
		String queryName = payload.getQueryName();
		String ip = payload.getIpAddress();
		String sortBy = payload.getSortBy();
		TransactionReportVistor vistor = new TransactionReportVistor();

		vistor.setType(type).setQueryName(queryName).setSortBy(sortBy).setCurrentIp(ip);
		vistor.setEventReport(eventReport);
		vistor.visitTransactionReport(transactionReport);
		return vistor.getCacheReport();
	}

	private String buildPieChart(CacheReport report) {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();
		List<CacheNameItem> nameItems = report.getNameItems();

		for (CacheNameItem cacheItem : nameItems) {
			String name = cacheItem.getName().getId();

			if (name.endsWith(":get") || name.endsWith(":mGet")) {
				items.add(new Item().setTitle(name).setNumber(cacheItem.getName().getTotalCount()));
			}
		}
		chart.addItems(items);
		return chart.getJsonString();
	}

	private EventReport getHistoryEventReport(Payload payload) {
		String domain = payload.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		EventReport report = m_eventReportService.queryReport(domain, start, end);

		if (Constants.ALL.equalsIgnoreCase(payload.getIpAddress())) {
			com.dianping.cat.report.page.event.transform.AllMachineMerger allEvent = new com.dianping.cat.report.page.event.transform.AllMachineMerger();

			allEvent.visitEventReport(report);
			report = allEvent.getReport();
		}
		if (Constants.ALL.equalsIgnoreCase(payload.getType())) {
			com.dianping.cat.report.page.event.transform.AllNameMerger allEvent = new com.dianping.cat.report.page.event.transform.AllNameMerger();

			allEvent.visitEventReport(report);
			report = allEvent.getReport();
		}
		return report;
	}

	private TransactionReport getHistoryTransactionReport(Payload payload) {
		String domain = payload.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		TransactionReport report = m_transactionReportService.queryReport(domain, start, end);

		if (Constants.ALL.equalsIgnoreCase(payload.getIpAddress())) {
			AllMachineMerger all = new AllMachineMerger();

			all.visitTransactionReport(report);
			report = all.getReport();
		}
		if (Constants.ALL.equalsIgnoreCase(payload.getType())) {
			AllNameMerger all = new AllNameMerger();

			all.visitTransactionReport(report);
			report = all.getReport();
		}
		return report;
	}

	private EventReport getHourlyEventReport(Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		String type = payload.getType();
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
								.setProperty("ip", ipAddress);
		EventReport eventReport = null;

		if (StringUtils.isEmpty(type)) {
			ModelResponse<EventReport> response = m_eventService.invoke(request);

			eventReport = response.getModel();
		} else {
			request.setProperty("type", type);
			ModelResponse<EventReport> response = m_eventService.invoke(request);

			eventReport = response.getModel();
		}
		if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
			com.dianping.cat.report.page.event.transform.AllMachineMerger allEvent = new com.dianping.cat.report.page.event.transform.AllMachineMerger();

			allEvent.visitEventReport(eventReport);
			eventReport = allEvent.getReport();
		}
		if (Constants.ALL.equalsIgnoreCase(type)) {
			com.dianping.cat.report.page.event.transform.AllNameMerger allEvent = new com.dianping.cat.report.page.event.transform.AllNameMerger();

			allEvent.visitEventReport(eventReport);
			eventReport = allEvent.getReport();
		}

		return eventReport;
	}

	private TransactionReport getHourlyTransactionReport(Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		String type = payload.getType();
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
								.setProperty("ip", ipAddress);
		TransactionReport transactionReport = null;

		if (StringUtils.isNotEmpty(type)) {
			request.setProperty("type", type);
		}

		ModelResponse<TransactionReport> response = m_transactionService.invoke(request);

		transactionReport = response.getModel();

		if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
			AllMachineMerger all = new AllMachineMerger();

			all.visitTransactionReport(transactionReport);
			transactionReport = all.getReport();
		}
		if (Constants.ALL.equalsIgnoreCase(type)) {
			AllNameMerger all = new AllNameMerger();

			all.visitTransactionReport(transactionReport);
			transactionReport = all.getReport();
		}
		return transactionReport;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "cache")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "cache")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		String type = payload.getType();
		TransactionReport transactionReport = null;
		EventReport eventReport = null;

		normalize(model, payload);
		switch (payload.getAction()) {
		case HOURLY_REPORT:
			transactionReport = getHourlyTransactionReport(payload);
			eventReport = getHourlyEventReport(payload);
			break;
		case HISTORY_REPORT:
			transactionReport = getHistoryTransactionReport(payload);
			eventReport = getHistoryEventReport(payload);
			break;
		}

		if (transactionReport != null && eventReport != null) {
			CacheReport cacheReport = buildCacheReport(transactionReport, eventReport, payload);

			model.setReport(cacheReport);
			if (!StringUtils.isEmpty(type)) {
				model.setPieChart(buildPieChart(model.getReport()));
			}
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		m_normalizePayload.normalize(model, payload);
		model.setAction(payload.getAction());
		model.setPage(ReportPage.CACHE);
		model.setQueryName(payload.getQueryName());
	}

}
