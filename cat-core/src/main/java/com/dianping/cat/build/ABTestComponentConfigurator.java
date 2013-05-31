package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.abtest.repository.ABTestEntityRepository;
import com.dianping.cat.abtest.repository.DefaultABTestEntityRepository;
import com.dianping.cat.abtest.spi.internal.ABTestContextManager;
import com.dianping.cat.abtest.spi.internal.ABTestEntityManager;
import com.dianping.cat.abtest.spi.internal.DefaultABTestContextManager;
import com.dianping.cat.abtest.spi.internal.DefaultABTestEntityManager;
import com.dianping.cat.configuration.ClientConfigManager;

class ABTestComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ABTestContextManager.class, DefaultABTestContextManager.class) //
		      .req(ABTestEntityManager.class));

		all.add(C(ABTestEntityRepository.class, DefaultABTestEntityRepository.class) //
		      .req(ClientConfigManager.class) //
		      .config(E("address").value("228.0.0.3:2283")));

		all.add(C(ABTestEntityManager.class, DefaultABTestEntityManager.class) //
				.req(ABTestEntityRepository.class));
		
		return all;
	}
}
