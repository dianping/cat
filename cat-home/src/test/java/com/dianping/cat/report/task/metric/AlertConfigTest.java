package com.dianping.cat.report.task.metric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.home.monitorrules.entity.MonitorRules;
import com.dianping.cat.home.monitorrules.entity.Rule;
import com.dianping.cat.home.monitorrules.transform.DefaultSaxParser;
import com.dianping.cat.report.task.alert.DataChecker;
import com.dianping.cat.report.task.alert.DefaultDataChecker;
import com.dianping.cat.report.task.alert.MetricType;
import com.dianping.cat.report.task.alert.metric.MetricAlertConfig;

public class AlertConfigTest {
	
	private DataChecker m_checker = new DefaultDataChecker();

	private MonitorRules buildMonitorRuleFromFile(String path) {
		try {
			String content = Files.forIO().readFrom(this.getClass().getResourceAsStream(path), "utf-8");
			return DefaultSaxParser.parse(content);
		} catch (Exception ex) {
			Cat.logError(ex);
			return null;
		}
	}

	private Map<String, List<com.dianping.cat.home.monitorrules.entity.Config>> buildConfigMap(MonitorRules monitorRules) {
		if (monitorRules == null || monitorRules.getRules().size() == 0) {
			return null;
		}

		Map<String, List<com.dianping.cat.home.monitorrules.entity.Config>> map = new HashMap<String, List<com.dianping.cat.home.monitorrules.entity.Config>>();

		for (Rule rule : monitorRules.getRules()) {
			map.put(rule.getId(), rule.getConfigs());
		}

		return map;
	}

	@Test
	public void test() {
		MetricAlertConfig alertConfig = new MetricAlertConfig();
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

	@Test
	public void testRule() {
		Map<String, List<com.dianping.cat.home.monitorrules.entity.Config>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/test-rule-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 200, 200 };
		double value[] = { 100, 100 };
		Pair<Boolean, String> result = m_checker.checkData(value, baseline,
		      configMap.get("decreasePercentage"));
		Assert.assertEquals(result.getKey().booleanValue(), true);

		double[] baseline2 = { 200, 300 };
		double[] value2 = { 100, 100 };
		result = m_checker.checkData(value2, baseline2,  configMap.get("decreaseValue"));
		Assert.assertEquals(result.getKey().booleanValue(), true);

		double[] baseline3 = { 200, 50 };
		double[] value3 = { 400, 100 };
		result = m_checker.checkData(value3, baseline3,  configMap.get("increasePercentage"));
		Assert.assertEquals(result.getKey().booleanValue(), true);

		double[] baseline4 = { 200, 50 };
		double[] value4 = { 400, 100 };
		result = m_checker.checkData(value4, baseline4,  configMap.get("increaseValue"));
		Assert.assertEquals(result.getKey().booleanValue(), true);

		double[] baseline5 = { 200, 200 };
		double[] value5 = { 500, 600 };
		result = m_checker.checkData(value5, baseline5,  configMap.get("absoluteMaxValue"));
		Assert.assertEquals(result.getKey().booleanValue(), true);

		double[] baseline6 = { 200, 200 };
		double[] value6 = { 50, 40 };
		result = m_checker.checkData(value6, baseline6,  configMap.get("absoluteMinValue"));
		Assert.assertEquals(result.getKey().booleanValue(), true);

		double[] baseline7 = { 200, 200 };
		double[] value7 = { 100, 100 };
		result = m_checker
		      .checkData(value7, baseline7,  configMap.get("conditionCombination"));
		Assert.assertEquals(result.getKey().booleanValue(), true);

		double[] baseline8 = { 200, 200 };
		double[] value8 = { 100, 100 };
		result = m_checker.checkData(value8, baseline8, 
		      configMap.get("subconditionCombination"));
		Assert.assertEquals(result.getKey().booleanValue(), false);
	}

	@Test
	public void testMinute() {
		Map<String, List<com.dianping.cat.home.monitorrules.entity.Config>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/test-minute-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 50, 200, 200 };
		double value[] = { 50, 100, 100 };
		Pair<Boolean, String> result = m_checker.checkData(value, baseline, 
		      configMap.get("two-minute"));
		Assert.assertEquals(result.getKey().booleanValue(), true);
	}
}
