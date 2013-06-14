package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.abtest.repository.ABTestEntityRepository;
import com.dianping.cat.abtest.repository.HttpABTestEntityRepository;
import com.dianping.cat.abtest.spi.internal.ABTestContextManager;
import com.dianping.cat.abtest.spi.internal.ABTestEntityManager;
import com.dianping.cat.abtest.spi.internal.DefaultABTestContextManager;
import com.dianping.cat.abtest.spi.internal.DefaultABTestEntityManager;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.spi.MessageManager;

class ABTestComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ABTestContextManager.class, DefaultABTestContextManager.class) //
		      .req(ABTestEntityManager.class, MessageManager.class));

		all.add(C(ABTestEntityRepository.class, HttpABTestEntityRepository.class) //
		      .req(ClientConfigManager.class).config(E("refreshTimeInSeconds").value("60")));

		all.add(C(ABTestEntityManager.class, DefaultABTestEntityManager.class) //
		      .req(ABTestEntityRepository.class));

		return all;
	}
}
