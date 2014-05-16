package com.dianping.cat.demo;

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
import com.dianping.cat.report.task.metric.AlertConfig;
import com.dianping.cat.report.task.metric.MetricType;

public class RuleConfigTest {

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
	public void testRule() {
		AlertConfig alertConfig = new AlertConfig();
		MetricItemConfig config = new MetricItemConfig();
		Map<String, List<com.dianping.cat.home.monitorrules.entity.Config>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/demo-rule-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 200, 200 };
		double value[] = { 100, 100 };
		Pair<Boolean, String> result = alertConfig.checkData(config, value, baseline, MetricType.COUNT,
		      configMap.get("demo1"));
		Assert.assertEquals(result.getKey().booleanValue(), true);
	}
	
	@Test
	public void testCondition() {
		AlertConfig alertConfig = new AlertConfig();
		MetricItemConfig config = new MetricItemConfig();
		Map<String, List<com.dianping.cat.home.monitorrules.entity.Config>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/demo-rule-monitor.xml"));
		Pair<Boolean, String> result;
		
		Assert.assertNotNull(configMap);

		double[] baseline7 = { 200, 200 };
		double[] value7 = { 100, 100 };
		result = alertConfig
		      .checkData(config, value7, baseline7, MetricType.COUNT, configMap.get("conditionCombination"));
		Assert.assertEquals(result.getKey().booleanValue(), true);

		double[] baseline8 = { 200, 200 };
		double[] value8 = { 100, 100 };
		result = alertConfig.checkData(config, value8, baseline8, MetricType.COUNT,
		      configMap.get("subconditionCombination"));
		Assert.assertEquals(result.getKey().booleanValue(), false);
	}

	@Test
	public void testMinute() {
		AlertConfig alertConfig = new AlertConfig();
		MetricItemConfig config = new MetricItemConfig();
		Map<String, List<com.dianping.cat.home.monitorrules.entity.Config>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/test-minute-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 50, 200, 200 };
		double value[] = { 50, 100, 100 };
		Pair<Boolean, String> result = alertConfig.checkData(config, value, baseline, MetricType.COUNT,
		      configMap.get("two-minute"));
		Assert.assertEquals(result.getKey().booleanValue(), true);
	}
}
