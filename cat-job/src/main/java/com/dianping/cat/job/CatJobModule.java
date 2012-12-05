package com.dianping.cat.job;

import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

public class CatJobModule extends AbstractModule {
	public static final String ID = "cat-job";

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules();
	}

	@Override
	protected void execute(ModuleContext ctx) {
		// nothing so far
	}
}
