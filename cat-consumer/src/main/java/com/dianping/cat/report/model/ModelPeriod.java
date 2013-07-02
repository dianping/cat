package com.dianping.cat.report.model;

import com.dianping.cat.report.ReportConstants;

public enum ModelPeriod {
	CURRENT, FUTURE, HISTORICAL, LAST;

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

		current -= current % ReportConstants.HOUR;

		if (timestamp >= current + ReportConstants.HOUR) {
			return ModelPeriod.FUTURE;
		} else if (timestamp >= current) {
			return ModelPeriod.CURRENT;
		} else if (timestamp >= current - ReportConstants.HOUR) {
			return ModelPeriod.LAST;
		} else {
			return ModelPeriod.HISTORICAL;
		}
	}

	public long getStartTime() {
		long current = System.currentTimeMillis();

		current -= current % ReportConstants.HOUR;

		switch (this) {
		case CURRENT:
			return current;
		case LAST:
			return current - ReportConstants.HOUR;
		default:
			break;
		}

		throw new RuntimeException("Internal error: can't getStartTime() for historical or future period!");
	}

	public boolean isCurrent() {
		return this == CURRENT;
	}

	public boolean isFuture() {
		return this == FUTURE;
	}

	public boolean isHistorical() {
		return this == HISTORICAL;
	}

	public boolean isLast() {
		return this == LAST;
	}
}