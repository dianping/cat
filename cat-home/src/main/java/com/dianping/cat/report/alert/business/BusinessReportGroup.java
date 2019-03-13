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
package com.dianping.cat.report.alert.business;

import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;
import com.dianping.cat.helper.MetricType;

public class BusinessReportGroup {

	private BusinessReport m_last;

	private BusinessReport m_current;

	private boolean m_dataReady;

	public double[] extractData(int currentMinute, int ruleMinute, String key, MetricType type) {
		double[] value = new double[ruleMinute];

		if (currentMinute >= ruleMinute - 1) {
			int start = currentMinute + 1 - ruleMinute;
			int end = currentMinute;

			value = queryRealData(start, end, key, m_current, type);
		} else if (currentMinute < 0) {
			int start = 60 + currentMinute + 1 - (ruleMinute);
			int end = 60 + currentMinute;

			value = queryRealData(start, end, key, m_last, type);
		} else {
			int currentStart = 0, currentEnd = currentMinute;
			double[] currentValue = queryRealData(currentStart, currentEnd, key, m_current, type);

			int lastStart = 60 + 1 - (ruleMinute - currentMinute);
			int lastEnd = 59;
			double[] lastValue = queryRealData(lastStart, lastEnd, key, m_last, type);

			value = mergerArray(lastValue, currentValue);
		}

		return value;
	}

	public BusinessReport getCurrent() {
		return m_current;
	}

	public BusinessReportGroup setCurrent(BusinessReport current) {
		m_current = current;
		return this;
	}

	public BusinessReport getLast() {
		return m_last;
	}

	public BusinessReportGroup setLast(BusinessReport last) {
		m_last = last;
		return this;
	}

	public boolean isDataReady() {
		return m_dataReady;
	}

	public BusinessReportGroup setDataReady(boolean dataReady) {
		m_dataReady = dataReady;
		return this;
	}

	public double[] mergerArray(double[] from, double[] to) {
		int fromLength = from.length;
		int toLength = to.length;
		double[] result = new double[fromLength + toLength];
		int index = 0;

		for (int i = 0; i < fromLength; i++) {
			result[i] = from[i];
			index++;
		}
		for (int i = 0; i < toLength; i++) {
			result[i + index] = to[i];
		}
		return result;
	}

	private double[] queryRealData(int start, int end, String key, BusinessReport report, MetricType type) {
		double[] all = new double[60];
		BusinessItem businessItems = report.findBusinessItem(key);

		if (businessItems != null) {
			Map<Integer, Segment> map = businessItems.getSegments();

			for (Entry<Integer, Segment> entry : map.entrySet()) {
				Integer minute = entry.getKey();
				Segment seg = entry.getValue();

				if (type == MetricType.AVG) {
					all[minute] = seg.getAvg();
				} else if (type == MetricType.COUNT) {
					all[minute] = (double) seg.getCount();
				} else if (type == MetricType.SUM) {
					all[minute] = seg.getSum();
				}
			}
		}
		int length = end - start + 1;
		double[] result = new double[length];
		System.arraycopy(all, start, result, 0, length);

		return result;
	}

}
