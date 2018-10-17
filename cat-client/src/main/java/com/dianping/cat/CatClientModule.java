/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

import org.unidal.helper.Threads;
import org.unidal.helper.Threads.AbstractThreadListener;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.analyzer.LocalAggregator;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.status.StatusUpdateTask;

@Named(type = Module.class, value = CatClientModule.ID)
public class CatClientModule extends AbstractModule {
	public static final String ID = "cat-client";

	@Override
	protected void execute(final ModuleContext ctx) throws Exception {
		ctx.info("Current working directory is " + System.getProperty("user.dir"));

		// initialize milli-second resolution level timer
		MilliSecondTimer.initialize();

		// tracking thread start/stop
		Threads.addListener(new CatThreadListener(ctx));

		ClientConfigManager clientConfigManager = ctx.lookup(ClientConfigManager.class);

		// warm up Cat
		Cat.getInstance().setContainer(((DefaultModuleContext) ctx).getContainer());

		// bring up TransportManager
		ctx.lookup(TransportManager.class);

		if (clientConfigManager.isCatEnabled()) {
			// start status update task
			StatusUpdateTask statusUpdateTask = ctx.lookup(StatusUpdateTask.class);
			Threads.forGroup("cat").start(statusUpdateTask);

			Threads.forGroup("cat").start(new LocalAggregator.DataUploader());

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
