package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.DomainManager;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.abtest.spi.internal.ABTestCodec;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.CatConsumerAdvancedModule;
import com.dianping.cat.consumer.advanced.dal.BusinessReportDao;
import com.dianping.cat.consumer.advanced.dal.SqltableDao;
import com.dianping.cat.consumer.browser.BrowserMetaAnalyzer;
import com.dianping.cat.consumer.browser.BrowserMetaDelegate;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossDelegate;
import com.dianping.cat.consumer.cross.IpConvertManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyDelegate;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixDelegate;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.sql.DatabaseParser;
import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.consumer.sql.SqlDelegate;
import com.dianping.cat.consumer.sql.SqlParseManager;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.service.DefaultReportManager;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.service.ReportManager;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.task.TaskManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineSqlComponents());
		all.addAll(defineCrossComponents());
		all.addAll(defineMatrixComponents());
		all.addAll(defineDependencyComponents());
		all.addAll(defineMetricComponents());
		all.addAll(defineBrowserComponents());

		all.add(C(Module.class, CatConsumerAdvancedModule.ID, CatConsumerAdvancedModule.class));

		all.addAll(new CatAdvancedDatabaseConfigurator().defineComponents());

		return all;
	}

	private Collection<Component> defineMetricComponents() {
		final List<Component> all = new ArrayList<Component>();

		all.add(C(MetricConfigManager.class).req(ConfigDao.class));
		all.add(C(ProductLineConfigManager.class).req(ConfigDao.class));
		all.add(C(MessageAnalyzer.class, MetricAnalyzer.ID, MetricAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, BusinessReportDao.class, MetricConfigManager.class)//
		      .req(ProductLineConfigManager.class, ABTestCodec.class, TaskManager.class));

		return all;
	}

	private Collection<Component> defineMatrixComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = MatrixAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, MatrixAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class, ID).req(ServerConfigManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class) //
		      .req(ReportDelegate.class, ID) //
		      .req(BucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, MatrixDelegate.class).req(TaskManager.class));

		return all;
	}

	private Collection<Component> defineDependencyComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = DependencyAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, DependencyAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class, ID).req(ServerConfigManager.class, DomainManager.class, DatabaseParser.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class) //
		      .req(ReportDelegate.class, ID) //
		      .req(BucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, DependencyDelegate.class).req(TaskManager.class));

		return all;
	}

	private Collection<Component> defineCrossComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = CrossAnalyzer.ID;

		all.add(C(IpConvertManager.class));
		all.add(C(MessageAnalyzer.class, ID, CrossAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class, ID).req(ServerConfigManager.class, IpConvertManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class) //
		      .req(ReportDelegate.class, ID) //
		      .req(BucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, CrossDelegate.class).req(TaskManager.class));

		return all;
	}
	
	private Collection<Component> defineBrowserComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = BrowserMetaAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, BrowserMetaAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class, ID));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class) //
		      .req(ReportDelegate.class, ID) //
		      .req(BucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, BrowserMetaDelegate.class).req(TaskManager.class));
		return all;
	}

	private Collection<Component> defineSqlComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = SqlAnalyzer.ID;

		all.add(C(SqlParseManager.class).req(SqltableDao.class));
		all.add(C(DatabaseParser.class));
		all.add(C(MessageAnalyzer.class, ID, SqlAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class, ID).req(SqlParseManager.class, DatabaseParser.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class) //
		      .req(ReportDelegate.class, ID) //
		      .req(BucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, SqlDelegate.class).req(TaskManager.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
