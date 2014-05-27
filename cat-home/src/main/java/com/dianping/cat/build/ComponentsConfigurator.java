package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.initialization.DefaultModuleManager;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatHomeModule;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.home.dal.report.EventDao;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.chart.AggregationGraphCreator;
import com.dianping.cat.report.chart.CachedMetricReportService;
import com.dianping.cat.report.chart.DataExtractor;
import com.dianping.cat.report.chart.NetworkGraphCreator;
import com.dianping.cat.report.chart.GraphCreator;
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
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphItemBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.page.externalError.EventCollectManager;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.state.StateGraphs;
import com.dianping.cat.report.page.userMonitor.graph.DefaultUserMonitGraphCreator;
import com.dianping.cat.report.page.userMonitor.graph.UserMonitorGraphCreator;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.exceptionAlert.ExceptionAlert;
import com.dianping.cat.report.task.metric.MetricAlertConfig;
import com.dianping.cat.report.task.metric.AlertInfo;
import com.dianping.cat.report.task.metric.MetricAlert;
import com.dianping.cat.report.task.metric.RemoteMetricReportService;
import com.dianping.cat.report.task.metric.SwitchAlert;
import com.dianping.cat.report.task.metric.SwitchAlertConfig;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.system.config.BugConfigManager;
import com.dianping.cat.system.config.ConfigReloadTask;
import com.dianping.cat.system.config.DomainGroupConfigManager;
import com.dianping.cat.system.config.ExceptionThresholdConfigManager;
import com.dianping.cat.system.config.MetricAggregationConfigManager;
import com.dianping.cat.system.config.MetricGroupConfigManager;
import com.dianping.cat.system.config.MetricRuleConfigManager;
import com.dianping.cat.system.config.UtilizationConfigManager;
import com.dianping.cat.system.tool.DefaultMailImpl;
import com.dianping.cat.system.tool.MailSMS;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(JsonBuilder.class));

		all.add(C(ValueTranslater.class, DefaultValueTranslater.class));
		all.add(C(GraphBuilder.class, DefaultGraphBuilder.class) //
		      .req(ValueTranslater.class));

		all.add(C(PayloadNormalizer.class).req(ServerConfigManager.class));

		all.add(C(StateGraphs.class, StateGraphs.class).//
		      req(ReportService.class));

		all.add(C(Module.class, CatHomeModule.ID, CatHomeModule.class));
		all.add(C(ModuleManager.class, DefaultModuleManager.class) //
		      .config(E("topLevelModules").value(CatHomeModule.ID)));
		all.add(C(DomainNavManager.class).req(ProjectDao.class));

		all.add(C(EventCollectManager.class).req(EventDao.class, ServerConfigManager.class));

		all.add(C(TopologyGraphConfigManager.class).req(ConfigDao.class));

		all.add(C(ExceptionThresholdConfigManager.class).req(ConfigDao.class));

		all.add(C(DomainGroupConfigManager.class).req(ConfigDao.class));

		all.add(C(BugConfigManager.class).req(ConfigDao.class));

		all.add(C(UtilizationConfigManager.class).req(ConfigDao.class));

		all.add(C(MetricGroupConfigManager.class).req(ConfigDao.class));

		all.add(C(MetricAggregationConfigManager.class).req(ConfigDao.class));

		all.add(C(MetricRuleConfigManager.class).req(ConfigDao.class));

		all.add(C(TopologyGraphItemBuilder.class).req(TopologyGraphConfigManager.class));

		all.add(C(TopologyGraphBuilder.class).req(TopologyGraphItemBuilder.class).is(PER_LOOKUP));

		all.add(C(TopologyGraphManager.class).req(TopologyGraphBuilder.class, ServerConfigManager.class) //
		      .req(ProductLineConfigManager.class, TopologyGraphDao.class, DomainNavManager.class)//
		      .req(ModelService.class, DependencyAnalyzer.ID));

		all.add(C(ConfigReloadTask.class).req(MetricConfigManager.class, ProductLineConfigManager.class));

		all.add(C(CachedMetricReportService.class, CachedMetricReportServiceImpl.class).req(ModelService.class,
		      MetricAnalyzer.ID).req(ReportService.class));

		all.add(C(DataExtractor.class, DataExtractorImpl.class));

		all.add(C(MetricDataFetcher.class, MetricDataFetcherImpl.class));

		all.add(C(AlertInfo.class).req(MetricConfigManager.class));

		all.add(C(GraphCreator.class).req(CachedMetricReportService.class, DataExtractor.class, MetricDataFetcher.class)
		      .req(BaselineService.class, MetricConfigManager.class, ProductLineConfigManager.class,
		            MetricGroupConfigManager.class, AlertInfo.class));
		all.add(C(AggregationGraphCreator.class).req(CachedMetricReportService.class, DataExtractor.class,
		      MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, MetricGroupConfigManager.class, MetricAggregationConfigManager.class,
		      AlertInfo.class));

		all.add(C(UserMonitorGraphCreator.class, DefaultUserMonitGraphCreator.class).req(CachedMetricReportService.class,
		      DataExtractor.class, MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, MetricGroupConfigManager.class, AlertInfo.class));

		all.add(C(NetworkGraphCreator.class).req(CachedMetricReportService.class, DataExtractor.class,
		      MetricDataFetcher.class).req(BaselineService.class, MetricConfigManager.class,
		      ProductLineConfigManager.class, MetricGroupConfigManager.class, AlertInfo.class));
		// report serivce
		all.addAll(new ReportServiceComponentConfigurator().defineComponents());
		// task
		all.addAll(new TaskComponentConfigurator().defineComponents());

		// model service
		all.addAll(new ServiceComponentConfigurator().defineComponents());

		all.add(C(RemoteMetricReportService.class).req(ServerConfigManager.class));

		all.add(C(MetricAlertConfig.class));

		all.add(C(SwitchAlertConfig.class));

		all.add(C(AlertInfo.class));

		all.add(C(DefaultMailImpl.class).req(ServerConfigManager.class));

		all.add(C(MetricAlert.class).req(MetricConfigManager.class, ProductLineConfigManager.class,
		      BaselineService.class, MailSMS.class, MetricAlertConfig.class, AlertInfo.class)//
		      .req(RemoteMetricReportService.class));

		all.add(C(SwitchAlert.class).req(MetricConfigManager.class, ProductLineConfigManager.class,
		      BaselineService.class, MailSMS.class, SwitchAlertConfig.class, AlertInfo.class)//
		      .req(RemoteMetricReportService.class, MetricRuleConfigManager.class));

		all.add(C(ExceptionAlert.class).req(ProjectDao.class, MetricAlertConfig.class, MailSMS.class,
		      ExceptionThresholdConfigManager.class).req(ModelService.class, TopAnalyzer.ID));

		// database
		all.add(C(JdbcDataSourceDescriptorManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));
		all.addAll(new CatDatabaseConfigurator().defineComponents());
		all.addAll(new UserDatabaseConfigurator().defineComponents());

		// web, please keep it last
		all.addAll(new WebComponentConfigurator().defineComponents());

		// for alarm module
		all.addAll(new AlarmComponentConfigurator().defineComponents());

		return all;
	}
}
