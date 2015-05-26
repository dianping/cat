package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.web.js.AggregationConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.home.dal.report.AlertSummaryDao;
import com.dianping.cat.home.dal.report.AlterationDao;
import com.dianping.cat.report.alert.AlertInfo;
import com.dianping.cat.report.alert.DataChecker;
import com.dianping.cat.report.alert.DefaultDataChecker;
import com.dianping.cat.report.alert.MetricReportGroupService;
import com.dianping.cat.report.alert.app.AppAlert;
import com.dianping.cat.report.alert.app.AppRuleConfigManager;
import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.business.BusinessRuleConfigManager;
import com.dianping.cat.report.alert.database.DatabaseAlert;
import com.dianping.cat.report.alert.database.DatabaseRuleConfigManager;
import com.dianping.cat.report.alert.exception.AlertExceptionBuilder;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.alert.exception.FrontEndExceptionAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.report.alert.network.NetworkAlert;
import com.dianping.cat.report.alert.network.NetworkRuleConfigManager;
import com.dianping.cat.report.alert.sender.AlertManager;
import com.dianping.cat.report.alert.sender.config.AlertConfigManager;
import com.dianping.cat.report.alert.sender.config.AlertPolicyManager;
import com.dianping.cat.report.alert.sender.config.SenderConfigManager;
import com.dianping.cat.report.alert.sender.decorator.AppDecorator;
import com.dianping.cat.report.alert.sender.decorator.BusinessDecorator;
import com.dianping.cat.report.alert.sender.decorator.DatabaseDecorator;
import com.dianping.cat.report.alert.sender.decorator.Decorator;
import com.dianping.cat.report.alert.sender.decorator.DecoratorManager;
import com.dianping.cat.report.alert.sender.decorator.ExceptionDecorator;
import com.dianping.cat.report.alert.sender.decorator.FrontEndExceptionDecorator;
import com.dianping.cat.report.alert.sender.decorator.HeartbeatDecorator;
import com.dianping.cat.report.alert.sender.decorator.NetworkDecorator;
import com.dianping.cat.report.alert.sender.decorator.StorageCacheDecorator;
import com.dianping.cat.report.alert.sender.decorator.StorageSQLDecorator;
import com.dianping.cat.report.alert.sender.decorator.SystemDecorator;
import com.dianping.cat.report.alert.sender.decorator.ThirdpartyDecorator;
import com.dianping.cat.report.alert.sender.decorator.TransactionDecorator;
import com.dianping.cat.report.alert.sender.decorator.WebDecorator;
import com.dianping.cat.report.alert.sender.receiver.AppContactor;
import com.dianping.cat.report.alert.sender.receiver.BusinessContactor;
import com.dianping.cat.report.alert.sender.receiver.Contactor;
import com.dianping.cat.report.alert.sender.receiver.ContactorManager;
import com.dianping.cat.report.alert.sender.receiver.DatabaseContactor;
import com.dianping.cat.report.alert.sender.receiver.ExceptionContactor;
import com.dianping.cat.report.alert.sender.receiver.FrontEndExceptionContactor;
import com.dianping.cat.report.alert.sender.receiver.HeartbeatContactor;
import com.dianping.cat.report.alert.sender.receiver.NetworkContactor;
import com.dianping.cat.report.alert.sender.receiver.StorageCacheContactor;
import com.dianping.cat.report.alert.sender.receiver.StorageSQLContactor;
import com.dianping.cat.report.alert.sender.receiver.SystemContactor;
import com.dianping.cat.report.alert.sender.receiver.ThirdpartyContactor;
import com.dianping.cat.report.alert.sender.receiver.TransactionContactor;
import com.dianping.cat.report.alert.sender.receiver.WebContactor;
import com.dianping.cat.report.alert.sender.sender.MailSender;
import com.dianping.cat.report.alert.sender.sender.Sender;
import com.dianping.cat.report.alert.sender.sender.SenderManager;
import com.dianping.cat.report.alert.sender.sender.SmsSender;
import com.dianping.cat.report.alert.sender.sender.WeixinSender;
import com.dianping.cat.report.alert.sender.spliter.MailSpliter;
import com.dianping.cat.report.alert.sender.spliter.SmsSpliter;
import com.dianping.cat.report.alert.sender.spliter.Spliter;
import com.dianping.cat.report.alert.sender.spliter.SpliterManager;
import com.dianping.cat.report.alert.sender.spliter.WeixinSpliter;
import com.dianping.cat.report.alert.service.AlertEntityService;
import com.dianping.cat.report.alert.storage.StorageCacheAlert;
import com.dianping.cat.report.alert.storage.StorageCacheRuleConfigManager;
import com.dianping.cat.report.alert.storage.StorageSQLAlert;
import com.dianping.cat.report.alert.storage.StorageSQLRuleConfigManager;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.alert.summary.AlertSummaryService;
import com.dianping.cat.report.alert.summary.build.AlertInfoBuilder;
import com.dianping.cat.report.alert.summary.build.AlterationSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.FailureSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.RelatedSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.SummaryBuilder;
import com.dianping.cat.report.alert.system.SystemAlert;
import com.dianping.cat.report.alert.system.SystemRuleConfigManager;
import com.dianping.cat.report.alert.thirdParty.HttpConnector;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlertBuilder;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyConfigManager;
import com.dianping.cat.report.alert.transaction.TransactionAlert;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.report.alert.web.WebAlert;
import com.dianping.cat.report.alert.web.WebRuleConfigManager;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.page.metric.service.BaselineService;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.topology.StorageAlertInfoBuilder;
import com.dianping.cat.report.page.storage.transform.StorageMergeHelper;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.page.web.service.WebApiService;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;

public class AlarmComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {

		List<Component> all = new ArrayList<Component>();

		all.add(C(AlertInfo.class));
		all.add(C(DataChecker.class, DefaultDataChecker.class));
		all.add(C(MetricReportGroupService.class).req(ModelService.class, MetricAnalyzer.ID));
		all.add(C(Contactor.class, BusinessContactor.ID, BusinessContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, NetworkContactor.ID, NetworkContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, DatabaseContactor.ID, DatabaseContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, SystemContactor.ID, SystemContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, ExceptionContactor.ID, ExceptionContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, HeartbeatContactor.ID, HeartbeatContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, ThirdpartyContactor.ID, ThirdpartyContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, FrontEndExceptionContactor.ID, FrontEndExceptionContactor.class).req(
		      AggregationConfigManager.class, AlertConfigManager.class));

		all.add(C(Contactor.class, AppContactor.ID, AppContactor.class).req(AlertConfigManager.class,
		      AppConfigManager.class, ProjectService.class));

		all.add(C(Contactor.class, WebContactor.ID, WebContactor.class).req(AlertConfigManager.class,
		      ProjectService.class, UrlPatternConfigManager.class));

		all.add(C(Contactor.class, TransactionContactor.ID, TransactionContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, StorageSQLContactor.ID, StorageSQLContactor.class).req(AlertConfigManager.class));

		all.add(C(Contactor.class, StorageCacheContactor.ID, StorageCacheContactor.class).req(AlertConfigManager.class));

		all.add(C(ContactorManager.class));

		all.add(C(Decorator.class, BusinessDecorator.ID, BusinessDecorator.class).req(ProductLineConfigManager.class,
		      AlertSummaryExecutor.class, ProjectService.class));

		all.add(C(Decorator.class, NetworkDecorator.ID, NetworkDecorator.class));

		all.add(C(Decorator.class, DatabaseDecorator.ID, DatabaseDecorator.class));

		all.add(C(Decorator.class, HeartbeatDecorator.ID, HeartbeatDecorator.class));

		all.add(C(Decorator.class, ExceptionDecorator.ID, ExceptionDecorator.class).req(ProjectService.class,
		      AlertSummaryExecutor.class));

		all.add(C(Decorator.class, SystemDecorator.ID, SystemDecorator.class));

		all.add(C(Decorator.class, ThirdpartyDecorator.ID, ThirdpartyDecorator.class).req(ProjectService.class));

		all.add(C(Decorator.class, FrontEndExceptionDecorator.ID, FrontEndExceptionDecorator.class));

		all.add(C(Decorator.class, AppDecorator.ID, AppDecorator.class));

		all.add(C(Decorator.class, WebDecorator.ID, WebDecorator.class));

		all.add(C(Decorator.class, TransactionDecorator.ID, TransactionDecorator.class));

		all.add(C(Decorator.class, StorageSQLDecorator.ID, StorageSQLDecorator.class));

		all.add(C(Decorator.class, StorageCacheDecorator.ID, StorageCacheDecorator.class));

		all.add(C(DecoratorManager.class));

		all.add(C(AlertPolicyManager.class).req(ConfigDao.class, ContentFetcher.class));

		all.add(C(Spliter.class, MailSpliter.ID, MailSpliter.class));

		all.add(C(Spliter.class, SmsSpliter.ID, SmsSpliter.class));

		all.add(C(Spliter.class, WeixinSpliter.ID, WeixinSpliter.class));

		all.add(C(SpliterManager.class));

		all.add(C(Sender.class, MailSender.ID, MailSender.class).req(SenderConfigManager.class));

		all.add(C(Sender.class, SmsSender.ID, SmsSender.class).req(SenderConfigManager.class));

		all.add(C(Sender.class, WeixinSender.ID, WeixinSender.class).req(SenderConfigManager.class));

		all.add(C(SenderManager.class).req(ServerConfigManager.class));

		all.add(C(AlertManager.class).req(AlertPolicyManager.class, DecoratorManager.class, ContactorManager.class,
		      AlertEntityService.class, SpliterManager.class, SenderManager.class, ServerConfigManager.class));

		all.add(C(BusinessAlert.class).req(MetricConfigManager.class, ProductLineConfigManager.class, AlertInfo.class)
		      .req(MetricReportGroupService.class, BusinessRuleConfigManager.class, DataChecker.class,
		            AlertManager.class, BaselineService.class));

		all.add(C(NetworkAlert.class).req(ProductLineConfigManager.class, AlertInfo.class).req(
		      MetricReportGroupService.class, NetworkRuleConfigManager.class, DataChecker.class, AlertManager.class));

		all.add(C(DatabaseAlert.class).req(ProductLineConfigManager.class, AlertInfo.class).req(
		      MetricReportGroupService.class, DatabaseRuleConfigManager.class, DataChecker.class, AlertManager.class));

		all.add(C(HeartbeatAlert.class)
		      .req(ProductLineConfigManager.class, HeartbeatDisplayPolicyManager.class)
		      .req(MetricReportGroupService.class, HeartbeatRuleConfigManager.class, DataChecker.class,
		            ServerFilterConfigManager.class, AlertManager.class, AlertInfo.class)
		      .req(ModelService.class, HeartbeatAnalyzer.ID, "m_heartbeatService")
		      .req(ModelService.class, TransactionAnalyzer.ID, "m_transactionService"));

		all.add(C(SystemAlert.class).req(ProductLineConfigManager.class, AlertInfo.class).req(
		      MetricReportGroupService.class, SystemRuleConfigManager.class, DataChecker.class, AlertManager.class));

		all.add(C(AppAlert.class).req(AppDataService.class, AlertManager.class, AppRuleConfigManager.class,
		      DataChecker.class, AppConfigManager.class));

		all.add(C(WebAlert.class).req(WebApiService.class, AlertManager.class).req(WebRuleConfigManager.class,
		      DataChecker.class, UrlPatternConfigManager.class));

		all.add(C(TransactionAlert.class).req(TransactionMergeHelper.class, DataChecker.class, AlertManager.class)
		      .req(ModelService.class, TransactionAnalyzer.ID).req(TransactionRuleConfigManager.class));

		all.add(C(StorageSQLAlert.class).req(StorageMergeHelper.class, DataChecker.class, AlertManager.class)
		      .req(ModelService.class, StorageAnalyzer.ID)
		      .req(StorageSQLRuleConfigManager.class, StorageGroupConfigManager.class, StorageAlertInfoBuilder.class));

		all.add(C(StorageCacheAlert.class).req(StorageMergeHelper.class, DataChecker.class, AlertManager.class)
		      .req(ModelService.class, StorageAnalyzer.ID)
		      .req(StorageCacheRuleConfigManager.class, StorageGroupConfigManager.class, StorageAlertInfoBuilder.class));

		all.add(C(AlertExceptionBuilder.class).req(ExceptionRuleConfigManager.class, AggregationConfigManager.class));

		all.add(C(ExceptionAlert.class).req(ExceptionRuleConfigManager.class, AlertExceptionBuilder.class,
		      AlertManager.class).req(ModelService.class, TopAnalyzer.ID));

		all.add(C(FrontEndExceptionAlert.class).req(ExceptionRuleConfigManager.class, AlertExceptionBuilder.class,
		      AlertManager.class).req(ModelService.class, TopAnalyzer.ID));

		all.add(C(ThirdPartyAlert.class).req(AlertManager.class));

		all.add(C(HttpConnector.class));

		all.add(C(ThirdPartyAlertBuilder.class).req(HttpConnector.class, ThirdPartyAlert.class,
		      ThirdPartyConfigManager.class));

		all.add(C(AlertEntityService.class).req(AlertDao.class));

		all.add(C(AlertInfoBuilder.class).req(AlertDao.class, TopologyGraphManager.class));

		all.add(C(AlertSummaryService.class).req(AlertSummaryDao.class));

		all.add(C(SummaryBuilder.class, RelatedSummaryBuilder.ID, RelatedSummaryBuilder.class).req(
		      AlertInfoBuilder.class, AlertSummaryService.class));

		all.add(C(SummaryBuilder.class, FailureSummaryBuilder.ID, FailureSummaryBuilder.class).req(ModelService.class,
		      ProblemAnalyzer.ID));

		all.add(C(SummaryBuilder.class, AlterationSummaryBuilder.ID, AlterationSummaryBuilder.class).req(
		      AlterationDao.class));

		all.add(C(AlertSummaryExecutor.class).req(SenderManager.class)
		      .req(SummaryBuilder.class, RelatedSummaryBuilder.ID, "m_relatedBuilder")
		      .req(SummaryBuilder.class, FailureSummaryBuilder.ID, "m_failureBuilder")
		      .req(SummaryBuilder.class, AlterationSummaryBuilder.ID, "m_alterationBuilder"));

		return all;
	}
}
