package com.dianping.cat.report.baseline;

import java.util.Arrays;

public class BaselineConfigManager {

	public BaselineConfig queryBaseLineConfig(String key) {
		BaselineConfig config = new BaselineConfig();
		Integer[] days = { 0, -1, -2, -3, -4, -5 };
		Double[] weights = { 1.0, 1.0, 1.0, 1.0, 1.0 };

		config.setDays(Arrays.asList(days));
		config.setId(1);
		config.setKey(key);
		config.setLowerLimit(100);
		config.setMinValue(20);
		config.setTargetDate(7);
		config.setUpperLimit(500);
		config.setWeights(Arrays.asList(weights));
		return config;
	}
}
