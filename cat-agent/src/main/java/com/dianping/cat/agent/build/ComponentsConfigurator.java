package com.dianping.cat.agent.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.agent.monitor.Executor;
import com.dianping.cat.agent.monitor.jvm.JVMMemoryExecutor;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		all.add(C(Executor.class, JVMMemoryExecutor.ID, JVMMemoryExecutor.class));
		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
