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
