package com.dianping.bee.engine.spi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.db.cat.CatDatabase;
import com.dianping.bee.db.cat.EventIndexer;
import com.dianping.bee.db.cat.TransactionIndexer;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class SpiTestConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new SpiTestConfigurator());
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
		return new File("src/test/resources/" + SpiTest.class.getName().replace('.', '/') + ".xml");
	}
}
