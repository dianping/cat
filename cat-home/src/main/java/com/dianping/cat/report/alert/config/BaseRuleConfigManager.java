package com.dianping.cat.report.alert.config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.home.rule.entity.MetricItem;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.entity.SubCondition;
import com.dianping.cat.home.rule.transform.DefaultJsonParser;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.alert.AlarmRule;
import com.dianping.cat.report.alert.MetricType;
import com.dianping.cat.report.alert.RuleType;

public abstract class BaseRuleConfigManager {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected UserDefinedRuleManager m_manager;

	protected int m_configId;

	protected MonitorRules m_config;

	private int calMaxNum(Set<Integer> nums) {
		int max = 0;

		for (int n : nums) {
			if (n > max) {
				max = n;
			}
		}
		return max;
	}

	private boolean checkTimeValidate(Config config) {
		try {
			if (compareTime(config.getStarttime(), config.getEndtime())) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			Cat.logError("throw exception when judge time: " + config.toString(), ex);
			return false;
		}
	}

	public boolean compareTime(String start, String end) {
		String[] startTime = start.split(":");
		int hourStart = Integer.parseInt(startTime[0]);
		int minuteStart = Integer.parseInt(startTime[1]);
		int startMinute = hourStart * 60 + minuteStart;

		String[] endTime = end.split(":");
		int hourEnd = Integer.parseInt(endTime[0]);
		int minuteEnd = Integer.parseInt(endTime[1]);
		int endMinute = hourEnd * 60 + minuteEnd;

		Calendar cal = Calendar.getInstance();
		int current = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

		return current >= startMinute && current <= endMinute;
	}

	public Pair<Integer, List<Condition>> convertConditions(List<Config> configs) {
		int maxMinute = 0;
		List<Condition> conditions = new ArrayList<Condition>();
		Iterator<Config> iterator = configs.iterator();

		while (iterator.hasNext()) {
			Config config = iterator.next();

			if (checkTimeValidate(config)) {
				List<Condition> tmpConditions = config.getConditions();
				conditions.addAll(tmpConditions);

				for (Condition con : tmpConditions) {
					int tmpMinute = con.getMinute();

					if (tmpMinute > maxMinute) {
						maxMinute = tmpMinute;
					}
				}
			}
		}

		if (maxMinute > 0) {
			return new Pair<Integer, List<Condition>>(maxMinute, conditions);
		} else {
			return null;
		}
	}

	protected Rule copyRule(Rule rule) {
		try {
			return DefaultSaxParser.parseEntity(Rule.class, rule.toString());
		} catch (Exception e) {
			Cat.logError(e);
			return null;
		}
	}

	protected void decorateConfigOnDelete(List<Config> configs) {
		for (Config config : configs) {
			for (Condition condition : config.getConditions()) {
				for (SubCondition subCondition : condition.getSubConditions()) {
					if (RuleType.UserDefine.getId().equals(subCondition.getType())) {
						try {
							String id = subCondition.getText();

							m_manager.removeById(id);
						} catch (DalException e) {
							Cat.logError(e);
						}
					}
				}
			}
		}
	}

	protected List<Config> decorateConfigOnRead(List<Config> originConfigs) {
		List<Config> configs = deepCopy(originConfigs);

		for (Config config : configs) {
			for (Condition condition : config.getConditions()) {
				for (SubCondition subCondition : condition.getSubConditions()) {
					if (RuleType.UserDefine.getId().equals(subCondition.getType())) {
						try {
							String id = subCondition.getText();

							subCondition.setText(m_manager.getUserDefineText(id));
						} catch (DalException e) {
							Cat.logError(e);
						}
					}
				}
			}
		}
		return configs;
	}

	protected Map<MetricType, List<Config>> decorateConfigOnRead(Map<MetricType, List<Config>> originConfigs) {
		Map<MetricType, List<Config>> configs = new HashMap<MetricType, List<Config>>();

		for (Entry<MetricType, List<Config>> originConfig : originConfigs.entrySet()) {
			configs.put(originConfig.getKey(), decorateConfigOnRead(originConfig.getValue()));
		}
		return configs;
	}

	private void decorateConfigOnStore(List<Config> configs) throws DalException {
		for (Config config : configs) {
			for (Condition condition : config.getConditions()) {
				for (SubCondition subCondition : condition.getSubConditions()) {
					if (RuleType.UserDefine.getId().equals(subCondition.getType())) {
						try {
							String userDefinedText = subCondition.getText();

							subCondition.setText(m_manager.addUserDefineText(userDefinedText));
						} catch (DalException e) {
							Cat.logError(e);
						}
					}
				}
			}
		}
	}

	private List<Config> deepCopy(List<Config> originConfigs) {
		List<Config> result = new ArrayList<Config>();

		for (Config config : originConfigs) {
			try {
				Config copiedConfig = DefaultSaxParser.parseEntity(Config.class, config.toString());

				result.add(copiedConfig);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return result;
	}

	public String deleteRule(String key) {
		Rule rule = m_config.getRules().get(key);

		if (rule != null) {
			decorateConfigOnDelete(rule.getConfigs());
			m_config.getRules().remove(key);
		}
		return m_config.toString();
	}

	private void extractConifgsByProduct(String product, Rule rule,
	      Map<String, Map<Integer, Map<MetricType, List<Config>>>> configs) {
		List<MetricItem> items = rule.getMetricItems();

		for (MetricItem item : items) {
			String configProduct = item.getProductText();
			String configMetricKey = item.getMetricItemText();
			int matchLevel = validateRegex(configProduct, product);

			if (matchLevel > 0) {
				Map<Integer, Map<MetricType, List<Config>>> configsByPriority = configs.get(configMetricKey);

				if (configsByPriority == null) {
					configsByPriority = new HashMap<Integer, Map<MetricType, List<Config>>>();

					configs.put(configMetricKey, configsByPriority);
				}

				Map<MetricType, List<Config>> configsByType = new HashMap<MetricType, List<Config>>();

				if (item.isMonitorAvg()) {
					configsByType.put(MetricType.AVG, rule.getConfigs());
				}
				if (item.isMonitorCount()) {
					configsByType.put(MetricType.COUNT, rule.getConfigs());
				}
				if (item.isMonitorSum()) {
					configsByType.put(MetricType.SUM, rule.getConfigs());
				}
				configsByPriority.put(matchLevel, configsByType);
			}
		}
	}

	private Map<String, Map<MetricType, List<Config>>> extractMaxPriorityConfigs(
	      Map<String, Map<Integer, Map<MetricType, List<Config>>>> configs) {
		Map<String, Map<MetricType, List<Config>>> result = new HashMap<String, Map<MetricType, List<Config>>>();

		for (Entry<String, Map<Integer, Map<MetricType, List<Config>>>> entry : configs.entrySet()) {
			String metirc = entry.getKey();
			Map<Integer, Map<MetricType, List<Config>>> priorityMap = entry.getValue();
			int maxPriority = calMaxNum(priorityMap.keySet());
			Map<MetricType, List<Config>> configsByType = priorityMap.get(maxPriority);

			result.put(metirc, decorateConfigOnRead(configsByType));
			Cat.logEvent("FindRule:" + getConfigName(), metirc, Event.SUCCESS, null);
		}
		return result;
	}

	protected abstract String getConfigName();

	protected List<Rule> getMaxPriorityRules(Map<Integer, List<Rule>> rules) {
		Set<Integer> keys = rules.keySet();
		int maxKey = 0;

		for (int key : keys) {
			if (key > maxKey) {
				maxKey = key;
			}
		}

		List<Rule> finalRules = rules.get(maxKey);

		if (finalRules == null) {
			finalRules = new ArrayList<Rule>();
		}
		return finalRules;
	}

	public MonitorRules getMonitorRules() {
		return m_config;
	}

	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	public AlarmRule queryConfigs(String product) {
		Map<String, Map<Integer, Map<MetricType, List<Config>>>> configs = new HashMap<String, Map<Integer, Map<MetricType, List<Config>>>>();

		for (Rule rule : m_config.getRules().values()) {
			extractConifgsByProduct(product, rule, configs);
		}
		Map<String, Map<MetricType, List<Config>>> maxPriority = extractMaxPriorityConfigs(configs);

		return new AlarmRule(maxPriority);
	}

	public Rule queryRule(String key) {
		Rule rule = m_config.getRules().get(key);

		if (rule != null) {
			return copyRule(rule);
		} else {
			return null;
		}
	}

	protected boolean storeConfig() {
		synchronized (this) {
			try {
				com.dianping.cat.core.config.Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(getConfigName());
				config.setContent(m_config.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

	public String updateRule(String id, String metricsStr, String configsStr) throws Exception {
		Rule rule = new Rule(id);
		List<MetricItem> metricItems = DefaultJsonParser.parseArray(MetricItem.class, metricsStr);
		List<Config> configs = DefaultJsonParser.parseArray(Config.class, configsStr);
		for (MetricItem metricItem : metricItems) {
			rule.addMetricItem(metricItem);
		}
		for (Config config : configs) {
			rule.addConfig(config);
		}
		decorateConfigOnStore(rule.getConfigs());
		m_config.getRules().put(id, rule);
		return m_config.toString();
	}

	public int validate(String productText, String metricKeyText, String product, String metricKey) {
		int groupMatchResult = validateRegex(productText, product);

		if (groupMatchResult > 0) {
			int metricMatchResult = validateRegex(metricKeyText, metricKey);

			if (metricMatchResult > 0) {
				return groupMatchResult;
			}
		}
		return 0;
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
