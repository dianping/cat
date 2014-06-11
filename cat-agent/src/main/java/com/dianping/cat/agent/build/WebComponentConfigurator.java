package com.dianping.cat.agent.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.agent.core.CoreModule;

import org.unidal.lookup.configuration.Component;
import org.unidal.web.configuration.AbstractWebComponentsConfigurator;

class WebComponentConfigurator extends AbstractWebComponentsConfigurator {
	@SuppressWarnings("unchecked")
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineModuleRegistry(all, CoreModule.class, CoreModule.class);

		return all;
	}
}
