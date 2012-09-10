package com.dianping.bee.engine.spi.internal;

import java.sql.SQLSyntaxErrorException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.recognizer.SQLParserDelegate;
import com.dianping.bee.engine.spi.PreparedStatement;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import com.site.lookup.ContainerHolder;

public class DefaultStatementManager extends ContainerHolder implements StatementManager {
	private Map<String, Statement> m_statements = new LRUCache<String, Statement>(1000);

	private Map<Long, PreparedStatement> m_prepares = new LRUCache<Long, PreparedStatement>(100);

	private long m_nextStatementId;

	@Override
	public Statement build(String sql) throws SQLSyntaxErrorException {
		Statement statement = m_statements.get(sql);

		if (statement == null) {
			synchronized (m_statements) {
				statement = m_statements.get(sql);

				if (statement == null) {
					statement = parseSQL(sql);
					m_statements.put(sql, statement);

					if (statement instanceof PreparedStatement) {
						PreparedStatement preparedStatement = (PreparedStatement) statement;

						preparedStatement.setStatementId(m_nextStatementId);
						m_prepares.put(m_nextStatementId++, preparedStatement);
					}
				}
			}
		}

		return statement;
	}

	@Override
	public PreparedStatement getPreparedStatement(Long stmtId) {
		return m_prepares.get(stmtId);
	}

	private Statement parseSQL(String sql) throws SQLSyntaxErrorException {
		SQLStatement statement = SQLParserDelegate.parse(sql);
		QueryDetector detector = new QueryDetector();

		statement.accept(detector);

		if (detector.isSingleTable()) {
			SingleTableStatementBuilder builder;

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

	private final class LRUCache<K, V> extends LinkedHashMap<K, V> {
		private static final long serialVersionUID = 1L;

		private int m_capacity;

		private LRUCache(int capacity) {
			super(capacity * 4 / 3 + 1, 0.75f, true);

			m_capacity = capacity * 4 / 3 + 1;
		}

		@Override
		protected boolean removeEldestEntry(Entry<K, V> eldest) {
			return size() > m_capacity;
		}
	}
}
