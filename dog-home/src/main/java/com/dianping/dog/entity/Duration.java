package com.dianping.dog.entity;

import java.util.List;

public class Duration {
	
	private int min;

	private int max;

	private List<AlarmStrategyEntity> strategys;

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public List<AlarmStrategyEntity> getStrategys() {
		return strategys;
	}

	public void setStrategys(List<AlarmStrategyEntity> strategys) {
		this.strategys = strategys;
	}

}
