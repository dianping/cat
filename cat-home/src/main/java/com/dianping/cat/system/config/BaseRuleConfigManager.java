package com.dianping.cat.system.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

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
import com.dianping.cat.report.task.alert.MetricType;
import com.dianping.cat.report.task.alert.RuleType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.site.lookup.util.StringUtils;

public abstract class BaseRuleConfigManager {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected UserDefinedRuleManager m_manager;

	protected int m_configId;

	protected MonitorRules m_config;

	protected Rule copyRule(Rule rule) {
		try {
			Rule result = new Rule(rule.getId());

			return DefaultSaxParser.parseEntity(Rule.class, result.toString());
		} catch (Exception e) {
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
		Gson gson = new Gson();
		String source = gson.toJson(originConfigs);
		List<Config> result = gson.fromJson(source, new TypeToken<List<Config>>() {
		}.getType());

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

	protected abstract String getConfigName();

	public MonitorRules getMonitorRules() {
		return m_config;
	}

	private List<com.dianping.cat.home.rule.entity.Config> getMaxPriorityConfigs(
	      Map<Integer, List<com.dianping.cat.home.rule.entity.Config>> configs) {
		Set<Integer> keys = configs.keySet();
		int maxKey = 0;

		for (int key : keys) {
			if (key > maxKey) {
				maxKey = key;
			}
		}

		List<com.dianping.cat.home.rule.entity.Config> finalConfigs = configs.get(maxKey);

		if (finalConfigs == null) {
			finalConfigs = new ArrayList<com.dianping.cat.home.rule.entity.Config>();
		}
		return finalConfigs;
	}

	private List<Rule> getMaxPriorityRules(Map<Integer, List<Rule>> rules) {
		Set<Integer> keys = rules.keySet();
		int maxKey = 0;

		for (int key : keys) {
			if (key > maxKey) {
				maxKey = key;
			}
		}

		List<Rule> rules = rules.get(maxKey);

		if (rules == null) {
			rules = new ArrayList<Rule>();
		}
		return rules;
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

	public List<com.dianping.cat.home.rule.entity.Config> queryConfigs(String groupText, String metricText) {
		return queryConfigs(groupText, metricText, null, FindRulePolicy.NORMAL);
	}

	public List<com.dianping.cat.home.rule.entity.Config> queryConfigs(String product, String metricKey, MetricType type) {
		return queryConfigs(product, metricKey, type, FindRulePolicy.BY_METRIC_TYPE);
	}

	public List<com.dianping.cat.home.rule.entity.Config> queryMaxPriorityConfigs(String product, String metricKey, MetricType type) {
		Map<Integer,List<Config>> result = new HashMap<Integer,List<Config>>();

		for (Rule rule : m_config.getRules().values()) {
			List<MetricItem> items = rule.getMetricItems();

			for (MetricItem item : items) {
				String configProduct = item.getProductText();
				String configMetricKey = item.getMetricItemText();
				int matchLevel = 0;

				if (type == null) {
					matchLevel = validate(configProduct, configMetricKey, product, metricKey);
				} else {
					if (type == MetricType.COUNT && item.isMonitorCount()) {
						matchLevel = validate(configProduct, configMetricKey, product, metricKey);
					} else if (type == MetricType.AVG && item.isMonitorAvg()) {
						matchLevel = validate(configProduct, configMetricKey, product, metricKey);
					} else if (type == MetricType.SUM && item.isMonitorSum()) {
						matchLevel = validate(configProduct, configMetricKey, product, metricKey);
					}
				}

				if (matchLevel > 0) {
					List<Config> rules = result.get(matchLevel);
					
					if(rules==null){
						rules= new ArrayList<com.dianping.cat.home.rule.entity.Config>();
						result.put(matchLevel, rules);
					}
					
					rules.addAll(rule.getConfigs());
				}
			}
		}
		List<Config> configs = getMaxPriorityConfigs(result);
		
		
		return decorateConfigOnRead(configs);
	}

	private List<com.dianping.cat.home.rule.entity.Config> queryConfigs(String product, String metricKey,
	      MetricType type, FindRulePolicy policy) {
		Map<Integer, List<com.dianping.cat.home.rule.entity.Config>> configs = new HashMap<Integer, List<com.dianping.cat.home.rule.entity.Config>>();

		for (Rule rule : m_config.getRules().values()) {
			List<MetricItem> items = rule.getMetricItems();

			for (MetricItem item : items) {
				String productText = item.getProductText();
				String metricItemText = item.getMetricItemText();
				int matchLevel = 0;

				if (policy == FindRulePolicy.NORMAL) {
					matchLevel = validate(productText, metricItemText, product, metricKey);
				} else if (policy == FindRulePolicy.BY_METRIC_TYPE) {
					if (type == MetricType.COUNT && item.isMonitorCount()) {
						matchLevel = validate(productText, metricItemText, product, metricKey);
					} else if (type == MetricType.AVG && item.isMonitorAvg()) {
						matchLevel = validate(productText, metricItemText, product, metricKey);
					} else if (type == MetricType.SUM && item.isMonitorSum()) {
						matchLevel = validate(productText, metricItemText, product, metricKey);
					}
				} else if (policy == FindRulePolicy.ALL_BY_GROUP) {
					matchLevel = validateRegex(productText, product) > 0 ? 1 : 0;
				}

				if (matchLevel > 0) {
					List<com.dianping.cat.home.rule.entity.Config> configList = configs.get(matchLevel);

					if (configList == null) {
						configList = new ArrayList<com.dianping.cat.home.rule.entity.Config>();

						configs.put(matchLevel, configList);
					}
					configList.addAll(rule.getConfigs());
					Cat.logEvent("FindRule:" + getConfigName(), rule.getId(), Event.SUCCESS, productText);
					break;
				}
			}
		}

		List<com.dianping.cat.home.rule.entity.Config> finalConfigs = getMaxPriorityConfigs(configs);

		return decorateConfigOnRead(finalConfigs);
	}

	public List<com.dianping.cat.home.rule.entity.Config> queryAllConfigsByGroup(String groupText) {
		return queryConfigs(groupText, null, null, FindRulePolicy.ALL_BY_GROUP);
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

	private void transformConfig(Config config) {
		for (Condition condition : config.getConditions()) {
			for (SubCondition subCondition : condition.getSubConditions()) {
				if (RuleType.UserDefine.getId().equals(subCondition.getType())) {
					String userDefineText = subCondition.getText();
					subCondition.setText(userDefineText.replaceAll("\"", "\\\\\""));
				}
			}
		}
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

	public static enum FindRulePolicy {
		ALL_BY_GROUP, BY_METRIC_TYPE, NORMAL;
	}

}
