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
package com.dianping.cat.report.task.metric;

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

public class AlertConfigTest {

	private DataChecker m_checker = new DefaultDataChecker();

	private Map<String, List<com.dianping.cat.alarm.rule.entity.Config>> buildConfigMap(MonitorRules monitorRules) {
		if (monitorRules == null || monitorRules.getRules().size() == 0) {
			return null;
		}

		Map<String, List<com.dianping.cat.alarm.rule.entity.Config>> map = new HashMap<String, List<com.dianping.cat.alarm.rule.entity.Config>>();

		for (Rule rule : monitorRules.getRules().values()) {
			map.put(rule.getId(), rule.getConfigs());
		}

		return map;
	}

	private List<Condition> buildConditions(List<Config> configs) {
		List<Condition> conditions = new ArrayList<Condition>();

		for (Config config : configs) {
			conditions.addAll(config.getConditions());
		}

		return conditions;
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
	public void testMinute() {
		Map<String, List<com.dianping.cat.alarm.rule.entity.Config>> configMap = buildConfigMap(
								buildMonitorRuleFromFile("/config/test-minute-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 50, 200, 200 };
		double value[] = { 50, 100, 100 };
		DataCheckEntity result = extractError(
								m_checker.checkData(value, baseline,	buildConditions(configMap.get("two-minute"))));
		Assert.assertEquals(result.isTriggered(), true);
	}

	@Test
	public void testRule() {
		Map<String, List<com.dianping.cat.alarm.rule.entity.Config>> configMap = buildConfigMap(
								buildMonitorRuleFromFile("/config/test-rule-monitor.xml"));

		Assert.assertNotNull(configMap);

		double baseline[] = { 200, 200 };
		double value[] = { 100, 100 };
		DataCheckEntity result = extractError(
								m_checker.checkData(value, baseline,	buildConditions(configMap.get("decreasePercentage"))));
		Assert.assertEquals(result.isTriggered(), true);

		double[] baseline2 = { 200, 300 };
		double[] value2 = { 100, 100 };
		result = extractError(m_checker.checkData(value2, baseline2, buildConditions(configMap.get("decreaseValue"))));
		Assert.assertEquals(result.isTriggered(), true);

		double[] baseline3 = { 200, 50 };
		double[] value3 = { 400, 100 };
		result = extractError(m_checker.checkData(value3, baseline3, buildConditions(configMap.get("increasePercentage"))));
		Assert.assertEquals(result.isTriggered(), true);

		double[] baseline4 = { 200, 50 };
		double[] value4 = { 400, 100 };
		result = extractError(m_checker.checkData(value4, baseline4, buildConditions(configMap.get("increaseValue"))));
		Assert.assertEquals(result.isTriggered(), true);

		double[] baseline5 = { 200, 200 };
		double[] value5 = { 500, 600 };
		result = extractError(m_checker.checkData(value5, baseline5, buildConditions(configMap.get("absoluteMaxValue"))));
		Assert.assertEquals(result.isTriggered(), true);

		double[] baseline6 = { 200, 200 };
		double[] value6 = { 50, 40 };
		result = extractError(m_checker.checkData(value6, baseline6, buildConditions(configMap.get("absoluteMinValue"))));
		Assert.assertEquals(result.isTriggered(), true);

		double[] baseline7 = { 200, 200 };
		double[] value7 = { 100, 100 };
		result = extractError(m_checker.checkData(value7, baseline7,	buildConditions(configMap.get("conditionCombination"))));
		Assert.assertEquals(result.isTriggered(), true);

		double[] baseline8 = { 200, 200 };
		double[] value8 = { 100, 100 };
		result = extractError(
								m_checker.checkData(value8, baseline8,	buildConditions(configMap.get("subconditionCombination"))));
		Assert.assertNull(result);
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
