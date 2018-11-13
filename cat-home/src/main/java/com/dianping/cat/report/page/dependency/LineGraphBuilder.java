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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.dependency.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.dependency.graph.GraphConstrant;

public class LineGraphBuilder extends BaseVisitor {

	private static final String TOTAL_COUNT = "TotalCount";

	private static final String ERROR_COUNT = "ErrorCount";

	private static final String AVG = "Avg";

	private static int SIZE = 60;

	public Map<String, Map<String, Item>> m_dependencies = new HashMap<String, Map<String, Item>>();

	private Set<String> m_types = new TreeSet<String>();

	private long m_period;

	private int m_currentMinute;

	private long m_sysMinute;

	private Date m_start;

	public LineGraphBuilder() {
		long current = System.currentTimeMillis();
		current -= current % Constants.HOUR;
		m_period = current;
		m_sysMinute = (System.currentTimeMillis()) / 1000 / 60 % 60;
	}

	private String appendStr(String... arg) {
		int length = arg.length;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++) {
			sb.append(arg[i]).append(GraphConstrant.DELIMITER);
		}
		return sb.toString().substring(0, sb.length() - 1);
	}

	private LineChart buildLineChart(String title, Map<String, Item> items) {
		LineChart result = new LineChart();

		result.setSize(SIZE);
		result.setStep(TimeHelper.ONE_MINUTE);
		result.setTitle(title);
		result.setStart(m_start);

		if (items != null) {
			for (Entry<String, Item> entry : items.entrySet()) {
				String subTitle = entry.getKey();
				Item item = entry.getValue();

				result.add(subTitle, item.getValue());
			}
		}
		return result;
	}

	public Item findOrCreateItem(String type, String id) {
		Map<String, Item> items = m_dependencies.get(type);

		if (items == null) {
			items = new HashMap<String, Item>();
			m_dependencies.put(type, items);
		}

		Item result = items.get(id);

		if (result == null) {
			result = generateItem();
			items.put(id, result);
		}

		return result;
	}

	private Item generateItem() {
		Item result = new Item();
		long size = (int) m_sysMinute + 1;

		if (!isCurrentPeriod()) {
			size = SIZE;
		}

		for (int i = 0; i < size; i++) {
			result.setValue(i, 0.0);
		}
		return result;
	}

	private boolean isCurrentPeriod() {
		return m_period == m_start.getTime();
	}

	public Map<String, List<LineChart>> queryDependencyGraph() {
		Map<String, List<LineChart>> allCharts = new HashMap<String, List<LineChart>>();
		for (String type : m_types) {
			List<LineChart> charts = new ArrayList<LineChart>();
			Map<String, Item> totalItems = m_dependencies.get(appendStr(type, TOTAL_COUNT));
			Map<String, Item> errorItems = m_dependencies.get(appendStr(type, ERROR_COUNT));
			Map<String, Item> avgItems = m_dependencies.get(appendStr(type, AVG));

			charts.add(buildLineChart(TOTAL_COUNT, totalItems));
			charts.add(buildLineChart(ERROR_COUNT, errorItems));
			charts.add(buildLineChart(AVG, avgItems));
			allCharts.put(type, charts);
		}
		return allCharts;
	}

	@Override
	public void visitDependency(Dependency dependency) {
		String type = dependency.getType();
		String target = dependency.getTarget();
		long count = dependency.getTotalCount();
		long error = dependency.getErrorCount();
		double avg = dependency.getAvg();

		m_types.add(type);
		findOrCreateItem(appendStr(type, TOTAL_COUNT), target).setValue(m_currentMinute, count);
		findOrCreateItem(appendStr(type, ERROR_COUNT), target).setValue(m_currentMinute, error);
		findOrCreateItem(appendStr(type, AVG), target).setValue(m_currentMinute, avg);
		super.visitDependency(dependency);
	}

	@Override
	public void visitDependencyReport(DependencyReport dependencyReport) {
		m_start = dependencyReport.getStartTime();
		super.visitDependencyReport(dependencyReport);
	}

	@Override
	public void visitIndex(Index index) {
		super.visitIndex(index);
	}

	@Override
	public void visitSegment(Segment segment) {
		m_currentMinute = segment.getId();
		super.visitSegment(segment);
	}

	public class Item {
		private Double[] m_values = new Double[60];

		public Double[] getValue() {
			return m_values;
		}

		private Item setValue(int minute, double value) {
			m_values[minute] = value;
			return this;
		}
	}

}
