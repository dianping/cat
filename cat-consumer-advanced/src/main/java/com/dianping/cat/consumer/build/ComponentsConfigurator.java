package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceConfigurationManager;
import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.CatConsumerAdvancedModule;
import com.dianping.cat.consumer.DomainManager;
import com.dianping.cat.consumer.MessageAnalyzer;
import com.dianping.cat.consumer.advanced.BussinessConfigManager;
import com.dianping.cat.consumer.advanced.CrossAnalyzer;
import com.dianping.cat.consumer.advanced.DatabaseAnalyzer;
import com.dianping.cat.consumer.advanced.DatabaseParser;
import com.dianping.cat.consumer.advanced.DependencyAnalyzer;
import com.dianping.cat.consumer.advanced.MatrixAnalyzer;
import com.dianping.cat.consumer.advanced.MetricAnalyzer;
import com.dianping.cat.consumer.advanced.SqlAnalyzer;
import com.dianping.cat.consumer.advanced.dal.BusinessReportDao;
import com.dianping.cat.consumer.advanced.dal.SqltableDao;
import com.dianping.cat.consumer.core.ProductLineConfigManager;
import com.dianping.cat.consumer.core.dal.HostinfoDao;
import com.dianping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.consumer.core.dal.TaskDao;
import com.dianping.cat.consumer.sql.SqlParseManager;
import com.dianping.cat.storage.BucketManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(BussinessConfigManager.class));

		all.add(C(DomainManager.class, DomainManager.class).req(ServerConfigManager.class, HostinfoDao.class));

		all.add(C(SqlParseManager.class).req(SqltableDao.class));

		all.add(C(DatabaseParser.class));

		all.add(C(MessageAnalyzer.class, CrossAnalyzer.ID, CrossAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(MessageAnalyzer.class, DatabaseAnalyzer.ID, DatabaseAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, SqlParseManager.class, DatabaseParser.class));

		all.add(C(MessageAnalyzer.class, SqlAnalyzer.ID, SqlAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, SqlParseManager.class, DatabaseParser.class));

		all.add(C(MessageAnalyzer.class, MatrixAnalyzer.ID, MatrixAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(MessageAnalyzer.class, DependencyAnalyzer.ID, DependencyAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class, TaskDao.class, DomainManager.class, DatabaseParser.class));

		all.add(C(MessageAnalyzer.class, MetricAnalyzer.ID, MetricAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, BusinessReportDao.class, BussinessConfigManager.class)//
		      .req(ProductLineConfigManager.class));

		all.add(C(Module.class, CatConsumerAdvancedModule.ID, CatConsumerAdvancedModule.class));

		// database
		all.add(C(JdbcDataSourceConfigurationManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));

		all.addAll(new CatAdvancedDatabaseConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
