package com.dianping.cat.system.page.abtest.advisor;

public class ABTestAdvice {
	private double m_ctrOfVariationA = 0.00;

	private double m_ctrOfVariationB = 0.00;

	private double m_difference = 0.00;

	private int m_sizePerGroup = 0;

	private int m_totalParticipants = 0;

	private double m_confidenceInterval = 0.95;

	private int m_days = 0;

	public double getConfidenceInterval() {
		return m_confidenceInterval;
	}

	public double getCtrOfVariationA() {
		return m_ctrOfVariationA;
	}

	public double getCtrOfVariationB() {
		return m_ctrOfVariationB;
	}

	public int getDays() {
		return m_days;
	}

	public double getDifference() {
		return m_difference;
	}

	public int getSizePerGroup() {
		return m_sizePerGroup;
	}

	public int getTotalParticipants() {
		return m_totalParticipants;
	}

	public void setConfidenceInterval(double confidenceInterval) {
		m_confidenceInterval = confidenceInterval;
	}

	public void setCtrOfVariationA(double ctrOfVariationA) {
		m_ctrOfVariationA = ctrOfVariationA;
	}

	public void setCtrOfVariationB(double ctrOfVariationB) {
		m_ctrOfVariationB = ctrOfVariationB;
	}

	public void setDays(int days) {
		m_days = days;
	}

	public void setDifference(double difference) {
		m_difference = difference;
	}

	public void setSizePerGroup(int sizePerGroup) {
		m_sizePerGroup = sizePerGroup;
	}

	public void setTotalParticipants(int totalParticipants) {
		m_totalParticipants = totalParticipants;
	}
}