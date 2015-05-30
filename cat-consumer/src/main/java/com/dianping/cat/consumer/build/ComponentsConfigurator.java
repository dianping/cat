package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.content.DefaultContentFetcher;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.dal.BusinessReportDao;
import com.dianping.cat.consumer.dependency.DatabaseParser;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyDelegate;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.dump.LocalMessageBucketManager;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateDelegate;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageDelegate;
import com.dianping.cat.consumer.storage.StorageReportUpdater;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopDelegate;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.hadoop.hdfs.HdfsUploader;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.report.DefaultReportManager;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.task.TaskManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineTopComponents());
		all.addAll(defineDumpComponents());
		all.addAll(defineStateComponents());
		all.addAll(defineDependencyComponents());
		all.addAll(defineMetricComponents());
		all.addAll(defineStorageComponents());

		all.add(C(Module.class, CatConsumerModule.ID, CatConsumerModule.class));
		all.addAll(new CatDatabaseConfigurator().defineComponents());
		return all;
	}

	private Collection<Component> defineDependencyComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = DependencyAnalyzer.ID;

		all.add(C(DatabaseParser.class));
		all.add(C(MessageAnalyzer.class, ID, DependencyAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ServerConfigManager.class, ServerFilterConfigManager.class, DatabaseParser.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
		      .req(ReportDelegate.class, ID) //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, DependencyDelegate.class).req(TaskManager.class));

		return all;
	}

	private Collection<Component> defineDumpComponents() {
		final List<Component> all = new ArrayList<Component>();
		all.add(C(MessageAnalyzer.class, DumpAnalyzer.ID, DumpAnalyzer.class).is(PER_LOOKUP) //
		      .req(ServerStatisticManager.class, ServerConfigManager.class) //
		      .req(MessageBucketManager.class, LocalMessageBucketManager.ID));

		all.add(C(MessageBucketManager.class, LocalMessageBucketManager.ID, LocalMessageBucketManager.class) //
		      .req(ServerConfigManager.class, PathBuilder.class, ServerStatisticManager.class)//
		      .req(HdfsUploader.class));

		return all;
	}

	private Collection<Component> defineMetricComponents() {
		final List<Component> all = new ArrayList<Component>();

		all.add(C(ContentFetcher.class, DefaultContentFetcher.class));
		all.add(C(ProductLineConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(MetricConfigManager.class).req(ConfigDao.class, ContentFetcher.class, ProductLineConfigManager.class));
		all.add(C(MessageAnalyzer.class, MetricAnalyzer.ID, MetricAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportBucketManager.class, BusinessReportDao.class, MetricConfigManager.class)//
		      .req(ProductLineConfigManager.class, TaskManager.class, ServerConfigManager.class));

		return all;
	}

	private Collection<Component> defineStateComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = StateAnalyzer.ID;

		all.add(C(ProjectService.class).req(ProjectDao.class, ServerConfigManager.class));
		all.add(C(MessageAnalyzer.class, ID, StateAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ServerStatisticManager.class, ServerConfigManager.class, ServerFilterConfigManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
		      .req(ReportDelegate.class, ID) //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, StateDelegate.class) //
		      .req(TaskManager.class, ReportBucketManager.class));

		return all;
	}

	private Collection<Component> defineTopComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = TopAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, TopAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ServerConfigManager.class, ServerFilterConfigManager.class)
		      .config(E("errorType").value("Error,RuntimeException,Exception")));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
		      .req(ReportDelegate.class, ID) //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, TopDelegate.class));

		return all;
	}

	private Collection<Component> defineStorageComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = StorageAnalyzer.ID;
		all.add(C(com.dianping.cat.consumer.storage.DatabaseParser.class));
		all.add(C(StorageReportUpdater.class));
		all.add(C(MessageAnalyzer.class, ID, StorageAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ReportDelegate.class, ID).req(ServerConfigManager.class)
		      .req(com.dianping.cat.consumer.storage.DatabaseParser.class).req(StorageReportUpdater.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
		      .req(ReportDelegate.class, ID) //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, StorageDelegate.class).req(TaskManager.class,
		      ServerFilterConfigManager.class, StorageReportUpdater.class));

		// database
		all.add(C(JdbcDataSourceDescriptorManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));

		return all;
	}
}
