package com.dianping.cat;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.DefaultComponentContext;
import com.dianping.cat.configuration.ClientConfigManager;
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

		Assert.assertNotNull(ctx.lookup(ClientConfigManager.class));
		Assert.assertNotNull(ctx.lookup(MessageIdFactory.class));
		Assert.assertNotNull(ctx.lookup(MessageManager.class));
		Assert.assertNotNull(ctx.lookup(MessageProducer.class));
		Assert.assertNotNull(ctx.lookup(TcpSocketSender.class));
		Assert.assertNotNull(ctx.lookup(TransportManager.class));
		Assert.assertNotNull(ctx.lookup(MessageStatistics.class));
		Assert.assertNotNull(ctx.lookup(StatusUpdateTask.class));
	}
}
