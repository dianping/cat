package com.dianping.cat.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModelResponse<M> {
	private Exception m_exception;

	private M m_model;

	private Map<String, String> m_properties;

	public Exception getException() {
		return m_exception;
	}

	public M getModel() {
		return m_model;
	}

	public Map<String, String> getProperties() {
		if (m_properties == null) {
			return Collections.emptyMap();
		} else {
			return m_properties;
		}
	}

	public String getProperty(String name) {
		if (m_properties == null) {
			return null;
		} else {
			return m_properties.get(name);
		}
	}

	public void setException(Exception exception) {
		m_exception = exception;
	}

	public void setModel(M model) {
		m_model = model;
	}

	public ModelResponse<M> setProperty(String name, String value) {
		if (m_properties == null) {
			m_properties = new HashMap<String, String>();
		}

		m_properties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		return String.format("ModelResponse[model=%s, exception=%s, properties=%s]", m_model, m_exception, m_properties);
	}

}
