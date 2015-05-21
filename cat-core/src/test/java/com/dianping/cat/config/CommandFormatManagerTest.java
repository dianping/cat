package com.dianping.cat.config;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.app.command.CommandFormatConfigManager;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;

public class CommandFormatManagerTest extends ComponentTestCase {

	@Test
	public void test() {
		CommandFormatConfigManager manager = lookup(CommandFormatConfigManager.class);

		manager.setConfigDao(new ExtendedConfigDao());
		manager.initialize();

		List<String> urls = manager.handle(1, "test");

		Assert.assertEquals("test", urls.get(0));

		urls = manager.handle(1, "http://m.dianping.com/shopping/mallshoplist/12313123");

		Assert.assertEquals("http://m.dianping.com/shopping/mallshoplist/", urls.get(0));
	}

	public static class ExtendedConfigDao extends ConfigDao {
		@Override
		public Config findByName(String name, Readset<Config> readset) throws DalException {
			Config c = new Config();
			String xml = null;
			try {
				xml = Files.forIO().readFrom(getClass().getResourceAsStream("app-command-format-config.xml"), "utf-8");
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
