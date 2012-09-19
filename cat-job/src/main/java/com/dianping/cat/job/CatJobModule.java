package com.dianping.cat.job;

import com.site.initialization.AbstractModule;
import com.site.initialization.Module;
import com.site.initialization.ModuleContext;

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
