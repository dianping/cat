package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.monitorrules.entity.MetricItem;
import com.dianping.cat.home.monitorrules.entity.MonitorRules;
import com.dianping.cat.home.monitorrules.entity.Rule;
import com.dianping.cat.home.monitorrules.transform.DefaultSaxParser;

public class MetricRuleConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private MonitorRules m_config;

	private static final String CONFIG_NAME = "monitorRulesConfig";

	public Map<String, List<com.dianping.cat.home.monitorrules.entity.Config>> getMetricIdRuleMap() {
		Map<String, List<com.dianping.cat.home.monitorrules.entity.Config>> map = new HashMap<String, List<com.dianping.cat.home.monitorrules.entity.Config>>();
		
		for(Rule rule : m_config.getRules()){
			for(MetricItem metricItem : rule.getMetricItems()){
				String type = metricItem.getType();
				
				if(type==null || !type.equals("id")){
					continue;
				}
				
				String key = metricItem.getText();
				List<com.dianping.cat.home.monitorrules.entity.Config> configs = getOrBuildRuleList(map, key);
				configs.addAll(rule.getConfigs());
			}
		}
		
	   return map;
   }

	public MonitorRules getMonitorRules() {
		return m_config;
	}

	private List<com.dianping.cat.home.monitorrules.entity.Config> getOrBuildRuleList(
         Map<String, List<com.dianping.cat.home.monitorrules.entity.Config>> map, String key) {
		List<com.dianping.cat.home.monitorrules.entity.Config> configs = map.get(key);
		
	   if(configs==null){
	   	configs = new ArrayList<com.dianping.cat.home.monitorrules.entity.Config>();
	   	map.put(key, configs);
	   }
	   return configs;
   }

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();
			
			m_config = DefaultSaxParser.parse(content);
			m_configId = config.getId();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-metric-rule-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_config = DefaultSaxParser.parse(content);
				m_configId = config.getId();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new MonitorRules();
		}
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

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_config.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}
	
}
