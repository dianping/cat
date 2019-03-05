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
package com.dianping.cat.report.page.top;

import java.util.LinkedHashMap;
import java.util.Map;

public class DomainInfo {

	public Map<String, Metric> m_metrics = new LinkedHashMap<String, Metric>();

	public Metric getMetric(String key) {
		Metric m = m_metrics.get(key);

		if (m == null) {
			m = new Metric();
			m_metrics.put(key, m);
		}
		return m;
	}

	public Map<String, Metric> getMetrics() {
		return m_metrics;
	}

	public static class Item {
		private long m_count;

		private double m_avg;

		private long m_fail;

		private double m_sum;

		public double getAvg() {
			return m_avg;
		}

		public long getCount() {
			return m_count;
		}

		public long getFail() {
			return m_fail;
		}

		public Item setFail(long fail) {
			m_fail = fail;
			return this;
		}

		public Item setValue(long count, double sum) {
			m_count = m_count + count;
			m_sum = m_sum + sum;

			if (m_count > 0) {
				m_avg = m_sum / m_count;
			}
			return this;
		}
	}

	public static class Metric {

		private int m_exception;

		private Map<String, Item> m_items = new LinkedHashMap<String, Item>();

		public void addException(int count) {
			m_exception = m_exception + count;
		}

		public Item get(String key) {
			Item item = m_items.get(key);

			if (item == null) {
				item = new Item();
				m_items.put(key, item);
			}
			return item;
		}

		public int getException() {
			return m_exception;
		}

		public void setException(int exception) {
			m_exception = exception;
		}
	}

}
