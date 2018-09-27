package com.dianping.cat.alarm.app;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;
import org.xml.sax.SAXException;

import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.transform.DefaultSaxParser;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;

public class AppAlarmRuleParamBuilderTest extends ComponentTestCase {

	private MobileConfigManager m_configManager;

	private AppAlarmRuleParamBuilder m_builder;

	@Before
	public void before() throws InitializationException {
		m_configManager = new MobileConfigManager();
		m_configManager.setConfigDao(new ExtendedConfigDao());
		m_configManager.initialize();

		m_builder = new AppAlarmRuleParamBuilder();
		m_builder.setMobileConfigManager(m_configManager);
	}

	@Test
	public void test() throws IOException, SAXException {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("app-alarm-rule.xml"), "utf-8");
		Rule rule = DefaultSaxParser.parseEntity(Rule.class, xml);
		System.out.println(rule);
		List<AppAlarmRuleParam> params = m_builder.build(rule);

		Assert.assertEquals(params.size(), 1);
		System.out.println("param : " + params);

	}

	public void test1() throws IOException, SAXException {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("app-alarm-rule1.xml"), "utf-8");
		Rule rule = DefaultSaxParser.parseEntity(Rule.class, xml);
		System.out.println(rule);
		List<AppAlarmRuleParam> params = m_builder.build(rule);

		Assert.assertEquals(params.size(), 7);

		for (AppAlarmRuleParam p : params) {
			System.out.println(p);
		}
	}

	public void test2() throws IOException, SAXException {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("app-alarm-rule2.xml"), "utf-8");
		Rule rule = DefaultSaxParser.parseEntity(Rule.class, xml);
		System.out.println(rule);
		List<AppAlarmRuleParam> params = m_builder.build(rule);

		Assert.assertEquals(params.size(), 42);

		for (AppAlarmRuleParam p : params) {
			System.out.println(p);
		}
	}

	public static class ExtendedConfigDao extends ConfigDao {
		@Override
		public Config findByName(String name, Readset<Config> readset) throws DalException {
			Config c = new Config();
			String xml = null;
			try {
				xml = Files.forIO().readFrom(getClass().getResourceAsStream("mobile-config.xml"), "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}

			c.setContent(xml);
			c.setModifyDate(new Date());
			return c;
		}

		@Override
		public int insert(Config proto) throws DalException {
			return 1;
		}
	}
}
