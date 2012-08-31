package com.dianping.bee.engine.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.db.CatDatabase;
import com.dianping.bee.db.DogDatabase;
import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.StatementManager;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.dianping.bee.engine.spi.handler.internal.DescHandler;
import com.dianping.bee.engine.spi.handler.internal.PrepareHandler;
import com.dianping.bee.engine.spi.handler.internal.SelectHandler;
import com.dianping.bee.engine.spi.handler.internal.ShowHandler;
import com.dianping.bee.engine.spi.handler.internal.UseHandler;
import com.dianping.bee.engine.spi.internal.DefaultStatementManager;
import com.dianping.bee.engine.spi.internal.DefaultTableProviderManager;
import com.dianping.bee.engine.spi.internal.SingleTableStatement;
import com.dianping.bee.engine.spi.internal.SingleTableStatementVisitor;
import com.dianping.bee.engine.spi.internal.TableHelper;
import com.dianping.bee.engine.spi.session.DefaultSessionManager;
import com.dianping.bee.engine.spi.session.SessionManager;
import com.dianping.bee.server.InformationSchemaDatabase;
import com.dianping.bee.server.SimpleServer;
import com.dianping.bee.server.SimpleServerQueryHandler;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(SimpleServer.class));

		all.add(C(DatabaseProvider.class, "information_schema", InformationSchemaDatabase.class));
		all.add(C(DatabaseProvider.class, "cat", CatDatabase.class));
		all.add(C(DatabaseProvider.class, "dog", DogDatabase.class));

		all.add(C(SessionManager.class, DefaultSessionManager.class));
		all.add(C(TableProviderManager.class, DefaultTableProviderManager.class) //
		      .req(SessionManager.class));
		all.add(C(StatementManager.class, DefaultStatementManager.class));
		all.add(C(SingleTableStatement.class).is(PER_LOOKUP));

		all.add(C(TableHelper.class) //
		      .req(TableProviderManager.class));

		all.add(C(SingleTableStatementVisitor.class).is(PER_LOOKUP) //
		      .req(TableHelper.class, SingleTableStatement.class));

		defineHandlers(all);

		return all;
	}

	private void defineHandlers(List<Component> all) {
		all.add(C(SimpleServerQueryHandler.class).is(PER_LOOKUP) //
		      .req(SelectHandler.class, ShowHandler.class, DescHandler.class, UseHandler.class, PrepareHandler.class));

		all.add(C(UseHandler.class));
		all.add(C(ShowHandler.class));
		all.add(C(DescHandler.class) //
		      .req(TableProviderManager.class));
		all.add(C(SelectHandler.class) //
		      .req(StatementManager.class));
		all.add(C(PrepareHandler.class)//
		      .req(StatementManager.class));
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
