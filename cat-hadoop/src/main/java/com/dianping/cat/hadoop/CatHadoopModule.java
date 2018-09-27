package com.dianping.cat.hadoop;

import org.unidal.cat.message.storage.clean.LogviewProcessor;
import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.CatCoreModule;

public class CatHadoopModule extends AbstractModule {
	public static final String ID = "cat-hadoop";

	@Override
	protected void execute(ModuleContext ctx) {
		LogviewProcessor processor = ctx.lookup(LogviewProcessor.class);

		Threads.forGroup("cat").start(processor);
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatCoreModule.ID);
	}
}
