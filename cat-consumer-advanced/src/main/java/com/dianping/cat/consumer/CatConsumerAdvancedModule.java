package com.dianping.cat.consumer;

import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

public class CatConsumerAdvancedModule extends AbstractModule {
	public static final String ID = "cat-consumer-advanced";

	@Override
	protected void execute(ModuleContext ctx) {
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatConsumerModule.ID);
	}
}
