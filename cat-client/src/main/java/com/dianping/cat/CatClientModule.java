package com.dianping.cat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

import org.unidal.helper.Threads;
import org.unidal.helper.Threads.AbstractThreadListener;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.status.StatusUpdateTask;

public class CatClientModule extends AbstractModule {
	public static final String ID = "cat-client";

	@Override
	protected void execute(final ModuleContext ctx) throws Exception {
		ctx.info("Current working directory is " + System.getProperty("user.dir"));

		// initialize milli-second resolution level timer
		MilliSecondTimer.initialize();

		// tracking thread start/stop
		Threads.addListener(new CatThreadListener(ctx));

		// warm up Cat
		Cat.getInstance().setContainer(((DefaultModuleContext) ctx).getContainer());

		// bring up TransportManager
		ctx.lookup(TransportManager.class);

		ClientConfigManager clientConfigManager = ctx.lookup(ClientConfigManager.class);
		
		if (clientConfigManager.isCatEnabled()) {
			// start status update task
			StatusUpdateTask statusUpdateTask = ctx.lookup(StatusUpdateTask.class);

			Threads.forGroup("cat").start(statusUpdateTask);
			LockSupport.parkNanos(10 * 1000 * 1000L); // wait 10 ms

			// MmapConsumerTask mmapReaderTask = ctx.lookup(MmapConsumerTask.class);
			// Threads.forGroup("cat").start(mmapReaderTask);
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
