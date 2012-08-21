package com.dianping.bee.engine.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.dianping.bee.engine.spi.internal.DefaultStatement;
import com.dianping.bee.engine.spi.internal.DefaultStatementManager;
import com.dianping.bee.engine.spi.internal.DefaultTableProviderManager;
import com.dianping.bee.engine.spi.internal.SingleTableStatementVisitor;
import com.dianping.bee.engine.spi.internal.TableHelper;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(TableProviderManager.class, DefaultTableProviderManager.class));
		all.add(C(StatementManager.class, DefaultStatementManager.class));
		all.add(C(Statement.class, DefaultStatement.class).is(PER_LOOKUP));

		all.add(C(TableHelper.class) //
		      .req(TableProviderManager.class));

		all.add(C(SingleTableStatementVisitor.class).is(PER_LOOKUP) //
		      .req(TableHelper.class, Statement.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
