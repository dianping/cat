package com.dianping.cat.report.page.model.spi;

public enum ModelPeriod {
	HISTORICAL, LAST, CURRENT, FUTURE;

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
}