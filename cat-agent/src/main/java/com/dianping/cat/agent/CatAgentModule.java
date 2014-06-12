package com.dianping.cat.agent;

import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.agent.monitor.DataSender;
import com.dianping.cat.agent.monitor.TaskExecutors;

public class CatAgentModule extends AbstractModule {

	public static final String ID = "cat-agent";

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
		System.out.println("coming catagentmodule");
		DataSender dataSender = ctx.lookup(DataSender.class);
		Threads.forGroup("Cat").start(dataSender);

		TaskExecutors taskExecutors = ctx.lookup(TaskExecutors.class);
		Threads.forGroup("Cat").start(taskExecutors);
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return null;
	}
}
