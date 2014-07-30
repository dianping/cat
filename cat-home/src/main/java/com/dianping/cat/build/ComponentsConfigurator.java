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
import com.dianping.cat.report.task.alert.exception.AlertExceptionBuilder;
import com.dianping.cat.report.task.alert.exception.ExceptionAlert;
import com.dianping.cat.report.task.alert.network.NetworkAlert;
import com.dianping.cat.report.task.alert.sender.AlertManager;
import com.dianping.cat.report.task.alert.sender.decorator.BusinessDecorator;
import com.dianping.cat.report.task.alert.sender.decorator.Decorator;
import com.dianping.cat.report.task.alert.sender.decorator.DecoratorManager;
import com.dianping.cat.report.task.alert.sender.decorator.ExceptionDecorator;
import com.dianping.cat.report.task.alert.sender.decorator.NetworkDecorator;
import com.dianping.cat.report.task.alert.sender.decorator.SystemDecorator;
import com.dianping.cat.report.task.alert.sender.decorator.ThirdpartyDecorator;
import com.dianping.cat.report.task.alert.sender.receiver.BusinessContactor;
import com.dianping.cat.report.task.alert.sender.receiver.Contactor;
import com.dianping.cat.report.task.alert.sender.receiver.ContactorManager;
import com.dianping.cat.report.task.alert.sender.receiver.ExceptionContactor;
import com.dianping.cat.report.task.alert.sender.receiver.NetworkContactor;
import com.dianping.cat.report.task.alert.sender.receiver.SystemContactor;
import com.dianping.cat.report.task.alert.sender.receiver.ThirdpartyContactor;
import com.dianping.cat.report.task.alert.sender.sender.MailSender;
import com.dianping.cat.report.task.alert.sender.sender.Sender;
import com.dianping.cat.report.task.alert.sender.sender.SenderManager;
import com.dianping.cat.report.task.alert.sender.sender.SmsSender;
import com.dianping.cat.report.task.alert.sender.sender.WeixinSender;
import com.dianping.cat.report.task.alert.sender.spliter.MailSpliter;
import com.dianping.cat.report.task.alert.sender.spliter.SmsSpliter;
import com.dianping.cat.report.task.alert.sender.spliter.Spliter;
import com.dianping.cat.report.task.alert.sender.spliter.SpliterManager;
import com.dianping.cat.report.task.alert.sender.spliter.WeixinSpliter;
import com.dianping.cat.report.task.alert.service.AlertEntityService;
import com.dianping.cat.report.task.alert.summary.AlertSummaryDecorator;
import com.dianping.cat.report.task.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.task.alert.summary.AlertSummaryFTLDecorator;
import com.dianping.cat.report.task.alert.summary.AlertSummaryGenerator;
import com.dianping.cat.report.task.alert.summary.AlertSummaryManager;
import com.dianping.cat.report.task.alert.system.SystemAlert;
import com.dianping.cat.report.task.alert.thirdParty.HttpConnector;
import com.dianping.cat.report.task.alert.thirdParty.ThirdPartyAlert;
import com.dianping.cat.report.task.alert.thirdParty.ThirdPartyAlertBuilder;
import com.dianping.cat.report.task.product.ProjectUpdateTask;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.service.IpService;
import com.dianping.cat.system.config.AlertConfigManager;
import com.dianping.cat.system.config.AlertPolicyManager;
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

		all.add(C(AlertInfo.class));
		all.add(C(DefaultMailImpl.class).req(ServerConfigManager.class));
		all.add(C(DataChecker.class, DefaultDataChecker.class));
		all.add(C(RemoteMetricReportService.class).req(ServerConfigManager.class));

		all.add(C(Contactor.class, BusinessContactor.ID, BusinessContactor.class).req(ProductLineConfigManager.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, NetworkContactor.ID, NetworkContactor.class).req(ProductLineConfigManager.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, SystemContactor.ID, SystemContactor.class).req(ProductLineConfigManager.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, ExceptionContactor.ID, ExceptionContactor.class).req(ProjectDao.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, ThirdpartyContactor.ID, ThirdpartyContactor.class).req(ProjectDao.class,
		      AlertConfigManager.class));

		all.add(C(ContactorManager.class).req(Contactor.class, BusinessContactor.ID, "businessContactor")
		      .req(Contactor.class, NetworkContactor.ID, "networkContactor")
		      .req(Contactor.class, SystemContactor.ID, "exceptionContactor")
		      .req(Contactor.class, ExceptionContactor.ID, "systemContactor")
		      .req(Contactor.class, ThirdpartyContactor.ID, "thirdpartyContactor"));

		all.add(C(Decorator.class, BusinessDecorator.ID, BusinessDecorator.class).req(ProductLineConfigManager.class));

		all.add(C(Decorator.class, NetworkDecorator.ID, NetworkDecorator.class).req(ProductLineConfigManager.class));

		all.add(C(Decorator.class, ExceptionDecorator.ID, ExceptionDecorator.class).req(ProjectDao.class));

		all.add(C(Decorator.class, SystemDecorator.ID, SystemDecorator.class).req(ProductLineConfigManager.class));

		all.add(C(Decorator.class, ThirdpartyDecorator.ID, ThirdpartyDecorator.class).req(ProjectDao.class));

		all.add(C(DecoratorManager.class).req(Decorator.class, BusinessDecorator.ID, "businessDecorator")
		      .req(Decorator.class, NetworkDecorator.ID, "networkDecorator")
		      .req(Decorator.class, ExceptionDecorator.ID, "exceptionDecorator")
		      .req(Decorator.class, SystemDecorator.ID, "systemDecorator")
		      .req(Decorator.class, ThirdpartyDecorator.ID, "thirdpartyDecorator"));

		all.add(C(AlertPolicyManager.class).req(ConfigDao.class));

		all.add(C(Spliter.class, MailSpliter.ID, MailSpliter.class));

		all.add(C(Spliter.class, SmsSpliter.ID, SmsSpliter.class));

		all.add(C(Spliter.class, WeixinSpliter.ID, WeixinSpliter.class));

		all.add(C(SpliterManager.class).req(Spliter.class, MailSpliter.ID, "mailSpliter")
		      .req(Spliter.class, SmsSpliter.ID, "smsSpliter").req(Spliter.class, WeixinSpliter.ID, "weixinSpliter"));

		all.add(C(Sender.class, MailSender.ID, MailSender.class).req(ServerConfigManager.class));

		all.add(C(Sender.class, SmsSender.ID, SmsSender.class));

		all.add(C(Sender.class, WeixinSender.ID, WeixinSender.class));

		all.add(C(SenderManager.class).req(Sender.class, MailSender.ID, "mailSender")
		      .req(Sender.class, WeixinSender.ID, "weixinSender").req(Sender.class, SmsSender.ID, "smsSender"));

		all.add(C(AlertManager.class).req(AlertPolicyManager.class, DecoratorManager.class, ContactorManager.class,
		      AlertEntityService.class, SpliterManager.class, SenderManager.class));

		all.add(C(BusinessAlert.class).req(MetricConfigManager.class, ProductLineConfigManager.class,
		      BaselineService.class, AlertInfo.class).req(RemoteMetricReportService.class,
		      BusinessRuleConfigManager.class, DataChecker.class, AlertManager.class));

		all.add(C(NetworkAlert.class).req(ProductLineConfigManager.class, BaselineService.class, AlertInfo.class).req(
		      RemoteMetricReportService.class, NetworkRuleConfigManager.class, DataChecker.class, AlertManager.class));

		all.add(C(SystemAlert.class).req(ProductLineConfigManager.class, BaselineService.class, AlertInfo.class).req(
		      RemoteMetricReportService.class, SystemRuleConfigManager.class, DataChecker.class, AlertManager.class));

		all.add(C(AlertExceptionBuilder.class).req(ExceptionConfigManager.class));

		all.add(C(ExceptionAlert.class)
		      .req(ExceptionConfigManager.class, AlertExceptionBuilder.class, AlertManager.class).req(ModelService.class,
		            TopAnalyzer.ID));

		all.add(C(ThirdPartyAlert.class).req(AlertManager.class));

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

		all.add(C(AlertEntityService.class).req(AlertDao.class));

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
