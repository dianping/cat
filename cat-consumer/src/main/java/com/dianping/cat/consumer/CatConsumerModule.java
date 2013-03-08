package com.dianping.cat.consumer;

import com.dianping.cat.CatCoreModule;
import com.dianping.cat.message.io.TcpSocketReceiver;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

public class CatConsumerModule extends AbstractModule {
	public static final String ID = "cat-consumer";

	@Override
	protected void execute(ModuleContext ctx) {
		TcpSocketReceiver receiver = ctx.lookup(TcpSocketReceiver.class);
		int encodeThreadNumber = 5;

		receiver.startEncoderThreads(encodeThreadNumber);
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatCoreModule.ID);
	}
}
