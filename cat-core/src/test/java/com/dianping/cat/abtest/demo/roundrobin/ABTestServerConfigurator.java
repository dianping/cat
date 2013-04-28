package com.dianping.cat.abtest.demo.roundrobin;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.abtest.spi.ABTestEntityManager;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.abtest.spi.internal.DefaultABTestEntityManager;

public class ABTestServerConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ABTestEntityManager.class, DefaultABTestEntityManager.class));
		all.add(C(ABTestGroupStrategy.class, "roundrobin", RoundRobinGroupStrategy.class));

		return all;
	}

	@Override
	protected Class<?> getTestClass() {
		return ABTestServer.class;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ABTestServerConfigurator());
	}
}
