package com.dianping.cat.report.alert.exception;

import com.dianping.cat.alarm.spi.AlertLevel;

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

	public double getCount() {
		return m_count;
	}

	public String showCount() {
		return doubleToText(m_count);
	}

	@Override
	public String toString() {
		return m_name + "=" + doubleToText(m_count);
	}

	public static String doubleToText(double count) {
		return String.valueOf(count).replaceAll("0+?$", "").replaceAll("[.]$", "");
	}
}
