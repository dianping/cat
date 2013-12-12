package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.home.dal.abtest.AbtestDao;
import com.dianping.cat.home.dal.abtest.AbtestReportDao;
import com.dianping.cat.home.dal.abtest.AbtestRunDao;
import com.dianping.cat.home.dal.abtest.GroupStrategyDao;
import com.dianping.cat.system.page.abtest.advisor.ABTestAdvisor;
import com.dianping.cat.system.page.abtest.advisor.DefaultABTestAdvisor;
import com.dianping.cat.system.page.abtest.handler.ABTestHandler;
import com.dianping.cat.system.page.abtest.handler.AdvisorHandler;
import com.dianping.cat.system.page.abtest.handler.GroupStrategyHandler;
import com.dianping.cat.system.page.abtest.handler.ListViewHandler;
import com.dianping.cat.system.page.abtest.handler.ModelHandler;
import com.dianping.cat.system.page.abtest.handler.ReportHandler;
import com.dianping.cat.system.page.abtest.handler.SubHandler;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.dianping.cat.system.page.abtest.service.ABTestServiceImpl;
import com.dianping.cat.system.page.abtest.util.CaseBuilder;
import com.dianping.cat.system.page.abtest.util.GroupStrategyParser;
import com.dianping.cat.system.page.abtest.util.GsonManager;

public class ABTestComponentConfigurator extends AbstractResourceConfigurator {

	@Override
	public List<Component> defineComponents() {

		List<Component> all = new ArrayList<Component>();

		all.add(C(GroupStrategyParser.class));

		all.add(C(GsonManager.class));

		all.add(C(ABTestAdvisor.class, DefaultABTestAdvisor.class));

		all.add(C(SubHandler.class, ABTestHandler.ID, ABTestHandler.class).req(ABTestService.class, GsonManager.class));

		all.add(C(SubHandler.class, AdvisorHandler.ID, AdvisorHandler.class).req(ABTestAdvisor.class));

		all.add(C(SubHandler.class, GroupStrategyHandler.ID, GroupStrategyHandler.class).req(GroupStrategyParser.class,
		      GsonManager.class, ABTestAdvisor.class));

		all.add(C(SubHandler.class, ModelHandler.ID, ModelHandler.class).req(ABTestService.class));

		all.add(C(ListViewHandler.class).req(ABTestService.class).config(E("pageSize").value("10")));

		all.add(C(SubHandler.class, ReportHandler.ID, ReportHandler.class).req(ABTestService.class).req(
		      MetricConfigManager.class));

		all.add(C(ABTestService.class, ABTestServiceImpl.class).req(AbtestDao.class).req(AbtestRunDao.class)
		      .req(GroupStrategyDao.class).req(ProjectDao.class).req(CaseBuilder.class).req(AbtestReportDao.class)
		      .config(E("refreshTimeInSeconds").value("60")));

		all.add(C(CaseBuilder.class).req(GsonManager.class));

		return all;
	}
}
