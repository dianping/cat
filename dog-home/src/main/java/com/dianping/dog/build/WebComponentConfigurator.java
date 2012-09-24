package com.dianping.dog.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.dog.alarm.AlarmModule;
import com.site.lookup.configuration.Component;
import com.site.web.configuration.AbstractWebComponentsConfigurator;

class WebComponentConfigurator extends AbstractWebComponentsConfigurator {
	@SuppressWarnings("unchecked")
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineModuleRegistry(all, AlarmModule.class, AlarmModule.class);

		return all;
	}
}
