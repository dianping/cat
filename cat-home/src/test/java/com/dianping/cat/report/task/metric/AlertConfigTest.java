package com.dianping.cat.report.task.metric;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.tuple.Pair;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;

public class AlertConfigTest {

	@Test
	public void test() {
		AlertConfig alertConfig = new AlertConfig();
		MetricItemConfig config = new MetricItemConfig();

		double baseline[] = { 100, 100 };
		double value[] = { 200, 200 };
		Pair<Boolean, String> result = alertConfig.checkData(config, value, baseline, MetricType.COUNT);
		Assert.assertEquals(result.getKey().booleanValue(), false);

		double[] baseline2 = { 100, 100 };
		double[] value2 = { 49, 49 };
		result = alertConfig.checkData(config, value2, baseline2, MetricType.COUNT);
		Assert.assertEquals(result.getKey().booleanValue(), false);

		double[] baseline3 = { 100, 100 };
		double[] value3 = { 51, 49 };
		result = alertConfig.checkData(config, value3, baseline3, MetricType.COUNT);
		Assert.assertEquals(result.getKey().booleanValue(), false);

		double[] baseline4 = { 50, 50 };
		double[] value4 = { 10, 10 };
		result = alertConfig.checkData(config, value4, baseline4, MetricType.COUNT);
		Assert.assertEquals(result.getKey().booleanValue(), false);

		config.setDecreaseValue(40);
		config.setDecreasePercentage(50);
		result = alertConfig.checkData(config, value4, baseline4, MetricType.COUNT);
		Assert.assertEquals(result.getKey().booleanValue(), true);

		config.setDecreaseValue(41);
		config.setDecreasePercentage(50);
		result = alertConfig.checkData(config, value4, baseline4, MetricType.COUNT);
		Assert.assertEquals(result.getKey().booleanValue(), false);

		config.setDecreaseValue(40);
		config.setDecreasePercentage(79);
		result = alertConfig.checkData(config, value4, baseline4, MetricType.COUNT);
		Assert.assertEquals(result.getKey().booleanValue(), true);
		
		config.setDecreaseValue(40);
		config.setDecreasePercentage(80);
		result = alertConfig.checkData(config, value4, baseline4, MetricType.COUNT);
		Assert.assertEquals(result.getKey().booleanValue(), false);
		
		config.setDecreaseValue(40);
		config.setDecreasePercentage(80);
		result = alertConfig.checkData(config, value4, baseline4, MetricType.COUNT);
		Assert.assertEquals(result.getKey().booleanValue(), false);
		
		double[] baseline5 = { 117, 118 };
		double[] value5 = { 43, 48 };
		config.setDecreasePercentage(50);
		config.setDecreasePercentage(50);
		result = alertConfig.checkData(config, value5, baseline5, MetricType.COUNT);
		Assert.assertEquals(result.getKey().booleanValue(), true);
	}
}
