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
package com.dianping.cat.system.page.config.processor;

import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class EventConfigProcessor extends BaseProcesser {

	@Inject
	private EventRuleConfigManager m_configManager;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case EVENT_RULE:
			Map<String, Rule> ruleMap = m_configManager.getMonitorRules().getRules();
			rulesAvailableBuild(ruleMap);
			model.setRules(ruleMap.values());
			break;
		case EVENT_RULE_ADD_OR_UPDATE:
			generateRuleConfigContent(payload.getRuleId(), m_configManager, model);
			break;
		case EVENT_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(m_configManager, payload.getRuleId(), "",
					payload.getConfigs(), payload.getAvailable()));
			model.setRules(m_configManager.getMonitorRules().getRules().values());
			break;
		case EVENT_RULE_DELETE:
			model.setOpState(deleteRule(m_configManager, payload.getRuleId()));
			model.setRules(m_configManager.getMonitorRules().getRules().values());
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

	//增加告警开关功能，但是线上并无available值，这里做一个兼容
	private void rulesAvailableBuild(Map<String, Rule> ruleMap) {
		if (ruleMap == null || ruleMap.isEmpty()) {
			return;
		}
		for (Rule rule : ruleMap.values()) {
			if (null == rule.getAvailable()) {
				rule.setAvailable(true);
			}
		}
	}
}
