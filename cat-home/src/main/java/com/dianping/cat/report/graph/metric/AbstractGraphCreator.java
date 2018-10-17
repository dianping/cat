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
package com.dianping.cat.report.graph.metric;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.metric.service.BaselineService;

public abstract class AbstractGraphCreator implements LogEnabled {
	@Inject
	protected BaselineService m_baselineService;

	@Inject
	protected DataExtractor m_dataExtractor;

	@Inject
	protected AlertManager m_alertManager;

	protected int m_lastMinute = 6;

	protected int m_extraTime = 1;

	protected Logger m_logger;

	protected void addLastMinuteData(Map<Long, Double> current, Map<Long, Double> all, int minute, Date end) {
		int step = m_dataExtractor.getStep();

		if (step == 1) {
			return;
		}
		long endTime = 0;
		long currentTime = System.currentTimeMillis();
		if (end.getTime() > currentTime) {
			endTime = currentTime - currentTime % TimeHelper.ONE_MINUTE - m_extraTime * TimeHelper.ONE_MINUTE;
		} else {
			endTime = end.getTime();
		}
		long start = endTime - minute * TimeHelper.ONE_MINUTE;
		Set<Long> sets = new HashSet<Long>();

		for (Entry<Long, Double> entry : current.entrySet()) {
			if (entry.getKey() >= start) {
				sets.add(entry.getKey());
			}
		}
		for (Long temp : sets) {
			current.remove(temp);
		}

		for (int i = minute; i > 0; i--) {
			long time = endTime - i * TimeHelper.ONE_MINUTE;
			Double value = all.get(time);

			if (value != null) {
				current.put(time, value);
			}
		}
	}

	public Map<Long, Double> buildNoneData(Date startDate, Date endDate, int step) {
		int n = 0;
		long current = System.currentTimeMillis();

		if (endDate.getTime() > current) {
			n = (int) ((current - startDate.getTime()) / 60000.0);
		} else {
			n = (int) ((endDate.getTime() - startDate.getTime()) / 60000.0);
		}

		double[] noneData = new double[n];
		Map<Long, Double> currentData = convertToMap(noneData, startDate, step);

		return currentData;
	}

	protected String buildUnit(String chartTitle) {
		if (isFlowMetric(chartTitle)) {
			return "流量(MB/秒)";
		} else {
			return "value/分钟";
		}
	}

	protected double[] convert(double[] value, int removeLength) {
		int length = value.length;
		int newLength = length - removeLength;
		double[] result = new double[newLength];

		for (int i = 0; i < newLength; i++) {
			result[i] = value[i];
		}
		return result;
	}

	protected void convertFlowMetric(LineChart lineChart, Map<Long, Double> current, String key) {
		if (isFlowMetric(lineChart.getId())) {
			Map<Long, Double> convertedData = new LinkedHashMap<Long, Double>();

			for (Entry<Long, Double> currentEntry : current.entrySet()) {
				double result = currentEntry.getValue() / (1024 * 1024) / 60 / 8;

				convertedData.put(currentEntry.getKey(), result);
			}
			lineChart.add(key, convertedData);
		} else {
			lineChart.add(key, current);
		}
	}

	protected Map<Long, Double> convertToMap(double[] data, Date start, int step) {
		Map<Long, Double> map = new LinkedHashMap<Long, Double>();
		int length = data.length;
		long startTime = start.getTime();

		for (int i = 0; i < length; i++) {
			map.put(startTime + step * i * TimeHelper.ONE_MINUTE, data[i]);
		}
		return map;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private boolean isCurrentMode(Date date) {
		Date current = TimeHelper.getCurrentHour();

		return current.getTime() == date.getTime() - TimeHelper.ONE_HOUR;
	}

	protected boolean isFlowMetric(String title) {
		if (title.toLowerCase().contains("flow")) {
			return true;
		} else {
			return false;
		}
	}

	protected void mergeMap(Map<String, double[]> all, Map<String, double[]> item, int size, int index) {
		for (Entry<String, double[]> entry : item.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			double[] result = all.get(key);

			if (result == null) {
				result = new double[size];
				all.put(key, result);
			}
			if (value != null) {
				int length = value.length;
				int pos = index * 60;
				for (int i = 0; i < length && pos < size; i++, pos++) {
					result[pos] = value[i];
				}
			}
		}
	}

	protected void put(Map<String, LineChart> charts, Map<String, LineChart> result, String key) {
		LineChart value = charts.get(key);

		if (value != null) {
			result.put(key, charts.get(key));
		}
	}

	protected void putKey(Map<String, double[]> datas, Map<String, double[]> values, String key) {
		double[] value = datas.get(key);

		if (value == null) {
			value = new double[60];
		}
		values.put(key, value);
	}

	protected double[] queryBaseline(String name, String key, Date start, Date end) {
		int size = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_MINUTE);
		double[] result = new double[size];
		int index = 0;
		long startLong = start.getTime();
		long endLong = end.getTime();

		for (; startLong < endLong; startLong += TimeHelper.ONE_HOUR) {
			double[] values = m_baselineService.queryHourlyBaseline(name, key, new Date(startLong));

			if (values != null) {
				for (int j = 0; j < values.length; j++) {
					result[index * 60 + j] = values[j];
				}
			}
			index++;
		}
		return result;
	}

	public Map<String, double[]> removeFutureData(Date endDate, final Map<String, double[]> allCurrentValues) {
		if (isCurrentMode(endDate)) {
			// remove the minute of future
			Map<String, double[]> newCurrentValues = new LinkedHashMap<String, double[]>();
			int step = m_dataExtractor.getStep();

			if (step <= 0) {
				return allCurrentValues;
			}
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			int removeLength = 60 / step - (minute / step);

			for (Entry<String, double[]> entry : allCurrentValues.entrySet()) {
				String key = entry.getKey();
				double[] value = entry.getValue();

				newCurrentValues.put(key, convert(value, removeLength));
			}
			return newCurrentValues;
		}
		return allCurrentValues;
	}
}
