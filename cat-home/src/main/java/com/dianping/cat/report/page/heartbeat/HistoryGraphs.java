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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;

public class HistoryGraphs {

	public static final int K = 1024;

	private static final int MINUTE_ONE_DAY = 1440;

	@Inject
	private HeartbeatReportService m_reportService;

	@Inject
	private HeartbeatDisplayPolicyManager m_manager;

	private Set<String> m_extensionMetrics = new HashSet<String>();

	private void addMachineDataToMap(Map<String, double[]> datas, Machine machine) {
		for (Period period : machine.getPeriods()) {
			int minute = period.getMinute();

			dealWithExtensions(datas, minute, period);
		}
		convertToDeltaArray(datas);
	}

	private Map<String, double[]> buildHeartbeatDatas(HeartbeatReport report, String ip) {
		m_extensionMetrics = new HashSet<String>();
		Map<String, double[]> datas = new HashMap<String, double[]>();
		Machine machine = report.findMachine(ip);

		if (machine != null) {
			addMachineDataToMap(datas, machine);
		}
		return datas;
	}

	private void convertToDeltaArray(Map<String, double[]> datas) {
		convertToDeltaArrayPerHour(datas, "TotalStartedThread");
		convertToDeltaArrayPerHour(datas, "StartedThread");
		convertToDeltaArrayPerHour(datas, "NewGcCount");
		convertToDeltaArrayPerHour(datas, "OldGcCount");
		convertToDeltaArrayPerHour(datas, "CatMessageSize");
		convertToDeltaArrayPerHour(datas, "CatMessageOverflow");
		for (String metric : m_extensionMetrics) {
			convertToDeltaArrayPerHour(datas, metric);
		}
	}

	private void convertToDeltaArrayPerHour(Map<String, double[]> datas, String metric) {
		double[] values = datas.get(metric);

		if (values != null) {
			double[] targets = new double[MINUTE_ONE_DAY];

			for (int i = 1; i < MINUTE_ONE_DAY; i++) {
				if (values[i - 1] > 0) {
					double delta = values[i] - values[i - 1];

					if (delta >= 0) {
						targets[i] = delta;
					}
				}
			}
			datas.put(metric, targets);
		}
	}

	private void dealWithExtensions(Map<String, double[]> datas, int minute, Period period) {
		for (String group : period.getExtensions().keySet()) {
			Extension currentExtension = period.findExtension(group);

			for (String metric : currentExtension.getDetails().keySet()) {
				m_extensionMetrics.add(metric);
				double value = currentExtension.findDetail(metric).getValue();
				int unit = m_manager.queryUnit(group, metric);
				double actualValue = value / unit;

				updateMetricArray(datas, minute, metric, actualValue);
			}
		}
	}

	private List<LineChart> getExtensionGraphs(List<String> metrics, Map<String, double[]> graphData, Date start,
							int size) {
		List<LineChart> graphs = new ArrayList<LineChart>();

		for (String metric : metrics) {
			graphs.add(getGraphItem(metric, metric, start, size, graphData));
		}
		return graphs;
	}

	private LineChart getGraphItem(String title, String key, Date start, int size, Map<String, double[]> graphData) {
		LineChart item = new LineChart();
		item.setStart(start);
		item.setSize(size);
		item.setTitle(title);
		item.addSubTitle(title);
		item.setStep(TimeHelper.ONE_MINUTE);
		double[] activeThread = graphData.get(key);
		item.addValue(activeThread);
		return item;
	}

	private Set<String> queryMetricNames(HeartbeatReport report, String groupName) {
		Set<String> result = new HashSet<String>();

		for (Machine machine : report.getMachines().values()) {
			for (Period period : machine.getPeriods()) {
				Extension extension = period.findExtension(groupName);

				if (extension != null) {
					result.addAll(extension.getDetails().keySet());
				}
			}
		}
		return result;
	}

	// show the graph of heartbeat
	public void showHeartBeatGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		int size = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_HOUR * 60);
		HeartbeatReport report = m_reportService.queryReport(payload.getDomain(), start, end);
		Map<String, double[]> graphData = buildHeartbeatDatas(report, payload.getIpAddress());

		String groupName = payload.getExtensionType();
		List<String> metrics = m_manager.sortMetricNames(groupName, queryMetricNames(report, groupName));
		List<LineChart> graphs = getExtensionGraphs(metrics, graphData, start, size);

		model.setExtensionCount(metrics.size());
		model.setExtensionHistoryGraphs(new JsonBuilder().toJson(graphs));
	}

	private void updateMetricArray(Map<String, double[]> datas, int minute, String metricName, double value) {
		double[] values = datas.get(metricName);

		if (values == null) {
			values = new double[MINUTE_ONE_DAY];

			datas.put(metricName, values);
		}
		values[minute] = value;
	}
}
