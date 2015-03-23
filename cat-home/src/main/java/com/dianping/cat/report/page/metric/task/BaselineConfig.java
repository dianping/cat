package com.dianping.cat.report.page.metric.task;

import java.util.List;

public class BaselineConfig {

	private int m_id;

	private String m_key;

	private int m_targetDate;

	private List<Double> m_weights;

	private List<Integer> m_days;


	public List<Integer> getDays() {
		return m_days;
	}

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

	public void setDays(List<Integer> days) {
		m_days = days;
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

}
