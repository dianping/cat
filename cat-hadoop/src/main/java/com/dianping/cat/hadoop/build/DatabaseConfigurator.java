package com.dianping.cat.hadoop.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.hadoop.dal._INDEX;
import com.site.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import com.site.lookup.configuration.Component;

final class DatabaseConfigurator extends AbstractJdbcResourceConfigurator {
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(defineJdbcDataSourceConfigurationManagerComponent("/data/appdatas/cat/datasources.xml"));

		// Hack for hadoop jobs
		all.add(defineJdbcDataSourceComponent("cat", "com.mysql.jdbc.Driver", "jdbc:mysql://10.1.1.220:3306/cat",
		      "dpcom_cat", "dp!@jWLcFDfEX", "<![CDATA[useUnicode=true&autoReconnect=true]]>"));

		defineSimpleTableProviderComponents(all, "cat", _INDEX.getEntityClasses());
		defineDaoComponents(all, _INDEX.getDaoClasses());

		return all;
	}
}