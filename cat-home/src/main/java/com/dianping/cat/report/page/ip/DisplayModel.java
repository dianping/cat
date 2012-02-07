package com.dianping.cat.report.page.ip;

public class DisplayModel {
	private String m_address;

	private int m_lastOne;

	private int m_lastFive;

	private int m_lastFifteen;

	public DisplayModel(String address) {
		m_address = address;
	}

	public String getAddress() {
		return m_address;
	}

	public int getLastFifteen() {
		return m_lastFifteen;
	}

	public int getLastFive() {
		return m_lastFive;
	}

	public int getLastOne() {
		return m_lastOne;
	}

	public void process(int current, int minute, int count) {
		if (current == minute) {
			m_lastOne += count;
			m_lastFive += count;
			m_lastFifteen += count;
		} else if (current < minute) {
			// ignore it
		} else if (current - 5 < minute) {
			m_lastFive += count;
			m_lastFifteen += count;
		} else if (current - 15 < minute) {
			m_lastFifteen += count;
		}
	}
}