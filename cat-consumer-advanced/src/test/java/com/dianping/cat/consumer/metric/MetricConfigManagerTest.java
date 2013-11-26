package com.dianping.cat.consumer.metric;

import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

import com.dianping.cat.advanced.metric.config.entity.MetricConfig;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.MetricAnalyzer.ConfigItem;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;

public class MetricConfigManagerTest {

	private static int s_storeCount;

	@Test
	public void testInitNormal() throws Exception {
		MockMetricConfigManager manager = new MockMetricConfigManager();

		manager.setConfigDao(new MockConfigDao1());
		manager.initialize();

		String domain1 = "domain1";
		String domain2 = "domain2";
		manager.insertIfNotExist(domain1, "type", "metricKey", new ConfigItem());

		Assert.assertEquals(1, s_storeCount);
		manager.insertIfNotExist(domain1, "type", "metricKey", new ConfigItem());
		Assert.assertEquals(1, s_storeCount);
		manager.insertIfNotExist(domain2, "type", "metricKey", new ConfigItem());
		Assert.assertEquals(2, s_storeCount);
		manager.insertMetricItemConfig(new MetricItemConfig());
		Assert.assertEquals(3, s_storeCount);

		MetricItemConfig item = manager.queryMetricItemConfig(manager.buildMetricKey(domain1, "type", "metricKey"));
		Assert.assertEquals(true, item != null);
		HashSet<String> hashSet = new HashSet<String>();
		hashSet.add(domain1);
		hashSet.add(domain2);
		List<MetricItemConfig> sets = manager.queryMetricItemConfigs(hashSet);
		Assert.assertEquals(2, sets.size());
	}

	@Test
	public void testInitThrowException() throws Exception {
		MockMetricConfigManager manager = new MockMetricConfigManager();

		manager.setConfigDao(new MockConfigDao2());
		manager.initialize();

		MetricConfig config = manager.getMetricConfig();

		Assert.assertEquals(0, config.getMetricItemConfigs().size());
	}

	public static class MockMetricConfigManager extends MetricConfigManager {

		public void setConfigDao(ConfigDao configDao) {
			m_configDao = configDao;
		}

	}

	public static class MockConfigDao2 extends MockConfigDao1 {
		@Override
		public Config findByName(String name, Readset<Config> readset) throws DalException {
			throw new DalException("this is a exception for test");
		}
	}

	public static class MockConfigDao1 extends ConfigDao {

		@Override
		public Config createLocal() {
			return super.createLocal();
		}

		@Override
		public int deleteByPK(Config proto) throws DalException {
			return 1;
		}

		@Override
		public Config findByPK(int keyId, Readset<Config> readset) throws DalException {
			Config config = new Config();

			config.setId(keyId);
			config.setContent(new MetricConfig().toString());
			return config;
		}

		@Override
		public Config findByName(String name, Readset<Config> readset) throws DalException {
			Config config = new Config();

			config.setName(name);
			MetricConfig metricConfig = new MetricConfig();

			MetricItemConfig metricItemConfig = new MetricItemConfig();

			metricItemConfig.setDomain("domain");
			metricItemConfig.setMetricKey("domain:URL:metricKey");
			metricItemConfig.setType("URL");
			metricItemConfig.setMetricKey("metricKey");
			metricConfig.addMetricItemConfig(metricItemConfig);
			config.setContent(metricConfig.toString());
			return config;
		}

		@Override
		public int insert(Config proto) throws DalException {
			return 1;
		}

		@Override
		public int updateByPK(Config proto, Updateset<Config> updateset) throws DalException {
			s_storeCount++;
			return 1;
		}
	}
}
