package com.dianping.cat.job.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.job.browser.BrowserJoblet;
import com.dianping.cat.job.browser.HelpJoblet;
import com.dianping.cat.joblet.Joblet;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(Joblet.class, "help", HelpJoblet.class));
		all.add(C(Joblet.class, "browser", BrowserJoblet.class));

		all.addAll(new DatabaseConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
