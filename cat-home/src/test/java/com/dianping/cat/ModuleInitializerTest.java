package com.dianping.cat;

import java.io.File;

import org.junit.Test;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.initialization.ModuleInitializer;
import org.unidal.lookup.ContainerLoader;

public class ModuleInitializerTest {
	@Test
	public void testInitialize() throws InterruptedException {
		ModuleContext ctx = new DefaultModuleContext(ContainerLoader.getDefaultContainer());
		ModuleInitializer initializer = ctx.lookup(ModuleInitializer.class);
		Module catCoreModule = ctx.lookup(Module.class, CatHomeModule.ID);

		ctx.setAttribute("cat-client-config-file", new File("/data/appdatas/cat/client.xml"));
		initializer.execute(ctx, catCoreModule);

		Thread.sleep(1000);
	}
}
