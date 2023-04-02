package com.dianping.cat.report.alert.exception;

/**
 * TODO
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class AlertMachine {

	private String m_ip;

	private double m_count;

	public AlertMachine(String m_ip, double m_count) {
		this.m_ip = m_ip;
		this.m_count = m_count;
	}

	public String showCount() {
		return doubleToText(m_count);
	}

	@Override
	public String toString() {
		return m_ip + "=" + doubleToText(m_count);
	}

	public static String doubleToText(double count) {
		return String.valueOf(count).replaceAll("0+?$", "").replaceAll("[.]$", "");
	}
}
