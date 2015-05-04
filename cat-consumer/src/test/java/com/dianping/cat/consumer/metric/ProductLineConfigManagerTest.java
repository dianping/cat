package com.dianping.cat.consumer.metric;

import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

import com.dianping.cat.consumer.MockLog;
import com.dianping.cat.consumer.company.model.entity.Company;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.config.ProductLineConfig;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;

public class ProductLineConfigManagerTest {

	private static int s_storeCount;

	@Test
	public void testInitNormal() throws Exception {
		ProductLineConfigManager manager = new MockProductLineConfigManager();

		((MockProductLineConfigManager) manager).setConfigDao(new MockConfigDao1());
		manager.initialize();

		ProductLine line1 = new ProductLine("Test1");
		ProductLine line2 = new ProductLine("Test2");
		String[] domains1 = { "domain1", "domain2" };
		String[] domains2 = { "domain3", "domain4" };

		manager.insertProductLine(line1, domains1, ProductLineConfig.METRIC.getTitle());
		manager.insertProductLine(line2, domains2, ProductLineConfig.METRIC.getTitle());

		Assert.assertEquals(2, s_storeCount);
		Assert.assertEquals(null, manager.queryProductLineByDomain("domain"));
		Assert.assertEquals("Test1", manager.queryProductLineByDomain("domain1"));
		List<String> pDomains = manager.queryDomainsByProductLine("Test1", ProductLineConfig.METRIC);
		Assert.assertEquals(2, pDomains.size());
		Map<String, ProductLine> productLines = manager.queryAllProductLines();

		Assert.assertEquals(3, productLines.size());

		manager.enableLogging(new MockLog());
		manager.refreshConfig();
		productLines = manager.queryAllProductLines();
		Assert.assertEquals(1, productLines.size());
	}

	@Test
	public void testInitThrowException() throws Exception {
		for (ProductLineConfig productLineConfig : ProductLineConfig.values()) {
			productLineConfig.getCompany().getProductLines().clear();
		}

		ProductLineConfigManager manager = new MockProductLineConfigManager();
		((MockProductLineConfigManager) manager).setConfigDao(new MockConfigDao2());

		try {
			manager.initialize();
		} catch (Exception e) {
		}

		for (ProductLineConfig productLineConfig : ProductLineConfig.values()) {
			Company config = productLineConfig.getCompany();

			Assert.assertEquals(0, config.getProductLines().size());
		}
	}

	public static class MockProductLineConfigManager extends ProductLineConfigManager {

		public void setConfigDao(ConfigDao configDao) {
			m_configDao = configDao;
		}

	}

	public static class MockConfigDao2 extends MockConfigDao1 {
		@Override
		public Config findByName(String name, Readset<Config> readset) throws DalNotFoundException {
			if (ProductLineConfigManager.CONFIG_NAME.equals(name)) {
				return new Config();
			} else {
				throw new DalNotFoundException("this is a exception for test");
			}
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
			config.setModifyDate(new Date());
			config.setContent(new Company().toString());
			return config;
		}

		@Override
		public Config findByName(String name, Readset<Config> readset) throws DalException {
			Config config = new Config();

			config.setName(name);
			Company company = new Company();
			ProductLine productLine = new ProductLine("Test");

			company.addProductLine(productLine);
			config.setContent(company.toString());
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
}
