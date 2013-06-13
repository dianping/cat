package com.dianping.cat.abtest.demo.roundrobin;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.abtest.spi.internal.ABTestContextManager;
import com.dianping.cat.abtest.spi.internal.ABTestEntityManager;
import com.dianping.cat.abtest.spi.internal.DefaultABTestContextManager;

public class ABTestServerConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ABTestContextManager.class, DefaultABTestContextManager.class) //
		      .req(ABTestEntityManager.class));

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
