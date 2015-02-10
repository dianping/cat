package com.dianping.cat.report.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;

public class AlarmRule {
	private Map<String, Map<MetricType, List<Config>>> m_configs;

	public AlarmRule(Map<String, Map<MetricType, List<Config>>> configs) {
		m_configs = configs;
	}

	public int calMaxRuleMinute() {
		int maxMinute = 0;

		for (Map<MetricType, List<Config>> subMap : m_configs.values()) {
			for (List<Config> tmpConfigs : subMap.values()) {
				for (Config config : tmpConfigs) {
					for (Condition condition : config.getConditions()) {
						int tmpMinute = condition.getMinute();

						if (tmpMinute > maxMinute) {
							maxMinute = tmpMinute;
						}
					}
				}
			}
		}
		return maxMinute;
	}

	public List<Map<MetricType, List<Config>>> findDetailRules(String metricName) {
		List<Map<MetricType, List<Config>>> arrays = new ArrayList<Map<MetricType, List<Config>>>();
		
		for (Entry<String, Map<MetricType, List<Config>>> entry : m_configs.entrySet()) {
			String metricPattern = entry.getKey();

			if (validateRegex(metricPattern, metricName) > 0) {
				arrays.add(entry.getValue());
			}
		}
		return arrays;
	}

	public Map<String, Map<MetricType, List<Config>>> getConfigs() {
		return m_configs;
	}

	public void setConfigs(Map<String, Map<MetricType, List<Config>>> configs) {
		this.m_configs = configs;
	}

	/**
	 * @return 0: not match; 1: global match; 2: regex match; 3: full match
	 */
	public int validateRegex(String regexText, String text) {
		if (StringUtils.isEmpty(regexText)) {
			return 1;
		} else if (regexText.equalsIgnoreCase(text)) {
			return 3;
		} else {
			Pattern p = Pattern.compile(regexText);
			Matcher m = p.matcher(text);

			if (m.find()) {
				return 2;
			} else {
				return 0;
			}
		}
	}

}