package com.dianping.cat.report.page.model.spi;

import com.dianping.cat.helper.TimeUtil;

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

		current -= current % TimeUtil.ONE_HOUR;

		if (timestamp >= current + TimeUtil.ONE_HOUR) {
			return ModelPeriod.FUTURE;
		} else if (timestamp >= current) {
			return ModelPeriod.CURRENT;
		} else if (timestamp >= current - TimeUtil.ONE_HOUR) {
			return ModelPeriod.LAST;
		} else {
			return ModelPeriod.HISTORICAL;
		}
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