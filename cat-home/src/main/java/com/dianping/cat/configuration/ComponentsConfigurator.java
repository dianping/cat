package com.dianping.cat.configuration;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.report.ReportModule;
import com.site.lookup.configuration.Component;
import com.site.web.configuration.AbstractWebComponentsConfigurator;

public class ComponentsConfigurator extends AbstractWebComponentsConfigurator {
	@Override
	@SuppressWarnings("unchecked")
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineModuleRegistry(all, ReportModule.class, ReportModule.class);

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
