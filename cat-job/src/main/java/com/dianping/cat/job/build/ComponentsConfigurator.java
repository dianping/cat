package com.dianping.cat.job.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.job.joblet.BrowserJoblet;
import com.dianping.cat.job.joblet.BrowserJoblet.BrowserOutputter;
import com.dianping.cat.job.joblet.BrowserJoblet.OsTypeAndVersionReporter;
import com.dianping.cat.job.joblet.HelpJoblet;
import com.dianping.cat.job.joblet.LocationJoblet;
import com.dianping.cat.job.joblet.LocationJoblet.LocationDatabaseDumper;
import com.dianping.cat.job.joblet.LocationJoblet.LocationOutputter;
import com.dianping.cat.job.joblet.LocationJoblet.LocationReporter;
import com.dianping.cat.job.joblet.SqlJoblet;
import com.dianping.cat.job.joblet.SqlJoblet.SqlDatabaseOutputter;
import com.dianping.cat.job.joblet.SqlJoblet.SqlOutputter;
import com.dianping.cat.job.spi.joblet.Joblet;
import com.dianping.cat.job.spi.joblet.JobletRunner;
import com.dianping.cat.job.sql.dal.LocationRecordDao;
import com.dianping.cat.job.sql.dal.SqlReportRecordDao;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(JobletRunner.class));

		// for Help
		all.add(C(Joblet.class, "help", HelpJoblet.class).is(PER_LOOKUP));

		// for Browser Analyzer
		all.add(C(Joblet.class, "browser", BrowserJoblet.class).is(PER_LOOKUP) //
		      .req(BrowserOutputter.class, "report"));
		all.add(C(BrowserOutputter.class, "report", OsTypeAndVersionReporter.class));

		// for Location Analyzer
		all.add(C(Joblet.class, "location", LocationJoblet.class).is(PER_LOOKUP) //
		      .req(LocationOutputter.class, "database"));
		all.add(C(LocationOutputter.class, "database", LocationDatabaseDumper.class) //
		      .req(LocationRecordDao.class));
		all.add(C(LocationOutputter.class, "report", LocationReporter.class));

		// for SQL Analyzer
		all.add(C(Joblet.class, "sql", SqlJoblet.class).is(PER_LOOKUP) //
		      .req(SqlOutputter.class));
		all.add(C(SqlOutputter.class, SqlDatabaseOutputter.class) //
		      .req(SqlReportRecordDao.class));

		all.addAll(new DatabaseConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
