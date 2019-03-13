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
package com.dianping.cat.report.page.business.task;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.Segment;
import com.dianping.cat.helper.MetricType;

@Named
public class BusinessPointParser {

	private static final int POINT_NUMBER = 60;

	public double[] buildDailyData(List<BusinessItem> items, MetricType type) {
		int size = items.size();
		double[] values = new double[24 * POINT_NUMBER];

		for (int i = 0; i < 24 * POINT_NUMBER; i++) {
			values[i] = -1;
		}

		for (int hour = 0; hour < size; hour++) {
			BusinessItem item = items.get(hour);
			try {
				double[] oneHourValues = buildHourlyData(item, type);

				for (int minute = 0; minute < 60; minute++) {
					int index = hour * 60 + minute;
					values[index] = oneHourValues[minute];
				}
			} catch (Exception e) {
				continue;
			}
		}
		return values;
	}

	private double[] buildHourlyData(BusinessItem item, MetricType type) {
		double[] result = new double[POINT_NUMBER];
		Map<Integer, Segment> map = item.getSegments();

		for (Entry<Integer, Segment> entry : map.entrySet()) {
			Integer minute = entry.getKey();
			Segment seg = entry.getValue();

			if (type == MetricType.AVG) {
				result[minute] = seg.getAvg();
			} else if (type == MetricType.COUNT) {
				result[minute] = (double) seg.getCount();
			} else if (type == MetricType.SUM) {
				result[minute] = seg.getSum();
			}
		}
		return result;
	}

}
