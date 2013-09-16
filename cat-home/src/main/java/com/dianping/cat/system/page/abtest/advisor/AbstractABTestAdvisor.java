package com.dianping.cat.system.page.abtest.advisor;

public abstract class AbstractABTestAdvisor implements ABTestAdvisor {

	protected double m_confidenceInterval = 0.95;

	protected int m_pv = 0;

	protected double m_difference = 0.01;

	@Override
	public void setConfidenceInterval(double interval) {
		m_confidenceInterval = interval;
	}

	@Override
	public void setCurrentPv(int pv) {
		m_pv = pv;
	}

	@Override
	public void setDifference(double difference) {
		m_difference = difference;
	}
}
