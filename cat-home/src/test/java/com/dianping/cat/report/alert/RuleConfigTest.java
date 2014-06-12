package com.dianping.cat.report.alert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.report.task.alert.DataChecker;
import com.dianping.cat.report.task.alert.DefaultDataChecker;

public class RuleConfigTest {

	private DataChecker m_check = new DefaultDataChecker();

	private MonitorRules buildMonitorRuleFromFile(String path) {
		try {
			String content = Files.forIO().readFrom(this.getClass().getResourceAsStream(path), "utf-8");
			return DefaultSaxParser.parse(content);
		} catch (Exception ex) {
			Cat.logError(ex);
			return null;
		}
	}

	private Map<String, List<com.dianping.cat.home.rule.entity.Config>> buildConfigMap(MonitorRules monitorRules) {
		if (monitorRules == null || monitorRules.getRules().size() == 0) {
			return null;
		}

		Map<String, List<com.dianping.cat.home.rule.entity.Config>> map = new HashMap<String, List<com.dianping.cat.home.rule.entity.Config>>();

		for (Rule rule : monitorRules.getRules().values()) {
			map.put(rule.getId(), rule.getConfigs());
		}

		return map;
	}

	@Test
	public void testRule() {
		Map<String, List<com.dianping.cat.home.rule.entity.Config>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/demo-rule-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 200, 200 };
		double value[] = { 100, 100 };
		Pair<Boolean, String> result = m_check.checkData(value, baseline, configMap.get("demo1"));
		Assert.assertEquals(result.getKey().booleanValue(), true);
	}

	@Test
	public void testCondition() {
		Map<String, List<com.dianping.cat.home.rule.entity.Config>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/demo-rule-monitor.xml"));
		Pair<Boolean, String> result;

		Assert.assertNotNull(configMap);

		double[] baseline7 = { 200, 200 };
		double[] value7 = { 100, 100 };
		result = m_check.checkData(value7, baseline7, configMap.get("conditionCombination"));
		Assert.assertEquals(result.getKey().booleanValue(), true);

		double[] baseline8 = { 200, 200 };
		double[] value8 = { 100, 100 };
		result = m_check.checkData(value8, baseline8, configMap.get("subconditionCombination"));
		Assert.assertEquals(result.getKey().booleanValue(), false);
	}

	@Test
	public void testMinute() {
		Map<String, List<com.dianping.cat.home.rule.entity.Config>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/test-minute-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 50, 200, 200 };
		double value[] = { 50, 100, 100 };
		Pair<Boolean, String> result = m_check.checkData(value, baseline, configMap.get("two-minute"));
		Assert.assertEquals(result.getKey().booleanValue(), true);
	}
}
