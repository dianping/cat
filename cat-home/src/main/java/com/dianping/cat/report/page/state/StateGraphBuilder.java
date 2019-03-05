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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.state.service.StateReportService;

@Named
public class StateGraphBuilder {

	@Inject
	private StateReportService m_reportService;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	public Pair<LineChart, PieChart> buildGraph(Payload payload, String key) {
		String domain = payload.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String ips = payload.getIpAddress();

		return buildHistoryGraph(domain, start, end, key, ips);
	}

	public Pair<LineChart, PieChart> buildGraph(Payload payload, String key, StateReport report) {
		String domain = payload.getDomain();
		String ips = payload.getIpAddress();

		return buildHourlyGraph(report, domain, key, ips);
	}

	private Pair<LineChart, PieChart> buildHistoryGraph(String domain, Date start, Date end, String key, String ip) {
		List<StateReport> reports = new ArrayList<StateReport>();
		StateHistoryGraphVisitor builder = new StateHistoryGraphVisitor(ip, start.getTime(), end.getTime(), key);
		StateDistirbutionVisitor visitor = new StateDistirbutionVisitor(key);
		long step;

		if (end.getTime() - start.getTime() <= TimeHelper.ONE_DAY) {
			step = TimeHelper.ONE_HOUR;
		} else {
			step = TimeHelper.ONE_DAY;
		}
		for (long date = start.getTime(); date < end.getTime(); date += step) {
			StateReport report = m_reportService.queryReport(domain, new Date(date), new Date(date + step));

			report.accept(builder);
			report.accept(visitor);
		}
		int size = reports.size();
		LineChart linechart = new LineChart();

		linechart.setStart(start).setSize(size).setTitle(key).setStep(step);
		linechart.addSubTitle(key);
		linechart.addValue(builder.getData());

		PieChart piechart = buildPiechart(visitor.getDistribute());

		return new Pair<LineChart, PieChart>(linechart, piechart);
	}

	private Pair<LineChart, PieChart> buildHourlyGraph(StateReport report, String domain, String key, String ip) {
		LineChart linechart = new LineChart();
		StateHourlyGraphVisitor builder = new StateHourlyGraphVisitor(ip, m_serverFilterConfigManager.getUnusedDomains(),	key,
								60);

		builder.visitStateReport(report);
		linechart.setStart(report.getStartTime()).setSize(60).setTitle(key).setStep(TimeHelper.ONE_MINUTE);
		linechart.add(key, builder.getData());

		StateDistirbutionVisitor visitor = new StateDistirbutionVisitor(key);

		visitor.visitStateReport(report);

		Map<String, Double> distributes = visitor.getDistribute();
		PieChart piechart = buildPiechart(distributes);

		return new Pair<LineChart, PieChart>(linechart, piechart);
	}

	private PieChart buildPiechart(Map<String, Double> distributes) {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();

		for (Entry<String, Double> entry : distributes.entrySet()) {
			Item item = new Item();

			item.setTitle(entry.getKey()).setNumber(entry.getValue());
			items.add(item);
		}

		chart.addItems(items);
		return chart;
	}
}
