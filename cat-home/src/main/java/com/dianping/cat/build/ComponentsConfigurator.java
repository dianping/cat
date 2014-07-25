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
import com.dianping.cat.config.app.AppDataCommandTableProvider;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.dal.HostinfoDao;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.home.dal.report.AlertSummaryDao;
import com.dianping.cat.home.dal.report.EventDao;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
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
import com.dianping.cat.report.page.userMonitor.graph.DefaultUserMonitGraphCreator;
import com.dianping.cat.report.page.userMonitor.graph.UserMonitorGraphCreator;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.alert.AlertInfo;
import com.dianping.cat.report.task.alert.DataChecker;
import com.dianping.cat.report.task.alert.DefaultDataChecker;
import com.dianping.cat.report.task.alert.RemoteMetricReportService;
import com.dianping.cat.report.task.alert.business.BusinessAlert;
import com.dianping.cat.report.task.alert.business.BusinessAlertConfig;
import com.dianping.cat.report.task.alert.exception.AlertExceptionBuilder;
import com.dianping.cat.report.task.alert.exception.ExceptionAlert;
import com.dianping.cat.report.task.alert.exception.ExceptionAlertConfig;
import com.dianping.cat.report.task.alert.manager.AlertManager;
import com.dianping.cat.report.task.alert.network.NetworkAlert;
import com.dianping.cat.report.task.alert.network.NetworkAlertConfig;
import com.dianping.cat.report.task.alert.sender.MailSender;
import com.dianping.cat.report.task.alert.sender.Postman;
import com.dianping.cat.report.task.alert.sender.SmsSender;
import com.dianping.cat.report.task.alert.sender.WeixinSender;
import com.dianping.cat.report.task.alert.summary.AlertSummaryDecorator;
import com.dianping.cat.report.task.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.task.alert.summary.AlertSummaryFTLDecorator;
import com.dianping.cat.report.task.alert.summary.AlertSummaryGenerator;
import com.dianping.cat.report.task.alert.summary.AlertSummaryManager;
import com.dianping.cat.report.task.alert.system.SystemAlert;
import com.dianping.cat.report.task.alert.system.SystemAlertConfig;
import com.dianping.cat.report.task.alert.thirdParty.HttpConnector;
import com.dianping.cat.report.task.alert.thirdParty.ThirdPartyAlert;
import com.dianping.cat.report.task.alert.thirdParty.ThirdPartyAlertBuilder;
import com.dianping.cat.report.task.product.ProjectUpdateTask;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.service.IpService;
import com.dianping.cat.system.config.AlertConfigManager;
import com.dianping.cat.system.config.AlertTypeManager;
import com.dianping.cat.system.config.BugConfigManager;
import com.dianping.cat.system.config.BusinessRuleConfigManager;
import com.dianping.cat.system.config.ConfigReloadTask;
import com.dianping.cat.system.config.DomainGroupConfigManager;
import com.dianping.cat.system.config.ExceptionConfigManager;
import com.dianping.cat.system.config.MetricGroupConfigManager;
import com.dianping.cat.system.config.NetGraphConfigManager;
import com.dianping.cat.system.config.NetworkRuleConfigManager;
import com.dianping.cat.system.config.RouterConfigManager;
import com.dianping.cat.system.config.SystemRuleConfigManager;
import com.dianping.cat.system.config.ThirdPartyConfigManager;
import com.dianping.cat.system.tool.DefaultMailImpl;
import com.dianping.cat.system.tool.MailSMS;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	private List<Component> defineAlertComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(BusinessAlertConfig.class).req(AlertConfigManager.class));
		all.add(C(NetworkAlertConfig.class).req(AlertConfigManager.class));
		all.add(C(SystemAlertConfig.class).req(AlertConfigManager.class));
		all.add(C(ExceptionAlertConfig.class).req(AlertConfigManager.class));
		all.add(C(AlertInfo.class));
		all.add(C(DefaultMailImpl.class).req(ServerConfigManager.class));
		all.add(C(DataChecker.class, DefaultDataChecker.class));
		all.add(C(RemoteMetricReportService.class).req(ServerConfigManager.class));

		all.add(C(BusinessAlert.class).req(MetricConfigManager.class, ProductLineConfigManager.class,
		      BaselineService.class, MailSMS.class, BusinessAlertConfig.class, AlertInfo.class, AlertDao.class)//
		      .req(RemoteMetricReportService.class, BusinessRuleConfigManager.class, DataChecker.class));

		all.add(C(NetworkAlert.class).req(MetricConfigManager.class, ProductLineConfigManager.class,
		      BaselineService.class, MailSMS.class, NetworkAlertConfig.class, AlertInfo.class, AlertDao.class)//
		      .req(RemoteMetricReportService.class, NetworkRuleConfigManager.class, DataChecker.class));

		all.add(C(SystemAlert.class).req(MetricConfigManager.class, ProductLineConfigManager.class,
		      BaselineService.class, MailSMS.class, SystemAlertConfig.class, AlertInfo.class, AlertDao.class)//
		      .req(RemoteMetricReportService.class, SystemRuleConfigManager.class, DataChecker.class));

		all.add(C(AlertExceptionBuilder.class).req(ExceptionConfigManager.class));

		all.add(C(ExceptionAlert.class).req(ProjectDao.class, ExceptionAlertConfig.class, MailSMS.class,
		      ExceptionConfigManager.class, AlertExceptionBuilder.class, AlertDao.class).req(ModelService.class,
		      TopAnalyzer.ID));

		all.add(C(ThirdPartyAlert.class).req(ProjectDao.class, MailSender.class));

		all.add(C(HttpConnector.class));

		all.add(C(ThirdPartyAlertBuilder.class).req(HttpConnector.class, ThirdPartyAlert.class,
		      ThirdPartyConfigManager.class));

		return all;
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
		all.add(C(DomainNavManager.class).req(ProjectDao.class));

		all.add(C(EventCollectManager.class).req(EventDao.class, ServerConfigManager.class));

		all.add(C(TopologyGraphItemBuilder.class).req(TopologyGraphConfigManager.class));

		all.add(C(TopologyGraphBuilder.class).req(TopologyGraphItemBuilder.class).is(PER_LOOKUP));

		all.add(C(TopologyGraphManager.class).req(TopologyGraphBuilder.class, ServerConfigManager.class) //
		      .req(ProductLineConfigManager.class, TopologyGraphDao.class, DomainNavManager.class)//
		      .req(ModelService.class, DependencyAnalyzer.ID));

		// update project database
		all.add(C(ProjectUpdateTask.class)//
		      .req(ProjectDao.class, HostinfoDao.class));

		return all;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineCommonComponents());

		all.addAll(defineConfigComponents());

		all.addAll(defineMetricComponents());

		all.addAll(defineAlertComponents());

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
		all.addAll(new UserDatabaseConfigurator().defineComponents());

		// web, please keep it last
		all.addAll(new WebComponentConfigurator().defineComponents());

		// for alarm module
		all.addAll(new AlarmComponentConfigurator().defineComponents());

		return all;
	}

	private List<Component> defineConfigComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(TopologyGraphConfigManager.class).req(ConfigDao.class));
		all.add(C(ExceptionConfigManager.class).req(ConfigDao.class));
		all.add(C(DomainGroupConfigManager.class).req(ConfigDao.class));
		all.add(C(BugConfigManager.class).req(ConfigDao.class));
		all.add(C(MetricGroupConfigManager.class).req(ConfigDao.class));
		all.add(C(NetworkRuleConfigManager.class).req(ConfigDao.class));
		all.add(C(BusinessRuleConfigManager.class).req(ConfigDao.class));
		all.add(C(AlertConfigManager.class).req(ConfigDao.class));
		all.add(C(NetGraphConfigManager.class).req(ConfigDao.class));
		all.add(C(ThirdPartyConfigManager.class).req(ConfigDao.class));
		all.add(C(RouterConfigManager.class).req(ConfigDao.class));
		all.add(C(ConfigReloadTask.class).req(MetricConfigManager.class, ProductLineConfigManager.class));

		return all;
	}

	private List<Component> defineMetricComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(IpService.class));
		all.add(C(CachedMetricReportService.class, CachedMetricReportServiceImpl.class)
		      .req(ModelService.class, MetricAnalyzer.ID).req(ReportServiceManager.class).req(IpService.class));
		all.add(C(DataExtractor.class, DataExtractorImpl.class));
		all.add(C(MetricDataFetcher.class, MetricDataFetcherImpl.class));
		all.add(C(AlertInfo.class).req(MetricConfigManager.class));
		all.add(C(CdnGraphCreator.class).req(BaselineService.class, DataExtractor.class, MetricDataFetcher.class,
		      CachedMetricReportService.class, MetricConfigManager.class, ProductLineConfigManager.class,
		      MetricGroupConfigManager.class, AlertInfo.class));
		all.add(C(MetricGraphCreator.class).req(CachedMetricReportService.class, DataExtractor.class,
		      MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, MetricGroupConfigManager.class, AlertInfo.class, ProjectDao.class));
		all.add(C(SystemGraphCreator.class).req(CachedMetricReportService.class, DataExtractor.class,
		      MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, MetricGroupConfigManager.class, AlertInfo.class));
		all.add(C(UserMonitorGraphCreator.class, DefaultUserMonitGraphCreator.class).req(CachedMetricReportService.class,
		      DataExtractor.class, MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, MetricGroupConfigManager.class, AlertInfo.class));
		all.add(C(NetworkGraphCreator.class).req(CachedMetricReportService.class, DataExtractor.class,
		      MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, MetricGroupConfigManager.class, AlertInfo.class));

		all.add(C(AppGraphCreator.class).req(AppDataService.class, CachedMetricReportService.class, DataExtractor.class,
		      MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, MetricGroupConfigManager.class, AlertInfo.class));

		all.add(C(NetGraphManager.class).req(ServerConfigManager.class, RemoteMetricReportService.class).req(
		      ReportServiceManager.class, NetGraphBuilder.class, AlertInfo.class, NetGraphConfigManager.class));

		all.add(C(MailSender.class).req(MailSMS.class));

		all.add(C(SmsSender.class).req(MailSMS.class));

		all.add(C(WeixinSender.class).req(MailSMS.class));

		all.add(C(AlertManager.class).req(AlertDao.class));

		all.add(C(Postman.class).req(ProjectDao.class, MailSMS.class, MailSender.class, WeixinSender.class,
		      SmsSender.class, AlertTypeManager.class));

		all.add(C(AlertExceptionBuilder.class).req(ExceptionConfigManager.class));

		all.add(C(AlertSummaryExecutor.class).req(AlertSummaryGenerator.class, AlertSummaryManager.class, MailSMS.class)
		      .req(AlertSummaryDecorator.class, AlertSummaryFTLDecorator.ID));

		all.add(C(AlertSummaryDecorator.class, AlertSummaryFTLDecorator.ID, AlertSummaryFTLDecorator.class));

		all.add(C(AlertSummaryGenerator.class).req(AlertDao.class, TopologyGraphManager.class));

		all.add(C(AlertSummaryManager.class).req(AlertSummaryDao.class));

		all.add(C(NetGraphBuilder.class));

		return all;
	}
}
