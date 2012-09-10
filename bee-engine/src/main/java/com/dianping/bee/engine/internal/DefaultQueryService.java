package com.dianping.bee.engine.internal;

import java.sql.SQLException;

import com.dianping.bee.engine.QueryService;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.session.SessionManager;
import com.site.lookup.annotation.Inject;

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
			RowSet rowset = stmt.query();

			return rowset;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}
}
