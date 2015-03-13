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
import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.config.app.AppCommandDataTableProvider;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppSpeedTableProvider;
import com.dianping.cat.config.black.BlackListManager;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.content.DefaultContentFetcher;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.home.dal.report.UserDefineRuleDao;
import com.dianping.cat.report.graph.metric.CachedMetricReportService;
import com.dianping.cat.report.graph.metric.DataExtractor;
import com.dianping.cat.report.graph.metric.MetricDataFetcher;
import com.dianping.cat.report.graph.metric.impl.CachedMetricReportServiceImpl;
import com.dianping.cat.report.graph.metric.impl.DataExtractorImpl;
import com.dianping.cat.report.graph.metric.impl.MetricDataFetcherImpl;
import com.dianping.cat.report.graph.svg.DefaultGraphBuilder;
import com.dianping.cat.report.graph.svg.DefaultValueTranslater;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.graph.svg.ValueTranslater;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.dependency.graph.DependencyItemBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.state.StateGraphBuilder;
import com.dianping.cat.report.page.storage.topology.StorageAlertInfoRTContainer;
import com.dianping.cat.report.page.storage.topology.StorageAlertInfoManager;
import com.dianping.cat.report.page.storage.topology.StorageGraphBuilder;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.service.app.AppDataService;
import com.dianping.cat.report.service.app.AppSpeedService;
import com.dianping.cat.report.alert.AlertInfo;
import com.dianping.cat.report.task.cmdb.ProjectUpdateTask;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.IpService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.config.ActivityConfigManager;
import com.dianping.cat.system.config.AlertConfigManager;
import com.dianping.cat.system.config.AppRuleConfigManager;
import com.dianping.cat.system.config.BugConfigManager;
import com.dianping.cat.system.config.BusinessRuleConfigManager;
import com.dianping.cat.system.config.ConfigReloadTask;
import com.dianping.cat.system.config.DomainGroupConfigManager;
import com.dianping.cat.system.config.ExceptionRuleConfigManager;
import com.dianping.cat.system.config.HeartbeatRuleConfigManager;
import com.dianping.cat.system.config.NetGraphConfigManager;
import com.dianping.cat.system.config.NetworkRuleConfigManager;
import com.dianping.cat.system.config.RouterConfigManager;
import com.dianping.cat.system.config.SenderConfigManager;
import com.dianping.cat.system.config.StorageCacheRuleConfigManager;
import com.dianping.cat.system.config.StorageDatabaseRuleConfigManager;
import com.dianping.cat.system.config.StorageGroupConfigManager;
import com.dianping.cat.system.config.SystemRuleConfigManager;
import com.dianping.cat.system.config.ThirdPartyConfigManager;
import com.dianping.cat.system.config.TopoGraphFormatConfigManager;
import com.dianping.cat.system.config.TransactionRuleConfigManager;
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

		all.add(C(StateGraphBuilder.class, StateGraphBuilder.class).//
		      req(ReportServiceManager.class, ServerConfigManager.class));

		all.add(C(DependencyItemBuilder.class).req(TopologyGraphConfigManager.class));

		all.add(C(TopologyGraphBuilder.class).req(DependencyItemBuilder.class));

		all.add(C(TopologyGraphManager.class)
		      .req(TopologyGraphBuilder.class, DependencyItemBuilder.class, ServerConfigManager.class) //
		      .req(ProductLineConfigManager.class, TopologyGraphDao.class)//
		      .req(ModelService.class, DependencyAnalyzer.ID));

		// update project database
		all.add(C(ProjectUpdateTask.class).req(ProjectService.class, HostinfoService.class)//
		      .req(ReportService.class, TransactionAnalyzer.ID));

		all.add(C(StorageAlertInfoRTContainer.class));
		all.add(C(StorageGraphBuilder.class).req(StorageAlertInfoRTContainer.class));
		all.add(C(StorageAlertInfoManager.class).req(ServerConfigManager.class, AlertDao.class)
		      .req(StorageAlertInfoRTContainer.class).req(StorageGraphBuilder.class));

		return all;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ContentFetcher.class, DefaultContentFetcher.class));

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

		all.add(C(TableProvider.class, "app-command-data", AppCommandDataTableProvider.class));
		all.add(C(TableProvider.class, "app-speed-data", AppSpeedTableProvider.class));

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
		all.add(C(TopologyGraphConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(ExceptionRuleConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(DomainGroupConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(BugConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(NetworkRuleConfigManager.class)
		      .req(ConfigDao.class, UserDefinedRuleManager.class, ContentFetcher.class));
		all.add(C(BusinessRuleConfigManager.class).req(ConfigDao.class, MetricConfigManager.class,
		      UserDefinedRuleManager.class, ContentFetcher.class));
		all.add(C(AppRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class, ContentFetcher.class));
		all.add(C(WebRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class, ContentFetcher.class));
		all.add(C(TransactionRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class,
		      ContentFetcher.class));
		all.add(C(HeartbeatRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class,
		      ContentFetcher.class));
		all.add(C(SystemRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class, ContentFetcher.class));
		all.add(C(StorageDatabaseRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class,
		      ContentFetcher.class));
		all.add(C(StorageGroupConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(StorageCacheRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class,
		      ContentFetcher.class));
		all.add(C(AlertConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(NetGraphConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(ThirdPartyConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(RouterConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(TopoGraphFormatConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(SenderConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(ActivityConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(ConfigReloadTask.class).req(MetricConfigManager.class, ProductLineConfigManager.class,
		      RouterConfigManager.class, BlackListManager.class));

		return all;
	}

	private List<Component> defineMetricComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(CachedMetricReportService.class, CachedMetricReportServiceImpl.class)
		      .req(ModelService.class, MetricAnalyzer.ID).req(ReportServiceManager.class).req(IpService.class));
		all.add(C(DataExtractor.class, DataExtractorImpl.class));
		all.add(C(MetricDataFetcher.class, MetricDataFetcherImpl.class));
		all.add(C(AlertInfo.class).req(MetricConfigManager.class));

		all.add(C(AppSpeedService.class).req(AppSpeedDataDao.class));
		all.add(C(AppDataService.class).req(AppCommandDataDao.class, AppConfigManager.class));

		return all;
	}
}
