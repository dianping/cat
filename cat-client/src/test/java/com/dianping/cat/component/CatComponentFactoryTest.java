package com.dianping.cat.component;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.component.factory.CatComponentFactory;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.message.analysis.LocalAggregator;
import com.dianping.cat.message.io.MessageStatistics;
import com.dianping.cat.message.io.TcpSocketSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.tree.MessageIdFactory;
import com.dianping.cat.status.StatusUpdateTask;

public class CatComponentFactoryTest {
	@Test
	public void testComponents() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new CatComponentFactory());

		Assert.assertNotNull(ctx.lookup(MessageIdFactory.class));
		Assert.assertNotNull(ctx.lookup(TcpSocketSender.class));
		Assert.assertNotNull(ctx.lookup(TransportManager.class));
		Assert.assertNotNull(ctx.lookup(MessageStatistics.class));
		Assert.assertNotNull(ctx.lookup(StatusUpdateTask.class));

		Assert.assertNotNull(ctx.lookup(ConfigureManager.class));

		Assert.assertNotNull(ctx.lookup(LocalAggregator.class));
	}

	@Test
	public void testConfigureSource() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new CatComponentFactory());

		Assert.assertEquals(5, ctx.lookupMap(ConfigureSource.class).size());
	}
}
