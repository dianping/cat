package com.dianping.bee.engine.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.StatementManager;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.dianping.bee.engine.spi.evaluator.Evaluator;
import com.dianping.bee.engine.spi.evaluator.function.ConcatEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.BetweenAndEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.ComparisionEqualsEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.ComparisionGreaterThanEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.ComparisionGreaterThanOrEqualsEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.ComparisionIsEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.ComparisionLessThanEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.ComparisionLessThanOrEqualsEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.IdentifierEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.InEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.LiteralBooleanEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.LiteralNumberEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.LiteralStringEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.LogicalAndEvaluator;
import com.dianping.bee.engine.spi.evaluator.logical.LogicalOrEvaluator;
import com.dianping.bee.engine.spi.handler.internal.DescHandler;
import com.dianping.bee.engine.spi.handler.internal.PrepareHandler;
import com.dianping.bee.engine.spi.handler.internal.SelectHandler;
import com.dianping.bee.engine.spi.handler.internal.ShowHandler;
import com.dianping.bee.engine.spi.handler.internal.UseHandler;
import com.dianping.bee.engine.spi.internal.DefaultStatementManager;
import com.dianping.bee.engine.spi.internal.DefaultTableProviderManager;
import com.dianping.bee.engine.spi.internal.SingleTableRowFilter;
import com.dianping.bee.engine.spi.internal.SingleTableStatement;
import com.dianping.bee.engine.spi.internal.SingleTableStatementBuilder;
import com.dianping.bee.engine.spi.internal.TableHelper;
import com.dianping.bee.engine.spi.session.DefaultSessionManager;
import com.dianping.bee.engine.spi.session.SessionManager;
import com.dianping.bee.server.SimpleServer;
import com.dianping.bee.server.SimpleServerQueryHandler;
import com.dianping.bee.server.is.InformationSchemaDatabaseProvider;
import com.dianping.bee.server.is.schema.SchemataIndexer;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(SimpleServer.class));

		all.add(C(DatabaseProvider.class, InformationSchemaDatabaseProvider.ID, InformationSchemaDatabaseProvider.class));
		all.add(C(SchemataIndexer.class));

		all.add(C(SessionManager.class, DefaultSessionManager.class));
		all.add(C(TableProviderManager.class, DefaultTableProviderManager.class) //
		      .req(SessionManager.class));
		all.add(C(StatementManager.class, DefaultStatementManager.class));

		all.add(C(TableHelper.class) //
		      .req(TableProviderManager.class));

		all.add(C(SingleTableStatement.class).is(PER_LOOKUP));
		all.add(C(SingleTableRowFilter.class).is(PER_LOOKUP));
		all.add(C(SingleTableStatementBuilder.class).is(PER_LOOKUP) //
		      .req(TableHelper.class, SingleTableStatement.class, SingleTableRowFilter.class));

		defineHandlers(all);
		defineLogicalEvaluators(all);
		defineFunctionEvaluators(all);

		return all;
	}

	private void defineFunctionEvaluators(List<Component> all) {
		all.add(C(Evaluator.class, ConcatEvaluator.ID, ConcatEvaluator.class));
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

	private void defineLogicalEvaluators(List<Component> all) {
		all.add(C(Evaluator.class, LogicalAndEvaluator.ID, LogicalAndEvaluator.class));
		all.add(C(Evaluator.class, LogicalOrEvaluator.ID, LogicalOrEvaluator.class));

		all.add(C(Evaluator.class, ComparisionEqualsEvaluator.ID, ComparisionEqualsEvaluator.class));
		all.add(C(Evaluator.class, ComparisionIsEvaluator.ID, ComparisionIsEvaluator.class));
		all.add(C(Evaluator.class, ComparisionGreaterThanEvaluator.ID, ComparisionGreaterThanEvaluator.class));
		all.add(C(Evaluator.class, ComparisionGreaterThanOrEqualsEvaluator.ID,
		      ComparisionGreaterThanOrEqualsEvaluator.class));
		all.add(C(Evaluator.class, ComparisionLessThanEvaluator.ID, ComparisionLessThanEvaluator.class));
		all.add(C(Evaluator.class, ComparisionLessThanOrEqualsEvaluator.ID, ComparisionLessThanOrEqualsEvaluator.class));

		all.add(C(Evaluator.class, BetweenAndEvaluator.ID, BetweenAndEvaluator.class));
		all.add(C(Evaluator.class, InEvaluator.ID, InEvaluator.class));

		all.add(C(Evaluator.class, IdentifierEvaluator.ID, IdentifierEvaluator.class));

		all.add(C(Evaluator.class, LiteralStringEvaluator.ID, LiteralStringEvaluator.class));
		all.add(C(Evaluator.class, LiteralNumberEvaluator.ID, LiteralNumberEvaluator.class));
		all.add(C(Evaluator.class, LiteralBooleanEvaluator.ID, LiteralBooleanEvaluator.class));
	}
}
