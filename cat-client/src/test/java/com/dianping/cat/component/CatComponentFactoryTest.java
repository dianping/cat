package com.dianping.cat.component;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.component.factory.CatComponentFactory;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.message.context.MessageIdFactory;
import com.dianping.cat.message.encoder.MessageTreeEncoder;
import com.dianping.cat.message.io.MessageSizeControl;
import com.dianping.cat.network.ClientTransportManager;
import com.dianping.cat.network.MessageTransporter;
import com.dianping.cat.status.MessageStatistics;
import com.dianping.cat.status.StatusUpdateTask;

public class CatComponentFactoryTest {
	@Test
	public void testComponents() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new CatComponentFactory());

		Assert.assertNotNull(ctx.lookup(MessageIdFactory.class));
		Assert.assertNotNull(ctx.lookup(MessageStatistics.class));
		Assert.assertNotNull(ctx.lookup(StatusUpdateTask.class));

		Assert.assertNotNull(ctx.lookup(ConfigureManager.class));

		Assert.assertEquals(5, ctx.lookupMap(ConfigureSource.class).size());

		Assert.assertEquals(2, ctx.lookupMap(MessageTreeEncoder.class).size());
		Assert.assertNotNull(ctx.lookup(MessageSizeControl.class));

		Assert.assertNotNull(ctx.lookup(ClientTransportManager.class));
		Assert.assertNotNull(ctx.lookup(MessageTransporter.class));
	}

	@Test
	public void testConfigureSource() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new CatComponentFactory());

		Assert.assertEquals(5, ctx.lookupMap(ConfigureSource.class).size());
	}
}