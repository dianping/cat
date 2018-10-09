package com.dianping.cat.server;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServerGroupByEntity {

	private String m_measurement;

	private String m_endPoint;

	private Map<Long, Double> m_values = new LinkedHashMap<Long, Double>();

	public ServerGroupByEntity(String measurement, String endPoint, Map<Long, Double> values) {
		m_measurement = measurement;
		m_endPoint = endPoint;
		m_values = values;
	}

	public String getEndPoint() {
		return m_endPoint;
	}

	public String getMeasurement() {
		return m_measurement;
	}

	public Map<Long, Double> getValues() {
		return m_values;
	}

}
