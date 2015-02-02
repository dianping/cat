package com.dianping.cat.consumer;

import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.CatCoreModule;

public class CatConsumerModule extends AbstractModule {
	public static final String ID = "cat-consumer";

	@Override
	protected void execute(ModuleContext ctx) {
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatCoreModule.ID);
	}
}
