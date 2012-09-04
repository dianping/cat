package com.dianping.bee.jdbc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.db.cat.CatDatabase;
import com.dianping.bee.db.cat.EventIndexer;
import com.dianping.bee.db.cat.TransactionIndexer;
import com.dianping.bee.db.dog.DogDatabase;
import com.dianping.bee.engine.spi.DatabaseProvider;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class JDBCTestConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new JDBCTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DatabaseProvider.class, "cat", CatDatabase.class));
		all.add(C(EventIndexer.class));
		all.add(C(TransactionIndexer.class));
		all.add(C(DatabaseProvider.class, "dog", DogDatabase.class));

		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/" + JDBCTest.class.getName().replace('.', '/') + ".xml");
	}
}
