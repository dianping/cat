package com.dianping.cat.report.task.app;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.app.AppCommandConfigManager;

public class AppConfigManagerTest extends ComponentTestCase {

	@Test
	public void test() {
		AppCommandConfigManager appconfigManger = lookup(AppCommandConfigManager.class);

		try {
			int i = appconfigManger.findAvailableId(0, 5);
			System.out.println(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
