package com.dianping.cat.configuration;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.DefaultComponentContext;
import com.dianping.cat.component.factory.CatComponentFactory;

public class ConfigureManagerTest extends ComponentTestCase {
	@Test
	public void testConfigureManager() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new CatComponentFactory());

		ConfigureManager manager = lookup(ConfigureManager.class);

		Assert.assertNotNull(manager);
	}

	@Test
	public void testConfigureSource() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new CatComponentFactory());

		Assert.assertEquals(5, ctx.lookupMap(ConfigureSource.class).size());
	}

	@Test
	public void testRefreshable() {
		// TODO
	}
}
