package com.dianping.cat.consumer.problem;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

import com.dianping.cat.consumer.aggreation.model.entity.Aggregation;
import com.dianping.cat.consumer.aggreation.model.entity.AggregationRule;
import com.dianping.cat.consumer.problem.aggregation.AggregationConfigManager;
import com.dianping.cat.consumer.problem.aggregation.AggregationHandler;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;

public class AggregationConfigManagerTest {

	private static int s_storeCount;

	@Test
	public void test() {
		AggregationConfigManager manager = new MockAggregationConfigManager();

		((MockAggregationConfigManager) manager).setAggregationHandler(new MockAggregationHandler());
		((MockAggregationConfigManager) manager).setConfigDao(new MockConfigDao1());
		manager.initialize();
		manager.deleteAggregationRule("rule");

		Assert.assertEquals(1, s_storeCount);
		AggregationRule rule = new AggregationRule();

		rule.setDomain("domain1");
		rule.setPattern("domain1");

		manager.insertAggregationRule(rule);

		Assert.assertEquals(2, s_storeCount);
		AggregationRule rule2 = new AggregationRule();
		rule2.setDomain("domain2");
		rule2.setPattern("domain2");
		manager.insertAggregationRule(rule2);

		Assert.assertEquals(3, s_storeCount);

		List<AggregationRule> rules = manager.queryAggrarationRules();
		Assert.assertEquals(3, rules.size());

		List<AggregationRule> dbRules = manager.queryAggrarationRulesFromDB();
		Assert.assertEquals(1, dbRules.size());

		AggregationRule aggration = manager.queryAggration("domain");
		Assert.assertEquals(true, aggration != null);
	}

	@Test
	public void testInitThrowException() {
		MockAggregationConfigManager manager = new MockAggregationConfigManager();

		manager.setAggregationHandler(new MockAggregationHandler());
		manager.setConfigDao(new MockConfigDao2());
		manager.initialize();

		List<AggregationRule> rules = manager.queryAggrarationRules();
		Assert.assertEquals(0, rules.size());
	}

	public static class MockAggregationConfigManager extends AggregationConfigManager {

		protected void setConfigDao(ConfigDao configDao) {
			m_configDao = configDao;
		}

		protected void setAggregationHandler(AggregationHandler handler) {
			m_handler = handler;
		}
	}

	public static class MockConfigDao2 extends MockConfigDao1 {
		@Override
		public Config findByName(String name, Readset<Config> readset) throws DalNotFoundException {
			throw new DalNotFoundException("this is test exception, please ignore it!");
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
			config.setContent(new Aggregation().toString());
			return config;
		}

		@Override
		public Config findByName(String name, Readset<Config> readset) throws DalException {
			Config config = new Config();

			config.setName(name);
			Aggregation aggregation = new Aggregation();

			AggregationRule rule = new AggregationRule();
			rule.setDomain("domain");
			rule.setType(1);
			rule.setPattern("domain");
			aggregation.addAggregationRule(rule);

			config.setContent(aggregation.toString());
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

	public static class MockAggregationHandler implements AggregationHandler {

		@Override
		public void register(List<AggregationRule> rules) {
		}

		@Override
		public String handle(int type, String domain, String input) {
			return null;
		}
	}

}
