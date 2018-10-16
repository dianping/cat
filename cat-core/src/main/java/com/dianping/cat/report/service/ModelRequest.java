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
package com.dianping.cat.report.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModelRequest {

	private String m_domain;

	private long m_startTime;

	private ModelPeriod m_period;

	private Map<String, String> m_properties = new LinkedHashMap<String, String>();

	public ModelRequest(String domain, long startTime) {
		m_domain = domain;
		m_startTime = startTime;
		m_period = ModelPeriod.getByTime(startTime);
	}

	public ModelRequest(String domain, ModelPeriod period) {
		m_domain = domain;
		m_period = period;
		m_startTime = period.getStartTime();
	}

	public String getDomain() {
		return m_domain;
	}

	public ModelPeriod getPeriod() {
		return m_period;
	}

	public Map<String, String> getProperties() {
		return m_properties;
	}

	public String getProperty(String name) {
		return getProperty(name, null);
	}

	public String getProperty(String name, String defaultValue) {
		if (m_properties.containsKey(name)) {
			return m_properties.get(name);
		} else {
			return defaultValue;
		}
	}

	public long getStartTime() {
		return m_startTime;
	}

	public ModelRequest setProperty(String name, String value) {
		m_properties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		return String.format("ModelRequest[domain=%s, period=%s, properties=%s]", m_domain, m_period, m_properties);
	}
}
