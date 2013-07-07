package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.home.dal.abtest.AbtestDao;
import com.dianping.cat.home.dal.abtest.AbtestRunDao;
import com.dianping.cat.home.dal.abtest.GroupStrategyDao;
import com.dianping.cat.system.page.abtest.GroupStrategyParser;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.dianping.cat.system.page.abtest.service.ABTestServiceImpl;

public class ABTestComponentConfigurator extends AbstractResourceConfigurator {

	@Override
	public List<Component> defineComponents() {

		List<Component> all = new ArrayList<Component>();

		all.add(C(GroupStrategyParser.class));
		
		all.add(C(ABTestService.class, ABTestServiceImpl.class)
				.req(AbtestDao.class).req(AbtestRunDao.class).req(GroupStrategyDao.class)
				.req(ProjectDao.class)
				.req(GroupStrategyParser.class).config(E("refreshTimeInSeconds").value("60")));
		
		return all;
	}

}
