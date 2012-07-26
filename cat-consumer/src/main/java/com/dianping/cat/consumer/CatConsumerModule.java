package com.dianping.cat.consumer;

import com.dianping.cat.CatCoreModule;
import com.dianping.cat.consumer.logview.LogviewUploader;
import com.dianping.cat.hadoop.build.CatHadoopModule;
import com.site.helper.Threads;
import com.site.initialization.AbstractModule;
import com.site.initialization.Module;
import com.site.initialization.ModuleContext;

public class CatConsumerModule extends AbstractModule {
	public static final String ID = "cat-consumer";

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatCoreModule.ID, CatHadoopModule.ID);
	}

	@Override
	protected void execute(ModuleContext ctx) {
		LogviewUploader uploader = ctx.lookup(LogviewUploader.class);

		if (!uploader.isLocalMode()) {
			Threads.forGroup("Cat").start(uploader);
		}
	}
}
