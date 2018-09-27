package com.dianping.cat.demo;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.command.entity.AppCommandConfig;
import com.dianping.cat.command.entity.Command;
import com.dianping.cat.config.app.AppCommandConfigManager;

public class AppCommandConvertor extends ComponentTestCase {

	private AppCommandConfigManager m_appconfigmanger;

	@Test
	public void test() {
		m_appconfigmanger = lookup(AppCommandConfigManager.class);
		AppCommandConfig config = m_appconfigmanger.getConfig();

		for (Command command : config.getCommands().values()) {
			command.setNamespace("default");
		}

		m_appconfigmanger.storeConfig();
		System.out.println(config);
	}
}
