package com.dianping.cat.consumer.metric;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.logging.Logger;
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
		MetricConfigManager manager = new MockMetricConfigManager();

		((MockMetricConfigManager) manager).setConfigDao(new MockConfigDao1());
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

		manager.deleteDomainConfig(manager.buildMetricKey(domain1, "type", "metricKey"));
		Assert.assertEquals(4, s_storeCount);
		Assert.assertEquals(2, manager.getMetricConfig().getMetricItemConfigs().size());

		HashSet<String> hashSet = new HashSet<String>();
		hashSet.add(domain1);
		hashSet.add(domain2);
		List<MetricItemConfig> sets = manager.queryMetricItemConfigs(hashSet);
		Assert.assertEquals(1, sets.size());
	}

	@Test
	public void testInitThrowException() throws Exception {
		MetricConfigManager manager = new MockMetricConfigManager();

		((MockMetricConfigManager) manager).setConfigDao(new MockConfigDao2());
		manager.enableLogging(new MockLog());
		try {
			manager.initialize();
		} catch (Exception e) {
		}
		MetricConfig config = manager.getMetricConfig();
		Assert.assertEquals(0, config.getMetricItemConfigs().size());
		try {
			manager.refreshMetricConfig();
		} catch (Exception e) {
		}
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
			config.setModifyDate(new Date());
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
			config.setModifyDate(new Date());
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

	public static class MockLog implements Logger {

		@Override
		public void debug(String message) {

		}

		@Override
		public void debug(String message, Throwable throwable) {

		}

		@Override
		public boolean isDebugEnabled() {

			return false;
		}

		@Override
		public void info(String message) {

		}

		@Override
		public void info(String message, Throwable throwable) {

		}

		@Override
		public boolean isInfoEnabled() {

			return false;
		}

		@Override
		public void warn(String message) {

		}

		@Override
		public void warn(String message, Throwable throwable) {

		}

		@Override
		public boolean isWarnEnabled() {

			return false;
		}

		@Override
		public void error(String message) {

		}

		@Override
		public void error(String message, Throwable throwable) {

		}

		@Override
		public boolean isErrorEnabled() {

			return false;
		}

		@Override
		public void fatalError(String message) {

		}

		@Override
		public void fatalError(String message, Throwable throwable) {

		}

		@Override
		public boolean isFatalErrorEnabled() {

			return false;
		}

		@Override
		public Logger getChildLogger(String name) {

			return null;
		}

		@Override
		public int getThreshold() {

			return 0;
		}

		@Override
		public void setThreshold(int threshold) {

		}

		@Override
		public String getName() {

			return null;
		}

	}
}
