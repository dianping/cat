package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.webres.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.core.config.Config;
import com.dianping.cat.consumer.core.config.ConfigDao;
import com.dianping.cat.consumer.core.config.ConfigEntity;
import com.dianping.cat.home.aggreation.entity.Aggregation;
import com.dianping.cat.home.aggreation.entity.AggregationRule;
import com.dianping.cat.home.aggreation.transform.DefaultSaxParser;

public class AggregationConfigManager implements Initializable {
	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private static final String CONFIG_NAME = "aggreationConfig";

	private Aggregation m_aggregation;

	@Override
	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			System.out.println(content);
			m_aggregation = DefaultSaxParser.parse(content);
			m_configId = config.getId();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-aggregation-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_aggregation = DefaultSaxParser.parse(content);
				m_configId = config.getId();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_aggregation == null) {
			m_aggregation = new Aggregation();
		}
	}

	private boolean storeConfig() {
		try {
			Config config = m_configDao.createLocal();

			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(m_aggregation.toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	public List<AggregationRule> queryAggrarationRules() {
		return new ArrayList<AggregationRule>(m_aggregation.getAggregationRules().values());
	}

	private Aggregation queryAggreation() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			return DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new Aggregation();
	}

	public List<AggregationRule> queryAggrarationRulesFromDB() {
		try {
			m_aggregation = queryAggreation();

			return new ArrayList<AggregationRule>(m_aggregation.getAggregationRules().values());
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new ArrayList<AggregationRule>();
	}

	public AggregationRule queryAggration(String key) {
		return m_aggregation.findAggregationRule(key);
	}

	public boolean insertAggregationRule(AggregationRule rule) {
		m_aggregation.addAggregationRule(rule);
		return storeConfig();
	}

	public boolean deleteAggregationRule(String rule) {
		m_aggregation.removeAggregationRule(rule);
		return storeConfig();
	}
}
