package com.dianping.cat;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.ClientConfigMerger;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.transform.DefaultDomParser;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.status.StatusUpdateTask;
import com.site.helper.Files;
import com.site.helper.Threads;
import com.site.helper.Threads.DefaultThreadListener;
import com.site.initialization.AbstractModule;
import com.site.initialization.DefaultModuleContext;
import com.site.initialization.Module;
import com.site.initialization.ModuleContext;

public class CatCoreModule extends AbstractModule {
	public static final String ID = "cat-core";

	private static final String CAT_CLIENT_XML = "/META-INF/cat/client.xml";

	@Override
	protected void execute(final ModuleContext ctx) throws Exception {
		ctx.info("Current working directory is " + System.getProperty("user.dir"));

		// initialize milli-second resolution level timer
		MilliSecondTimer.initialize();

		// disable thread renaming of Netty
		ThreadRenamingRunnable.setThreadNameDeterminer(ThreadNameDeterminer.CURRENT);

		// tracking thread start/stop
		Threads.addListener(new CatThreadListener(ctx));

		File clientConfigFile = ctx.getAttribute("cat-client-config-file");
		ClientConfigManager clientConfigManager = ctx.lookup(ClientConfigManager.class);

		clientConfigManager.initialize(clientConfigFile);

		// warm up Cat
		Cat.getInstance().setContainer(((DefaultModuleContext) ctx).getContainer());

		// bring up TransportManager
		ctx.lookup(TransportManager.class);

		// start status update task
		if (clientConfigManager.isCatEnabled()) {
			StatusUpdateTask statusUpdateTask = ctx.lookup(StatusUpdateTask.class);

			Threads.forGroup("Cat").start(statusUpdateTask);
			LockSupport.parkNanos(10 * 1000 * 1000L); // wait 10 ms
		}
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return null; // no dependencies
	}

	ClientConfig loadClientConfig(ModuleContext ctx, File configFile) {
		ClientConfig globalConfig = null;
		ClientConfig clientConfig = null;

		try {
			// read the global configure from local file system
			// so that OPS can:
			// - configure the cat servers to connect
			// - enable/disable Cat for specific domain(s)
			if (configFile != null) {
				if (configFile.exists()) {
					String xml = Files.forIO().readFrom(configFile.getCanonicalFile(), "utf-8");

					globalConfig = new DefaultDomParser().parse(xml);
					ctx.info(String.format("Global config file(%s) found.", configFile));
				} else {
					ctx.warn(String.format("Global config file(%s) not found, IGNORED.", configFile));
				}
			}

			// load the client configure from Java class-path
			if (clientConfig == null) {
				InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CAT_CLIENT_XML);

				if (in == null) {
					in = Cat.class.getResourceAsStream(CAT_CLIENT_XML);
				}

				if (in != null) {
					String xml = Files.forIO().readFrom(in, "utf-8");

					clientConfig = new DefaultDomParser().parse(xml);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error when loading configuration file(%s)!", configFile), e);
		}

		// merge the two configures together to make it effected
		if (globalConfig != null && clientConfig != null) {
			globalConfig.accept(new ClientConfigMerger(clientConfig));
		}

		return clientConfig;
	}

	public final class CatThreadListener extends DefaultThreadListener {
		private final ModuleContext m_ctx;

		private CatThreadListener(ModuleContext ctx) {
			m_ctx = ctx;
		}

		@Override
		public void onThreadGroupCreated(ThreadGroup group, String name) {
			m_ctx.info(String.format("Thread group(%s) created.", name));
		}

		@Override
		public void onThreadPoolCreated(ExecutorService pool, String name) {
			m_ctx.info(String.format("Thread pool(%s) created.", name));
		}

		@Override
		public void onThreadStarting(Thread thread, String name) {
			m_ctx.info(String.format("Starting thread(%s) ...", name));
		}

		@Override
		public void onThreadStopping(Thread thread, String name) {
			m_ctx.info(String.format("Stopping thread(%s).", name));
		}

		@Override
		public boolean onUncaughtException(Thread thread, Throwable e) {
			m_ctx.error(String.format("Uncaught exception thrown out of thread(%s)", thread.getName()), e);
			return true;
		}
	}
}
