package com.dianping.bee.engine.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.engine.QueryService;
import com.dianping.bee.engine.evaluator.Evaluator;
import com.dianping.bee.engine.evaluator.IdentifierEvaluator;
import com.dianping.bee.engine.evaluator.ParamMarkerEvaluator;
import com.dianping.bee.engine.evaluator.function.AvgEvaluator;
import com.dianping.bee.engine.evaluator.function.ConcatEvaluator;
import com.dianping.bee.engine.evaluator.function.CountEvaluator;
import com.dianping.bee.engine.evaluator.function.MaxEvaluator;
import com.dianping.bee.engine.evaluator.function.MinEvaluator;
import com.dianping.bee.engine.evaluator.function.SumEvaluator;
import com.dianping.bee.engine.evaluator.literal.LiteralBooleanEvaluator;
import com.dianping.bee.engine.evaluator.literal.LiteralNumberEvaluator;
import com.dianping.bee.engine.evaluator.literal.LiteralStringEvaluator;
import com.dianping.bee.engine.evaluator.logical.BetweenAndEvaluator;
import com.dianping.bee.engine.evaluator.logical.ComparisionEqualsEvaluator;
import com.dianping.bee.engine.evaluator.logical.ComparisionGreaterThanEvaluator;
import com.dianping.bee.engine.evaluator.logical.ComparisionGreaterThanOrEqualsEvaluator;
import com.dianping.bee.engine.evaluator.logical.ComparisionIsEvaluator;
import com.dianping.bee.engine.evaluator.logical.ComparisionLessThanEvaluator;
import com.dianping.bee.engine.evaluator.logical.ComparisionLessThanOrEqualsEvaluator;
import com.dianping.bee.engine.evaluator.logical.InEvaluator;
import com.dianping.bee.engine.evaluator.logical.LogicalAndEvaluator;
import com.dianping.bee.engine.evaluator.logical.LogicalOrEvaluator;
import com.dianping.bee.engine.internal.DefaultQueryService;
import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.SessionManager;
import com.dianping.bee.engine.spi.StatementManager;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.dianping.bee.engine.spi.handler.DescHandler;
import com.dianping.bee.engine.spi.handler.PrepareHandler;
import com.dianping.bee.engine.spi.handler.SelectHandler;
import com.dianping.bee.engine.spi.handler.SetHandler;
import com.dianping.bee.engine.spi.handler.ShowHandler;
import com.dianping.bee.engine.spi.handler.UseHandler;
import com.dianping.bee.engine.spi.internal.DefaultRowContext;
import com.dianping.bee.engine.spi.internal.DefaultSessionManager;
import com.dianping.bee.engine.spi.internal.DefaultStatementManager;
import com.dianping.bee.engine.spi.internal.DefaultTableProviderManager;
import com.dianping.bee.engine.spi.internal.SingleTablePreparedStatement;
import com.dianping.bee.engine.spi.internal.SingleTablePreparedStatementBuilder;
import com.dianping.bee.engine.spi.internal.SingleTableRowFilter;
import com.dianping.bee.engine.spi.internal.SingleTableStatement;
import com.dianping.bee.engine.spi.internal.SingleTableStatementBuilder;
import com.dianping.bee.engine.spi.internal.TableHelper;
import com.dianping.bee.server.SimpleServer;
import com.dianping.bee.server.SimpleServerQueryHandler;
import com.dianping.bee.server.mysql.ColumnsIndexer;
import com.dianping.bee.server.mysql.InformationSchemaDatabaseProvider;
import com.dianping.bee.server.mysql.SchemataIndexer;
import com.dianping.bee.server.mysql.TablesIndexer;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(QueryService.class, DefaultQueryService.class).is(PER_LOOKUP) //
		      .req(SessionManager.class, StatementManager.class));

		all.add(C(SimpleServer.class));

		all.add(C(SessionManager.class, DefaultSessionManager.class));
		all.add(C(TableProviderManager.class, DefaultTableProviderManager.class) //
		      .req(SessionManager.class));
		all.add(C(StatementManager.class, DefaultStatementManager.class));

		all.add(C(TableHelper.class) //
		      .req(TableProviderManager.class));

		all.add(C(RowContext.class, DefaultRowContext.class));
		all.add(C(SingleTableStatement.class).is(PER_LOOKUP)//
		      .req(RowContext.class));
		all.add(C(SingleTableRowFilter.class).is(PER_LOOKUP));
		all.add(C(SingleTableStatementBuilder.class).is(PER_LOOKUP) //
		      .req(TableHelper.class, SingleTableStatement.class, SingleTableRowFilter.class));

		all.add(C(SingleTablePreparedStatement.class).is(PER_LOOKUP)//
		      .req(RowContext.class));
		all.add(C(SingleTablePreparedStatementBuilder.class).is(PER_LOOKUP)//
		      .req(TableHelper.class, SingleTablePreparedStatement.class, SingleTableRowFilter.class));

		defineInformationSchema(all);
		defineHandlers(all);
		definePrimaryEvaluators(all);
		defineLiteralEvaluators(all);
		defineLogicalEvaluators(all);
		defineFunctionEvaluators(all);

		return all;
	}

	private void defineFunctionEvaluators(List<Component> all) {
		all.add(C(Evaluator.class, ConcatEvaluator.ID, ConcatEvaluator.class));
		all.add(C(Evaluator.class, SumEvaluator.ID, SumEvaluator.class).is(PER_LOOKUP));
		all.add(C(Evaluator.class, CountEvaluator.ID, CountEvaluator.class).is(PER_LOOKUP));
		all.add(C(Evaluator.class, MaxEvaluator.ID, MaxEvaluator.class).is(PER_LOOKUP));
		all.add(C(Evaluator.class, MinEvaluator.ID, MinEvaluator.class).is(PER_LOOKUP));
		all.add(C(Evaluator.class, AvgEvaluator.ID, AvgEvaluator.class).is(PER_LOOKUP));
	}

	private void defineHandlers(List<Component> all) {
		all.add(C(SimpleServerQueryHandler.class).is(PER_LOOKUP) //
		      .req(SelectHandler.class, ShowHandler.class, DescHandler.class, UseHandler.class, SetHandler.class,
		            PrepareHandler.class));

		all.add(C(UseHandler.class));
		all.add(C(SetHandler.class).req(SessionManager.class));
		all.add(C(ShowHandler.class).req(SessionManager.class));
		all.add(C(DescHandler.class) //
		      .req(TableProviderManager.class));
		all.add(C(SelectHandler.class) //
		      .req(StatementManager.class, SessionManager.class));
		all.add(C(PrepareHandler.class)//
		      .req(StatementManager.class));
	}

	private void defineInformationSchema(List<Component> all) {
		all.add(C(DatabaseProvider.class, InformationSchemaDatabaseProvider.ID, InformationSchemaDatabaseProvider.class));
		all.add(C(SchemataIndexer.class));
		all.add(C(TablesIndexer.class));
		all.add(C(ColumnsIndexer.class));
	}

	private void defineLiteralEvaluators(List<Component> all) {
		all.add(C(Evaluator.class, LiteralStringEvaluator.ID, LiteralStringEvaluator.class));
		all.add(C(Evaluator.class, LiteralNumberEvaluator.ID, LiteralNumberEvaluator.class));
		all.add(C(Evaluator.class, LiteralBooleanEvaluator.ID, LiteralBooleanEvaluator.class));
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
	}

	private void definePrimaryEvaluators(List<Component> all) {
		all.add(C(Evaluator.class, IdentifierEvaluator.ID, IdentifierEvaluator.class));
		all.add(C(Evaluator.class, ParamMarkerEvaluator.ID, ParamMarkerEvaluator.class));
	}
}
