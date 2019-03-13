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

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.alarm.Alert;
import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.builder.StorageBuilderManager;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.home.dal.report.AlterationDao;
import com.dianping.cat.home.dal.report.AlterationEntity;
import com.dianping.cat.home.storage.alert.entity.Storage;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager.Department;
import com.dianping.cat.report.page.storage.display.StorageAlertInfoBuilder;
import com.dianping.cat.report.page.storage.display.StorageSorter;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.page.storage.transform.HourlyLineChartVisitor;
import com.dianping.cat.report.page.storage.transform.PieChartVisitor;
import com.dianping.cat.report.page.storage.transform.StorageMergeHelper;
import com.dianping.cat.report.page.storage.transform.StorageOperationFilter;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private StorageReportService m_reportService;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject(type = ModelService.class, value = StorageAnalyzer.ID)
	private ModelService<StorageReport> m_service;

	@Inject
	private StorageMergeHelper m_mergeHelper;

	@Inject
	private StorageGroupConfigManager m_storageGroupConfigManager;

	@Inject
	private JsonBuilder m_jsonBuilder;

	@Inject
	private AlterationDao m_alterationDao;

	@Inject
	private AlertService m_alertService;

	@Inject
	private StorageAlertInfoBuilder m_alertInfoBuilder;

	@Inject
	private StorageBuilderManager m_storageBuilderManager;

	private Map<String, Map<String, List<String>>> buildAlertLinks(Map<String, StorageAlertInfo> alertInfos, String type) {
		Map<String, Map<String, List<String>>> links = new LinkedHashMap<String, Map<String, List<String>>>();
		String format = m_storageGroupConfigManager.queryLinkFormat(type);

		if (format != null) {
			for (Entry<String, StorageAlertInfo> alertInfo : alertInfos.entrySet()) {
				String key = alertInfo.getKey();
				Map<String, List<String>> linkMap = links.get(key);

				if (linkMap == null) {
					linkMap = new LinkedHashMap<String, List<String>>();
					links.put(key, linkMap);
				}
				for (Entry<String, Storage> entry : alertInfo.getValue().getStorages().entrySet()) {
					String id = entry.getKey();
					Storage storage = entry.getValue();
					List<String> ls = linkMap.get(id);

					if (ls == null) {
						ls = new ArrayList<String>();
						linkMap.put(id, ls);
					}
					for (String ip : storage.getMachines().keySet()) {
						String url = m_storageGroupConfigManager.buildUrl(format, id, ip);

						if (url != null) {
							ls.add(url);
						}
					}
				}
			}
		}
		return links;
	}

	private List<Alteration> buildAlterations(Date start, Date end, String type) {
		List<Alteration> results = new LinkedList<Alteration>();

		try {
			List<Alteration> alterations = m_alterationDao.findByTypeDruation(start, end, type,	AlterationEntity.READSET_FULL);

			for (Alteration alteration : alterations) {
				results.add(alteration);
			}
		} catch (DalNotFoundException e) {
			// ignore it
		} catch (Exception e) {
			Cat.logError(e);
		}
		return results;
	}

	private void buildDepartments(Payload payload, Model model, StorageReport storageReport) {
		Map<String, Department> departments = m_storageGroupConfigManager
								.queryStorageDepartments(SortHelper.sortDomain(storageReport.getIds()), payload.getType());

		model.setDepartments(departments);
	}

	private void buildLineCharts(Model model, Payload payload, StorageReport storageReport) {
		HourlyLineChartVisitor visitor = new HourlyLineChartVisitor(payload.getIpAddress(), payload.getProject(),
								storageReport.getOps(), storageReport.getStartTime());

		visitor.visitStorageReport(storageReport);
		Map<String, LineChart> lineCharts = visitor.getLineChart();

		model.setCountTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.COUNT)));
		model.setAvgTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.AVG)));
		model.setErrorTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.ERROR)));
		model.setLongTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.LONG)));
	}

	private Pair<Boolean, Set<String>> buildOperations(Payload payload, Model model, Set<String> defaultValue) {
		String operations = payload.getOperations();
		Set<String> ops = new HashSet<String>();
		boolean filter = false;

		if (operations.length() > 0) {
			filter = true;
			String[] op = operations.split(";");

			for (int i = 0; i < op.length; i++) {
				ops.add(op[i]);
			}
		} else {
			ops.addAll(defaultValue);
		}
		return new Pair<Boolean, Set<String>>(filter, ops);
	}

	private String buildOperationStr(List<String> ops) {
		return StringUtils.join(ops, ";");
	}

	private void buildPieCharts(Model model, Payload payload, StorageReport report) {
		PieChartVisitor visitor = new PieChartVisitor();

		visitor.visitStorageReport(report);
		model.setDistributionChart(visitor.getPiechartJson());
	}

	private String buildReportId(Payload payload) {
		return payload.getId() + "-" + payload.getType();
	}

	private StorageReport filterReport(Payload payload, Model model, StorageReport storageReport) {
		if (storageReport != null) {
			Set<String> allOps = storageReport.getOps();
			model.setOperations(allOps);

			Pair<Boolean, Set<String>> pair = buildOperations(payload, model, allOps);
			if (pair.getKey()) {
				StorageOperationFilter filter = new StorageOperationFilter(pair.getValue());
				filter.visitStorageReport(storageReport);

				storageReport = filter.getStorageReport();
			}
		}
		return storageReport;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "storage")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "storage")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		normalize(model, payload);
		StorageReport storageReport = null;
		StorageReport rawReport = null;

		switch (payload.getAction()) {
		case HOURLY_STORAGE:
			storageReport = queryHourlyReport(payload);
			model.setOriginalReport(storageReport);

			rawReport = filterReport(payload, model, storageReport);
			storageReport = mergeReport(payload, rawReport);

			model.setReport(storageReport);
			buildDepartments(payload, model, storageReport);
			break;
		case HOURLY_STORAGE_GRAPH:
			storageReport = queryHourlyReport(payload);
			rawReport = filterReport(payload, model, storageReport);

			if (Constants.ALL.equals(payload.getIpAddress())) {
				buildPieCharts(model, payload, rawReport);
			}
			storageReport = mergeReport(payload, rawReport);

			model.setReport(storageReport);
			buildLineCharts(model, payload, storageReport);
			buildDepartments(payload, model, storageReport);
			break;
		case HISTORY_STORAGE:
			storageReport = queryHistoryReport(payload);
			model.setOriginalReport(storageReport);

			rawReport = filterReport(payload, model, storageReport);
			storageReport = mergeReport(payload, rawReport);

			model.setReport(storageReport);
			buildDepartments(payload, model, storageReport);
			break;
		case DASHBOARD:
			int minuteCounts = payload.getMinuteCounts();
			long time = payload.getDate();
			long end = time + model.getMinute() * TimeHelper.ONE_MINUTE;
			Date startDate = new Date(end - (minuteCounts - 1) * TimeHelper.ONE_MINUTE);
			Date endDate = new Date(end);
			String type = payload.getType();

			List<Alert> alerts = m_alertService.query(new Date(startDate.getTime() + TimeHelper.ONE_MINUTE),
									new Date(endDate.getTime() + TimeHelper.ONE_MINUTE), type);
			Map<String, StorageAlertInfo> alertInfos = m_alertInfoBuilder
									.buildStorageAlertInfos(startDate, endDate,	minuteCounts, type, alerts);
			alertInfos = sortAlertInfos(alertInfos);

			model.setLinks(buildAlertLinks(alertInfos, type));
			model.setAlertInfos(alertInfos);
			model.setReportStart(new Date(time));
			model.setReportEnd(new Date(time + TimeHelper.ONE_HOUR - 1));
			model.setAlterations(buildAlterations(startDate, endDate, type));
			break;
		}

		model.setPage(ReportPage.STORAGE);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private StorageReport mergeReport(Payload payload, StorageReport storageReport) {
		storageReport = m_mergeHelper.mergeReport(storageReport, payload.getIpAddress(), Constants.ALL);
		StorageSorter sorter = new StorageSorter(storageReport, payload.getSort());

		return sorter.getSortedReport();
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.STORAGE);
		model.setAction(payload.getAction());
		m_normalizePayload.normalize(model, payload);

		if (payload.getAction() == Action.DASHBOARD) {
			Integer minute = parseQueryMinute(payload);
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
		} else {
			if (payload.getOperations() == null) {
				List<String> defaultMethods = m_storageBuilderManager.getDefaultMethods(payload.getType());

				payload.setOperations(buildOperationStr(defaultMethods));
			}
		}
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

	public StorageReport queryHistoryReport(Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.queryReport(buildReportId(payload), start, end);
	}

	private StorageReport queryHourlyReport(Payload payload) {
		ModelRequest request = new ModelRequest(buildReportId(payload), payload.getDate())
								.setProperty("ip",	payload.getIpAddress());

		if (m_service.isEligable(request)) {
			ModelResponse<StorageReport> response = m_service.invoke(request);
			StorageReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	public Map<String, StorageAlertInfo> sortAlertInfos(Map<String, StorageAlertInfo> alertInfos) {
		Map<String, StorageAlertInfo> results = new LinkedHashMap<String, StorageAlertInfo>();

		for (Entry<String, StorageAlertInfo> entry : alertInfos.entrySet()) {
			StorageAlertInfo alertInfo = entry.getValue();
			List<Entry<String, Storage>> entries = new ArrayList<Entry<String, Storage>>(alertInfo.getStorages().entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String, Storage>>() {
				@Override
				public int compare(Map.Entry<String, Storage> o1, Map.Entry<String, Storage> o2) {
					int gap = o2.getValue().getLevel() - o1.getValue().getLevel();

					return gap == 0 ? o2.getValue().getCount() - o1.getValue().getCount() : gap;
				}
			});

			StorageAlertInfo result = m_alertInfoBuilder.makeAlertInfo(alertInfo.getId(), alertInfo.getStartTime());
			Map<String, Storage> storages = result.getStorages();

			for (Entry<String, Storage> storage : entries) {
				storages.put(storage.getKey(), storage.getValue());
			}
			results.put(entry.getKey(), result);
		}

		return SortHelper.sortMap(results, new MinuteComparator());
	}

	public static class MinuteComparator implements Comparator<Map.Entry<String, StorageAlertInfo>> {

		@Override
		public int compare(Map.Entry<String, StorageAlertInfo> o1, Map.Entry<String, StorageAlertInfo> o2) {
			String key1 = o1.getKey();
			String key2 = o2.getKey();
			String hour1 = key1.substring(0, 2);
			String hour2 = key2.substring(0, 2);

			if (!hour1.equals(hour2)) {
				int hour1Value = Integer.parseInt(hour1);
				int hour2Value = Integer.parseInt(hour2);

				if (hour1Value == 0 && hour2Value == 23) {
					return -1;
				} else if (hour1Value == 23 && hour2Value == 0) {
					return 1;
				} else {
					return hour2Value - hour1Value;
				}
			} else {
				String first = key1.substring(3, 5);
				String end = key2.substring(3, 5);

				return Integer.parseInt(end) - Integer.parseInt(first);
			}
		}
	}

	public static class StringCompartor implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			String hour1 = o1.substring(0, 2);
			String hour2 = o2.substring(0, 2);

			if (!hour1.equals(hour2)) {
				int hour1Value = Integer.parseInt(hour1);
				int hour2Value = Integer.parseInt(hour2);

				if (hour1Value == 0 && hour2Value == 23) {
					return -1;
				} else if (hour1Value == 23 && hour2Value == 0) {
					return 1;
				} else {
					return hour2Value - hour1Value;
				}
			} else {
				String first = o1.substring(3, 5);
				String end = o2.substring(3, 5);

				return Integer.parseInt(end) - Integer.parseInt(first);
			}
		}
	}

}
