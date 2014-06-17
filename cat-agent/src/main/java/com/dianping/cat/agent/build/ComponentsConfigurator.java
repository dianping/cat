package com.dianping.cat.agent.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.agent.monitor.EnvironmentConfig;
import com.dianping.cat.agent.monitor.executors.DataSender;
import com.dianping.cat.agent.monitor.executors.Executor;
import com.dianping.cat.agent.monitor.executors.TaskExecutors;
import com.dianping.cat.agent.monitor.executors.jvm.JVMMemoryExecutor;
import com.dianping.cat.agent.monitor.executors.jvm.JVMStateExecutor;
import com.dianping.cat.agent.monitor.executors.system.SystemPerformanceExecutor;
import com.dianping.cat.agent.monitor.executors.system.SystemStateExecutor;
import com.dianping.cat.agent.monitor.puppet.PuppetTask;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(EnvironmentConfig.class));

		all.add(C(Executor.class, JVMMemoryExecutor.ID, JVMMemoryExecutor.class).req(EnvironmentConfig.class));
		all.add(C(Executor.class, JVMStateExecutor.ID, JVMStateExecutor.class).req(EnvironmentConfig.class));
		all.add(C(Executor.class, SystemPerformanceExecutor.ID, SystemPerformanceExecutor.class).req(
		      EnvironmentConfig.class));
		all.add(C(Executor.class, SystemStateExecutor.ID, SystemStateExecutor.class).req(EnvironmentConfig.class));
		all.add(C(DataSender.class).req(EnvironmentConfig.class));
		all.add(C(TaskExecutors.class).req(DataSender.class));

		all.add(C(PuppetTask.class).req(EnvironmentConfig.class));

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());
		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
