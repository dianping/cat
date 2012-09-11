package com.dianping.bee.jdbc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.testdb.CatDatabase;
import com.dianping.bee.testdb.EventIndexer;
import com.dianping.bee.testdb.TransactionIndexer;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class TestServerConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new TestServerConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DatabaseProvider.class, "cat", CatDatabase.class));
		all.add(C(EventIndexer.class));
		all.add(C(TransactionIndexer.class));

		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/" + TestServer.class.getName().replace('.', '/') + ".xml");
	}
}
