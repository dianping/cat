package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.home.dal.abtest.AbtestDao;
import com.dianping.cat.home.dal.abtest.AbtestReportDao;
import com.dianping.cat.home.dal.abtest.AbtestRunDao;
import com.dianping.cat.home.dal.abtest.GroupStrategyDao;
import com.dianping.cat.system.page.abtest.GroupStrategyParser;
import com.dianping.cat.system.page.abtest.GsonBuilderManager;
import com.dianping.cat.system.page.abtest.ListViewHandler;
import com.dianping.cat.system.page.abtest.ReportHandler;
import com.dianping.cat.system.page.abtest.advisor.ABTestAdvisor;
import com.dianping.cat.system.page.abtest.advisor.DefaultABTestAdvisor;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.dianping.cat.system.page.abtest.service.ABTestServiceImpl;

public class ABTestComponentConfigurator extends AbstractResourceConfigurator {

	@Override
	public List<Component> defineComponents() {

		List<Component> all = new ArrayList<Component>();

		all.add(C(GroupStrategyParser.class));

		all.add(C(GsonBuilderManager.class));

		all.add(C(ABTestAdvisor.class, DefaultABTestAdvisor.class));

		all.add(C(ListViewHandler.class).req(AbtestDao.class).req(AbtestRunDao.class).config(E("pageSize").value("10")));

		all.add(C(ReportHandler.class).req(AbtestDao.class).req(AbtestRunDao.class).req(AbtestReportDao.class)
		      .req(MetricConfigManager.class));

		all.add(C(ABTestService.class, ABTestServiceImpl.class).req(AbtestDao.class).req(AbtestRunDao.class)
		      .req(GroupStrategyDao.class).req(ProjectDao.class).req(GsonBuilderManager.class)
		      .config(E("refreshTimeInSeconds").value("60")));

		return all;
	}
}
