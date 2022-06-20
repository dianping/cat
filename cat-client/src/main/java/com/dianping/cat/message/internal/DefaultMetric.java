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
package com.dianping.cat.message.internal;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.message.Metric;
import com.dianping.cat.message.context.MetricContext;

public class DefaultMetric implements Metric {
	private MetricContext m_ctx;

	private String m_name;

	private long m_timestamp;

	private Kind m_kind;

	private int m_count;

	private double m_sum;

	private long m_duration;

	private Map<String, String> m_tags;

	public DefaultMetric(MetricContext ctx, String name) {
		m_ctx = ctx;
		m_name = name;
		m_timestamp = System.currentTimeMillis();
	}

	@Override
	public void count(int quantity) {
		m_kind = Kind.COUNT;
		m_count += quantity;
		m_ctx.add(this);
	}

	@Override
	public void duration(int quantity, long durationInMillis) {
		m_kind = Kind.DURATION;
		m_count += quantity;
		m_duration += durationInMillis;
		m_ctx.add(this);
	}

	@Override
	public int getCount() {
		return m_count;
	}

	@Override
	public long getDuration() {
		return m_duration;
	}

	@Override
	public String getName() {
		return m_name;
	}

	public Map<String, String> getTags() {
		return m_tags;
	}

	@Override
	public long getTimestamp() {
		return m_timestamp;
	}

	@Override
	public double getSum() {
		return m_sum;
	}

	@Override
	public Kind getKind() {
		return m_kind;
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	@Override
	public void sum(int count, double sum) {
		m_kind = Kind.SUM;
		m_count += count;
		m_sum += sum;
		m_ctx.add(this);
	}

	@Override
	public Metric tag(String name, String value) {
		if (m_tags == null) {
			m_tags = new HashMap<String, String>();
		}

		m_tags.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		return String.format("Metric(type=%s, count=%s, total=%s, duration=%s)", m_kind, m_count, m_sum, m_duration);
	}

	@Override
	public void add(Metric metric) {
		m_count += metric.getCount();
		m_sum += metric.getSum();
		m_duration += metric.getDuration();
	}
}