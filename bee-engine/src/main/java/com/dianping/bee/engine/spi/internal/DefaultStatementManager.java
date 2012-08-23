package com.dianping.bee.engine.spi.internal;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.recognizer.SQLParserDelegate;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import com.site.lookup.ContainerHolder;

public class DefaultStatementManager extends ContainerHolder implements StatementManager {
	private Map<String, Statement> m_statements = new HashMap<String, Statement>();

	@Override
	public Statement build(String sql) throws SQLSyntaxErrorException {
		Statement statement = m_statements.get(sql);

		if (statement == null) {
			synchronized (m_statements) {
				statement = m_statements.get(sql);

				if (statement == null) {
					statement = parseSQL(sql);
					m_statements.put(sql, statement);
				}
			}
		}

		return statement;
	}

	private Statement parseSQL(String sql) throws SQLSyntaxErrorException {
		SQLStatement statement = SQLParserDelegate.parse(sql);
		
		DefaultStatementVisitor defaultVisitor = lookup(DefaultStatementVisitor.class);
		statement.accept(defaultVisitor);
		
		if (defaultVisitor.getTableAlias().size() > 1) {
			MultiTableStatementVisitor visitor = lookup(MultiTableStatementVisitor.class);
			statement.accept(visitor);
			return visitor.getStatement();
		} else {
			SingleTableStatementVisitor visitor = lookup(SingleTableStatementVisitor.class);
			statement.accept(visitor);
			return visitor.getStatement();
		}
	}
}
