package com.dianping.cat.consumer.metric;

import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.plexus.logging.Logger;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

import com.dianping.cat.consumer.company.model.entity.Company;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
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

		manager.insertProductLine(line1, domains1);
		manager.insertProductLine(line2, domains2);

		Assert.assertEquals(2, s_storeCount);
		Assert.assertEquals("Default", manager.queryProductLineByDomain("domain"));
		Assert.assertEquals("Test1", manager.queryProductLineByDomain("domain1"));
		List<String> pDomains = manager.queryProductLineDomains("Test1");
		Assert.assertEquals(2, pDomains.size());
		Map<String, ProductLine> productLines = manager.queryProductLines();

		Assert.assertEquals(3, productLines.size());
	}

	@Test
	public void testInitThrowException() throws Exception {
		ProductLineConfigManager manager = new MockProductLineConfigManager();
		((MockProductLineConfigManager) manager).setConfigDao(new MockConfigDao2());

		try {
			manager.initialize();
		} catch (Exception e) {
		}
		manager.enableLogging(new MockLog());

		Company config = manager.getCompany();
		try {
			manager.refreshProductLineConfig();
		} catch (Exception e) {
		}
		Assert.assertEquals(0, config.getProductLines().size());
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

	public static class MockProductLineConfigManager extends ProductLineConfigManager {

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
