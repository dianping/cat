package com.dianping.cat.config.web.js;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.web.js.entity.Aggregation;
import com.dianping.cat.configuration.web.js.entity.AggregationRule;
import com.dianping.cat.configuration.web.js.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;

public class AggregationConfigManager implements Initializable {
	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected AggregationHandler m_handler;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private static final String CONFIG_NAME = "aggreationConfig";

	private volatile Aggregation m_aggregation;

	private long m_modifyTime;

	public static final int PROBLEM_TYPE = 3;

	public boolean deleteAggregationRule(String rule) {
		m_aggregation.removeAggregationRule(rule);
		m_handler.register(queryAggregationRules());
		return storeConfig();
	}

	public String handle(int type, String domain, String status) {
		return m_handler.handle(type, domain, status);
	}

	@Override
	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_aggregation = DefaultSaxParser.parse(content);
			m_modifyTime = config.getModifyDate().getTime();
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_configId = config.getId();
				m_aggregation = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_aggregation == null) {
			m_aggregation = new Aggregation();
		}
		m_handler.register(queryAggregationRules());

		Threads.forGroup("cat").start(new ConfigReloadTask());
	}

	public boolean insertAggregationRule(AggregationRule rule) {
		m_aggregation.addAggregationRule(rule);
		m_handler.register(queryAggregationRules());
		return storeConfig();
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

	public List<AggregationRule> queryAggregationRules() {
		return new ArrayList<AggregationRule>(m_aggregation.getAggregationRules().values());
	}

	public void refreshAggreationConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				Aggregation aggregation = DefaultSaxParser.parse(content);

				m_aggregation = aggregation;
				m_handler.register(queryAggregationRules());
				m_modifyTime = modifyTime;
			}
		}
	}

	public void refreshRule() {
		List<AggregationRule> rules = queryAggrarationRulesFromDB();

		m_handler.register(rules);
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

	public class ConfigReloadTask implements Task {

		@Override
		public String getName() {
			return "Aggreation-Config-Reload";
		}

		@Override
		public void run() {
			boolean active = true;
			while (active) {
				try {
					refreshAggreationConfig();
				} catch (Exception e) {
					Cat.logError(e);
				}
				try {
					Thread.sleep(60 * 1000L);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
