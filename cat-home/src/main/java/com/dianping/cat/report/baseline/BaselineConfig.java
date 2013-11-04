package com.dianping.cat.report.baseline;

import java.util.List;

public class BaselineConfig {

	private int m_id;

	private String m_key;

	private int m_targetDate;

	private List<Double> m_weights;

	private List<Integer> m_days;

	private double m_upperLimit;

	private double m_lowerLimit;

	private double m_minValue;

	public int getId() {
		return m_id;
	}

	public String getKey() {
		return m_key;
	}

	public int getTargetDate() {
		return m_targetDate;
	}

	public List<Double> getWeights() {
		return m_weights;
	}

	public List<Integer> getDays() {
		return m_days;
	}

	public double getUpperLimit() {
		return m_upperLimit;
	}

	public double getLowerLimit() {
		return m_lowerLimit;
	}

	public double getMinValue() {
		return m_minValue;
	}

	public void setId(int id) {
		m_id = id;
	}

	public void setKey(String key) {
		m_key = key;
	}

	public void setTargetDate(int targetDate) {
		m_targetDate = targetDate;
	}

	public void setWeights(List<Double> weights) {
		m_weights = weights;
	}

	public void setDays(List<Integer> days) {
		m_days = days;
	}

	public void setUpperLimit(double upperLimit) {
		m_upperLimit = upperLimit;
	}

	public void setLowerLimit(double lowerLimit) {
		m_lowerLimit = lowerLimit;
	}

	public void setMinValue(double minValue) {
		m_minValue = minValue;
	}

}
