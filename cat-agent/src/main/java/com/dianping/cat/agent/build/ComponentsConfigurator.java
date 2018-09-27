package com.dianping.cat.agent.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.agent.monitor.CatServers;
import com.dianping.cat.agent.monitor.CommandUtils;
import com.dianping.cat.agent.monitor.DataSender;
import com.dianping.cat.agent.monitor.executors.EnvConfig;
import com.dianping.cat.agent.monitor.executors.Executor;
import com.dianping.cat.agent.monitor.executors.TaskExecutors;
import com.dianping.cat.agent.monitor.executors.jvm.JVMMemoryExecutor;
import com.dianping.cat.agent.monitor.executors.jvm.JVMStateExecutor;
import com.dianping.cat.agent.monitor.executors.jvm.TomcatPidManager;
import com.dianping.cat.agent.monitor.executors.system.SystemPerformanceExecutor;
import com.dianping.cat.agent.monitor.executors.system.SystemStateExecutor;
import com.dianping.cat.agent.monitor.paas.DataBuilder;
import com.dianping.cat.agent.monitor.paas.PaasTask;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(CommandUtils.class));

		all.add(C(DataBuilder.class).req(CommandUtils.class));

		all.add(C(CatServers.class));

		all.add(C(DataSender.class).req(CatServers.class));

		all.add(C(EnvConfig.class));

		all.add(C(TomcatPidManager.class).req(CommandUtils.class));

		all.add(C(Executor.class, JVMMemoryExecutor.ID, JVMMemoryExecutor.class).req(EnvConfig.class, CommandUtils.class,
		      TomcatPidManager.class));
		all.add(C(Executor.class, JVMStateExecutor.ID, JVMStateExecutor.class).req(EnvConfig.class, CommandUtils.class));
		all.add(C(Executor.class, SystemPerformanceExecutor.ID, SystemPerformanceExecutor.class).req(EnvConfig.class,
		      CommandUtils.class));
		all.add(C(Executor.class, SystemStateExecutor.ID, SystemStateExecutor.class).req(EnvConfig.class,
		      CommandUtils.class));

		all.add(C(TaskExecutors.class).req(DataSender.class, EnvConfig.class));

		all.add(C(PaasTask.class).req(DataSender.class, DataBuilder.class));

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());
		return all;
	}
}
