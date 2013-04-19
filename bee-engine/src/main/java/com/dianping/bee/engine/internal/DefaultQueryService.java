package com.dianping.bee.engine.internal;

import java.sql.SQLException;

import com.dianping.bee.engine.QueryService;
import com.dianping.bee.engine.RowSet;
import com.dianping.bee.engine.spi.PreparedStatement;
import com.dianping.bee.engine.spi.SessionManager;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import org.unidal.lookup.annotation.Inject;

public class DefaultQueryService implements QueryService {
	@Inject
	private StatementManager m_statementManager;

	@Inject
	private SessionManager m_sessionManager;

	public void use(String database) {
		m_sessionManager.getSession().setDatabase(database);
	}

	public RowSet query(String sql, Object... params) throws SQLException {
		try {
			Statement stmt = m_statementManager.build(sql);

			if (stmt instanceof PreparedStatement) {
				PreparedStatement preparedStatement = (PreparedStatement) stmt;
				int index = 0;

				for (Object param : params) {
					preparedStatement.setParameter(index++, param);
				}
			}

			RowSet rowset = stmt.query();

			return rowset;
		} catch (Exception e) {
			throw new SQLException(String.format("Error when querying with SQL(%s)!", sql), e);
		}
	}
}
