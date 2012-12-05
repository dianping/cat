package com.dianping.cat.report.page.model.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModelRequest {
	public static ModelRequest from(String domain, String period) {
		ModelRequest request = new ModelRequest(domain, ModelPeriod.getByName(period, ModelPeriod.CURRENT));

		return request;
	}

	private String m_domain;

	private ModelPeriod m_period;

	private Map<String, String> m_properties;

	public ModelRequest(String domain, ModelPeriod period) {
		m_domain = domain;
		m_period = period;
	}

	public String getDomain() {
		return m_domain;
	}

	public ModelPeriod getPeriod() {
		return m_period;
	}

	public Map<String, String> getProperties() {
		if (m_properties == null) {
			return Collections.emptyMap();
		} else {
			return m_properties;
		}
	}

	public String getProperty(String name) {
		return getProperty(name, null);
	}

	public String getProperty(String name, String defaultValue) {
		if (m_properties == null) {
			return defaultValue;
		} else if (m_properties.containsKey(name)) {
			return m_properties.get(name);
		} else {
			return defaultValue;
		}
	}

	public boolean hasProperty(String name) {
		if (m_properties != null) {
			return m_properties.containsKey(name);
		} else {
			return false;
		}
	}

	public ModelRequest setProperty(String name, String value) {
		if (m_properties == null) {
			m_properties = new HashMap<String, String>();
		}

		m_properties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		return String.format("ModelRequest[domain=%s, period=%s, properties=%s]", m_domain, m_period, m_properties);
	}
}
