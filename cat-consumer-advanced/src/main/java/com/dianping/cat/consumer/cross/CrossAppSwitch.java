package com.dianping.cat.consumer.cross;

public class CrossAppSwitch {
	public boolean m_turnOn = false;

	public boolean isTurnOn() {
		return m_turnOn;
	}

	public CrossAppSwitch setTurnOn(boolean turnOn) {
		m_turnOn = turnOn;
		return this;
	}

}
