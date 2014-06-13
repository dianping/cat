package com.dianping.cat.agent.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.agent.monitor.DataSender;
import com.dianping.cat.agent.monitor.EnvironmentConfig;
import com.dianping.cat.agent.monitor.Executor;
import com.dianping.cat.agent.monitor.TaskExecutors;
import com.dianping.cat.agent.monitor.jvm.JVMMemoryExecutor;
import com.dianping.cat.agent.monitor.jvm.JVMStateExecutor;
import com.dianping.cat.agent.monitor.system.SigarUtil;
import com.dianping.cat.agent.monitor.system.SystemPerformanceExecutor;
import com.dianping.cat.agent.monitor.system.SystemStateExecutor;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(EnvironmentConfig.class));

		all.add(C(SigarUtil.class));

		all.add(C(Executor.class, JVMMemoryExecutor.ID, JVMMemoryExecutor.class).req(EnvironmentConfig.class));
		all.add(C(Executor.class, JVMStateExecutor.ID, JVMStateExecutor.class).req(EnvironmentConfig.class));
		all.add(C(Executor.class, SystemPerformanceExecutor.ID, SystemPerformanceExecutor.class).req(
		      EnvironmentConfig.class, SigarUtil.class));
		all.add(C(Executor.class, SystemStateExecutor.ID, SystemStateExecutor.class).req(EnvironmentConfig.class,
		      SigarUtil.class));

		all.add(C(DataSender.class).req(EnvironmentConfig.class));

		all.add(C(TaskExecutors.class).req(DataSender.class));

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());
		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
