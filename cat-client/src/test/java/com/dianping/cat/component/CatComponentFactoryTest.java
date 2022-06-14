package com.dianping.cat.component;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.component.factory.CatComponentFactory;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.message.analysis.EventAggregator;
import com.dianping.cat.message.analysis.LocalAggregator;
import com.dianping.cat.message.analysis.TransactionAggregator;
import com.dianping.cat.message.io.MessageSizeControl;
import com.dianping.cat.message.io.MessageStatistics;
import com.dianping.cat.message.io.MessageTreePool;
import com.dianping.cat.message.tree.MessageEncoder;
import com.dianping.cat.message.tree.MessageIdFactory;
import com.dianping.cat.network.ClientTransportManager;
import com.dianping.cat.network.handler.MessageTreeEncoder;
import com.dianping.cat.network.handler.MessageTreeSender;
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

		Assert.assertEquals(2, ctx.lookupMap(MessageEncoder.class).size());
		Assert.assertNotNull(ctx.lookup(MessageTreePool.class));
		Assert.assertNotNull(ctx.lookup(MessageSizeControl.class));

		Assert.assertNotNull(ctx.lookup(ClientTransportManager.class));
		Assert.assertNotNull(ctx.lookup(MessageTreeEncoder.class));
		Assert.assertNotNull(ctx.lookup(MessageTreeSender.class));

		Assert.assertNotNull(ctx.lookup(LocalAggregator.class));
		Assert.assertNotNull(ctx.lookup(TransactionAggregator.class));
		Assert.assertNotNull(ctx.lookup(EventAggregator.class));
	}

	@Test
	public void testConfigureSource() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new CatComponentFactory());

		Assert.assertEquals(5, ctx.lookupMap(ConfigureSource.class).size());
	}
}