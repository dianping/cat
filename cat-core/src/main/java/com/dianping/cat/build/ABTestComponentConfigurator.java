package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.abtest.spi.ABTestContextManager;
import com.dianping.cat.abtest.spi.ABTestEntityManager;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.abtest.spi.RoundRobinGroupStrategy;
import com.dianping.cat.abtest.spi.internal.DefaultABTestContextManager;
import com.dianping.cat.abtest.spi.internal.DefaultABTestEntityManager;

class ABTestComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ABTestContextManager.class, DefaultABTestContextManager.class) //
		      .req(ABTestEntityManager.class));

		all.add(C(ABTestEntityManager.class, DefaultABTestEntityManager.class));
		
		all.add(C(ABTestGroupStrategy.class,"roundrobin",RoundRobinGroupStrategy.class));

		return all;
	}
}
