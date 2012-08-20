package com.dianping.bee.engine.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.engine.spi.TableProviderManager;
import com.dianping.bee.engine.spi.internal.DefaultTableProviderManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(TableProviderManager.class, DefaultTableProviderManager.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
