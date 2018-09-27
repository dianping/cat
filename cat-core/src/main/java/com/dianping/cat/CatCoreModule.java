package com.dianping.cat;

import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.report.server.ServersUpdaterManager;

@Named(type = Module.class, value = CatCoreModule.ID)
public class CatCoreModule extends AbstractModule {
	public static final String ID = "cat-core";

	@Override
	protected void execute(final ModuleContext ctx) throws Exception {
		// bring up ServersUpdaterManager
		ctx.lookup(ServersUpdaterManager.class);
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatClientModule.ID);
	}
}
