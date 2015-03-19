package com.dianping.cat.report.page.metric.task;

import java.util.Arrays;

public class BaselineConfigManager {

	public BaselineConfig queryBaseLineConfig(String key) {
		BaselineConfig config = new BaselineConfig();
		Integer[] days = { -21, -14, -7, 0 };
		Double[] weights = { 1.0, 2.0, 3.0, 4.0 };

		config.setDays(Arrays.asList(days));
		config.setId(1);
		config.setKey(key);
		config.setTargetDate(7);
		config.setWeights(Arrays.asList(weights));
		return config;
	}
}
