package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

final class CatDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineSimpleTableProviderComponents(all, "cat", com.dianping.cat.consumer.dal._INDEX.getEntityClasses());
		defineDaoComponents(all, com.dianping.cat.consumer.dal._INDEX.getDaoClasses());

		return all;
	}
}
