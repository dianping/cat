package com.dianping.cat.job.joblet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.job.joblet.BrowserJoblet.BrowserOutputter;
import com.dianping.cat.job.joblet.BrowserJobletTest.MockOutputter;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class BrowserJobletTestConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		// replace the default one
		all.add(C(BrowserOutputter.class, MockOutputter.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new BrowserJobletTestConfigurator());
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/" + BrowserJobletTest.class.getName().replace('.', '/') + ".xml");
	}
}
