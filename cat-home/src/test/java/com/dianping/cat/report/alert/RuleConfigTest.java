/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.transform.DefaultSaxParser;
import com.dianping.cat.alarm.spi.rule.DataCheckEntity;
import com.dianping.cat.alarm.spi.rule.DataChecker;
import com.dianping.cat.alarm.spi.rule.DefaultDataChecker;

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
		Map<String, List<Condition>> conditionsMap = buildConfigMap(
								buildMonitorRuleFromFile("/config/demo-rule-monitor.xml"));
		DataCheckEntity result;

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
		DataCheckEntity result = extractError(m_check.checkData(value, baseline, configMap.get("two-minute")));
		Assert.assertEquals(result.isTriggered(), true);
	}

	@Test
	public void testRule() {
		Map<String, List<Condition>> configMap = buildConfigMap(buildMonitorRuleFromFile("/config/demo-rule-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 200, 350 };
		double value[] = { 100, 50 };
		DataCheckEntity result = extractError(m_check.checkData(value, baseline, configMap.get("demo1")));
		Assert.assertEquals(result.isTriggered(), true);
	}

	private DataCheckEntity extractError(List<DataCheckEntity> alertResults) {
		int length = alertResults.size();
		if (length == 0) {
			return null;
		}

		for (DataCheckEntity alertResult : alertResults) {
			if (alertResult.getAlertLevel().equals("error")) {
				return alertResult;
			}
		}

		return alertResults.get(length - 1);
	}
}
