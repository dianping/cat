package com.dianping.cat;

import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.report.ReportManager;

public class CatCoreModule extends AbstractModule {
	public static final String ID = "cat-core";

	@Override
	protected void execute(final ModuleContext ctx) throws Exception {
		ctx.lookup(ReportManager.class).initialize();
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatClientModule.ID);
	}
}
