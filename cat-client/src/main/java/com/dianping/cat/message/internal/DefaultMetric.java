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

	private long m_timestampInMillis;

	private Type m_type;

	private int m_count;

	private double m_total;

	private long m_duration;

	private Map<String, String> m_tags;

	public DefaultMetric(MetricContext ctx, String name) {
		m_ctx = ctx;
		m_name = name;
	}

	@Override
	public void count(int quantity) {
		m_type = Type.COUNT;
		m_count += quantity;
		m_ctx.add(this);
	}

	@Override
	public void duration(int quantity, long durationInMillis) {
		m_type = Type.DURATION;
		m_count += quantity;
		m_duration += durationInMillis;
		m_ctx.add(this);
	}

	public int getCount() {
		return m_count;
	}

	public MetricContext getCtx() {
		return m_ctx;
	}

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
		return m_timestampInMillis;
	}

	public long getTimestampInMillis() {
		return m_timestampInMillis;
	}

	public double getTotal() {
		return m_total;
	}

	public Type getType() {
		return m_type;
	}

	public void setTimestamp(long timestampInMillis) {
		m_timestampInMillis = timestampInMillis;
	}

	@Override
	public void sum(int count, double total) {
		m_type = Type.SUM;
		m_count += count;
		m_total += total;
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

	public enum Type {
		COUNT,

		SUM,

		DURATION;
	}
}