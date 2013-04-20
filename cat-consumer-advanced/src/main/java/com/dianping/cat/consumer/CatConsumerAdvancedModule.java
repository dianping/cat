package com.dianping.cat.consumer;

import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.DumpUploader;

public class CatConsumerAdvancedModule extends AbstractModule {
	public static final String ID = "cat-consumer-advanced";

	@Override
	protected void execute(ModuleContext ctx) {
		ServerConfigManager configManager = ctx.lookup(ServerConfigManager.class);

		if (!configManager.isLocalMode()) {
			DumpUploader uploader = ctx.lookup(DumpUploader.class);

			uploader.start();
		}
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatConsumerModule.ID);
	}
}
