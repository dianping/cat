package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.abtest.spi.internal.ABTestCodec;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.CatConsumerAdvancedModule;
import com.dianping.cat.consumer.DomainManager;
import com.dianping.cat.consumer.advanced.CrossAnalyzer;
import com.dianping.cat.consumer.advanced.DatabaseParser;
import com.dianping.cat.consumer.advanced.DependencyAnalyzer;
import com.dianping.cat.consumer.advanced.MatrixAnalyzer;
import com.dianping.cat.consumer.advanced.MetricAnalyzer;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.advanced.SqlAnalyzer;
import com.dianping.cat.consumer.advanced.dal.BusinessReportDao;
import com.dianping.cat.consumer.advanced.dal.SqltableDao;
import com.dianping.cat.consumer.core.ProductLineConfigManager;
import com.dianping.cat.consumer.sql.SqlParseManager;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.dal.HostinfoDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.task.TaskManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MetricConfigManager.class).req(ConfigDao.class));

		all.add(C(DomainManager.class, DomainManager.class).req(ServerConfigManager.class, HostinfoDao.class));

		all.add(C(SqlParseManager.class).req(SqltableDao.class));

		all.add(C(DatabaseParser.class));

		all.add(C(MessageAnalyzer.class, CrossAnalyzer.ID, CrossAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, HourlyReportDao.class, TaskManager.class));

		all.add(C(MessageAnalyzer.class, SqlAnalyzer.ID, SqlAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, HourlyReportDao.class, TaskManager.class, SqlParseManager.class,
		            DatabaseParser.class));

		all.add(C(MessageAnalyzer.class, MatrixAnalyzer.ID, MatrixAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, HourlyReportDao.class, TaskManager.class));

		all.add(C(MessageAnalyzer.class, DependencyAnalyzer.ID, DependencyAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, HourlyReportDao.class, TaskManager.class, DomainManager.class,
		            DatabaseParser.class));

		all.add(C(MessageAnalyzer.class, MetricAnalyzer.ID, MetricAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, BusinessReportDao.class, MetricConfigManager.class)//
		      .req(ProductLineConfigManager.class, ABTestCodec.class));

		all.add(C(Module.class, CatConsumerAdvancedModule.ID, CatConsumerAdvancedModule.class));

		all.addAll(new CatAdvancedDatabaseConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
