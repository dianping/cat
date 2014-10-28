package com.dianping.cat.consumer.cross;

public class CrossAppSwitch {
	public boolean m_turnOn = false;

	public CrossAppSwitch() {
	}

	public CrossAppSwitch(boolean turnOn) {
		m_turnOn = turnOn;
	}

	public boolean switchOn() {
		return m_turnOn;
	}
}
