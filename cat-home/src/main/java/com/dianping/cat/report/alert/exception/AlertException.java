package com.dianping.cat.report.alert.exception;

import com.dianping.cat.alarm.spi.AlertLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class AlertException {

	private String m_name;

	private AlertLevel m_type;

	private double m_count;

	private Map<String, Double> m_exceptions = new HashMap<String, Double>();

	public AlertException(String name, AlertLevel type, double count) {
		m_name = name;
		m_type = type;
		m_count = count;
	}

	public String getName() {
		return m_name;
	}

	public AlertLevel getType() {
		return m_type;
	}

	@Override
	public String toString() {
		return m_name + "=" + m_count;
	}
}
