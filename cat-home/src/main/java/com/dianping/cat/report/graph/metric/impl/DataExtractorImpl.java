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
package com.dianping.cat.report.graph.metric.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.report.graph.metric.DataExtractor;

@Named(type = DataExtractor.class)
public class DataExtractorImpl implements DataExtractor {

	private static final int MIN_POINT_NUMBER = 60;

	private static final int MAX_POINT_NUMBER = 180;

	private int m_step;

	@Override
	public int calculateInterval(int length) {
		int[] values = { 1, 2, 3, 6, 10, 20, 30, 60 };
		for (int value : values) {
			int pm = length / value;
			if (pm >= MIN_POINT_NUMBER && pm < MAX_POINT_NUMBER) {
				return value;
			}
		}
		int pm = length / 60;
		if (pm > MAX_POINT_NUMBER) {
			return 60;
		} else {
			return 1;
		}
	}

	@Override
	public double[] extract(double[] values) {
		int length = values.length;
		m_step = calculateInterval(length);
		int size = length / m_step;

		if (size * m_step < length) {
			size++;
		}
		double[] result = new double[size];

		for (int i = 0; i < length; i = i + m_step) {
			double sum = 0;
			for (int j = 0; j < m_step; j++) {
				if (i + j <= length - 1) {
					sum = sum + values[i + j];
				}
			}
			result[i / m_step] = sum / m_step;
		}
		return result;
	}

	@Override
	public Map<String, double[]> extract(Map<String, double[]> values) {
		Map<String, double[]> result = new LinkedHashMap<String, double[]>();

		for (Entry<String, double[]> entry : values.entrySet()) {
			result.put(entry.getKey(), extract(entry.getValue()));
		}
		return result;
	}

	@Override
	public int getStep() {
		return m_step;
	}

}
