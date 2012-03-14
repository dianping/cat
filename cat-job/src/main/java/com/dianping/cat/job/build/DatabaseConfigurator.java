package com.dianping.cat.job.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.job.sql.dal._INDEX;
import com.site.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import com.site.lookup.configuration.Component;

final class DatabaseConfigurator extends AbstractJdbcResourceConfigurator {
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(defineJdbcDataSourceConfigurationManagerComponent("/data/appdatas/cat/datasources.xml"));
		all.add(defineJdbcDataSourceComponent("cat", "${jdbc.driver}", "${jdbc.url}", "${jdbc.user}", "${jdbc.password}",
		      "<![CDATA[${jdbc.connectionProperties}]]>"));

		defineSimpleTableProviderComponents(all, "cat", _INDEX.getEntityClasses());
		defineDaoComponents(all, _INDEX.getDaoClasses());

		return all;
	}
}