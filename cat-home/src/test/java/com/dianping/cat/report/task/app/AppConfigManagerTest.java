package com.dianping.cat.report.task.app;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.app.AppConfigManager;

public class AppConfigManagerTest extends ComponentTestCase {

	@Test
	public void test() {
		AppConfigManager appconfigManger = lookup(AppConfigManager.class);

		try {
			int i = appconfigManger.findAvailableId(0, 5);
			System.out.println(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
