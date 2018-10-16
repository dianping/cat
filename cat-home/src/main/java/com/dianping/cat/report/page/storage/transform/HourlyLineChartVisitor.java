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
package com.dianping.cat.report.page.storage.transform;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.storage.StorageConstants;

public class HourlyLineChartVisitor extends BaseVisitor {

	private static final int SIZE = 60;

	private String m_ip;

	private Map<String, LineChart> m_lineCharts = new LinkedHashMap<String, LineChart>();

	private Date m_start;

	private Map<String, LineChartData> m_datas = new LinkedHashMap<String, LineChartData>();

	private String m_domain;

	private String m_currentOperation;

	public HourlyLineChartVisitor(String ip, String domain, Set<String> operations, Date start) {
		m_ip = ip;
		m_domain = domain;

	}

	private Map<Integer, Double> buildAvgData(Map<Integer, Double> counts, Map<Integer, Double> sums) {
		Map<Integer, Double> values = new LinkedHashMap<Integer, Double>();

		for (Entry<Integer, Double> entry : counts.entrySet()) {
			int minute = entry.getKey();
			Double count = counts.get(minute);
			Double sum = sums.get(minute);

			if (count != null && count > 0 && sum != null && sum > 0) {
				values.put(minute, sum / count);
			}
		}
		return values;
	}

	private void buildLineChart(long size, String key, String title, Map<Integer, Double> values) {
		Double[] value = new Double[SIZE];

		for (int i = 0; i < size; i++) {
			value[i] = 0.0;
		}

		for (int i = 0; i < SIZE; i++) {
			Double temp = values.get(i);

			if (temp != null) {
				value[i] = temp;
			}
		}
		m_lineCharts.get(title).add(key, value);
	}

	public Map<String, LineChart> getLineChart() {
		long minute = (System.currentTimeMillis()) / 1000 / 60 % 60;
		long current = System.currentTimeMillis();
		current -= current % Constants.HOUR;
		long size = (int) minute + 1;

		if (m_start.getTime() < current) {
			size = SIZE;
		}

		for (Entry<String, LineChartData> entry : m_datas.entrySet()) {
			String key = entry.getKey();
			LineChartData data = entry.getValue();

			buildLineChart(size, key, StorageConstants.COUNT, data.getCounts());
			buildLineChart(size, key, StorageConstants.AVG, buildAvgData(data.getCounts(), data.getSums()));
			buildLineChart(size, key, StorageConstants.ERROR, data.getErrors());
			buildLineChart(size, key, StorageConstants.LONG, data.getLongs());
		}
		return m_lineCharts;
	}

	@Override
	public void visitDomain(Domain domain) {
		if (StringUtils.isEmpty(m_domain) || m_domain.equals(domain.getId())) {
			super.visitDomain(domain);
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		if (StringUtils.isEmpty(m_ip) || m_ip.equals(machine.getId())) {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitOperation(Operation operation) {
		m_currentOperation = operation.getId();
		super.visitOperation(operation);
	}

	@Override
	public void visitSegment(Segment segment) {
		int minute = segment.getId();
		LineChartData data = m_datas.get(m_currentOperation);

		data.incCounts(minute, segment.getCount());
		data.incSums(minute, segment.getSum());
		data.incErrors(minute, segment.getError());
		data.incLongs(minute, segment.getLongCount());
	}

	@Override
	public void visitStorageReport(StorageReport storageReport) {
		m_start = storageReport.getStartTime();

		for (String title : StorageConstants.TITLES) {
			LineChart linechart = new LineChart();

			linechart.setSize(SIZE);
			linechart.setStep(TimeHelper.ONE_MINUTE);
			linechart.setStart(m_start);
			m_lineCharts.put(title, linechart);
		}

		for (String operation : storageReport.getOps()) {
			LineChartData data = new LineChartData(operation);

			m_datas.put(operation, data);
		}
		super.visitStorageReport(storageReport);
	}

	public static class LineChartData {

		private String m_operation;

		private Map<Integer, Double> m_counts = new LinkedHashMap<Integer, Double>();

		private Map<Integer, Double> m_errors = new LinkedHashMap<Integer, Double>();

		private Map<Integer, Double> m_longs = new LinkedHashMap<Integer, Double>();

		private Map<Integer, Double> m_sums = new LinkedHashMap<Integer, Double>();

		public LineChartData(String operation) {
			m_operation = operation;
		}

		public Map<Integer, Double> getCounts() {
			return m_counts;
		}

		public Map<Integer, Double> getErrors() {
			return m_errors;
		}

		public Map<Integer, Double> getLongs() {
			return m_longs;
		}

		public String getOperation() {
			return m_operation;
		}

		public Map<Integer, Double> getSums() {
			return m_sums;
		}

		public void incCounts(int min, long value) {
			Double data = m_counts.get(min);

			if (data == null) {
				m_counts.put(min, value + 0.0);
			} else {
				m_counts.put(min, data + value);
			}
		}

		public void incErrors(int min, long value) {
			Double data = m_errors.get(min);

			if (data == null) {
				m_errors.put(min, value + 0.0);
			} else {
				m_errors.put(min, data + value);
			}
		}

		public void incLongs(int min, long value) {
			Double data = m_longs.get(min);

			if (data == null) {
				m_longs.put(min, value + 0.0);
			} else {
				m_longs.put(min, data + value);
			}
		}

		public void incSums(int min, double value) {
			Double data = m_sums.get(min);

			if (data == null) {
				m_sums.put(min, value + 0.0);
			} else {
				m_sums.put(min, data + value);
			}
		}
	}

}
