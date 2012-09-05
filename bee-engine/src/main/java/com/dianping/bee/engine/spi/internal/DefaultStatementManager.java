package com.dianping.bee.engine.spi.internal;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.recognizer.SQLParserDelegate;
import com.dianping.bee.engine.spi.PreparedStatement;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import com.site.lookup.ContainerHolder;

public class DefaultStatementManager extends ContainerHolder implements StatementManager {
	private Map<String, Statement> m_statements = new HashMap<String, Statement>();

	private Map<Long, PreparedStatement> m_prepares = new HashMap<Long, PreparedStatement>();

	private static long stmtId = 0;

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

	public Statement parseSQL(String sql) throws SQLSyntaxErrorException {
		SQLStatement statement = SQLParserDelegate.parse(sql);
		QueryDetector detector = new QueryDetector();

		statement.accept(detector);

		if (detector.isSingleTable()) {
			SingleTableStatementBuilder builder = null;
			if (detector.isPrepared()) {
				builder = lookup(SingleTablePreparedStatementBuilder.class);
			} else {
				builder = lookup(SingleTableStatementBuilder.class);
			}

			try {
				statement.accept(builder);
				return builder.getStatement();
			} finally {
				release(builder);
			}
		} else {
			throw new SQLSyntaxErrorException(sql);
		}
	}

	@Override
	public long stmtPrepare(PreparedStatement stmt) {
		synchronized (m_prepares) {
			m_prepares.put(stmtId++ % Long.MAX_VALUE, stmt);
		}

		return stmtId - 1;
	}

	@Override
	public void stmtClose(long stmtId) {
		synchronized (m_prepares) {
			m_prepares.remove(stmtId);
		}
	}

	@Override
	public PreparedStatement getStatement(Long stmtId) {
		return m_prepares.get(stmtId);
	}
}
