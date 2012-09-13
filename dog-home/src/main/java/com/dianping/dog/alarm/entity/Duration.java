package com.dianping.dog.alarm.entity;

import java.util.List;

import com.dianping.dog.alarm.rule.AlarmType;

public class Duration {

	private int min;

	private int max;

	private List<AlarmType> alarmType;

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

	public boolean isIn(int count) {
		if (min <= count && count < max) {
			return true;
		}
		return false;
	}

	public List<AlarmType> getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(List<AlarmType> alarmType) {
		this.alarmType = alarmType;
	}

}
