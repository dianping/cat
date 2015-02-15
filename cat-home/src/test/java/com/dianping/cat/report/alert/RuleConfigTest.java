package com.dianping.cat.report.alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.report.alert.AlertResultEntity;
import com.dianping.cat.report.alert.DataChecker;
import com.dianping.cat.report.alert.DefaultDataChecker;

public class RuleConfigTest {

	private DataChecker m_check = new DefaultDataChecker();

	private List<Condition> buildConditions(List<Config> configs) {
		List<Condition> conditions = new ArrayList<Condition>();

		for (Config config : configs) {
			conditions.addAll(config.getConditions());
		}

		return conditions;
	}

	private Map<String, List<Condition>> buildConfigMap(MonitorRules monitorRules) {
		if (monitorRules == null || monitorRules.getRules().size() == 0) {
			return null;
		}

		Map<String, List<Condition>> map = new HashMap<String, List<Condition>>();

		for (Rule rule : monitorRules.getRules().values()) {
			String id = rule.getId();
			List<Condition> ruleConditions = buildConditions(rule.getConfigs());
			List<Condition> conditions = map.get(id);

			if (conditions == null) {
				map.put(id, ruleConditions);
			} else {
				conditions.addAll(ruleConditions);
			}
		}

		return map;
	}

	private MonitorRules buildMonitorRuleFromFile(String path) {
		try {
			String content = Files.forIO().readFrom(this.getClass().getResourceAsStream(path), "utf-8");
			return DefaultSaxParser.parse(content);
		} catch (Exception ex) {
			Cat.logError(ex);
			return null;
		}
	}

	@Test
	public void testCondition() {
		Map<String, List<Condition>> conditionsMap = buildConfigMap(buildMonitorRuleFromFile("/config/demo-rule-monitor.xml"));
		AlertResultEntity result;

		Assert.assertNotNull(conditionsMap);

		double[] baseline7 = { 200, 200 };
		double[] value7 = { 100, 100 };
		result = extractError(m_check.checkData(value7, baseline7, conditionsMap.get("conditionCombination")));
		Assert.assertEquals(result.isTriggered(), true);

		double[] baseline8 = { 200, 200 };
		double[] value8 = { 100, 100 };
		result = extractError(m_check.checkData(value8, baseline8, conditionsMap.get("subconditionCombination")));
		Assert.assertNull(result);
	}

	@Test
	public void testMinute() {
		Map<String, List<Condition>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/test-minute-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 50, 200, 200 };
		double value[] = { 50, 100, 100 };
		AlertResultEntity result = extractError(m_check.checkData(value, baseline, configMap.get("two-minute")));
		Assert.assertEquals(result.isTriggered(), true);
	}

	@Test
	public void testRule() {
		Map<String, List<Condition>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/demo-rule-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 200, 350 };
		double value[] = { 100, 50 };
		AlertResultEntity result = extractError(m_check.checkData(value, baseline, configMap.get("demo1")));
		Assert.assertEquals(result.isTriggered(), true);
	}

	private AlertResultEntity extractError(List<AlertResultEntity> alertResults) {
		int length = alertResults.size();
		if (length == 0) {
			return null;
		}

		for (AlertResultEntity alertResult : alertResults) {
			if (alertResult.getAlertLevel().equals("error")) {
				return alertResult;
			}
		}

		return alertResults.get(length - 1);
	}
}
