package com.dianping.cat.report.service;

import com.dianping.cat.Constants;


public enum ModelPeriod {
	CURRENT, HISTORICAL, LAST;

	public static ModelPeriod getByName(String name, ModelPeriod defaultValue) {
		for (ModelPeriod period : values()) {
			if (period.name().equals(name)) {
				return period;
			}
		}

		return defaultValue;
	}

	public static ModelPeriod getByTime(long timestamp) {
		long current = System.currentTimeMillis();

		current -= current % Constants.HOUR;

		if (timestamp >= current) {
			return ModelPeriod.CURRENT;
		} else if (timestamp >= current - Constants.HOUR) {
			return ModelPeriod.LAST;
		} else {
			return ModelPeriod.HISTORICAL;
		}
	}

	public long getStartTime() {
		long current = System.currentTimeMillis();

		current -= current % Constants.HOUR;

		switch (this) {
		case CURRENT :
			return current;
		case LAST:
			return current - Constants.HOUR;
		default:
			return current;
		}
	}

	public boolean isCurrent() {
		return this == CURRENT;
	}

	public boolean isHistorical() {
		return this == HISTORICAL;
	}

	public boolean isLast() {
		return this == LAST;
	}
}