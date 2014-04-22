package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregation;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregationConfig;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregationGroup;
import com.dianping.cat.home.metricAggregation.transform.DefaultSaxParser;

public class MetricAggregationConfigManager  implements Initializable {

		@Inject
		private ConfigDao m_configDao;

		private int m_configId;

		private MetricAggregationConfig m_config;

		private static final String CONFIG_NAME = "metricAggregationConfig";

		public MetricAggregationConfig getMetricAggregationConfig() {
			return m_config;
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
					      this.getClass().getResourceAsStream("/config/default-metric-aggregation-config.xml"), "utf-8");
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
				m_config = new MetricAggregationConfig();
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

		public List<MetricAggregation> queryMetricAggregationGroup(String group) {
			MetricAggregationGroup metricAggregationGroup = m_config.findMetricAggregationGroup(group);

			if (metricAggregationGroup == null) {
				return new ArrayList<MetricAggregation>();
			} else {
				return metricAggregationGroup.getMetricAggregations();
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

