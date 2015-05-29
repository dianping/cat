package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.Component;
import org.unidal.web.configuration.AbstractWebComponentsConfigurator;

import com.dianping.cat.cross.report.ReportModule;

class WebComponentConfigurator extends AbstractWebComponentsConfigurator {
	@SuppressWarnings("unchecked")
   @Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineModuleRegistry(all, ReportModule.class, ReportModule.class);
		
		return all;
	}
}
