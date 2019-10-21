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
package com.dianping.cat.report.alert.heartbeat;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.MetricItem;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.alert.spi.config.BaseRuleConfigManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.lookup.annotation.Named;

@Named
public class HeartbeatRuleConfigManager extends BaseRuleConfigManager implements Initializable {

	private static final String CONFIG_NAME = "heartbeatRuleConfig";

	private void addRuleToMap(Rule rule, String metric, int priority, Map<String, Map<Integer, List<Rule>>> rules) {
		Map<Integer, List<Rule>> rulesByPriority = rules.get(metric);

		if (rulesByPriority == null) {
			rulesByPriority = new HashMap<Integer, List<Rule>>();

			rules.put(metric, rulesByPriority);
		}

		List<Rule> ruleList = rulesByPriority.get(priority);

		if (ruleList == null) {
			ruleList = new ArrayList<Rule>();

			rulesByPriority.put(priority, ruleList);
		}

		ruleList.add(rule);
	}

	private Map<String, List<com.dianping.cat.alarm.rule.entity.Config>> extractConfigs(String domain,
							Map<String, Map<Integer, List<Rule>>> rulesByMetricPriority) {
		Map<String, List<com.dianping.cat.alarm.rule.entity.Config>> result = new HashMap<String, List<com.dianping.cat.alarm.rule.entity.Config>>();

		for (Entry<String, Map<Integer, List<Rule>>> entry : rulesByMetricPriority.entrySet()) {
			String metric = entry.getKey();
			List<Rule> rules = getMaxPriorityRules(entry.getValue());
			List<com.dianping.cat.alarm.rule.entity.Config> configs = new ArrayList<com.dianping.cat.alarm.rule.entity.Config>();

			for (Rule rule : rules) {
				configs.addAll(rule.getConfigs());

				String nameValuePairs = "product=" + domain + "&metricKey=" + metric;
				Cat.logEvent("FindRule:" + getConfigName(), rule.getId(), Event.SUCCESS, nameValuePairs);
			}
			result.put(metric, configs);
		}
		return result;
	}

	@Override
	protected String getConfigName() {
		return CONFIG_NAME;
	}

	public Map<String, List<com.dianping.cat.alarm.rule.entity.Config>> queryConfigsByDomain(String domain) {
		Map<String, Map<Integer, List<Rule>>> rules = new HashMap<String, Map<Integer, List<Rule>>>();

		for (Rule rule : m_config.getRules().values()) {
			if (rule.getAvailable() != null && !rule.getAvailable()) {
				continue;
			}
			for (MetricItem metricItem : rule.getMetricItems()) {
				String domainPattern = metricItem.getProductText();
				int matchLevel = validateRegex(domainPattern, domain);

				if (matchLevel > 0) {
					String metric = metricItem.getMetricItemText();

					addRuleToMap(rule, metric, matchLevel, rules);
				}
			}
		}
		if (rules.size() == 0) {
			return null;
		}
		return extractConfigs(domain, rules);
	}

}
