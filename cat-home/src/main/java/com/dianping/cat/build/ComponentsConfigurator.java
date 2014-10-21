package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.initialization.DefaultModuleManager;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatHomeModule;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppDataCommandTableProvider;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.home.dal.report.EventDao;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.home.dal.report.UserDefineRuleDao;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.chart.CachedMetricReportService;
import com.dianping.cat.report.chart.DataExtractor;
import com.dianping.cat.report.chart.MetricDataFetcher;
import com.dianping.cat.report.chart.impl.CachedMetricReportServiceImpl;
import com.dianping.cat.report.chart.impl.DataExtractorImpl;
import com.dianping.cat.report.chart.impl.MetricDataFetcherImpl;
import com.dianping.cat.report.graph.DefaultGraphBuilder;
import com.dianping.cat.report.graph.DefaultValueTranslater;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.graph.ValueTranslater;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.app.graph.AppGraphCreator;
import com.dianping.cat.report.page.cdn.graph.CdnGraphCreator;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphItemBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.page.externalError.EventCollectManager;
import com.dianping.cat.report.page.metric.graph.MetricGraphCreator;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.network.graph.NetworkGraphCreator;
import com.dianping.cat.report.page.network.nettopology.NetGraphBuilder;
import com.dianping.cat.report.page.network.nettopology.NetGraphManager;
import com.dianping.cat.report.page.state.StateGraphs;
import com.dianping.cat.report.page.system.graph.SystemGraphCreator;
import com.dianping.cat.report.page.web.graph.DefaultWebGraphCreator;
import com.dianping.cat.report.page.web.graph.WebGraphCreator;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.alert.AlertInfo;
import com.dianping.cat.report.task.alert.RemoteMetricReportService;
import com.dianping.cat.report.task.product.ProjectUpdateTask;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.IpService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.config.AlertConfigManager;
import com.dianping.cat.system.config.AppRuleConfigManager;
import com.dianping.cat.system.config.BugConfigManager;
import com.dianping.cat.system.config.BusinessRuleConfigManager;
import com.dianping.cat.system.config.ConfigReloadTask;
import com.dianping.cat.system.config.DomainGroupConfigManager;
import com.dianping.cat.system.config.ExceptionConfigManager;
import com.dianping.cat.system.config.HeartbeatRuleConfigManager;
import com.dianping.cat.system.config.NetGraphConfigManager;
import com.dianping.cat.system.config.NetworkRuleConfigManager;
import com.dianping.cat.system.config.RouterConfigManager;
import com.dianping.cat.system.config.SystemRuleConfigManager;
import com.dianping.cat.system.config.ThirdPartyConfigManager;
import com.dianping.cat.system.config.UserDefinedRuleManager;
import com.dianping.cat.system.config.WebRuleConfigManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	private List<Component> defineCommonComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(JsonBuilder.class));

		all.add(C(ValueTranslater.class, DefaultValueTranslater.class));
		all.add(C(GraphBuilder.class, DefaultGraphBuilder.class) //
		      .req(ValueTranslater.class));

		all.add(C(PayloadNormalizer.class).req(ServerConfigManager.class));

		all.add(C(StateGraphs.class, StateGraphs.class).//
		      req(ReportServiceManager.class));
		all.add(C(DomainNavManager.class).req(ProjectService.class));

		all.add(C(EventCollectManager.class).req(EventDao.class, ServerConfigManager.class));

		all.add(C(TopologyGraphItemBuilder.class).req(TopologyGraphConfigManager.class));

		all.add(C(TopologyGraphBuilder.class).req(TopologyGraphItemBuilder.class).is(PER_LOOKUP));

		all.add(C(TopologyGraphManager.class).req(TopologyGraphBuilder.class, ServerConfigManager.class) //
		      .req(ProductLineConfigManager.class, TopologyGraphDao.class, DomainNavManager.class)//
		      .req(ModelService.class, DependencyAnalyzer.ID));

		// update project database
		all.add(C(ProjectUpdateTask.class).req(ProjectService.class, HostinfoService.class)//
		      .req(ReportService.class, TransactionAnalyzer.ID));

		return all;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineCommonComponents());

		all.addAll(defineConfigComponents());

		all.addAll(defineMetricComponents());

		all.add(C(Module.class, CatHomeModule.ID, CatHomeModule.class));

		all.add(C(ModuleManager.class, DefaultModuleManager.class) //
		      .config(E("topLevelModules").value(CatHomeModule.ID)));

		// report serivce
		all.addAll(new ReportServiceComponentConfigurator().defineComponents());
		// task
		all.addAll(new TaskComponentConfigurator().defineComponents());

		// model service
		all.addAll(new ServiceComponentConfigurator().defineComponents());

		all.add(C(TableProvider.class, "app-data-command", AppDataCommandTableProvider.class));
		// database
		all.add(C(JdbcDataSourceDescriptorManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));
		all.addAll(new CatDatabaseConfigurator().defineComponents());
		all.addAll(new AppDatabaseConfigurator().defineComponents());

		// for alarm module
		all.addAll(new AlarmComponentConfigurator().defineComponents());

		// web, please keep it last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	private List<Component> defineConfigComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(UserDefinedRuleManager.class).req(UserDefineRuleDao.class));
		all.add(C(TopologyGraphConfigManager.class).req(ConfigDao.class));
		all.add(C(ExceptionConfigManager.class).req(ConfigDao.class));
		all.add(C(DomainGroupConfigManager.class).req(ConfigDao.class));
		all.add(C(BugConfigManager.class).req(ConfigDao.class));
		all.add(C(NetworkRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class));
		all.add(C(BusinessRuleConfigManager.class).req(ConfigDao.class, MetricConfigManager.class,
		      UserDefinedRuleManager.class));
		all.add(C(AppRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class));
		all.add(C(WebRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class));
		all.add(C(HeartbeatRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class));
		all.add(C(SystemRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class));
		all.add(C(AlertConfigManager.class).req(ConfigDao.class));
		all.add(C(NetGraphConfigManager.class).req(ConfigDao.class));
		all.add(C(ThirdPartyConfigManager.class).req(ConfigDao.class));
		all.add(C(RouterConfigManager.class).req(ConfigDao.class));
		all.add(C(ConfigReloadTask.class).req(MetricConfigManager.class, ProductLineConfigManager.class));

		return all;
	}

	private List<Component> defineMetricComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(CachedMetricReportService.class, CachedMetricReportServiceImpl.class)
		      .req(ModelService.class, MetricAnalyzer.ID).req(ReportServiceManager.class).req(IpService.class));
		all.add(C(DataExtractor.class, DataExtractorImpl.class));
		all.add(C(MetricDataFetcher.class, MetricDataFetcherImpl.class));
		all.add(C(AlertInfo.class).req(MetricConfigManager.class));
		all.add(C(CdnGraphCreator.class)
		      .req(BaselineService.class, DataExtractor.class, MetricDataFetcher.class, CachedMetricReportService.class,
		            MetricConfigManager.class, ProductLineConfigManager.class, AlertInfo.class));
		all.add(C(MetricGraphCreator.class).req(CachedMetricReportService.class, DataExtractor.class,
		      MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, AlertInfo.class, ProjectService.class));
		all.add(C(SystemGraphCreator.class).req(CachedMetricReportService.class, DataExtractor.class,
		      MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, AlertInfo.class));
		all.add(C(WebGraphCreator.class, DefaultWebGraphCreator.class).req(CachedMetricReportService.class,
		      DataExtractor.class, MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, AlertInfo.class));
		all.add(C(NetworkGraphCreator.class).req(CachedMetricReportService.class, DataExtractor.class,
		      MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, AlertInfo.class));

		all.add(C(AppGraphCreator.class).req(AppDataService.class, CachedMetricReportService.class, DataExtractor.class,
		      MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, AlertInfo.class, AppConfigManager.class));

		all.add(C(NetGraphManager.class).req(ServerConfigManager.class, RemoteMetricReportService.class).req(
		      ReportServiceManager.class, NetGraphBuilder.class, AlertInfo.class, NetGraphConfigManager.class));

		all.add(C(NetGraphBuilder.class));

		return all;
	}
}
