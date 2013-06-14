package com.dianping.cat.consumer;

import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.CatCoreModule;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.core.aggregation.AggregationManager;
import com.dianping.cat.message.io.TcpSocketReceiver;

public class CatConsumerModule extends AbstractModule {
	public static final String ID = "cat-consumer";

	@Override
	protected void execute(ModuleContext ctx) {
		TcpSocketReceiver receiver = ctx.lookup(TcpSocketReceiver.class);
		ServerConfigManager manager = ctx.lookup(ServerConfigManager.class);
		ctx.lookup(AggregationManager.class);
		int encodeThreadNumber = 10;

		if (manager.isLocalMode()) {
			encodeThreadNumber = 1;
		}
		receiver.startEncoderThreads(encodeThreadNumber);
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatCoreModule.ID);
	}
}
