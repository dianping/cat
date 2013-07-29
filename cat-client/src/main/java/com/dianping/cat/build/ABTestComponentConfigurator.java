package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.abtest.repository.ABTestEntityRepository;
import com.dianping.cat.abtest.repository.HttpABTestEntityRepository;
import com.dianping.cat.abtest.spi.internal.ABTestCodec;
import com.dianping.cat.abtest.spi.internal.ABTestContextManager;
import com.dianping.cat.abtest.spi.internal.ABTestEntityManager;
import com.dianping.cat.abtest.spi.internal.DefaultABTestCodec;
import com.dianping.cat.abtest.spi.internal.DefaultABTestContextManager;
import com.dianping.cat.abtest.spi.internal.DefaultABTestEntityManager;
import com.dianping.cat.abtest.spi.internal.conditions.ABTestCondition;
import com.dianping.cat.abtest.spi.internal.conditions.ABTestConditionManager;
import com.dianping.cat.abtest.spi.internal.conditions.PercentageCondition;
import com.dianping.cat.abtest.spi.internal.conditions.URLCondition;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.spi.MessageManager;

public class ABTestComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ABTestContextManager.class, DefaultABTestContextManager.class) //
		      .req(ABTestEntityManager.class, MessageManager.class, ABTestCodec.class));
		all.add(C(ABTestCodec.class, DefaultABTestCodec.class));

		all.add(C(ABTestEntityRepository.class, HttpABTestEntityRepository.class) //
		      .req(ClientConfigManager.class).config(E("refreshTimeInSeconds").value("60")));

		all.add(C(ABTestEntityManager.class, DefaultABTestEntityManager.class) //
		      .req(ABTestEntityRepository.class));

		all.add(C(ABTestConditionManager.class, ABTestConditionManager.class));

		all.add(C(ABTestCondition.class, URLCondition.ID, URLCondition.class));

		all.add(C(ABTestCondition.class, PercentageCondition.ID, PercentageCondition.class));

		return all;
	}
}
