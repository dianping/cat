package com.dianping.cat.report.alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.tuple.Triple;

import com.dianping.cat.Cat;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.report.task.alert.DataChecker;
import com.dianping.cat.report.task.alert.DefaultDataChecker;

public class RuleConfigTest {

	private DataChecker m_check = new DefaultDataChecker();

	private List<Condition> buildConditions(List<Config> configs) {
		List<Condition> conditions = new ArrayList<Condition>();
		
		for(Config config : configs){
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
			}else{
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
		Triple<Boolean, String, String> result;

		Assert.assertNotNull(conditionsMap);

		double[] baseline7 = { 200, 200 };
		double[] value7 = { 100, 100 };
		result = m_check.checkData(value7, baseline7, conditionsMap.get("conditionCombination"));
		Assert.assertEquals(result.getFirst().booleanValue(), true);

		double[] baseline8 = { 200, 200 };
		double[] value8 = { 100, 100 };
		result = m_check.checkData(value8, baseline8, conditionsMap.get("subconditionCombination"));
		Assert.assertEquals(result.getFirst().booleanValue(), false);
	}

	@Test
	public void testMinute() {
		Map<String, List<Condition>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/test-minute-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 50, 200, 200 };
		double value[] = { 50, 100, 100 };
		Triple<Boolean, String, String> result = m_check.checkData(value, baseline, configMap.get("two-minute"));
		Assert.assertEquals(result.getFirst().booleanValue(), true);
	}

	@Test
	public void testRule() {
		Map<String, List<Condition>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/demo-rule-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 200, 350 };
		double value[] = { 100, 50 };
		Triple<Boolean, String, String> result = m_check.checkData(value, baseline, configMap.get("demo1"));
		Assert.assertEquals(result.getFirst().booleanValue(), true);
	}
}
