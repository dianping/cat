package com.dianping.cat.report.page.model.spi;

public enum ModelPeriod {
	HISTORICAL, LAST, CURRENT, FUTURE;

	private static final long ONE_HOUR = 3600 * 1000L;

	public static ModelPeriod getByName(String name, ModelPeriod defaultValue) {
		for (ModelPeriod period : values()) {
			if (period.name().equals(name)) {
				return period;
			}
		}

		return defaultValue;
	}

	public boolean isCurrent() {
		return this == CURRENT;
	}

	public boolean isLast() {
		return this == LAST;
	}

	public boolean isHistorical() {
		return this == HISTORICAL;
	}

	public boolean isFuture() {
		return this == FUTURE;
	}

	public static ModelPeriod getByTime(long timestamp) {
		long current = System.currentTimeMillis();

		current -= current % ONE_HOUR;

		if (timestamp >= current + ONE_HOUR) {
			return ModelPeriod.FUTURE;
		} else if (timestamp >= current) {
			return ModelPeriod.CURRENT;
		} else if (timestamp >= current - ONE_HOUR) {
			return ModelPeriod.LAST;
		} else {
			return ModelPeriod.HISTORICAL;
		}
	}
}