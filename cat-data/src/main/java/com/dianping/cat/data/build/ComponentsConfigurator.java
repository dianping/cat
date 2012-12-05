package com.dianping.cat.data.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.cat.data.CatDatabaseProvider;
import com.dianping.cat.data.transaction.TransactionIndexer;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DatabaseProvider.class, CatDatabaseProvider.ID, CatDatabaseProvider.class));

		all.add(C(TransactionIndexer.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
