package com.dianping.cat.component;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.analyzer.LocalAggregator;
import com.dianping.cat.component.factory.CatComponentFactory;
import com.dianping.cat.configuration.ApplicationProperties;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.io.TcpSocketSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.StatusUpdateTask;

public class CatComponentFactoryTest {
	@Test
	public void test() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new CatComponentFactory());

		Assert.assertNotNull(ctx.lookup(ApplicationProperties.class));
		Assert.assertNotNull(ctx.lookup(MessageIdFactory.class));
		Assert.assertNotNull(ctx.lookup(MessageManager.class));
		Assert.assertNotNull(ctx.lookup(MessageProducer.class));
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

		System.out.println(ctx.lookupMap(ConfigureSource.class));
	}
}
