package com.dianping.cat.report.alert.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.report.alert.config.BaseRuleConfigManager;
import com.dianping.cat.report.page.storage.StorageConstants;

public abstract class StorageRuleConfigManager extends BaseRuleConfigManager implements Initializable {

	@Inject
	private ContentFetcher m_fetcher;

	private Map<String, RuleMappingConfig> m_ruleMappings = new HashMap<String, RuleMappingConfig>();

	protected abstract String getConfigName();

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(getConfigName(), ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_config = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(getConfigName());
				Config config = m_configDao.createLocal();

				config.setName(getConfigName());
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new MonitorRules();
		}
		refreshData();
	}

	private void refreshData() {
		Map<String, Rule> rules = m_config.getRules();
		Map<String, RuleMappingConfig> mapping = new HashMap<String, RuleMappingConfig>();

		for (Entry<String, Rule> entry : rules.entrySet()) {
			String ruleId = entry.getValue().getId();
			String[] conditions = ruleId.split(StorageConstants.FIELD_SEPARATOR);

			if (conditions.length >= 4) {
				String name = conditions[0];
				String machine = conditions[1];
				String operation = conditions[2];
				String attribute = conditions[3];
				RuleMappingConfig ruleMappingConfig = mapping.get(name);

				if (ruleMappingConfig == null) {
					ruleMappingConfig = new RuleMappingConfig(name);

					mapping.put(name, ruleMappingConfig);
				}
				IpMappingConfig ip = ruleMappingConfig.findOrCreate(machine);
				OperationConfig op = ip.findOrCreate(operation);
				op.addRule(attribute, entry.getValue());
			} else {
				Cat.logError(new RuntimeException("Unrecognized " + getConfigName() + " rule size != 4 : " + ruleId));
			}
		}
		m_ruleMappings = mapping;
	}

	@Override
	protected boolean storeConfig() {
		boolean success = super.storeConfig();

		if (success) {
			refreshData();
		}
		return success;
	}

	public List<Rule> findRules(String name, String machine) {
		List<Rule> rules = new ArrayList<Rule>();
		RuleMappingConfig ruleMapping = m_ruleMappings.get(name);

		if (ruleMapping == null) {
			ruleMapping = m_ruleMappings.get(Constants.ALL);
		}

		if (ruleMapping != null) {
			IpMappingConfig ipMapping = ruleMapping.find(machine);

			if (ipMapping == null) {
				ipMapping = ruleMapping.find(Constants.ALL);
			}

			if (ipMapping != null) {
				Map<String, OperationConfig> operations = ipMapping.getOperations();

				for (OperationConfig operation : operations.values()) {
					rules.addAll(operation.getRules().values());
				}
			}
		}
		return rules;
	}

	public static class RuleMappingConfig {

		private String m_domain;

		private Map<String, IpMappingConfig> m_ips = new HashMap<String, IpMappingConfig>();

		public RuleMappingConfig(String domain) {
			m_domain = domain;
		}

		public String getDomain() {
			return m_domain;
		}

		public IpMappingConfig findOrCreate(String ip) {
			IpMappingConfig config = m_ips.get(ip);

			if (config != null) {
				return config;
			} else {
				config = new IpMappingConfig(ip);

				m_ips.put(ip, config);
				return config;
			}
		}

		public IpMappingConfig find(String ip) {
			return m_ips.get(ip);
		}
	}

	public static class IpMappingConfig {

		private String m_ip;

		private Map<String, OperationConfig> m_operations = new HashMap<String, OperationConfig>();

		public IpMappingConfig(String ip) {
			m_ip = ip;
		}

		public String getIp() {
			return m_ip;
		}

		public Map<String, OperationConfig> getOperations() {
			return m_operations;
		}

		public OperationConfig findOrCreate(String operation) {
			OperationConfig config = m_operations.get(operation);

			if (config != null) {
				return config;
			} else {
				config = new OperationConfig(operation);

				m_operations.put(operation, config);
				return config;
			}
		}

		public OperationConfig find(String operation) {
			return m_operations.get(operation);
		}
	}

	public static class OperationConfig {

		private String m_operation;

		private Map<String, Rule> m_rules = new HashMap<String, Rule>();

		public OperationConfig(String operation) {
			m_operation = operation;
		}

		public String getOperation() {
			return m_operation;
		}

		public Map<String, Rule> getRules() {
			return m_rules;
		}

		public void addRule(String operation, Rule rule) {
			m_rules.put(operation, rule);
		}

	}

}
