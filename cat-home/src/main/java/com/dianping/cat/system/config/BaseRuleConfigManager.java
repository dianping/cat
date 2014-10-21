package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

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

	protected Rule copyRuleWithDeepCopyConditions(Rule rule) {
		Rule result = new Rule(rule.getId());

		for (MetricItem item : rule.getMetricItems()) {
			result.addMetricItem(item);
		}
		for (Config config : decorateConfigOnRead(rule.getConfigs())) {
			transformConfig(config);
			result.addConfig(config);
		}
		return result;
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
		List<com.dianping.cat.home.rule.entity.Config> configs = new ArrayList<com.dianping.cat.home.rule.entity.Config>();

		for (Rule rule : m_config.getRules().values()) {
			List<MetricItem> metricItems = rule.getMetricItems();

			for (MetricItem metricItem : metricItems) {
				String productPattern = metricItem.getProductText();
				String metrciPattern = metricItem.getMetricItemText();

				if (validate(productPattern, metrciPattern, groupText, metricText)) {
					configs.addAll(rule.getConfigs());
					Cat.logEvent("FindRule:" + getConfigName(), rule.getId(), Event.SUCCESS, groupText);
					break;
				}
			}
		}
		return decorateConfigOnRead(configs);
	}

	public List<com.dianping.cat.home.rule.entity.Config> queryConfigs(String product, String metricKey, MetricType type) {
		List<com.dianping.cat.home.rule.entity.Config> configs = new ArrayList<com.dianping.cat.home.rule.entity.Config>();

		for (Rule rule : m_config.getRules().values()) {
			List<MetricItem> items = rule.getMetricItems();

			for (MetricItem item : items) {
				String productText = item.getProductText();
				String metricItemText = item.getMetricItemText();
				boolean validate = false;

				if (type == MetricType.COUNT && item.isMonitorCount()) {
					validate = validate(productText, metricItemText, product, metricKey);
				} else if (type == MetricType.AVG && item.isMonitorAvg()) {
					validate = validate(productText, metricItemText, product, metricKey);
				} else if (type == MetricType.SUM && item.isMonitorSum()) {
					validate = validate(productText, metricItemText, product, metricKey);
				}

				if (validate) {
					configs.addAll(rule.getConfigs());
					Cat.logEvent("FindRule:" + getConfigName(), rule.getId(), Event.SUCCESS, product + "," + metricKey);
					break;
				}
			}
		}
		return decorateConfigOnRead(configs);
	}

	public List<com.dianping.cat.home.rule.entity.Config> queryConfigsByGroup(String groupText) {
		List<com.dianping.cat.home.rule.entity.Config> configs = new ArrayList<com.dianping.cat.home.rule.entity.Config>();

		for (Rule rule : m_config.getRules().values()) {
			List<MetricItem> metricItems = rule.getMetricItems();

			for (MetricItem metricItem : metricItems) {
				String productPattern = metricItem.getProductText();

				if (validateRegex(productPattern, groupText)) {
					configs.addAll(rule.getConfigs());
					break;
				}
			}
		}
		return decorateConfigOnRead(configs);
	}

	public Rule queryRule(String key) {
		Rule rule = m_config.getRules().get(key);

		if (rule != null) {
			return copyRuleWithDeepCopyConditions(rule);
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

	public boolean validate(String productText, String metricKeyText, String product, String metricKey) {
		if (validateRegex(productText, product)) {
			return validateRegex(metricKeyText, metricKey);
		} else {
			return false;
		}
	}

	public boolean validateRegex(String regexText, String text) {
		if (StringUtils.isEmpty(regexText)) {
			return true;
		}

		Pattern p = Pattern.compile(regexText);
		Matcher m = p.matcher(text);

		if (m.find()) {
			return true;
		} else {
			return false;
		}
	}

}
