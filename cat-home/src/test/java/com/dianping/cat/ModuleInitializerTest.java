package com.dianping.cat;

import java.io.File;

import org.junit.Test;

import com.site.initialization.DefaultModuleContext;
import com.site.initialization.Module;
import com.site.initialization.ModuleContext;
import com.site.initialization.ModuleInitializer;
import com.site.lookup.ContainerLoader;

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
