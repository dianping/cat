package com.dianping.cat;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.AbstractThreadListener;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.ClientConfigReloader;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.status.StatusUpdateTask;

public class CatCoreModule extends AbstractModule {
	public static final String ID = "cat-core";

	@Override
	protected void execute(final ModuleContext ctx) throws Exception {
		ctx.info("Current working directory is " + System.getProperty("user.dir"));

		// initialize milli-second resolution level timer
		MilliSecondTimer.initialize();

		// disable thread renaming of Netty
		ThreadRenamingRunnable.setThreadNameDeterminer(ThreadNameDeterminer.CURRENT);

		// tracking thread start/stop
		// Threads.addListener(new CatThreadListener(ctx));
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

			ClientConfig config = clientConfigManager.getClientConfig();

			if (config != null) {
				Threads.forGroup("Cat").start(new ClientConfigReloader(clientConfigFile.getAbsolutePath(), config));
			}
		}
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return null; // no dependencies
	}

	public static final class CatThreadListener extends AbstractThreadListener {
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
