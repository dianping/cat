package com.dianping.cat.agent.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.agent.monitor.DataSender;
import com.dianping.cat.agent.monitor.executors.EnvConfig;
import com.dianping.cat.agent.monitor.executors.Executor;
import com.dianping.cat.agent.monitor.executors.TaskExecutors;
import com.dianping.cat.agent.monitor.executors.jvm.JVMMemoryExecutor;
import com.dianping.cat.agent.monitor.executors.jvm.JVMStateExecutor;
import com.dianping.cat.agent.monitor.executors.system.SystemPerformanceExecutor;
import com.dianping.cat.agent.monitor.executors.system.SystemStateExecutor;
import com.dianping.cat.agent.monitor.paas.DataFetcher;
import com.dianping.cat.agent.monitor.paas.PaasTask;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(EnvConfig.class));

		all.add(C(DataFetcher.class));

		all.add(C(Executor.class, JVMMemoryExecutor.ID, JVMMemoryExecutor.class).req(EnvConfig.class));
		all.add(C(Executor.class, JVMStateExecutor.ID, JVMStateExecutor.class).req(EnvConfig.class));
		all.add(C(Executor.class, SystemPerformanceExecutor.ID, SystemPerformanceExecutor.class).req(EnvConfig.class));
		all.add(C(Executor.class, SystemStateExecutor.ID, SystemStateExecutor.class).req(EnvConfig.class));
		all.add(C(TaskExecutors.class).req(DataSender.class));

		all.add(C(PaasTask.class).req(DataSender.class).req(DataFetcher.class));

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());
		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
